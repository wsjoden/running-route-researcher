package com.example.runningrouteresearcher.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {

    private val _distance = MutableLiveData<Double>()
    val distance: LiveData<Double> = _distance

    private val _route = MutableLiveData<String>()
    val route: LiveData<String> = _route

    fun setDistance(distanceKm: Double) {
        _distance.value = distanceKm
    }

    fun generateRoute(distanceKm: Double) {
        // TODO: Call API to generate route
        _route.value = "Route generated for ${distanceKm}km"
    }
}