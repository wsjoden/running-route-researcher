package com.example.runningrouteresearcher.data

import com.google.android.gms.maps.model.LatLng

data class Route (
    val waypoints: List<LatLng>,
    val distance: Double,
    val polylinePoints: List<LatLng>
    // Poly lines are the actual route drawn on the map
)
