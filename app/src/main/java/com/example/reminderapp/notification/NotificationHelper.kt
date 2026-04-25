package com.example.reminderapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.reminderapp.R
import com.example.reminderapp.data.Reminder
import com.example.reminderapp.receiver.NotificationActionReceiver
import com.example.reminderapp.ui.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "reminders_channel"
    private const val CHANNEL_NAME = "Reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for your scheduled reminders"
                    enableVibration(true)
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    fun showReminderNotification(context: Context, reminder: Reminder) {
        ensureChannel(context)

        // Tap notification -> open the app
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context, reminder.id.toInt(), tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action (10 minutes)
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(AlarmScheduler.EXTRA_REMINDER_ID, reminder.id)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context, (reminder.id * 10 + 1).toInt(), snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark Done action
        val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_DONE
            putExtra(AlarmScheduler.EXTRA_REMINDER_ID, reminder.id)
        }
        val donePending = PendingIntent.getBroadcast(
            context, (reminder.id * 10 + 2).toInt(), doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priority = when (reminder.priority) {
            2 -> NotificationCompat.PRIORITY_HIGH
            0 -> NotificationCompat.PRIORITY_LOW
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(reminder.title)
            .setContentText(reminder.description.ifBlank { "Reminder" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminder.description))
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .setPriority(priority)
            .addAction(0, "Snooze 10 min", snoozePending)
            .addAction(0, "Done", donePending)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(reminder.id.toInt(), builder.build())
    }
}
