package com.example.runningrouteresearcher.models

import com.google.android.gms.maps.model.LatLng
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class RouteGenerator {

    fun generateCircularWaypoints(
        centerLocation: LatLng,
        distance: Double,
        numberOfWayPoints: Int = 8,
        degrees: Double = 360.0
    ):List<LatLng> {
        val radius = distance / (2 * PI)

        val waypoints = mutableListOf<LatLng>()
        // Angle between each waypoint
        val angleStep = Math.toRadians(degrees) / numberOfWayPoints

        for(i in 0 until numberOfWayPoints){
            val angle = i * angleStep

            //Convert polar to cartesian coordinates
            val x = radius * cos(angle)
            val y = radius * sin(angle)

            val waypoint = offsetLocation(centerLocation, x, y)
            waypoints.add(waypoint)
        }
        return waypoints
    }

    /**
     * Offsets a location by x and y km from the center
     */
    private fun offsetLocation(
        center: LatLng,
        offsetEast: Double,
        offsetNorth: Double
    ): LatLng {
        // 1 degree of lat is about 111km
        val latOffset = offsetNorth / 111.0

        // 1 degree of longitude varies by latitude
        // longitude offset = offset / (111 * cos(latitude))
        val lonOffset = offsetEast / (111.0 * cos(Math.toRadians(center.latitude)))

        return LatLng(
            center.latitude + latOffset,
            center.longitude + lonOffset
        )
    }
}