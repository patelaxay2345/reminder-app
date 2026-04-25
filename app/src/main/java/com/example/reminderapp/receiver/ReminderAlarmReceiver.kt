package com.example.reminderapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.reminderapp.data.ReminderDatabase
import com.example.reminderapp.notification.AlarmScheduler
import com.example.reminderapp.notification.NotificationHelper
import com.example.reminderapp.utils.QuietHours
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Triggered by AlarmManager when a reminder is due.
 *
 * Responsibilities:
 *  1. Look up the reminder.
 *  2. Re-check quiet hours (defense in depth — the user might have edited
 *     the window after the alarm was scheduled).
 *  3. Show the notification.
 *  4. If the reminder repeats, schedule the next occurrence.
 */
class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(AlarmScheduler.EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        // Use goAsync because Room calls are suspending.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = ReminderDatabase.getInstance(context)
                val dao = db.reminderDao()
                val reminder = dao.getById(reminderId) ?: return@launch
                if (!reminder.isEnabled) return@launch

                val now = System.currentTimeMillis()
                val scheduler = AlarmScheduler(context)

                val inQuietHours = reminder.quietHoursEnabled &&
                    QuietHours.isInQuietHours(
                        now,
                        reminder.quietStartHour,
                        reminder.quietEndHour
                    )

                if (!inQuietHours) {
                    // Show the notification on the main thread context.
                    NotificationHelper.showReminderNotification(context, reminder)
                }

                // For repeating reminders, schedule the next occurrence.
                if (reminder.repeatIntervalMinutes > 0) {
                    val nextTime = scheduler.computeNextTrigger(
                        lastFireMillis = now,
                        intervalMinutes = reminder.repeatIntervalMinutes,
                        quietHoursEnabled = reminder.quietHoursEnabled,
                        quietStartHour = reminder.quietStartHour,
                        quietEndHour = reminder.quietEndHour
                    )
                    dao.updateNextTrigger(reminder.id, nextTime)
                    scheduler.schedule(reminder.copy(nextTriggerAtMillis = nextTime))
                } else {
                    // One-time reminder — disable after firing.
                    dao.setEnabled(reminder.id, false)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
