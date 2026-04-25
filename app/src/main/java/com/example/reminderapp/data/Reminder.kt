package com.example.reminderapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Reminder entity stored in Room database.
 *
 * Supports:
 *  - One-time reminders (repeatIntervalMinutes = 0)
 *  - Repeating reminders (e.g., every 60 minutes for water intake)
 *  - Quiet hours window: notifications are suppressed between
 *    quietStartHour and quietEndHour (e.g., 23 -> 10 means
 *    "do not fire between 11 PM and 10 AM").
 */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",

    /** Epoch millis of the next time this reminder should fire. */
    val nextTriggerAtMillis: Long,

    /** 0 = one-time. > 0 means repeat every N minutes. */
    val repeatIntervalMinutes: Int = 0,

    /** Whether the reminder is active (toggle on the list screen). */
    val isEnabled: Boolean = true,

    /** Whether quiet hours apply to this specific reminder. */
    val quietHoursEnabled: Boolean = false,

    /** Hour of day (0-23) when quiet period starts. Default 23 = 11 PM. */
    val quietStartHour: Int = 23,

    /** Hour of day (0-23) when quiet period ends. Default 10 = 10 AM. */
    val quietEndHour: Int = 10,

    /** Optional category like "Health", "Work", "Personal". */
    val category: String = "General",

    /** Priority level: 0 = Low, 1 = Normal, 2 = High. */
    val priority: Int = 1,

    val createdAt: Long = System.currentTimeMillis()
)
