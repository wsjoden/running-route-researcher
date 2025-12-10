package com.example.runningrouteresearcher.models

import android.util.Log
import com.example.runningrouteresearcher.data.Route
import com.example.runningrouteresearcher.data.RouteRepository
import com.google.android.gms.maps.model.LatLng


class RouteBuilder {
    private val routeGenerator = RouteGenerator()
    private val routeRepository = RouteRepository()
    private val TAG = "RouteBuilder"

    suspend fun buildRoute(
        userLocation: LatLng,
        radius: Double
    ): Route {
        // Step 1: Generate circular waypoints
        try {
            var waypoints = routeGenerator.generateCircularWaypoints(
                centerLocation = userLocation,
                distance = radius,
                numberOfWayPoints = 8
            )
            // Step 2: Add user location as first and last waypoint
            waypoints = listOf(userLocation) + waypoints + listOf(userLocation)
            Log.d(TAG, "Generated ${waypoints.size} waypoints with radius: ${radius}km")

            // Step 3: Snap waypoints to roads
            val snappedWaypoints = routeRepository.snapWaypoints(waypoints)
            Log.d(TAG, "Snapped waypoints!")

            // Step 4: Get direction between waypoints
            val route = routeRepository.getDirections(snappedWaypoints.waypoints)

            return route
        } catch(e: Exception) {
            Log.e(TAG,"Error building route: ${e.message}")
            throw e
        }
    }

}