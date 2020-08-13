package com.gianlucaparadise.memorandaloco.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gianlucaparadise.memorandaloco.vo.Place
import com.gianlucaparadise.memorandaloco.vo.Reminder

@Database(entities = [Reminder::class, Place::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

    abstract fun placeDao(): PlaceDao

}