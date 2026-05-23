package com.example.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    private val IS_FAHRENHEIT = booleanPreferencesKey("is_fahrenheit")
    private val BACKGROUND_IMAGE_PATH = stringPreferencesKey("background_image_path")

    val isFahrenheitFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_FAHRENHEIT] ?: false
    }

    suspend fun setFahrenheit(isFahrenheit: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_FAHRENHEIT] = isFahrenheit
        }
    }

    val backgroundImagePathFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[BACKGROUND_IMAGE_PATH]
    }

    suspend fun setBackgroundImagePath(path: String?) {
        context.dataStore.edit { preferences ->
            if (path == null) {
                preferences.remove(BACKGROUND_IMAGE_PATH)
            } else {
                preferences[BACKGROUND_IMAGE_PATH] = path
            }
        }
    }
}
