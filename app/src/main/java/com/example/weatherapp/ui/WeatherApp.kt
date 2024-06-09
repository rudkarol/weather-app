package com.example.weatherapp.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.R
import com.example.weatherapp.model.WeatherData


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun WeatherApp(viewModel: WeatherViewModel) {
    Scaffold(
        topBar = { TopBar(viewModel) },
        snackbarHost = { SnackbarHost(hostState = viewModel.snackbarHostState) },
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    if (viewModel.isSearchClicked) {
                        viewModel.unFocus()
                    }
                })
            }
    ) { innerPadding ->
        if (!viewModel.initialRun) {
            Content(viewModel.conditions, viewModel, innerPadding)
        }
        else if (!viewModel.networkState) {
            ErrorContent(innerPadding, 1, viewModel)
        }
        else if (!viewModel.hasLocationPermission) {
            ErrorContent(innerPadding, 0, viewModel)
        }
        else {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun Content(conditions: WeatherData, viewModel: WeatherViewModel, innerPadding: PaddingValues) {
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

        Text (
            text = conditions.current?.condition?.text ?: "",
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        DailyForecastCard(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        HourlyForecastCard(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            InfoCardLeft(viewModel, Modifier.weight(1f))
            InfoCardRight(viewModel, Modifier.weight(1f))
        }
    }

    if (!viewModel.initialRun && viewModel.showSnackbar) {
        viewModel.showSnackbar()
    }
}

@Composable
fun ErrorContent(innerPadding: PaddingValues, errorCode: Int, viewModel: WeatherViewModel) {
    val icon: Int
    val message: String

    if (errorCode == 0) {
//        0 = permission error
        icon = R.drawable.location_off_24dp_fill0_wght400_grad0_opsz24
        message = "Enable location services or enter a location"
    } else {
//        1 = connection error
        icon = R.drawable.signal_disconnected_24dp_fill0_wght400_grad0_opsz24
        message = "Connection error - check your internet connection"

        if ((!viewModel.initialRun || viewModel.searchFieldUsed) && viewModel.showSnackbar) {
            viewModel.showSnackbar()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = message,
            modifier = Modifier
                .size(128.dp)
                .padding(bottom = 16.dp)
        )
        Text(message)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(viewModel: WeatherViewModel) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    CenterAlignedTopAppBar(
        title = {
            if (viewModel.isSearchClicked) {
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    singleLine = true,
                    onValueChange = { viewModel.searchQuery = it },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                        capitalization = KeyboardCapitalization.Words
                    ),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.getWeatherUpdate(viewModel.searchQuery)
                        viewModel.searchFieldUsed = true
                        viewModel.unFocus()
                    }),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .focusRequester(viewModel.focusRequester),
                )

                LaunchedEffect(Unit) {
                    viewModel.focusRequester.requestFocus()
                }
            } else {
                Text(
                    text = viewModel.conditions.location?.name ?: "Weather App",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    if (viewModel.isSearchClicked) {
                        viewModel.unFocus()
                    }

                    viewModel.getWeatherUpdate()
                })
            {
                Icon(
                    painter = painterResource(id = R.drawable.my_location_24dp_fill0_wght400_grad0_opsz24),
                    contentDescription = "Get current location weather"
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    viewModel.isSearchClicked = !viewModel.isSearchClicked
                }) {
                Icon(
                    painter = painterResource(id = R.drawable.search_24dp_fill0_wght400_grad0_opsz24),
                    contentDescription = "Search for a location"
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun DailyForecastCard(viewModel: WeatherViewModel) {
    WeatherCard(
        title = "3 Day Forecast",
        icon = painterResource(id = R.drawable.calendar_today_24dp_fill0_wght400_grad0_opsz24),
        iconDescription = "3 Day Forecast",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        LazyColumn {
            viewModel.conditions.forecast?.forecastDay?.let {
                items(it.size) { index ->
                    val timestamp = viewModel.conditions.forecast!!.forecastDay[index].dateEpoch * 1000
                    val maxTempC = viewModel.conditions.forecast!!.forecastDay[index].day.maxTempC
                    val minTemp = viewModel.conditions.forecast!!.forecastDay[index].day.minTempC


                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = viewModel.getTimeDay(timestamp))
                        Text(text = viewModel.conditions.forecast!!.forecastDay[index].day.condition.text)
                        Text(text = "$maxTempC°C/$minTemp°C")
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyForecastCard(viewModel: WeatherViewModel) {
    WeatherCard(
        title = "24h Forecast",
        icon = painterResource(id = R.drawable.schedule_24dp_fill0_wght400_grad0_opsz24),
        iconDescription = "24h Forecast",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        LazyRow {
            viewModel.conditions.forecast?.forecastDay?.get(0)?.hour.let {
                if (it != null) {
                    items(it.size) { index ->
                        val timestamp = viewModel.conditions.forecast?.forecastDay?.get(0)?.hour?.get(index)?.timeEpoch?.times(
                            1000
                        )
                        val tempC = viewModel.conditions.forecast?.forecastDay?.get(0)?.hour?.get(index)?.tempC

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = viewModel.getTimeHour(timestamp!!),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Text(
                                text = "$tempC°C",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCardLeft(viewModel: WeatherViewModel, modifier: Modifier = Modifier) {
    WeatherCard(
        modifier = modifier
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Humidity: ")
                Text(text = "${viewModel.conditions.current?.humidity?.toInt()}%")
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Feels like: ")
                Text(text = "${viewModel.conditions.current?.feelsLikeC}\u00B0C")
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Pressure: ")
                Text(text = "${viewModel.conditions.current?.pressureMb?.toInt()} mb")
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "UV Index: ")
                Text(text = "${viewModel.conditions.current?.uv?.toInt()}")
            }
        }
    }
}

@Composable
fun InfoCardRight(viewModel: WeatherViewModel, modifier: Modifier = Modifier) {
    WeatherCard(
        modifier = modifier
            .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Wind: ")
                Text(text = "${viewModel.conditions.current?.windKph} km/h")
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Direction: ")
                Text(text = "${viewModel.conditions.current?.windDirection}")
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Sunrise: ")
                Text(text = "${viewModel.conditions.forecast?.forecastDay?.get(0)?.astro?.sunrise}")
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Sunset: ")
                Text(text = "${viewModel.conditions.forecast?.forecastDay?.get(0)?.astro?.sunset}")
            }
        }
    }
}

@Composable
fun WeatherCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: Painter? = null,
    iconDescription: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = modifier
    ) {
        Column {
            if (title != null) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                ) {
                    if (icon != null && iconDescription != null) {
                        Icon(painter = icon, contentDescription = iconDescription)
                    }

                    Text(
                        text = title,
                        modifier = Modifier
                            .padding(start = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            content()
        }
        
    }
}