package com.example.reminderapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.reminderapp.data.ReminderDatabase
import com.example.reminderapp.notification.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reschedules all enabled reminders after the device boots.
 * Without this, AlarmManager would forget every alarm on reboot.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = ReminderDatabase.getInstance(context).reminderDao()
                val scheduler = AlarmScheduler(context)
                val now = System.currentTimeMillis()

                dao.getAllEnabled().forEach { reminder ->
                    // If the saved next-trigger is in the past, advance it
                    // to the next valid slot (handles long power-offs).
                    val updated = if (reminder.nextTriggerAtMillis < now &&
                        reminder.repeatIntervalMinutes > 0
                    ) {
                        val intervalMs = reminder.repeatIntervalMinutes * 60_000L
                        val skipped = ((now - reminder.nextTriggerAtMillis) / intervalMs) + 1
                        reminder.copy(
                            nextTriggerAtMillis = reminder.nextTriggerAtMillis + skipped * intervalMs
                        )
                    } else reminder

                    dao.update(updated)
                    scheduler.schedule(updated)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
