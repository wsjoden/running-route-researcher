package com.example.runningrouteresearcher.ui.fragments

import com.example.runningrouteresearcher.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.runningrouteresearcher.viewmodels.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class MapFragment : Fragment(), OnMapReadyCallback {
    private val viewModel: MapViewModel by activityViewModels()
    private lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Handle Settings button click
        val settingsButton = view.findViewById<Button>(R.id.settings_button)
        settingsButton.setOnClickListener {
            openSettings()
        }

        viewModel.route.observe(viewLifecycleOwner) { route ->
            // TODO: Draw the route on the map
            println("Route: $route")
        }
    }

    private fun openSettings() {
        val settingsFragment = SettingsFragment()
        settingsFragment.show(parentFragmentManager, "settings")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set a default location (San Francisco)
        val defaultLocation = LatLng(37.7749, -122.4194)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
    }
}