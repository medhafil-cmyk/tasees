package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val current: CurrentWeather?,
    val hourly: HourlyWeather?,
    val daily: DailyWeather?
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    val time: String,
    val temperature_2m: Double,
    val relative_humidity_2m: Int,
    val apparent_temperature: Double,
    val weather_code: Int,
    val wind_speed_10m: Double
)

@JsonClass(generateAdapter = true)
data class HourlyWeather(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val weather_code: List<Int>
)

@JsonClass(generateAdapter = true)
data class DailyWeather(
    val time: List<String>,
    val weather_code: List<Int>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val sunrise: List<String>,
    val sunset: List<String>
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<LocationResult>?
)

@JsonClass(generateAdapter = true)
data class LocationResult(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val admin1: String?
)
