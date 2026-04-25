package com.example.reminderapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY nextTriggerAtMillis ASC")
    fun observeAll(): LiveData<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    suspend fun getAllEnabled(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): Reminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("UPDATE reminders SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE reminders SET nextTriggerAtMillis = :nextTime WHERE id = :id")
    suspend fun updateNextTrigger(id: Long, nextTime: Long)
}
