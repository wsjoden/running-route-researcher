package com.example.runningrouteresearcher.data

import android.util.Log
import com.example.runningrouteresearcher.data.Route
import com.example.runningrouteresearcher.data.OpenRouteServiceClient
import com.example.runningrouteresearcher.models.RouteGenerator
import com.example.runningrouteresearcher.utils.PolylineDecoder
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RouteRepository {
    private val routeGenerator = RouteGenerator()
    private val apiClient = OpenRouteServiceClient
    private val TAG = "RouteRepository"

    // Callback interface for async operations
    interface RouteCallback {
        fun onSuccess(route: Route)
        fun onError(error: String)
    }

    fun generateRoute(
        userLocation: LatLng,
        distanceKm: Double,
        callback: RouteCallback
    ) {
        try {
            // Step 1: Generate circular waypoints
            var waypoints = routeGenerator.generateCircularWaypoints(
                centerLocation = userLocation,
                distance = distanceKm,
                numberOfWayPoints = 8   //this needs to be dynamic at some point.
            )

            // Step 2: add user location as first and last waypoint
            waypoints = listOf(userLocation) + waypoints + listOf(userLocation)

            Log.d(TAG, "Generated ${waypoints.size} waypoints in circle")

            // Step 3: Snap waypoints to roads
            snapWaypoints(waypoints, userLocation, distanceKm, callback)

        } catch (e: Exception) {
            Log.e(TAG, "Error generating route: ${e.message}")
            callback.onError(e.message ?: "Unknown error")
        }
    }

    private fun snapWaypoints(
        waypoints: List<LatLng>,
        userLocation: LatLng,
        desiredDistanceKm: Double,
        callback: RouteCallback
    ) {
        // Convert waypoints to format expected by API
        val locations = waypoints.map { listOf(it.longitude, it.latitude) }

        val snapRequest = com.example.runningrouteresearcher.data.SnapRequest(
            locations = locations,
            radius = 300
        )

        apiClient.apiService.snapCoordinates(snapRequest)
            .enqueue(object : Callback<com.example.runningrouteresearcher.data.SnapResponse> {
                override fun onResponse(
                    call: Call<com.example.runningrouteresearcher.data.SnapResponse>,
                    response: Response<com.example.runningrouteresearcher.data.SnapResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val snappedLocations = response.body()!!.locations.filterNotNull()
                        Log.d(TAG, "Snapped ${snappedLocations.size} waypoints to roads")

                        // Convert snapped locations back to LatLng
                        val snappedWaypoints = snappedLocations.map { snapped ->
                            LatLng(snapped.location[1], snapped.location[0])
                        }

                        getDirections(snappedWaypoints, desiredDistanceKm, callback)
                    } else {
                        callback.onError("Failed to snap waypoints: ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: Call<com.example.runningrouteresearcher.data.SnapResponse>,
                    t: Throwable
                ) {
                    Log.e(TAG, "Snap API error: ${t.message}")
                    callback.onError(t.message ?: "Network error")
                }
            })
    }
    private fun getDirections(
        waypoints: List<LatLng>,
        wantedDistance:Double,
        callback: RouteCallback
    ) {
        // convert waypoints to coordinates
        val coordinates = waypoints.map{listOf(it.longitude,it.latitude)}

        val directionsRequest = DirectionsRequest(
            coordinates = coordinates
        )

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

                            // Decode the polyline
                            val polylinePoints = PolylineDecoder.decode(firstRoute.geometry)
                            val actualDistance = firstRoute.summary.distance / 1000.0  // Convert meters to km

                            Log.d(TAG, "Got route with ${polylinePoints.size} points, distance: ${actualDistance}km")

                            val route = Route(
                                waypoints = waypoints,
                                distance = actualDistance,
                                polylinePoints = polylinePoints
                            )

                            callback.onSuccess(route)
                        } else {
                            callback.onError("No routes returned from API")
                        }
                    } else {
                        callback.onError("Failed to get directions: ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: Call<DirectionsResponse>,
                    t: Throwable
                ) {
                    Log.e(TAG, "Directions API error: ${t.message}")
                    callback.onError(t.message ?: "Network error")
                }
            })
    }
}