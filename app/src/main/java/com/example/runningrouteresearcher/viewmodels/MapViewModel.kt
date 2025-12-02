package com.example.runningrouteresearcher.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.runningrouteresearcher.data.Route
import com.example.runningrouteresearcher.models.RouteGenerator
import com.google.android.gms.maps.model.LatLng

class MapViewModel : ViewModel() {

    private val _distance = MutableLiveData<Double>()
    val distance: LiveData<Double> = _distance

    private val _route = MutableLiveData<Route>()
    val route: LiveData<Route> = _route

    private val routeGenerator = RouteGenerator()

    fun setDistance(distanceKm: Double) {
        _distance.value = distanceKm
    }

    fun generateRoute(userLocation: LatLng, distance: Double) {
        try{
            // Generate waypoints in a circle
            val waypoints = routeGenerator.generateCircularWaypoints(
                centerLocation = userLocation,
                distance = distance,
                numberOfWayPoints = 8
            )

            val generatedRoute = Route(
                waypoints = waypoints,
                distance = distance,
                /**
                 * Same as waypoints for now
                 * TBA Snap to road functionality
                 */

                polylinePoints = waypoints
            )
        _route.value = generatedRoute
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
}