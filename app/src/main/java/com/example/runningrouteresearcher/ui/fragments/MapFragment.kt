package com.example.runningrouteresearcher.ui.fragments

import com.example.runningrouteresearcher.utils.MarkerMaker
import android.Manifest
import android.content.pm.PackageManager
import com.example.runningrouteresearcher.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Initialize location client
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
        val myLocationButton = view.findViewById<ImageButton>(R.id.my_location_button)
        myLocationButton.setOnClickListener {
            getCurrentLocation()
        }

        viewModel.route.observe(viewLifecycleOwner) { route ->
            if (route != null) {
                drawRouteOnMap(route)
            }
        }
    }

    private fun openSettings() {
        val settingsFragment = SettingsFragment()
        if(currentUserLocation != null) {
            settingsFragment.setUserLocation(currentUserLocation!!)
        }
        settingsFragment.show(parentFragmentManager, "settings")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Request location permission and get location
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
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
                        .icon(BitmapDescriptorFactory.fromResource(MarkerMaker.getMarkerDrawableForNumber(index)))
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