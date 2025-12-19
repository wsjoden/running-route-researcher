package com.example.runningrouteresearcher.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningrouteresearcher.data.Route
import com.example.runningrouteresearcher.data.RouteRepository
import com.example.runningrouteresearcher.models.RouteAdjuster
import com.example.runningrouteresearcher.models.RouteGenerator
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Map ViewModel
 *
 * Manages route generation and states for UI
 * Uses LiveData for reactive updates
 */
class MapViewModel : ViewModel() {
    private val TAG = "MapViewModel"
    private val _distance = MutableLiveData<Double>()
    val distance: LiveData<Double> = _distance

    private val _route = MutableLiveData<Route>()
    val route: LiveData<Route> = _route

    private val _routes = MutableLiveData<List<Route>>()
    val routes: LiveData<List<Route>> = _routes

    private val _currentRouteIndex = MutableLiveData<Int>()
    val currentRouteIndex:LiveData<Int> = _currentRouteIndex

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val routeRepository = RouteRepository()
    private val routeAdjuster = RouteAdjuster()

    private val routeGenerator = RouteGenerator()

    /**
     * Generates one or more route for input distance
     *
     * Flow:
     * 1. Set loading flag
     * 2. Launch async coroutine scope
     * 3. Start parallel route generation
     * 4. Wait for all route generations to finish
     * 5. Store routes and set current to first
     * 6. Uncheck loading flag
     *
     * Future potential:
     * OpenRouteService API only allows for 40 API calls/minute for free.
     * Generating more routes often exceeds this limit(2 actually works most of the times)
     * With upgraded API key more routes could be generated for the user to pick and choose between.
     */
    fun generateRoute(userLocation: LatLng, distanceKm: Double) {
        _loading.value = true
        Log.d(TAG, "Generating route for ${distanceKm}km from ${userLocation.latitude}, ${userLocation.longitude}")

        viewModelScope.launch {
            try {
                val route1Deferred = async {
                    routeAdjuster.adjustRadiusForDistance(
                        userLocation = userLocation,
                        targetDistance = distanceKm,
                        arcDegrees = 360.0
                    )
                }



                val route2Deferred = async {
                    routeAdjuster.adjustRadiusForDistance(
                        userLocation = userLocation,
                        targetDistance = distanceKm,
                        arcDegrees = 270.0
                    )
                }

/*  More routes could be generated like this
                val route3Deferred = async {
                    routeAdjuster.adjustRadiusForDistance(
                        userLocation = userLocation,
                        targetDistance = distanceKm,
                        arcDegrees = 180.0
                    )
                }
*/
                // Let route generation finish
                val route1 = route1Deferred.await()
                val route2 = route2Deferred.await()

                _routes.value = listOf(route1,route2)
                _currentRouteIndex.value = 0    // Default to first route
                _route.value = route1   // Trigger UI update

                Log.d(TAG,"Route generated successfully!")
                _error.value = null
            } catch (e: Exception) {
                Log.d(TAG, "Route generation failed!")
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Switches to a different route variant when user taps
     * next/previous buttons
     *
     * Validates index is within bounds
     * update index and _route (triggers drawRouteOnMap in MapFragment)
     *
     * @param index Index of route to switch to(0based)
     */
    fun switchToRoute(index: Int) {
        if (index in 0 until (_routes.value?.size ?: 0)) {
            _currentRouteIndex.value = index
            _route.value = _routes.value!![index]
        }
    }
}