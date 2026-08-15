package com.example.shopsafe.data.ml

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

data class DirectionsResponse(
    val routes: List<Route>
)
data class Route(
    val legs: List<Leg>,
    val overview_polyline: Polyline
)
data class Leg(
    val distance: TextValue,
    val duration: TextValue,
    val duration_in_traffic: TextValue? = null
)
data class TextValue(
    val text: String,
    val value: Int // In meters or seconds
)
data class Polyline(
    val points: String
)

interface GoogleMapsDirectionsApi {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String,
        @Query("departure_time") departureTime: String = "now",
        @Query("traffic_model") trafficModel: String = "best_guess"
    ): DirectionsResponse
}

class DynamicRoutingEngine(
    private val apiKey: String
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        
    private val api = retrofit.create(GoogleMapsDirectionsApi::class.java)
    
    /**
     * Evaluates if the driver should switch to a new route due to traffic.
     * @param originLat Current driver latitude
     * @param originLng Current driver longitude
     * @param destLat Destination latitude
     * @param destLng Destination longitude
     * @param currentEtaSeconds The driver's currently predicted ETA in seconds
     * @return A new better Route if found, else null.
     */
    suspend fun recalculateRouteIfTrafficChanged(
        originLat: Double, 
        originLng: Double, 
        destLat: Double, 
        destLng: Double,
        currentEtaSeconds: Int
    ): Route? = withContext(Dispatchers.IO) {
        try {
            val response = api.getDirections(
                origin = "$originLat,$originLng",
                destination = "$destLat,$destLng",
                apiKey = apiKey
            )
            
            val bestRoute = response.routes.firstOrNull() ?: return@withContext null
            val bestLeg = bestRoute.legs.firstOrNull() ?: return@withContext null
            
            // Check traffic duration
            val newEtaSeconds = bestLeg.duration_in_traffic?.value ?: bestLeg.duration.value
            
            // If the new route is at least 3 minutes (180 seconds) faster than our current projection,
            // we suggest dynamic rerouting.
            val improvementSeconds = currentEtaSeconds - newEtaSeconds
            if (improvementSeconds > 180) {
                Log.d("DynamicRoutingEngine", "Found a faster route! Saves $improvementSeconds seconds.")
                return@withContext bestRoute
            } else {
                Log.d("DynamicRoutingEngine", "Current route is optimal. Improvement: $improvementSeconds seconds.")
            }
            null
        } catch (e: Exception) {
            Log.e("DynamicRoutingEngine", "Failed to fetch directions: ${e.message}")
            null
        }
    }
}
