package com.example.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

fun getWeatherIcon(weatherCode: Int): ImageVector {
    return when (weatherCode) {
        0 -> Icons.Filled.WbSunny
        1, 2 -> Icons.Filled.WbCloudy
        3 -> Icons.Filled.Cloud
        45, 48 -> Icons.Filled.FilterDrama
        51, 53, 55, 56, 57 -> Icons.Filled.Air
        61, 63, 65, 66, 67, 80, 81, 82 -> Icons.Filled.WaterDrop
        71, 73, 75, 77, 85, 86 -> Icons.Filled.AcUnit
        95, 96, 99 -> Icons.Filled.Thunderstorm
        else -> Icons.Filled.CloudQueue
    }
}

@StringRes
fun getWeatherDescriptionRes(weatherCode: Int): Int {
    return when (weatherCode) {
        0 -> R.string.desc_clear_sky
        1 -> R.string.desc_mainly_clear
        2 -> R.string.desc_partly_cloudy
        3 -> R.string.desc_overcast
        45, 48 -> R.string.desc_fog
        51, 53, 55 -> R.string.desc_drizzle
        56, 57 -> R.string.desc_freezing_drizzle
        61, 63 -> R.string.desc_rain
        65 -> R.string.desc_heavy_rain
        66, 67 -> R.string.desc_freezing_rain
        71, 73, 75 -> R.string.desc_snow_fall
        77 -> R.string.desc_snow_grains
        80, 81, 82 -> R.string.desc_rain_showers
        85, 86 -> R.string.desc_snow_showers
        95 -> R.string.desc_thunderstorm
        96, 99 -> R.string.desc_thunderstorm_hail
        else -> R.string.desc_unknown
    }
}
