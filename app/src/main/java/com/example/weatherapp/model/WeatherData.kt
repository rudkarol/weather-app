package com.example.weatherapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val location: Location? = null,
    val current: Current? = null,
    val forecast: Forecast? = null
)

@Serializable
data class Location(
    val name: String,
    val region: String,
    val country: String
)

@Serializable
data class Current(
    @SerialName("last_updated_epoch") val lastUpdatedEpoch: Long,
    @SerialName("temp_c") val tempC: Double,
    @SerialName("temp_f") val tempF: Double,
    val condition: Condition
)

@Serializable
data class Condition(
    val text: String,
    val icon: String
)

@Serializable
data class Forecast(
    @SerialName("forecastday") val forecastDay: List<ForecastDay>
)

@Serializable
data class ForecastDay(
    @SerialName("date_epoch") val dateEpoch: Long,
    val day: Day,
    @SerialName("hour") val hour: List<Hour>
)

@Serializable
data class Day(
    @SerialName("maxtemp_c") val maxTempC: Double,
    @SerialName("maxtemp_f") val maxTempF: Double,
    @SerialName("mintemp_c") val minTempC: Double,
    @SerialName("mintemp_f") val minTempF: Double,
    val condition: Condition
)

@Serializable
data class Hour(
    @SerialName("time_epoch") val timeEpoch: Long,
    @SerialName("temp_c") val tempC: Double,
    @SerialName("temp_f") val tempF: Double,
    val condition: Condition
)
