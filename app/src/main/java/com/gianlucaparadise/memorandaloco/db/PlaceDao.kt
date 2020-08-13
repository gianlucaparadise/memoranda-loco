package com.gianlucaparadise.memorandaloco.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.gianlucaparadise.memorandaloco.vo.Place

@Dao
interface PlaceDao {

    @Query("SELECT * FROM Place")
    suspend fun getAll(): List<Place>

    @Query("SELECT * FROM Place WHERE type IN (:type)")
    suspend fun loadAllByType(type: Place.Type): List<Place>

    @Insert
    suspend fun insertAll(vararg places: Place)

    @Delete
    suspend fun delete(item: Place)
}