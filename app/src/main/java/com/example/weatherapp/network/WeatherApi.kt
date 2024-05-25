package com.example.weatherapp.network

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.model.WeatherData
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.Retrofit
import retrofit2.http.GET
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.http.Query


private const val BASE_URL = "https://weatherapi-com.p.rapidapi.com"
private const val API_KEY = BuildConfig.RAPID_API_KEY

private val httpClient = OkHttpClient.Builder()
private val json: Json = Json { ignoreUnknownKeys = true }

private val retrofit = Retrofit.Builder()
    .client(httpClient.build())
    .addConverterFactory(
        json.asConverterFactory(
            "application/json; charset=UTF8".toMediaType())
    )
    .baseUrl(BASE_URL)
    .build()

interface ForecastApiService {
    @GET("forecast.json")
    suspend fun getWeather(
        @Query("q") location: String,
        @Query("days") days: Int = 3
    ): WeatherData
}

object WeatherApi {
    val retrofitService: ForecastApiService by lazy {
        retrofit.create(ForecastApiService::class.java)
    }
}
