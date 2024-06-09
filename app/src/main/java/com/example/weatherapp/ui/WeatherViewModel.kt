package com.example.weatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
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

    var showSnackbar by mutableStateOf(false)
    private var snackbarText by mutableStateOf("")
    val snackbarHostState =  SnackbarHostState()
    var initialRun by mutableStateOf(true)
    var networkState by mutableStateOf(false)


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
            networkState = checkNetworkState()

            if (locationName != null) {
                location = locationName
            } else {
                val currentLocation = getLocation()

                if (currentLocation != null) {
                    location = "${currentLocation.latitude},${currentLocation.longitude}"
                }
            }

            val response = getWeather(location)

            if (response != null) {
                conditions = response
            }
        }
    }

    private suspend fun getWeather(location: String): WeatherData? {
        return try {
            val response = WeatherApi.retrofitService.getWeather(location)
            initialRun = false
            showSnackbar = false

            response
        }
        catch (e: retrofit2.HttpException) {

            snackbarText = if (e.code() == 400) {
                "No matching location found"
            } else {
                "An error occurred"
            }

            showSnackbar = true
            null
        }
        catch (e: java.net.UnknownHostException) {
            snackbarText = "Connection error - check your internet connection"
            showSnackbar = true
            null
        }
        catch (e: Exception) {
            snackbarText = "An error occurred"
            showSnackbar = true
            null
        }
    }

    private fun checkNetworkState(): Boolean {
        val connectivityManager = getApplication<Application>().applicationContext.getSystemService(
            Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun showSnackbar() {
        viewModelScope.launch {
            snackbarHostState.showSnackbar(message = snackbarText, duration = SnackbarDuration.Short)
            showSnackbar = false
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
