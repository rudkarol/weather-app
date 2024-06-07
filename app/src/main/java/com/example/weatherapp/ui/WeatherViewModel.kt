package com.example.weatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.network.WeatherApi
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date


class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    var conditions by mutableStateOf(WeatherData())
        private set
    private var location = ""
    var hasLocationPermission by mutableStateOf(false)
    private val _requestLocationPermission = MutableLiveData<Boolean>()
    val requestLocationPermission: LiveData<Boolean> get() = _requestLocationPermission

    val focusRequester = FocusRequester()

    var searchFieldUsed by mutableStateOf(false)
    var isSearchClicked by mutableStateOf(false)
    var searchQuery by mutableStateOf("")

    private suspend fun getLocation(): Location? {
        return try {
            if (ActivityCompat.checkSelfPermission(
                    getApplication<Application>().applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                hasLocationPermission = true
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            } else {
                _requestLocationPermission.postValue(true)
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
                val currentLocation = getLocation()

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

    fun unFocus() {
        focusRequester.freeFocus()
        isSearchClicked = false
        searchQuery = ""
    }

    @SuppressLint("SimpleDateFormat")
    fun getTimeHour(timestamp: Long): String {
        val date = Date(timestamp)

        return SimpleDateFormat("H:mm").format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun getTimeDay(timestamp: Long): String {
        val date = Date(timestamp)

        return SimpleDateFormat("EEE").format(date)
    }
}
