package com.example.reminderapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.reminderapp.data.Reminder
import com.example.reminderapp.receiver.ReminderAlarmReceiver
import com.example.reminderapp.utils.QuietHours

/**
 * Schedules reminders with Android's AlarmManager.
 *
 * Key behaviour: before scheduling, we check the configured quiet-hours
 * window. If the next fire time would fall inside it, we shift the fire
 * time to the moment quiet hours end. This is what makes the hourly
 * water-intake reminder skip 11 PM – 10 AM cleanly.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: Reminder) {
        if (!reminder.isEnabled) return

        // Apply quiet hours shift if the user enabled them on this reminder.
        val effectiveTime = if (reminder.quietHoursEnabled) {
            QuietHours.shiftOutOfQuietHours(
                reminder.nextTriggerAtMillis,
                reminder.quietStartHour,
                reminder.quietEndHour
            )
        } else {
            reminder.nextTriggerAtMillis
        }

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use exact alarms for accurate reminder timing. On Android 12+
        // the SCHEDULE_EXACT_ALARM permission is required; we fall back
        // to inexact scheduling if the system rejects it.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, effectiveTime, pendingIntent
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, effectiveTime, pendingIntent
                )
            }
        } catch (se: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, effectiveTime, pendingIntent
            )
        }
    }

    fun cancel(reminderId: Long) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * For repeating reminders, compute the next trigger time after
     * [lastFireMillis] using [intervalMinutes], then push past quiet
     * hours if needed.
     */
    fun computeNextTrigger(
        lastFireMillis: Long,
        intervalMinutes: Int,
        quietHoursEnabled: Boolean,
        quietStartHour: Int,
        quietEndHour: Int
    ): Long {
        val raw = lastFireMillis + intervalMinutes * 60_000L
        return if (quietHoursEnabled) {
            QuietHours.shiftOutOfQuietHours(raw, quietStartHour, quietEndHour)
        } else raw
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
    }
}
