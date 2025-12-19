package com.example.runningrouteresearcher.ui.fragments

import com.example.runningrouteresearcher.utils.MarkerUtil
import android.Manifest
import android.content.pm.PackageManager
import com.example.runningrouteresearcher.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.runningrouteresearcher.data.Route
import com.example.runningrouteresearcher.viewmodels.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.button.MaterialButton

/**
 * Fragment for displaying and interacting with map
 *
 * Features:
 * - Display Google maps map
 * - Display generated route as polyline
 * - Display route distance
 * - Display waypoint markers
 * - Display user location on launch, user can update their location via tap
 * - Display loading during route generation
 * - Pans map to user location
 * - Adjust zoom to fit entire route
 */

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val viewModel: MapViewModel by activityViewModels()
    private var currentUserLocation: LatLng? = null

    // Location permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    /**
     * Initialize UI views and set up observers
     *
     * Sets up:
     * - Loading spinner
     * - Location service client
     * - Map initialization
     * - Button click listeners
     * - Viewmodel for route updates
     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingSpinner = view.findViewById<ProgressBar>(R.id.loading_spinner)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Handle Settings button click
        val settingsButton = view.findViewById<Button>(R.id.settings_button)
        settingsButton.setOnClickListener {
            openSettings()
        }

        // Handle My Location button click
        val myLocationButton = view.findViewById<MaterialButton>(R.id.my_location_button)
        myLocationButton.setOnClickListener {
            getCurrentLocation()
        }

        // Route navigation buttons
        val routeNavigation = view.findViewById<LinearLayout>(R.id.route_navigation)
        val prevButton = view.findViewById<MaterialButton>(R.id.prev_route_button)
        val nextButton = view.findViewById<MaterialButton>(R.id.next_route_button)
        val routeCounter = view.findViewById<TextView>(R.id.route_counter)
        val routeDistance = view.findViewById<TextView>(R.id.route_distance)

        // Previous route button
        prevButton.setOnClickListener {
            val currentIndex = viewModel.currentRouteIndex.value ?: 0
            if (currentIndex > 0) {
                viewModel.switchToRoute(currentIndex - 1)
            }
        }
        // Next route button
        nextButton.setOnClickListener {
            val currentIndex = viewModel.currentRouteIndex.value ?: 0
            val totalRoutes = viewModel.routes.value?.size ?: 0
            if (currentIndex < totalRoutes - 1) {
                viewModel.switchToRoute(currentIndex + 1)
            }
        }

        // Show/hide navigation buttons and update counter
        viewModel.routes.observe(viewLifecycleOwner) { routes ->
            if (routes != null && routes.size > 1) {
                routeNavigation.visibility = View.VISIBLE
            } else {
                routeNavigation.visibility = View.GONE
            }
        }
        // Update route counter and distance when route selection changes
        viewModel.currentRouteIndex.observe(viewLifecycleOwner) { index ->
            val total = viewModel.routes.value?.size ?: 0
            routeCounter.text = "${index + 1}/$total"

            val distance = viewModel.routes.value?.get(index)?.distance
            if (distance != null) {
                routeDistance.text = String.format("%.2f km", distance)
            }
        }
        // Show/hide loading spinner during route generation
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        // Draw route on map when route is generated
        viewModel.route.observe(viewLifecycleOwner) { route ->
            if (route != null) {
                routeDistance.text = String.format("%.2f km", route.distance)
                drawRouteOnMap(route)
            }
        }
    }
    /**
     * Opens settings bottom sheet for distance input
     * Passes current user location to settings fragment
     */
    private fun openSettings() {
        val settingsFragment = SettingsFragment()
        if(currentUserLocation != null) {
            settingsFragment.setUserLocation(currentUserLocation!!)
        }
        settingsFragment.show(parentFragmentManager, "settings")
    }

    /**
     * Called when map is ready for interaction
     *
     * Sets up:
     * - Tap listener for user to update start/end location
     * - Location permission checker
     * - GPS location retriever
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set location on tap
        mMap.setOnMapClickListener { latLng ->
            currentUserLocation = latLng

            // Update the marker
            mMap.clear()
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Starting Location")
            )

            Log.d("MapFragment", "Custom location set: $latLng")
        }


        // Request location permission and get location
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Delay to ensure map is fully initialized
            view?.postDelayed({ getCurrentLocation() }, 500)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /**
     * Retrieves GPS location via FusedLocationProviderClient and displays on map
     *
     * On success:
     * - Sets currentUserLocation
     * - Pans view to location
     * - Adds marker
     *
     * On failure:
     * - Logs warning but doesn't crash
     *   since user can still manually set location
     */
    private fun getCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val userLocation = LatLng(location.latitude, location.longitude)
                            currentUserLocation = userLocation

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                            mMap.clear()
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(userLocation)
                                    .title("You are here")
                            )

                            Log.d("MapFragment", "Location set: $userLocation")
                        } else {
                            Log.d("MapFragment", "Location is NULL - emulator has no cached location")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("MapFragment", "Location failed: ${e.message}")
                        e.printStackTrace()
                    }
            } else {
                Log.d("MapFragment", "Permission not granted")
            }
        } catch (e: Exception) {
            Log.e("MapFragment", "Exception in getCurrentLocation: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Draws route on map
     *
     * Clears map and adds:
     * 1. Start/End mark at user location
     * 2. Blue polyline showing the route
     * 3. Numbered waypoints
     * 4. Adjusts view to show entire route
     *
     * Polyline is hundreds of points decoded from API's encoded polyline.
     *
     * @param route Route with polyline points with waypoints
     */
    private fun drawRouteOnMap(route: Route) {
        Log.d("MapFragment", "drawRouteOnMap called")
        Log.d("MapFragment", "Route waypoints: ${route.waypoints.size}")
        Log.d("MapFragment", "Route polylinePoints: ${route.polylinePoints.size}")
        Log.d("MapFragment", "First 3 polyline points: ${route.polylinePoints.take(3)}")

        // Clear previous polylines
        mMap.clear()

        // Re-add user location marker
        if (currentUserLocation != null) {
            mMap.addMarker(
                MarkerOptions()
                    .position(currentUserLocation!!)
                    .title("Start/End")
            )
        }

        // Draw the route as a polyline
        mMap.addPolyline(
            com.google.android.gms.maps.model.PolylineOptions()
                .addAll(route.polylinePoints)
                .color(android.graphics.Color.BLUE)
                .width(8f)
        )

        // Draw waypoints as markers
        for ((index, waypoint) in route.waypoints.withIndex()) {
            if (index == 0 || index == route.waypoints.size - 1) {
                // Start/End marker - use default Google Maps pin
                mMap.addMarker(
                    MarkerOptions()
                        .position(waypoint)
                        .title("Start/End")
                )
            } else {
                // Numbered waypoint marker
                mMap.addMarker(
                    MarkerOptions()
                        .position(waypoint)
                        .title("Waypoint $index")
                        .icon(BitmapDescriptorFactory.fromResource(MarkerUtil.getMarkerDrawableForNumber(index)))
                )
            }
        }

        // Zoom to fit the route
        val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
        for (point in route.waypoints) {
            bounds.include(point)
        }
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(bounds.build(), 100)
        )
        Log.d("MapFragment", "Route drawn! Distance: ${route.distance}km")
    }
}