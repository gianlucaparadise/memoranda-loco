package com.gianlucaparadise.memorandaloco.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.gianlucaparadise.memorandaloco.vo.Reminder

@Dao
interface ReminderDao {

    @Query("SELECT * FROM Reminder")
    fun getAll(): List<Reminder>

    @Query("SELECT * FROM Reminder WHERE uid IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<Reminder>

    @Insert
    fun insertAll(vararg reminders: Reminder)

    @Delete
    fun delete(item: Reminder)
}