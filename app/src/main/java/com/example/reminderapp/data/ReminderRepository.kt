package com.example.reminderapp.data

import android.content.Context
import com.example.reminderapp.notification.AlarmScheduler

class ReminderRepository(context: Context) {

    private val dao = ReminderDatabase.getInstance(context).reminderDao()
    private val scheduler = AlarmScheduler(context)

    fun observeAll() = dao.observeAll()

    suspend fun upsert(reminder: Reminder): Reminder {
        val id = if (reminder.id == 0L) dao.insert(reminder) else {
            dao.update(reminder); reminder.id
        }
        val saved = reminder.copy(id = id)
        scheduler.cancel(id)
        if (saved.isEnabled) scheduler.schedule(saved)
        return saved
    }

    suspend fun delete(reminder: Reminder) {
        scheduler.cancel(reminder.id)
        dao.delete(reminder)
    }

    suspend fun toggleEnabled(reminder: Reminder, enabled: Boolean) {
        dao.setEnabled(reminder.id, enabled)
        if (enabled) scheduler.schedule(reminder.copy(isEnabled = true))
        else scheduler.cancel(reminder.id)
    }
}
