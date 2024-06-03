package com.example.weatherapp.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.MainActivity
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.network.WeatherApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WeatherViewModel(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
): ViewModel(){
    var conditions by mutableStateOf(WeatherData())
        private set
    var location = ""
    var hasLocationPermission by mutableStateOf(false)


    private suspend fun getLocation(
        fusedLocationClient: FusedLocationProviderClient
    ): Location? {
        return try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                hasLocationPermission = true
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            }
            else {
                ActivityCompat.requestPermissions(
                    context as MainActivity,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    0
                )
                // TODO onRequestPermissionsResult
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getWeatherUpdate(locationName: String? = null) {
        viewModelScope.launch {
            if (locationName != null) {
                location = locationName
            } else {
                val currentLocation = getLocation(fusedLocationClient)

                if (currentLocation != null) {
                    location = "${currentLocation.latitude},${currentLocation.longitude}"
                }
            }

            val newConditions = getWeather(location)

            if (newConditions != null) {
                conditions = newConditions
            }
        }
    }

    private suspend fun getWeather(location: String): WeatherData? {
        return try {
            WeatherApi.retrofitService.getWeather(location)
        } catch (e: Exception) {
            null
        }
    }
}