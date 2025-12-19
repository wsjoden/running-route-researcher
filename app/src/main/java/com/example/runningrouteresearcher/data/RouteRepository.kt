package com.example.runningrouteresearcher.data

import android.util.Log
import com.example.runningrouteresearcher.utils.PolylineDecoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.resume
/**
 * Route Repository
 *
 * Data access layer that handles all communication with the OpenRouteService API.
 *
 * 1. Snap waypoints to roads
 * 2. Calculate driving directions between waypoints
 *
 * Uses Kotlin coroutines for async API calls
 */
class RouteRepository {
    private val apiClient = OpenRouteServiceClient
    private val TAG = "RouteRepository"

    /**
     * Snaps a list of waypoints/coordinates to nearest road
     *
     * @param waypoints List of LatLng points to snap to roads
     * @return Route object with snapped waypoints (distance and polylinePoints are empty)
     * @throws Exception if API call fails or returns error code
     */
    suspend fun snapWaypoints(waypoints: List<LatLng>): Route {
        // Convert LatLng waypoints to [lon, lat] format for API
        val locations = waypoints.map {
            listOf(it.longitude, it.latitude)
        }

        val snapRequest = SnapRequest(
            locations = locations,
            radius = 300    // Radius to look for roads in meters
        )

        var route: Route? = null
        // Call OpenRouteService Snap API
        suspendCancellableCoroutine { continuation ->
            apiClient.apiService.snapCoordinates(snapRequest)
                .enqueue(object : Callback<SnapResponse> {
                    override fun onResponse(
                        call: Call<SnapResponse>,
                        response: Response<SnapResponse>
                    ) {
                        try {
                            if (response.isSuccessful && response.body() != null) {
                                val snappedLocations = response.body()!!.locations.filterNotNull()
                                Log.d(TAG, "Snapped ${snappedLocations.size} waypoints to roads")

                                // Convert snapped coordinates back to LatLng
                                val snappedWaypoints = snappedLocations.map { snapped ->
                                    LatLng(snapped.location[1], snapped.location[0])
                                }

                                route = Route(
                                    waypoints = snappedWaypoints,
                                    distance = 0.0,
                                    polylinePoints = emptyList()
                                )

                                continuation.resume(Unit)
                            } else {
                                throw Exception("Failed to snap waypoints: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    }

                    override fun onFailure(
                        call: Call<SnapResponse>,
                        t: Throwable
                    ) {
                        Log.e(TAG, "Snap API error: ${t.message}")
                        continuation.resumeWithException(t)
                    }
                })
        }
        //!! for safe API call guarantee the object is created before returning
        return route!!
    }

    /**
     * Calculates driving route between waypoints/coordinates
     *
     * Takes snapped waypoints and requests directions from
     * OpenRouteService Directions API that returns an
     * encoded polyline and total distance in meters.
     *
     * @param waypoints List of LatLng waypoints
     * @return Route object with polyline points and calculated distance
     * @throws Exception if API call fails, no routes returned, or decoding fails
     */
    suspend fun getDirections(waypoints: List<LatLng>): Route {
        // Convert LatLng waypoints to [lon, lat] format for API
        val coordinates = waypoints.map { listOf(it.longitude, it.latitude) }

        val directionsRequest = DirectionsRequest(
            coordinates = coordinates
        )

        var route: Route? = null

        // Call OpenRouteService Directions API
        suspendCancellableCoroutine { continuation ->
            apiClient.apiService.getDirections(directionsRequest)
                .enqueue(object : Callback<DirectionsResponse> {
                    override fun onResponse(
                        call: Call<DirectionsResponse>,
                        response: Response<DirectionsResponse>
                    ) {
                        try {
                            if (response.isSuccessful && response.body() != null) {
                                val routes = response.body()!!.routes
                                if (routes.isNotEmpty()) {
                                    val firstRoute = routes[0]

                                    // Decode polyline into individual points
                                    val polylinePoints = PolylineDecoder.decode(firstRoute.geometry)
                                    // Convert meters -> km
                                    val actualDistance = firstRoute.summary.distance / 1000.0

                                    Log.d(TAG, "Got route with ${polylinePoints.size} points, distance: ${actualDistance}km")

                                    route = Route(
                                        waypoints = waypoints,
                                        distance = actualDistance,
                                        polylinePoints = polylinePoints
                                    )

                                    continuation.resume(Unit)
                                } else {
                                    throw Exception("No routes returned from API")
                                }
                            } else {
                                throw Exception("Failed to get directions: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    }

                    override fun onFailure(
                        call: Call<DirectionsResponse>,
                        t: Throwable
                    ) {
                        Log.e(TAG, "Directions API error: ${t.message}")
                        continuation.resumeWithException(t)
                    }
                })
        }
        //!! for safe API call guarantee the object is created before returning
        return route!!
    }
}



