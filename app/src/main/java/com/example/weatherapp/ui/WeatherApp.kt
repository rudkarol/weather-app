package com.example.weatherapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Space
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherapp.R
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.network.WeatherApi
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.launch
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

@Composable
fun WeatherApp(fusedLocationClient: FusedLocationProviderClient) {
    var conditions by remember { mutableStateOf(WeatherData()) }
    val coroutineScope = rememberCoroutineScope()

    fun getWeatherUpdate(locationName: String? = null) {
        var location = ""

        coroutineScope.launch {
            if (locationName != null) {
                location = locationName
            } else {
                val lastKnownLocation = getLocation(fusedLocationClient)

                if (lastKnownLocation != null) {
                    location = "${lastKnownLocation.latitude},${lastKnownLocation.longitude}"
                }
            }

            Log.d("WeatherApp location", "Getting weather for $location")

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
            getWeatherUpdate()
        }
    }

    LaunchedEffect(key1 = context) {
        val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true

            getWeatherUpdate()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    Scaffold(
        topBar = { TopBar(conditions) { getWeatherUpdate(it) } }
    ) { innerPadding ->
        if (hasLocationPermission) {
            Content(conditions, innerPadding) { getWeatherUpdate(it) }
        } else {
            ErrorContent(innerPadding)
        }
    }


}

@Composable
fun Content(conditions: WeatherData, innerPadding: PaddingValues, onGetWeather: (String?) -> Unit) {
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

@Composable
fun ErrorContent(innerPadding: PaddingValues) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.location_off_24dp_fill0_wght400_grad0_opsz24),
            contentDescription = "Location permission required",
            modifier = Modifier
                .size(128.dp)
                .padding(bottom = 16.dp)
        )
        Text("App requires location permission to function")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TopBar(conditions: WeatherData, onGetWeather: (String?) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val keyboardController = LocalSoftwareKeyboardController.current

    var isSearchClicked by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    CenterAlignedTopAppBar(
        title = {
            if (isSearchClicked) {
                OutlinedTextField(
                    value = searchQuery,
                    singleLine = true,
                    onValueChange = { searchQuery = it },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        onGetWeather(searchQuery)
                        isSearchClicked = false
                        searchQuery = ""
                        keyboardController?.hide()
                    }),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                )
            } else {
                Text(
                    text = conditions.location?.name ?: "Weather App",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { onGetWeather(null) })
            {
                Icon(
                    painter = painterResource(id = R.drawable.my_location_24dp_fill0_wght400_grad0_opsz24),
                    contentDescription = "Get current location weather"
                )
            }
        },
        actions = {
            IconButton(onClick = { isSearchClicked = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.search_24dp_fill0_wght400_grad0_opsz24),
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
        null
    }
}

suspend fun getLocation(fusedLocationClient: FusedLocationProviderClient): Location? {
    return try {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
    } catch (e: Exception) {
        null
    }
}
