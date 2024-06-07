package com.example.weatherapp.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.R
import com.example.weatherapp.model.WeatherData


@Composable
fun WeatherApp(viewModel: WeatherViewModel) {
    LaunchedEffect(key1 = LocalContext.current) {
        viewModel.getWeatherUpdate(null)
    }

    Scaffold(
        topBar = { TopBar(viewModel) },
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    if (viewModel.isSearchClicked) {
                        viewModel.unFocus()
                    }
                })
            }
    ) { innerPadding ->
        if (viewModel.hasLocationPermission || viewModel.searchFieldUsed) {
            Content(viewModel.conditions, viewModel, innerPadding)
        } else {
            ErrorContent(innerPadding)
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

        Spacer(modifier = Modifier.height(24.dp))

        DailyForecastCard(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        HourlyForecastCard(viewModel)
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
        iconDescription = "3 Day Forecast"
    ) {
        LazyColumn {
            viewModel.conditions.forecast?.forecastDay?.let {
                items(it.size) { index ->
                    val timestamp = viewModel.conditions.forecast!!.forecastDay[index].dateEpoch * 1000
                    val maxTempC = viewModel.conditions.forecast!!.forecastDay[index].day.maxTempC

                    Text(
                        text = "${viewModel.getTimeDay(timestamp)} $maxTempC°C",
                        modifier = Modifier
                            .padding(8.dp)
                    )
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
        iconDescription = "24h Forecast"
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
fun WeatherCard(
    title: String?,
    icon: Painter? = null,
    iconDescription: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
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