package com.example.runningrouteresearcher.models

import android.util.Log
import com.example.runningrouteresearcher.data.Route
import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs

class RouteAdjuster {
    private val routeBuilder = RouteBuilder()
    private val TAG = "RouteAdjuster"

    suspend fun adjustRadiusForDistance(
        userLocation: LatLng,
        targetDistance: Double,
        attempt: Int = 0,
        maxAttempts: Int = 5,
        minRadius: Double = 0.5,
        maxRadius: Double = 10.0,
        bestRoute: Route? = null,
        bestDiff: Double = Double.MAX_VALUE
        ): Route {
        if(attempt >= maxAttempts) {
            Log.d(TAG, "Max attempts reached! Best route: ${bestRoute?.distance}")
            return bestRoute?: routeBuilder.buildRoute(userLocation, (minRadius - maxRadius) / 2)
        }

        val midRadius = (minRadius + maxRadius) / 2

        try {
            val route = routeBuilder.buildRoute(userLocation, midRadius)
            val actualDistance = route.distance
            val tolerance = 0.5
            val diff = actualDistance - targetDistance
            val absDiff = abs(diff)

            Log.d(TAG, "Attempt $attempt, Radius = $midRadius got = $actualDistance target = $targetDistance")

            val newBestRoute = if(absDiff < bestDiff ) route else bestRoute
            val newBestDiff = if(absDiff < bestDiff) diff else bestDiff

            return if (absDiff <= tolerance) {
                Log.d(TAG, "Distance ok, returning route")
                route
            } else if (actualDistance < targetDistance) {
                Log.d(TAG, "Route too short, increasing radius")
                adjustRadiusForDistance(
                    userLocation,
                    targetDistance,
                    attempt + 1,
                    maxAttempts,
                    minRadius = midRadius,
                    maxRadius = maxRadius,
                    bestRoute = newBestRoute,
                    bestDiff = newBestDiff
                )
            } else {
                Log.d(TAG, "Route too long, decreasing radius")
                adjustRadiusForDistance(
                    userLocation,
                    targetDistance,
                    attempt + 1,
                    maxAttempts,
                    minRadius = minRadius,
                    maxRadius = midRadius,
                    bestRoute = newBestRoute,
                    bestDiff = newBestDiff
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error building route ${e.message}")
            throw e
        }
    }
}