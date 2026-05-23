package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Query("SELECT * FROM saved_cities ORDER BY name ASC")
    fun getAllCities(): Flow<List<SavedCity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: SavedCity)

    @Query("DELETE FROM saved_cities WHERE id = :id")
    suspend fun deleteCityById(id: Long)

    @Query("SELECT * FROM saved_cities LIMIT 1")
    suspend fun getFirstCity(): SavedCity?
}
