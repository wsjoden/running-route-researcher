package com.example.runningrouteresearcher.models

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class RouteGenerator {
    private val TAG = "RouteGenerator"

    /**
     * Route Generator
     *
     * Generates circular waypoint pattern around user location(center)
     * These waypoints are used as nodes for route generation after being
     * snapped to nearest roads
     *
     * Algorithm:
     * 1. Calculate number of waypoints based on distance and arc degrees
     * 2. Convert distance to radius
     * 3. Calculate angle between each waypoint
     * 4. Convert to cartesian coordinates
     * 5. Offset waypoints from center
     *
     * @param centerLocation user location, center of circle
     * @param distance radius in km for circular pattern
     * @param arcDegrees Arc degrees (360=full circle)
     * @param rotationOffset Degrees to rotate the circular pattern. Currently not used.
     *
     * @return List of LatLng waypoints
     */
    fun generateCircularWaypoints(
        centerLocation: LatLng,
        distance: Double,
        arcDegrees: Double = 360.0,
        rotationOffset: Double = 0.0 // TODO: Use to avoid large masses of water/mountains etc...
    ):List<LatLng> {
        val numberOfWayPoints = calcAmountOfWayPoints(distance, arcDegrees)
        // Convert distance to radius
        val radius = distance / (2 * PI)

        val waypoints = mutableListOf<LatLng>()
        // Angle between each waypoint
        val angleStep = Math.toRadians(arcDegrees) / numberOfWayPoints

        for(i in 0 until numberOfWayPoints){
            val angle = i * angleStep + Math.toRadians(rotationOffset)

            //Convert polar to cartesian coordinates
            val x = radius * cos(angle)
            val y = radius * sin(angle)

            val waypoint = offsetLocation(centerLocation, x, y)
            waypoints.add(waypoint)
        }
        return waypoints
    }

    /**
     * Converts cartesian offset (x,y km) to latitude/longitude offset
     *
     * 1 degree of lat is about 111km
     * 1 degree of longitude varies by latitude but can be calculated with:
     * longitude offset = offset / (111 * cos(latitude))
     *
     * @param center Center location
     * @param offsetEast Distance to move in east (negative = west)
     * @param offsetNorth Distance to move in north (negative = south)
     *
     * @return LatLng at the offset location
     */
    private fun offsetLocation(
        center: LatLng,
        offsetEast: Double,
        offsetNorth: Double
    ): LatLng {
        val latOffset = offsetNorth / 111.0
        val lonOffset = offsetEast / (111.0 * cos(Math.toRadians(center.latitude)))

        return LatLng(
            center.latitude + latOffset,
            center.longitude + lonOffset
        )
    }

    /**
     * Calculates number of waypoints based on distance and arc degrees
     *
     * Will always return a minimum of 3 waypoints
     * Determine base amount of waypoint from distance
     * Scale by arc degrees
     *
     * @param radius Radius in km
     * @param arcDegrees Arc degrees (360=full circle)
     *
     * @return Amount of waypoints to generate
     */
    private fun calcAmountOfWayPoints(
        radius: Double,
        arcDegrees: Double = 360.0
    ):Int {
        val waypoints = when {
            radius < 2.0 -> 4
            radius < 3.0 -> 6
            radius < 8.0 -> 8
            radius < 15.0 -> 10
            else -> ((radius / 2) + 3).toInt()
        }

        // Scale down based on arc degrees
        val arcFactor = arcDegrees / 360.0
        val adjustedWaypoints = Math.max(3, (waypoints * arcFactor).toInt())
        Log.d(TAG,"waypoints = $waypoints adjusted to = $adjustedWaypoints")
        return adjustedWaypoints
    }
}