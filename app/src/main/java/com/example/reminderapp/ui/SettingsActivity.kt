package com.example.reminderapp.ui

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.reminderapp.databinding.ActivitySettingsBinding
import com.example.reminderapp.utils.QuietHours

/**
 * Global defaults for quiet hours. New reminders pick these up as their
 * starting values. Stored in SharedPreferences.
 */
class SettingsActivity : AppCompatActivity() {

    companion object {
        const val PREFS = "reminder_prefs"
        const val KEY_DEFAULT_QUIET_ENABLED = "default_quiet_enabled"
        const val KEY_DEFAULT_QUIET_START = "default_quiet_start"
        const val KEY_DEFAULT_QUIET_END = "default_quiet_end"

        fun defaultQuietEnabled(ctx: Context): Boolean =
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_DEFAULT_QUIET_ENABLED, true)

        fun defaultQuietStart(ctx: Context): Int =
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_DEFAULT_QUIET_START, 23)

        fun defaultQuietEnd(ctx: Context): Int =
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_DEFAULT_QUIET_END, 10)
    }

    private lateinit var binding: ActivitySettingsBinding
    private var startHour: Int = 23
    private var endHour: Int = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        binding.defaultQuietSwitch.isChecked =
            prefs.getBoolean(KEY_DEFAULT_QUIET_ENABLED, true)
        startHour = prefs.getInt(KEY_DEFAULT_QUIET_START, 23)
        endHour = prefs.getInt(KEY_DEFAULT_QUIET_END, 10)
        refreshLabels()

        binding.startButton.setOnClickListener { pick(true) }
        binding.endButton.setOnClickListener { pick(false) }

        binding.defaultQuietSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(KEY_DEFAULT_QUIET_ENABLED, checked).apply()
        }
        binding.saveButton.setOnClickListener {
            prefs.edit()
                .putInt(KEY_DEFAULT_QUIET_START, startHour)
                .putInt(KEY_DEFAULT_QUIET_END, endHour)
                .apply()
            finish()
        }
    }

    private fun pick(isStart: Boolean) {
        val initial = if (isStart) startHour else endHour
        TimePickerDialog(this, { _, h, _ ->
            if (isStart) startHour = h else endHour = h
            refreshLabels()
        }, initial, 0, false).show()
    }

    private fun refreshLabels() {
        binding.startButton.text = QuietHours.formatHour(startHour)
        binding.endButton.text = QuietHours.formatHour(endHour)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
