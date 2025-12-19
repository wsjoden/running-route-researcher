package com.example.runningrouteresearcher.data

import android.util.Log
import com.example.runningrouteresearcher.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * API Interfaces, data classes and client config
 * to communicate with the OpenRouteService API
 * https://openrouteservice.org/
 *
 * The application uses two endpoints from OpenRouteService
 * 1. Snap API: Snaps coordinates to nearest road
 * 2. Directions API: Calculates routes between coordinates
 */



interface OpenRouteServiceApiService {
    /**
     * Snaps a list of coordinates to the nearest roads
     *
     * Endpoint: POST /v2/snap/driving-car
     *
     * @param request unsnapped coordinates
     * @return SnapResponse with snapped coordinates
     */
    @POST("v2/snap/driving-car")
    @Headers("Content-Type: application/json")
    fun snapCoordinates(
        @Body request: SnapRequest
    ): Call<SnapResponse>

    /**
     * Calculates the optimal driving route between two coordinates
     *
     * Takes snapped waypoints and returns a route including
     * an encoded polyline, distance and duration(to drive)
     *
     * Endpoint: POST /v2/directions/driving-car
     *
     * @param request DirectionsRequest containing waypoint coordinates
     * @return DirectionsResponse with a route geometry and summary
     */
    @POST("v2/directions/driving-car")
    @Headers("Content-Type: application/json")
    fun getDirections(
        @Body request: DirectionsRequest
    ): Call<DirectionsResponse>
}

/**
 * Request body for Snap API
 *
 * @param locations List of[lon, lat]
 * @param radius Search radius in meters, default = 300m
 */
data class SnapRequest(
    val locations: List<List<Double>>,
    val radius: Int = 300
)

/**
 * Response from Snap API
 *
 * @param locations List of snapped waypoints, may contain null if snap fails
 */
data class SnapResponse(
    val locations: List<SnappedLocation?>
)

/**
 * A single snapped waypoint from Snap API
 *
 * @param location [lon, lat] coordinate snapped to nearest road
 * @param name Name of the road the waypoint snapped to
 * @param snapped_distance Distance in meter that the point was moved
 */
data class SnappedLocation(
    val location: List<Double>,
    val name: String,
    val snapped_distance: Double
)

/**
 * Request body for Directions API
 *
 * @param coordinates List of [lon, lat] pairs representing waypoints
 */
data class DirectionsRequest(
    val coordinates: List<List<Double>>
)

/**
 * Response from Directions API
 *
 * @param routes List of possible routes
 */
data class DirectionsResponse(
    val routes: List<DirectionsRoute>
)

/**
 * A single route from the Directions API
 *
 * @param summary Route metadata containing metadata (we only use distance)
 * @param geometry Encoded polyline string representing the route
 */
data class DirectionsRoute(
    val summary: RouteSummary,
    val geometry: String    //encoded polyline
)

/**
 * Route metadata from Directions API
 *
 * @param distance Total distance of the route in meters
 * @param duration Estimated driving duration in seconds (we dont use this)
 */
data class RouteSummary(
    val distance: Double,
    val duration: Double
)

/**
 * Manages Retrofit config, Http client setup and
 * API key auth.
 * Lazy init to only create Retrofit instance when accessed
 *
 * API KEY is stored in local.properties as
 * OPEN_ROUTE_SERVICE_API_KEY and is added to all
 * requests via OkHttp interceptor
 */
object OpenRouteServiceClient {
    private const val BASE_URL = "https://api.openrouteservice.org/"
    private const val TAG = "OpenRouteServiceClient"

    /**
     * Lazy initialized Retrofit instance
     * Includes Gson converter for JSON serialization/deserialization
     */
    val apiService: OpenRouteServiceApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(createHttpClient())
            .build()

        retrofit.create(OpenRouteServiceApiService::class.java)
    }

    /**
     * Creates OkHttp client with authentication
     *
     * Adds an interceptor that includes the API key
     * in Authorization header for all requests
     */
    private fun createHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", getApiKey())
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    /**
     * Retrieves the API key from local.properties
     */
    fun getApiKey(): String = BuildConfig.OPEN_ROUTE_SERVICE_API_KEY
}