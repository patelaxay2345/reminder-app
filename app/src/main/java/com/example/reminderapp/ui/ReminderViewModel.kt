package com.example.reminderapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.reminderapp.data.Reminder
import com.example.reminderapp.data.ReminderRepository
import kotlinx.coroutines.launch

class ReminderViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ReminderRepository(app)
    val reminders = repo.observeAll()

    fun upsert(reminder: Reminder) = viewModelScope.launch { repo.upsert(reminder) }
    fun delete(reminder: Reminder) = viewModelScope.launch { repo.delete(reminder) }
    fun toggleEnabled(reminder: Reminder, enabled: Boolean) =
        viewModelScope.launch { repo.toggleEnabled(reminder, enabled) }
}
