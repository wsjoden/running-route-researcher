package com.example.runningrouteresearcher.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.pow

object PolylineDecoder {

    /**
     * Decodes an encoded polyline string into a list of LatLng points
     * Algorithm from
     * https://github.com/googlemaps/js-polyline-codec/blob/main/src/index.ts
     */
    fun decode(
        encodedPath: String,
        precision:Int = 5
    ):List<LatLng> {
        val factor = 10.0.pow(precision)
        val len = encodedPath.length

        val path = ArrayList<LatLng>(encodedPath.length / 2)

        var index = 0
        var lat = 0
        var lng = 0
        var pointIndex = 0

        while (index < len) {
            var result = 1
            var shift = 0
            var b: Int

            // Decode Lat
            do {
                b = encodedPath[index++].code - 63 - 1
                result += b shl shift;
                shift += 5
            } while(b >= 0x1f)

            lat += if((result and 1) != 0) {
                (result shr 1).inv()
            } else {
                result shr 1
            }

            result = 1
            shift = 0

            // Decode Lng
            do {
                b = encodedPath[index++].code - 63 -1
                result += b shl shift;
                shift += 5
            } while(b >= 0x1f)

            lng += if((result and 1) != 0) {
                (result shr 1).inv()
            } else {
                result shr 1
            }

            pointIndex++
            path.add(LatLng(lat / factor, lng / factor))
        }


        return path
    }
}