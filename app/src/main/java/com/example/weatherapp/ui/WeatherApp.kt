package com.example.weatherapp.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import com.example.weatherapp.network.WeatherApi
import kotlinx.coroutines.runBlocking

@Composable
fun WeatherApp() {
//    val currentTemp = remember { mutableDoubleStateOf(0.0) }

    Column {
        Button(onClick = { }) {
            Text("Get Weather")
        }
    }
}
