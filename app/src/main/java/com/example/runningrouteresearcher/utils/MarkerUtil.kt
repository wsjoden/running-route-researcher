package com.example.runningrouteresearcher.utils

import com.example.runningrouteresearcher.R

/**
 * Marker Utility
 *
 * Maps waypoint numbers to corresponding drawable resource
 * Supports up to 55 waypoints (~100km)
 */

object MarkerUtil {


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
            10 -> R.drawable.waypoint_10
            11 -> R.drawable.waypoint_11
            12 -> R.drawable.waypoint_12
            13 -> R.drawable.waypoint_13
            14 -> R.drawable.waypoint_14
            15 -> R.drawable.waypoint_15
            16 -> R.drawable.waypoint_16
            17 -> R.drawable.waypoint_17
            18 -> R.drawable.waypoint_18
            19 -> R.drawable.waypoint_19
            20 -> R.drawable.waypoint_20
            21 -> R.drawable.waypoint_21
            22 -> R.drawable.waypoint_22
            23 -> R.drawable.waypoint_23
            24 -> R.drawable.waypoint_24
            25 -> R.drawable.waypoint_25
            26 -> R.drawable.waypoint_26
            27 -> R.drawable.waypoint_27
            28 -> R.drawable.waypoint_28
            29 -> R.drawable.waypoint_29
            30 -> R.drawable.waypoint_30
            31 -> R.drawable.waypoint_31
            32 -> R.drawable.waypoint_32
            33 -> R.drawable.waypoint_33
            34 -> R.drawable.waypoint_34
            35 -> R.drawable.waypoint_35
            36 -> R.drawable.waypoint_36
            37 -> R.drawable.waypoint_37
            38 -> R.drawable.waypoint_38
            39 -> R.drawable.waypoint_39
            40 -> R.drawable.waypoint_40
            41 -> R.drawable.waypoint_41
            42 -> R.drawable.waypoint_42
            43 -> R.drawable.waypoint_43
            44 -> R.drawable.waypoint_44
            45 -> R.drawable.waypoint_45
            46 -> R.drawable.waypoint_46
            47 -> R.drawable.waypoint_47
            48 -> R.drawable.waypoint_48
            49 -> R.drawable.waypoint_49
            50 -> R.drawable.waypoint_50
            51 -> R.drawable.waypoint_51
            52 -> R.drawable.waypoint_52
            53 -> R.drawable.waypoint_53
            54 -> R.drawable.waypoint_54
            55 -> R.drawable.waypoint_55
            else -> R.drawable.waypoint_1  // fallback
        }
    }
}