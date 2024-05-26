package com.example.weatherapp.ui

import android.annotation.SuppressLint
import android.widget.Space
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.network.WeatherApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WeatherApp() {
    var conditions by remember { mutableStateOf(WeatherData()) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopBar(conditions) }
    ) {
        Content(conditions) { location ->
            coroutineScope.launch {
                val newConditions = getWeather(location)

                if (newConditions != null) {
                    conditions = newConditions
                }
            }
        }
    }
}

@Composable
fun Content(conditions: WeatherData, onGetWeather: (String) -> Unit) {
    Column {
        Spacer(Modifier.size(64.dp))
        Button(
            onClick = {
                onGetWeather("Warsaw")
            }
        ) {
            Text("Get Weather")
        }
        Text(text = "Location: ${conditions.location?.name ?: "Unknown"}")
        Text(text = "Temperature: ${conditions.current?.tempC ?: "N/A"}")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(conditions: WeatherData) {
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
            IconButton(onClick = { /* TODO */ }) {
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