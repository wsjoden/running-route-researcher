package com.example.runningrouteresearcher.models

import android.util.Log
import com.example.runningrouteresearcher.data.Route
import com.example.runningrouteresearcher.data.RouteRepository
import com.google.android.gms.maps.model.LatLng

/**
 * Route Builder
 *
 * Coordinates route generation pipeline by coordinating
 * waypoint generation, road snapping, directions generations
 *
 * Pipeline:
 * 1. Generate waypoints in a circle around user location
 * 2. Add start/end (user location)
 * 3. Snap waypoints to nearest roads
 * 4. Calculate driving directions between snapped waypoints
 * 5. Return route with polyline and distance
 */

class RouteBuilder {
    private val routeGenerator = RouteGenerator()
    private val routeRepository = RouteRepository()
    private val TAG = "RouteBuilder"

    /**
     * Coordinates route generation pipeline by coordinating
     *
     * @param userLocation user starting position, center of circle
     * @param radius radius in km for the circular pattern
     * @param arcDegrees degrees of arc (360=full circle)
     *
     * @return Complete Route with:
     * - waypoints: snapped waypoints used for routing
     * - distance: actual calculated distance in km
     * - polylinePoints: hundreds of points forming the visual route
     *
     * @throws Exception if any step fails
     */
    suspend fun buildRoute(
        userLocation: LatLng,
        radius: Double,
        arcDegrees: Double
    ): Route {
        // Step 1: Generate circular waypoints
        try {

            var waypoints = routeGenerator.generateCircularWaypoints(
                centerLocation = userLocation,
                distance = radius,
                arcDegrees = arcDegrees
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