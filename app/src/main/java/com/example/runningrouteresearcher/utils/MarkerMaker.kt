package com.example.runningrouteresearcher.utils

import com.example.runningrouteresearcher.R


object MarkerMaker {


    fun getMarkerDrawableForNumber(number: Int): Int {
        return when (number) {
            1 -> R.drawable.waypoint_1
            2 -> R.drawable.waypoint_2
            3 -> R.drawable.waypoint_3
            4 -> R.drawable.waypoint_4
            5 -> R.drawable.waypoint_5
            6 -> R.drawable.waypoint_6
            7 -> R.drawable.waypoint_7
            8 -> R.drawable.waypoint_8
            9 -> R.drawable.waypoint_9
            else -> R.drawable.waypoint_1  // fallback
        }
    }
}