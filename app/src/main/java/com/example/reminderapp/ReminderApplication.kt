package com.example.reminderapp

import android.app.Application
import com.example.reminderapp.notification.NotificationHelper

class ReminderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
    }
}
