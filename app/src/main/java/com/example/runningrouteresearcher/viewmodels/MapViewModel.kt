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
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val TAG = "MapViewModel"
    private val _distance = MutableLiveData<Double>()
    val distance: LiveData<Double> = _distance

    private val _route = MutableLiveData<Route>()
    val route: LiveData<Route> = _route

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val routeRepository = RouteRepository()
    private val routeAdjuster = RouteAdjuster()

    private val routeGenerator = RouteGenerator()

    fun setDistance(distanceKm: Double) {
        _distance.value = distanceKm
    }

    fun generateRoute(userLocation: LatLng, distanceKm: Double) {
        _loading.value = true
        Log.d(TAG, "Generating route for ${distanceKm}km from ${userLocation.latitude}, ${userLocation.longitude}")

        viewModelScope.launch {
            try {
                val route = routeAdjuster.adjustRadiusForDistance(
                    userLocation = userLocation,
                    targetDistance = distanceKm
                )
                Log.d(TAG,"Route generated successfully!")
                _route.value = route
                _error.value = null
            } catch (e: Exception) {
                Log.d(TAG, "Route generation failed!")
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}