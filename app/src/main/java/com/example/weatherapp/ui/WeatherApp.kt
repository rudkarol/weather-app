package com.example.weatherapp.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.weatherapp.R
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.network.WeatherApi
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.launch
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

@Composable
fun WeatherApp(viewModel: WeatherViewModel) {


    LaunchedEffect(key1 = LocalContext.current) {
        viewModel.getWeatherUpdate(null)
    }

    Scaffold(
        topBar = { TopBar(viewModel.conditions) { viewModel.getWeatherUpdate(it) } }
    ) { innerPadding ->
        if (viewModel.hasLocationPermission) {
            Content(viewModel.conditions, innerPadding) { viewModel.getWeatherUpdate(it) }
//            TODO show content on search field use (if no location permission is given)
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

@OptIn(ExperimentalMaterial3Api::class)
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
