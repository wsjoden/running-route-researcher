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

class RouteRepository {
    private val apiClient = OpenRouteServiceClient
    private val TAG = "RouteRepository"

    suspend fun snapWaypoints(waypoints: List<LatLng>): Route {
        val locations = waypoints.map {
            listOf(it.longitude, it.latitude)
        }
        val snapRequest = SnapRequest(
            locations = locations,
            radius = 300
        )
        return suspendCancellableCoroutine { continuation ->
            apiClient.apiService.snapCoordinates(snapRequest)
                .enqueue(object : Callback<SnapResponse> {
                    override fun onResponse(
                        call: Call<SnapResponse>,
                        response: Response<SnapResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val snappedLocations = response.body()!!.locations.filterNotNull()
                            Log.d(TAG, "Snapped ${snappedLocations.size} waypoints to roads")

                            val snappedWaypoints = snappedLocations.map { snapped ->
                                LatLng(snapped.location[1], snapped.location[0])
                            }

                            continuation.resume(
                                Route(
                                    waypoints = snappedWaypoints,
                                    distance = 0.0,
                                    polylinePoints = emptyList()
                                )
                            )
                        } else {
                            continuation.resumeWithException(Exception("Failed to snap waypoints: ${response.code()}"))
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
    }

    suspend fun getDirections(waypoints: List<LatLng>): Route {
        val coordinates = waypoints.map { listOf(it.longitude, it.latitude) }

        val directionsRequest = DirectionsRequest(
            coordinates = coordinates
        )

        return suspendCancellableCoroutine { continuation ->
            apiClient.apiService.getDirections(directionsRequest)
                .enqueue(object : Callback<DirectionsResponse> {
                    override fun onResponse(
                        call: Call<DirectionsResponse>,
                        response: Response<DirectionsResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val routes = response.body()!!.routes
                            if (routes.isNotEmpty()) {
                                val firstRoute = routes[0]

                                val polylinePoints = PolylineDecoder.decode(firstRoute.geometry)
                                val actualDistance = firstRoute.summary.distance / 1000.0

                                Log.d(TAG, "Got route with ${polylinePoints.size} points, distance: ${actualDistance}km")

                                continuation.resume(
                                    Route(
                                        waypoints = waypoints,
                                        distance = actualDistance,
                                        polylinePoints = polylinePoints
                                    )
                                )
                            } else {
                                continuation.resumeWithException(Exception("No routes returned from API"))
                            }
                        } else {
                            continuation.resumeWithException(Exception("Failed to get directions: ${response.code()}"))
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
    }
}



