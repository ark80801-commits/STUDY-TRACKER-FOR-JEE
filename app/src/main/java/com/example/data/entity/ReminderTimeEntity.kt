package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(tableName = "reminder_times")
data class ReminderTimeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int, // 0..23
    val minute: Int, // 0..59
    val isEnabled: Boolean = true,
    val label: String = "" // Optional custom label, e.g. "Morning Practice", "Night Revision"
) {
    val formattedTime: String
        get() {
            val amPm = if (hour >= 12) "PM" else "AM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)
        }
}
