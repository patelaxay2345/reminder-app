package com.example.reminderapp.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.reminderapp.data.ReminderDatabase
import com.example.reminderapp.notification.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SNOOZE = "com.example.reminderapp.ACTION_SNOOZE"
        const val ACTION_DONE = "com.example.reminderapp.ACTION_DONE"
        const val SNOOZE_MINUTES = 10
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(AlarmScheduler.EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        // Always dismiss the notification first.
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(reminderId.toInt())

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = ReminderDatabase.getInstance(context).reminderDao()
                val reminder = dao.getById(reminderId) ?: return@launch
                val scheduler = AlarmScheduler(context)

                when (intent.action) {
                    ACTION_SNOOZE -> {
                        val newTime = System.currentTimeMillis() + SNOOZE_MINUTES * 60_000L
                        val updated = reminder.copy(nextTriggerAtMillis = newTime)
                        dao.update(updated)
                        scheduler.schedule(updated)
                    }
                    ACTION_DONE -> {
                        if (reminder.repeatIntervalMinutes == 0) {
                            dao.setEnabled(reminder.id, false)
                            scheduler.cancel(reminder.id)
                        }
                        // For repeating reminders, the next occurrence was
                        // already scheduled by ReminderAlarmReceiver.
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
