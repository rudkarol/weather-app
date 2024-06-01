package com.example.weatherapp.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.network.WeatherApi
import kotlinx.coroutines.launch

@Composable
fun WeatherApp() {
    var conditions by remember { mutableStateOf(WeatherData()) }
    val coroutineScope = rememberCoroutineScope()

    fun getWeatherUpdate(location: String) {
        coroutineScope.launch {
            val newConditions = getWeather(location)
            if (newConditions != null) {
                conditions = newConditions
            }
        }
    }

    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted

        if (isGranted) {
            getWeatherUpdate("Krakow")  // Replace with actual location
        }
    }

    LaunchedEffect(key1 = context) {
        val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true
            getWeatherUpdate("Opole")  // Replace with actual location
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    if (hasLocationPermission) {
        Scaffold(
            topBar = { TopBar(conditions) { getWeatherUpdate(it) } }
        ) { innerPadding ->
            Content(conditions, innerPadding) { getWeatherUpdate(it) }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("App requires location permission to function")
        }
    }
}

@Composable
fun Content(conditions: WeatherData, innerPadding: PaddingValues, onGetWeather: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        Text(
            text = "${conditions.current?.tempC ?: 0.0}\u00B0C",
            fontSize = 64.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(conditions: WeatherData, onGetWeather: (String) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    CenterAlignedTopAppBar(
        title = {
            Text(
                conditions.location?.name ?: "Weather App",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = { onGetWeather("Warsaw") }) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Get current location weather"
                )
            }
        },
        actions = {
            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search for a location"
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

suspend fun getWeather(location: String): WeatherData? {
    return try {
        WeatherApi.retrofitService.getWeather(location)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}