package com.example.reminderapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reminderapp.data.Reminder
import com.example.reminderapp.databinding.ItemReminderBinding
import com.example.reminderapp.utils.QuietHours
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderAdapter(
    private val onClick: (Reminder) -> Unit,
    private val onToggle: (Reminder, Boolean) -> Unit,
    private val onLongPress: (Reminder) -> Unit
) : ListAdapter<Reminder, ReminderAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemReminderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    private val timeFormat = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = getItem(position)
        with(holder.binding) {
            titleText.text = r.title
            descText.text = if (r.description.isBlank()) "No description" else r.description
            timeText.text = timeFormat.format(Date(r.nextTriggerAtMillis))

            val repeat = if (r.repeatIntervalMinutes > 0)
                "Repeats every ${r.repeatIntervalMinutes} min" else "One-time"
            repeatText.text = repeat

            quietText.text = if (r.quietHoursEnabled) {
                "Quiet: ${QuietHours.formatHour(r.quietStartHour)} – ${QuietHours.formatHour(r.quietEndHour)}"
            } else "Quiet hours: off"

            categoryChip.text = r.category

            enabledSwitch.setOnCheckedChangeListener(null)
            enabledSwitch.isChecked = r.isEnabled
            enabledSwitch.setOnCheckedChangeListener { _, checked -> onToggle(r, checked) }

            root.setOnClickListener { onClick(r) }
            root.setOnLongClickListener { onLongPress(r); true }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Reminder>() {
            override fun areItemsTheSame(a: Reminder, b: Reminder) = a.id == b.id
            override fun areContentsTheSame(a: Reminder, b: Reminder) = a == b
        }
    }
}
