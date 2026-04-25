package com.example.reminderapp.utils

import java.util.Calendar

/**
 * Quiet Hours logic — the headline feature missing from Samsung's Reminder app.
 *
 * Given a window like 23 (11 PM) -> 10 (10 AM), this class determines:
 *  1. Whether a given moment falls inside the quiet window.
 *  2. The next "wake" moment (when the window ends) so we can shift
 *     a scheduled reminder to fire right after quiet hours end instead
 *     of skipping it entirely.
 *
 * The window is allowed to wrap past midnight (start > end), which is the
 * normal case for a sleep schedule.
 */
object QuietHours {

    /**
     * @param timeMillis the moment to test
     * @param startHour  hour of day (0–23) when quiet starts, e.g. 23
     * @param endHour    hour of day (0–23) when quiet ends, e.g. 10
     * @return true if [timeMillis] falls inside the quiet window
     */
    fun isInQuietHours(timeMillis: Long, startHour: Int, endHour: Int): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        return if (startHour == endHour) {
            // Edge case: zero-length window means quiet hours disabled.
            false
        } else if (startHour < endHour) {
            // Same-day window, e.g. 13 -> 17 (1 PM to 5 PM)
            hour in startHour until endHour
        } else {
            // Wrap-around window, e.g. 23 -> 10 (11 PM to 10 AM next day)
            hour >= startHour || hour < endHour
        }
    }

    /**
     * If [timeMillis] falls inside the quiet window, return the millis
     * corresponding to [endHour]:00 of the appropriate day (today or tomorrow).
     * Otherwise return [timeMillis] unchanged.
     *
     * This is used to "push" a scheduled fire-time to right after the
     * quiet window ends, so the user does not simply lose that reminder.
     */
    fun shiftOutOfQuietHours(timeMillis: Long, startHour: Int, endHour: Int): Long {
        if (!isInQuietHours(timeMillis, startHour, endHour)) return timeMillis

        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)

        // If we're past midnight but before endHour, end is later today.
        // If we're after startHour (e.g. 23 PM), end is tomorrow.
        val endIsTomorrow = if (startHour < endHour) {
            false
        } else {
            currentHour >= startHour
        }

        if (endIsTomorrow) cal.add(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, endHour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Format a 24-hour hour value into a friendly 12-hour string for display.
     */
    fun formatHour(hour: Int): String {
        val h = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val suffix = if (hour < 12) "AM" else "PM"
        return "$h:00 $suffix"
    }
}
