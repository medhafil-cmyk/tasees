package com.example.domain

import com.example.data.api.GeocodingApi
import com.example.data.api.LocationResult
import com.example.data.api.WeatherApi
import com.example.data.api.WeatherResponse
import com.example.data.db.CityDao
import com.example.data.db.SavedCity
import kotlinx.coroutines.flow.Flow

class WeatherRepository(
    private val weatherApi: WeatherApi,
    private val geocodingApi: GeocodingApi,
    private val cityDao: CityDao
) {
    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return weatherApi.getWeather(lat, lon)
    }

    suspend fun searchCity(name: String): List<LocationResult> {
        val response = geocodingApi.searchCity(name)
        return response.results ?: emptyList()
    }

    fun getSavedCities(): Flow<List<SavedCity>> = cityDao.getAllCities()

    suspend fun saveCity(city: SavedCity) {
        cityDao.insertCity(city)
    }

    suspend fun deleteCity(id: Long) {
        cityDao.deleteCityById(id)
    }
}
