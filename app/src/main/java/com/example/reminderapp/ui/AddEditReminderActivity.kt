package com.example.reminderapp.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.reminderapp.data.Reminder
import com.example.reminderapp.data.ReminderDatabase
import com.example.reminderapp.databinding.ActivityAddEditReminderBinding
import com.example.reminderapp.utils.QuietHours
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditReminderActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "reminder_id"
    }

    private lateinit var binding: ActivityAddEditReminderBinding
    private val viewModel: ReminderViewModel by viewModels()

    private var editingId: Long = 0L
    private var triggerCalendar: Calendar = Calendar.getInstance().apply {
        add(Calendar.MINUTE, 5)
    }
    private var quietStart: Int = 23
    private var quietEnd: Int = 10

    private val dateFmt = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    private val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Category dropdown
        val categories = listOf("General", "Health", "Work", "Personal", "Shopping")
        binding.categorySpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, categories
        )

        // Priority dropdown (0 Low, 1 Normal, 2 High)
        val priorities = listOf("Low", "Normal", "High")
        binding.prioritySpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, priorities
        )
        binding.prioritySpinner.setSelection(1)

        // Repeat dropdown — minutes interval
        val repeatLabels = listOf("Does not repeat", "Every 30 minutes", "Every 1 hour",
            "Every 2 hours", "Every 4 hours", "Every 6 hours", "Every 12 hours", "Every 24 hours")
        val repeatValues = listOf(0, 30, 60, 120, 240, 360, 720, 1440)
        binding.repeatSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, repeatLabels
        )

        editingId = intent.getLongExtra(EXTRA_ID, 0L)
        if (editingId != 0L) {
            supportActionBar?.title = "Edit reminder"
            lifecycleScope.launch {
                val dao = ReminderDatabase.getInstance(this@AddEditReminderActivity).reminderDao()
                dao.getById(editingId)?.let { existing ->
                    binding.titleInput.setText(existing.title)
                    binding.descInput.setText(existing.description)
                    triggerCalendar.timeInMillis = existing.nextTriggerAtMillis
                    binding.categorySpinner.setSelection(
                        categories.indexOf(existing.category).coerceAtLeast(0)
                    )
                    binding.prioritySpinner.setSelection(existing.priority.coerceIn(0, 2))
                    val idx = repeatValues.indexOf(existing.repeatIntervalMinutes)
                    binding.repeatSpinner.setSelection(if (idx >= 0) idx else 0)
                    binding.quietHoursSwitch.isChecked = existing.quietHoursEnabled
                    quietStart = existing.quietStartHour
                    quietEnd = existing.quietEndHour
                    refreshDateTimeLabels()
                    refreshQuietLabels()
                }
            }
        } else {
            supportActionBar?.title = "New reminder"
            refreshDateTimeLabels()
            refreshQuietLabels()
        }

        binding.dateButton.setOnClickListener { pickDate() }
        binding.timeButton.setOnClickListener { pickTime() }

        binding.quietHoursSwitch.setOnCheckedChangeListener { _, checked ->
            binding.quietHoursGroup.visibility = if (checked) View.VISIBLE else View.GONE
        }
        binding.quietHoursGroup.visibility =
            if (binding.quietHoursSwitch.isChecked) View.VISIBLE else View.GONE

        binding.quietStartButton.setOnClickListener { pickQuietHour(true) }
        binding.quietEndButton.setOnClickListener { pickQuietHour(false) }

        binding.saveButton.setOnClickListener { save(repeatValues) }
    }

    private fun pickDate() {
        DatePickerDialog(
            this,
            { _, y, m, d ->
                triggerCalendar.set(Calendar.YEAR, y)
                triggerCalendar.set(Calendar.MONTH, m)
                triggerCalendar.set(Calendar.DAY_OF_MONTH, d)
                refreshDateTimeLabels()
            },
            triggerCalendar.get(Calendar.YEAR),
            triggerCalendar.get(Calendar.MONTH),
            triggerCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun pickTime() {
        TimePickerDialog(
            this,
            { _, h, m ->
                triggerCalendar.set(Calendar.HOUR_OF_DAY, h)
                triggerCalendar.set(Calendar.MINUTE, m)
                triggerCalendar.set(Calendar.SECOND, 0)
                refreshDateTimeLabels()
            },
            triggerCalendar.get(Calendar.HOUR_OF_DAY),
            triggerCalendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun pickQuietHour(isStart: Boolean) {
        val initial = if (isStart) quietStart else quietEnd
        TimePickerDialog(
            this,
            { _, h, _ ->
                if (isStart) quietStart = h else quietEnd = h
                refreshQuietLabels()
            },
            initial, 0, false
        ).show()
    }

    private fun refreshDateTimeLabels() {
        binding.dateButton.text = dateFmt.format(Date(triggerCalendar.timeInMillis))
        binding.timeButton.text = timeFmt.format(Date(triggerCalendar.timeInMillis))
    }

    private fun refreshQuietLabels() {
        binding.quietStartButton.text = QuietHours.formatHour(quietStart)
        binding.quietEndButton.text = QuietHours.formatHour(quietEnd)
    }

    private fun save(repeatValues: List<Int>) {
        val title = binding.titleInput.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }
        if (triggerCalendar.timeInMillis < System.currentTimeMillis()) {
            Toast.makeText(this, "Please pick a future date/time", Toast.LENGTH_SHORT).show()
            return
        }

        val reminder = Reminder(
            id = editingId,
            title = title,
            description = binding.descInput.text.toString().trim(),
            nextTriggerAtMillis = triggerCalendar.timeInMillis,
            repeatIntervalMinutes = repeatValues[binding.repeatSpinner.selectedItemPosition],
            isEnabled = true,
            quietHoursEnabled = binding.quietHoursSwitch.isChecked,
            quietStartHour = quietStart,
            quietEndHour = quietEnd,
            category = binding.categorySpinner.selectedItem.toString(),
            priority = binding.prioritySpinner.selectedItemPosition
        )
        viewModel.upsert(reminder)
        Toast.makeText(this, "Reminder saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
