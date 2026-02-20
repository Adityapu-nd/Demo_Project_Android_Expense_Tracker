package com.example.expense_tracker_android.model

import android.content.Context
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRatesService {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("access_key") apiKey: String,
//        @Query("base") base: String = "INR"

    ): LatestRatesResponse
}

data class LatestRatesResponse(
    val success: Boolean,
    val timestamp: Long? = null,
    val base: String? = null,
    val date: String? = null,
    val rates: Map<String, Double>? = null,
    val error: ErrorResponse? = null
)

data class ErrorResponse(
    val code: Int? = null,
    val type: String? = null,
    val info: String? = null
)

object ExchangeRatesApi {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.exchangeratesapi.io/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service: ExchangeRatesService = retrofit.create(ExchangeRatesService::class.java)
}

fun saveExchangeRateResponseToFile(context: Context, response: LatestRatesResponse, fileName: String = "exchange_rate_response.json") {
    val gson = Gson()
    val jsonString = gson.toJson(response)
    val file = File(context.filesDir, fileName)
    FileWriter(file).use { writer ->
        writer.write(jsonString)
    }
}
