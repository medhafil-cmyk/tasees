package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.WeatherResponse
import com.example.data.db.SavedCity
import com.example.data.prefs.PreferencesManager
import com.example.domain.LocationTracker
import com.example.domain.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherResponse, val city: String) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class HomeViewModel(
    private val repository: WeatherRepository,
    private val locationTracker: LocationTracker,
    val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    val isFahrenheit = preferencesManager.isFahrenheitFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val backgroundImagePath = preferencesManager.backgroundImagePathFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    init {
        fetchWeatherForInitialLocation()
    }

    private fun fetchWeatherForInitialLocation() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            // First try to get current location
            val location = locationTracker.getCurrentLocation()
            if (location != null) {
                fetchWeather(location.latitude, location.longitude, "Current Location")
                // save it as current location
                repository.saveCity(
                    SavedCity(
                        id = 0, // 0 for current location
                        name = "Current Location",
                        latitude = location.latitude,
                        longitude = location.longitude,
                        country = "",
                        admin1 = "",
                        isCurrentLocation = true
                    )
                )
            } else {
                // If no location, check if we have any saved city
                val cities = repository.getSavedCities()
                cities.collect { savedList ->
                    if (savedList.isNotEmpty()) {
                        val first = savedList.first()
                        fetchWeather(first.latitude, first.longitude, first.name)
                    } else {
                        // Default to London if nothing else works
                        fetchWeather(51.5074, -0.1278, "London")
                    }
                }
            }
        }
    }

    private fun checkSevereWeather(weatherCode: Int, city: String) {
        // Severe weather codes like Thunderstorm
        if (weatherCode in listOf(95, 96, 99)) {
            val context = locationTracker.context
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            val channelId = "weather_alerts"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Weather Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Severe Weather Alert in $city")
                .setContentText("Thunderstorms expected. Please stay safe.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            with(NotificationManagerCompat.from(context)) {
                notify(1001, builder.build())
            }
        }
    }

    fun fetchWeather(lat: Double, lon: Double, cityName: String) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val response = repository.getWeather(lat, lon)
                _uiState.value = WeatherUiState.Success(response, cityName)
                response.current?.weather_code?.let { checkSevereWeather(it, cityName) }
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(e.message ?: "Failed to fetch weather")
            }
        }
    }

    class Factory(
        private val repository: WeatherRepository,
        private val locationTracker: LocationTracker,
        private val preferencesManager: PreferencesManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository, locationTracker, preferencesManager) as T
        }
    }
}
