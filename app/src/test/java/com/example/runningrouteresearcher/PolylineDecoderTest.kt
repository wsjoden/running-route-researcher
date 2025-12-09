package com.example.runningrouteresearcher

import com.example.runningrouteresearcher.utils.PolylineDecoder
import com.google.android.gms.maps.model.LatLng
import junit.framework.TestCase.assertEquals
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.Test

class PolylineDecoderTest {

    @Test
    fun testDecodeSimplePolyline() {
        // This is a famous test polyline from Google's documentation
        // It represents: (38.5, -120.2), (40.7, -120.95), (43.252, -126.453)
        val encoded = "_p~iF~ps|U_ulLnnqC_mqNvxq`@"

        val result = PolylineDecoder.decode(encoded)

        assertEquals(3, result.size)
        assertLatLngClose(LatLng(38.5, -120.2), result[0], 0.001)
        assertLatLngClose(LatLng(40.7, -120.95), result[1], 0.001)
        assertLatLngClose(LatLng(43.252, -126.453), result[2], 0.001)
    }

    @Test
    fun testDecodeSinglePoint() {
        val encoded = "_p~iF~ps|U"
        val result = PolylineDecoder.decode(encoded)

        assertEquals(1, result.size)
        assertLatLngClose(LatLng(38.5, -120.2), result[0], 0.001)
    }

    private fun assertLatLngClose(expected: LatLng, actual: LatLng, tolerance: Double) {
        assertTrue("Lat mismatch: expected ${expected.latitude}, got ${actual.latitude}",
            Math.abs(expected.latitude - actual.latitude) < tolerance)
        assertTrue("Lng mismatch: expected ${expected.longitude}, got ${actual.longitude}",
            Math.abs(expected.longitude - actual.longitude) < tolerance)
    }
}