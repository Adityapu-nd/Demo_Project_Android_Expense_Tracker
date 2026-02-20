package com.example.expense_tracker_android.ui.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.expense_tracker_android.apikey.API_KEY
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Data model for the API response
data class LatestRatesResponse(
    val success: Boolean,
    val timestamp: Long?,
    val base: String?,
    val date: String?,
    val rates: Map<String, Double>?
)

// Retrofit service interface
interface LatestRatesService {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("access_key") accessKey: String
    ): LatestRatesResponse
}

object LatestRatesApi {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.exchangeratesapi.io/v1/") // changed to https
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service: LatestRatesService = retrofit.create(LatestRatesService::class.java)
}

@Composable
fun TestScreen() {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    // Top right button
    IconButton(onClick = {
        coroutineScope.launch {
            try {
                val response = LatestRatesApi.service.getLatestRates(API_KEY)
                // Only save the rates map (String:Double) with base INR
                val ratesMap = response.rates ?: emptyMap()
                // Convert the map to JSON string
                val json = com.google.gson.Gson().toJson(ratesMap)
                // Save the JSON string to a file
                context.openFileOutput("inr_rates_map.json", android.content.Context.MODE_PRIVATE).use { fos ->
                    fos.write(json.toByteArray())
                }
                println("INR Rates Map saved: $json")
                Log.d("LatestRates", "INR Rates Map saved: $json")
                Toast.makeText(context, "INR Rates Map saved.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("LatestRates", "Exception: " + (e.localizedMessage ?: e.javaClass.simpleName))
                Toast.makeText(context, "Exception: " + (e.localizedMessage ?: e.javaClass.simpleName), Toast.LENGTH_SHORT).show()
            }
        }
    }) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.List,
            contentDescription = "Fetch Rates",
            tint = Color(0xFF2B5DF5)
        )
    }
}

@Preview
@Composable
fun PreviewTestScreen() {
    TestScreen()
}