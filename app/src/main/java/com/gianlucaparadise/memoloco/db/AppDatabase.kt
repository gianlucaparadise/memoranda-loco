package com.gianlucaparadise.memoloco.db

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gianlucaparadise.memoloco.vo.LocationDescriptor
import com.gianlucaparadise.memoloco.vo.Place
import com.gianlucaparadise.memoloco.vo.Reminder

@Database(entities = [Reminder::class, Place::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    private val tag = "AppDatabase"

    abstract fun reminderDao(): ReminderDao

    abstract fun placeDao(): PlaceDao

    suspend fun getHome(): Place? {
        val home = placeDao().loadAllByType(Place.Type.Home)
        return home.firstOrNull()
    }

    /**
     * This will delete the saved home place if there's one
     */
    suspend fun deleteHomeIfPresent() {
        val lastSavedHome = getHome() ?: return

        Log.d(tag, "deleteHome: Removing old home place")
        placeDao().delete(lastSavedHome)
    }

    suspend fun saveHome(location: LocationDescriptor, label: String) {
        deleteHomeIfPresent()

        val homePlace = Place(name = label, type = Place.Type.Home, location = location)
        placeDao().insertAll(homePlace)
    }
}