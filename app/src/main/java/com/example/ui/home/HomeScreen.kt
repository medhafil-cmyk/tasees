package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.getWeatherDescriptionRes
import com.example.ui.components.getWeatherIcon
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    MainScreen(viewModel, modifier)
}

@Composable
fun MainScreen(viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFahrenheit by viewModel.isFahrenheit.collectAsStateWithLifecycle()
    val backgroundImagePath by viewModel.backgroundImagePath.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is WeatherUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is WeatherUiState.Error -> {
                Text("Error: ${state.message}", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
            }
            is WeatherUiState.Success -> {
                val current = state.weather.current
                val hourly = state.weather.hourly
                val isWarm = current != null && current.temperature_2m > 20.0
                
                BackgroundImageManager(backgroundImagePath = backgroundImagePath, isWarm = isWarm)
                WeatherContent(state = state, isFahrenheit = isFahrenheit)
            }
        }
    }
}

@Composable
fun BackgroundImageManager(backgroundImagePath: String?, isWarm: Boolean) {
    // Warm gradient for hot weather, cool gradient for cold
    val backgroundBrush = if (isWarm) {
        Brush.linearGradient(
            colors = listOf(Color(0xFFFF9800), Color(0xFFFF5722))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF03A9F4), Color(0xFF3F51B5))
        )
    }

    val imageFile = backgroundImagePath?.let { File(it) }
    val imageExists = imageFile?.exists() == true

    if (imageExists) {
        AsyncImage(
            model = imageFile,
            contentDescription = "Custom Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Dark overlay to maintain readability
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
    } else {
        // Fallback to gradient
        Box(modifier = Modifier.fillMaxSize().background(backgroundBrush))
    }
}

@Composable
fun WeatherContent(state: WeatherUiState.Success, isFahrenheit: Boolean, modifier: Modifier = Modifier) {
    val current = state.weather.current
    val hourly = state.weather.hourly

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(48.dp))
                    Text(
                        text = state.city,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (current != null) {
                        val temp = if (isFahrenheit) current.temperature_2m * 9/5 + 32 else current.temperature_2m
                        val unit = if (isFahrenheit) "°F" else "°C"
                        val feelsLike = if (isFahrenheit) current.apparent_temperature * 9/5 + 32 else current.apparent_temperature

                        Icon(
                            imageVector = getWeatherIcon(current.weather_code),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(120.dp)
                        )
                        Text(
                            text = "${temp.toInt()}$unit",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(getWeatherDescriptionRes(current.weather_code)),
                            fontSize = 24.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.feels_like, "${feelsLike.toInt()}$unit"),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            WeatherDetail(stringResource(R.string.wind), "${current.wind_speed_10m} km/h")
                            WeatherDetail(stringResource(R.string.humidity), "${current.relative_humidity_2m}%")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    if (hourly != null) {
                        Text(
                            text = stringResource(R.string.today),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val nowHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date()).toInt()
                            itemsIndexed(hourly.temperature_2m.take(24)) { index, tempRaw ->
                                val timeLabel = hourly.time[index].substringAfter("T")
                                val hTemp = if (isFahrenheit) tempRaw * 9/5 + 32 else tempRaw
                                val hCode = hourly.weather_code[index]
                                HourlyItem(timeLabel, hTemp.toInt(), isFahrenheit, hCode)
                            }
                        }
                    }
                }
}

@Composable
fun WeatherDetail(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun HourlyItem(time: String, temp: Int, isFahrenheit: Boolean, code: Int) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = time, color = Color.White, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            imageVector = getWeatherIcon(code),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$temp${if (isFahrenheit) "°F" else "°C"}",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
