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
    val condition: Condition,
    @SerialName("humidity") val humidity: Double,
    @SerialName("feelslike_c") val feelsLikeC: Double,
    @SerialName("feelslike_f") val feelsLikeF: Double,
    @SerialName("uv") val uv: Double,
    @SerialName("pressure_mb") val pressureMb: Double,
    @SerialName("pressure_in") val pressureIn: Double,
    @SerialName("wind_mph") val windMph: Double,
    @SerialName("wind_kph") val windKph: Double,
    @SerialName("wind_dir") val windDirection: String,
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
    val astro: Astro,
    @SerialName("hour") val hour: List<Hour>
)

@Serializable
data class Day(
    @SerialName("maxtemp_c") val maxTempC: Double,
    @SerialName("maxtemp_f") val maxTempF: Double,
    @SerialName("mintemp_c") val minTempC: Double,
    @SerialName("mintemp_f") val minTempF: Double,
    @SerialName("daily_chance_of_rain") val dailyChanceOfRain: Double,
    val condition: Condition
)

@Serializable
data class Hour(
    @SerialName("time_epoch") val timeEpoch: Long,
    @SerialName("temp_c") val tempC: Double,
    @SerialName("temp_f") val tempF: Double,
    val condition: Condition
)

@Serializable
data class Astro(
    @SerialName("sunrise") val sunrise: String,
    @SerialName("sunset") val sunset: String,
)