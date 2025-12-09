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

interface OpenRouteServiceApiService {

    @POST("v2/snap/driving-car")
    @Headers("Content-Type: application/json")
    fun snapCoordinates(
        @Body request: SnapRequest
    ): Call<SnapResponse>

    @POST("v2/directions/driving-car")
    @Headers("Content-Type: application/json")
    fun getDirections(
        @Body request: DirectionsRequest
    ): Call<DirectionsResponse>
}

data class SnapRequest(
    val locations: List<List<Double>>,
    val radius: Int = 300
)

data class SnapResponse(
    val locations: List<SnappedLocation?>
)

data class SnappedLocation(
    val location: List<Double>,
    val name: String,
    val snapped_distance: Double
)

data class DirectionsRequest(
    val coordinates: List<List<Double>>
)

data class DirectionsResponse(
    val routes: List<DirectionsRoute>
)

data class DirectionsRoute(
    val summary: RouteSummary,
    val geometry: String    //encoded polyline
)

data class RouteSummary(
    val distance: Double,
    val duration: Double
)

object OpenRouteServiceClient {
    private const val BASE_URL = "https://api.openrouteservice.org/"
    private const val TAG = "OpenRouteServiceClient"

    val apiService: OpenRouteServiceApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(createHttpClient())
            .build()

        retrofit.create(OpenRouteServiceApiService::class.java)
    }

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

    fun getApiKey(): String = BuildConfig.OPEN_ROUTE_SERVICE_API_KEY

    fun testApiKey() {
        Log.d(TAG, "Testing API Key: ${getApiKey()}")

        val testRequest = SnapRequest(
            locations = listOf(
                listOf(18.0686, 59.3293),
                listOf(18.0950, 59.3250)
            ),
            radius = 300
        )

        apiService.snapCoordinates(testRequest).enqueue(object : Callback<SnapResponse> {
            override fun onResponse(call: Call<SnapResponse>, response: Response<SnapResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "✅ API Test SUCCESS!")
                    val snappedLocations = response.body()?.locations?.filterNotNull()
                    Log.d(TAG, "Snapped locations: $snappedLocations")
                } else {
                    Log.e(TAG, "❌ API Test FAILED: ${response.code()} - ${response.message()}")
                    Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<SnapResponse>, t: Throwable) {
                Log.e(TAG, "❌ API Test FAILED with exception: ${t.message}")
                t.printStackTrace()
            }
        })
    }
}