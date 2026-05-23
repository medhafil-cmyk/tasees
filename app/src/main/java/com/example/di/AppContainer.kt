package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.api.RetrofitClient
import com.example.data.db.AppDatabase
import com.example.data.prefs.PreferencesManager
import com.example.domain.WeatherRepository

class AppContainer(private val context: Context) {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "weather_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(context)
    }

    val weatherRepository: WeatherRepository by lazy {
        WeatherRepository(
            weatherApi = RetrofitClient.weatherApi,
            geocodingApi = RetrofitClient.geocodingApi,
            cityDao = database.cityDao()
        )
    }
}
