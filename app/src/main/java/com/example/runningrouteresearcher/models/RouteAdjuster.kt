package com.example.runningrouteresearcher.models

import android.util.Log
import com.example.runningrouteresearcher.data.Route
import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs
import kotlin.math.max

/**
 * Route Adjuster
 *
 * Uses binary search to find optimal circle radius that
 * generates a route to find best route
 *
 *
 */
class RouteAdjuster {
    private val routeBuilder = RouteBuilder()
    private val TAG = "RouteAdjuster"

    /**
     * Finds optimal radius for target distance using binary search + fallback logic
     *
     * Algorithm:
     * 1. Start with min max radius(0.5km - 10.0km)
     * 2. Test middle radius and check result
     * 3. If too short increase min radius / If too long decrease radius
     * 4. Repeat until distance is ok
     *
     * Tolerances:
     * - Primary: 0.5km, which is our ideal target.
     * - Secondary: 1.0km or 5%, This is ensure we don't return a really bad result
     *
     * Attempt Management:
     * - Short routes: 5 attempts
     * - Medium routes (5-15km): 7 attempts
     * - Long routes (15km+): 10 attempts
     * - If both tolerances fail at max attempts: double attempts for one more pass
     *
     * Best Route Tracking:
     * - Tracks the closest match throughout all attempts
     * - Returns best route if tolerances can't be met
     *
     * @param userLocation Start/End point
     * @param targetDistance Requested distance
     * @param arcDegrees Degrees of arc of waypoint circle (360 = full circle)
     * @param attempt Current attempt
     * @param maxAttempts Max amount of attempts before return
     * @param attemptIncrease Boolean - weather we have doubled max attempts (used as last resort)
     * @param minRadius Minimum radius to test
     * @param maxRadius Maximum radius to test
     * @param bestRoute Best route found (so far)
     * @param bestDiff best distance difference from target (so far)
     *
     * @return Route with distance closest to target
     * @throws Exception if route building fails
     */
    suspend fun adjustRadiusForDistance(
        userLocation: LatLng,
        targetDistance: Double,
        arcDegrees: Double = 360.0,
        attempt: Int = 0,
        maxAttempts: Int = calculateMaxAttempts(targetDistance),
        attemptIncrease: Boolean = false,
        minRadius: Double = 0.5,
        maxRadius: Double = 10.0,
        bestRoute: Route? = null,
        bestDiff: Double = Double.MAX_VALUE
        ): Route {

        // Primary tolerance the Target
        val primaryTolerance = 0.5

        // Secondary tolerance: attempt to mitigate really bad results
        // Longer routes get percentage-based tolerance
        val secondaryTolerance = if (targetDistance >= 20.0) {
            targetDistance * 0.05
        } else {
            1.0
        }

        if(attempt >= maxAttempts) {
            Log.d(TAG, "Max attempts reached! Best route: ${bestRoute?.distance}")
            // Return best route found, or use middle radius as fallback
            return bestRoute?: routeBuilder.buildRoute(userLocation, (minRadius + maxRadius) / 2, arcDegrees)
        }

        val midRadius = (minRadius + maxRadius) / 2

        try {
            // Build route with current radius
            val route = routeBuilder.buildRoute(userLocation, midRadius, arcDegrees)
            val actualDistance = route.distance
            val diff = actualDistance - targetDistance
            val absDiff = abs(diff)

            Log.d(TAG, "Attempt $attempt, Radius = $midRadius got = $actualDistance target = $targetDistance")

            val newBestRoute = if(absDiff < bestDiff ) route else bestRoute
            val newBestDiff = if(absDiff < bestDiff) diff else absDiff

            return if (absDiff <= primaryTolerance){
                // Success: within primary tolerance
                Log.d(TAG, "Distance ok, returnning route")
                route
            } else if (absDiff <= secondaryTolerance && !attemptIncrease) {
                // Good enough: within secondary tolerance
                Log.d(TAG, "Primary tolerance fail, but secondary= $secondaryTolerance met.")
                route
            } else if (attempt >= maxAttempts - 1 && !attemptIncrease) {
                // Increase max attempts to try to find mitigate very bad route
                Log.d(TAG, "Neither tolerances met. Increasing max attempts for second chance")
                adjustRadiusForDistance(
                    userLocation,
                    targetDistance,
                    arcDegrees,
                    attempt + 1,
                    maxAttempts = maxAttempts * 2, // Double the attempt limit
                    minRadius = minRadius,
                    maxRadius = maxRadius,
                    bestRoute = newBestRoute,
                    bestDiff = newBestDiff,
                    attemptIncrease = true // set flag that attempts has been increased
                )
            } else if(actualDistance < targetDistance){
                adjustRadiusForDistance(
                    userLocation,
                    targetDistance,
                    arcDegrees,
                    attempt + 1,
                    maxAttempts,
                    minRadius = midRadius,
                    maxRadius = maxRadius,
                    bestRoute = newBestRoute,
                    bestDiff = newBestDiff,
                    attemptIncrease = attemptIncrease
                )
            } else {
                adjustRadiusForDistance(
                    userLocation,
                    targetDistance,
                    arcDegrees,
                    attempt + 1,
                    maxAttempts,
                    minRadius = minRadius,
                    maxRadius = midRadius,
                    bestRoute = newBestRoute,
                    bestDiff = newBestDiff,
                    attemptIncrease = attemptIncrease
                )
            }

        } catch (e: Exception) {
            Log.d(TAG, "Error building route ${e.message}")
            throw e
        }
    }
    /**
     * Calculates maximum attempts based on target distance
     *
     * Longer routes are more complex
     * Users searching for a longer route are more likely
     * fine/expect longer loading time
     *
     * @param targetDistance Distance in km
     * @return Maximum attempts allowed
     */
    private fun calculateMaxAttempts(targetDistance: Double):Int{
        return when {
            targetDistance < 5.0 -> 5
            targetDistance < 15.0 -> 7
            else -> 10
        }
    }
}