package com.example.runningrouteresearcher.data

import com.google.android.gms.maps.model.LatLng

/**
 * Data class representing a route
 *
 * A route consists of:
 * 1. Waypoints: Points placed in a circular path.
 *    This are used as nodes to generate the route
 * 2. Distance: The distance of the route in kilometers
 * 3. PolylinePoints: Hundreds of GPS points that form the detailed path
 *    to be drawn on the map
 *
 * @param waypoints List of LatLng representing routing nodes.
 *                  First and last node is start/end position
 *
 * @param distance The distance of the route in kilometers.
 *                 Determined by the routing API
 *                 Used to verify if route meets distance request.
 *
 * @param polylinePoints List of hundreds of LatLng points representing
 *                       the detailed route path. This polyline is encoded
 *                       from the API and has been decoded
 */
data class Route (
    val waypoints: List<LatLng>,
    val distance: Double,
    val polylinePoints: List<LatLng>
)
