package com.example.ui.forecast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.getWeatherDescriptionRes
import com.example.ui.components.getWeatherIcon
import com.example.ui.home.HomeViewModel
import com.example.ui.home.WeatherUiState
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFahrenheit by viewModel.isFahrenheit.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.seven_day_forecast)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is WeatherUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is WeatherUiState.Error -> {
                    Text("Error: ${state.message}", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                }
                is WeatherUiState.Success -> {
                    val daily = state.weather.daily
                    if (daily != null) {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(daily.time) { index, dateStr ->
                                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                                val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(date!!)
                                
                                val minT = daily.temperature_2m_min[index]
                                val maxT = daily.temperature_2m_max[index]
                                val code = daily.weather_code[index]
                                
                                val minDisplay = if (isFahrenheit) (minT * 9/5) + 32 else minT
                                val maxDisplay = if (isFahrenheit) (maxT * 9/5) + 32 else maxT

                                DailyForecastItem(
                                    day = dayName,
                                    description = stringResource(getWeatherDescriptionRes(code)),
                                    code = code,
                                    min = minDisplay.toInt(),
                                    max = maxDisplay.toInt(),
                                    isFahrenheit = isFahrenheit,
                                    sunrise = daily.sunrise[index],
                                    sunset = daily.sunset[index]
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyForecastItem(day: String, description: String, code: Int, min: Int, max: Int, isFahrenheit: Boolean, sunrise: String, sunset: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = day, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = description, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.8f), fontSize = 14.sp)
            Text(text = "🌅 ${sunrise.substringAfter("T")}  🌇 ${sunset.substringAfter("T")}", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f), fontSize = 12.sp, modifier = Modifier.padding(top=4.dp))
        }
        Icon(
            imageVector = getWeatherIcon(code),
            contentDescription = null,
            modifier = Modifier.size(40.dp).padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "$max${if(isFahrenheit) "°F" else "°C"}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "$min${if(isFahrenheit) "°F" else "°C"}", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.8f), fontSize = 14.sp)
        }
    }
}
