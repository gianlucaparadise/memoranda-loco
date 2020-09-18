package com.gianlucaparadise.memoloco.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.gianlucaparadise.memoloco.vo.Reminder

@Dao
interface ReminderDao {

    @Query("SELECT * FROM Reminder")
    suspend fun getAll(): List<Reminder>

    @Query("SELECT * FROM Reminder WHERE uid IN (:ids)")
    suspend fun loadAllByIds(ids: IntArray): List<Reminder>

    @Insert
    suspend fun insertAll(vararg reminders: Reminder)

    @Delete
    suspend fun delete(item: Reminder)
}