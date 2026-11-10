package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun EditExamDialog(
    currentTitle: String,
    currentTargetDateMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (title: String, targetDateMillis: Long) -> Unit
) {
    var title by remember { mutableStateOf(currentTitle) }
    var isError by remember { mutableStateOf(false) }

    val todayCalendar = remember { Calendar.getInstance() }
    val currentBaseYear = todayCalendar.get(Calendar.YEAR)

    // Calendar for tracking selected date
    val calendar = remember {
        Calendar.getInstance().apply {
            timeInMillis = currentTargetDateMillis
        }
    }

    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) } // 0..11
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    // Helper to sync calendar & state
    fun updateCalendarDate(year: Int, month: Int, day: Int) {
        val tempCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val maxDaysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val validDay = day.coerceIn(1, maxDaysInMonth)

        selectedYear = year
        selectedMonth = month
        selectedDay = validDay

        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, validDay)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
    }

    fun applyPresetDaysAhead(days: Int) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        updateCalendarDate(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH),
            day = cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Years list: current year up to +5 years (exceeding 3+ years into the future)
    val yearOptions = remember(currentBaseYear) {
        (currentBaseYear..(currentBaseYear + 5)).toList()
    }

    val monthNames = remember {
        listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
    }

    val daysInCurrentMonth = remember(selectedYear, selectedMonth) {
        val tempCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        (1..tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)).toList()
    }

    // Calculate current days diff
    val now = System.currentTimeMillis()
    val targetTime = calendar.timeInMillis
    val diffMillis = targetTime - now
    val daysLeft = TimeUnit.MILLISECONDS.toDays(diffMillis)

    val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(targetTime))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Exam Date & Schedule",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Exam Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Exam Name") },
                    placeholder = { Text("e.g. Final Semester Exams, Bar Exam, USMLE") },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Exam title cannot be blank") }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exam_title_input")
                )

                // Quick Presets Dropdown
                val presetOptions = listOf(
                    "Custom Date Selection" to 0,
                    "In 1 Month (30 Days)" to 30,
                    "In 3 Months (90 Days)" to 90,
                    "In 6 Months (180 Days)" to 180,
                    "In 1 Year (365 Days)" to 365,
                    "In 2 Years (730 Days)" to 730,
                    "In 3 Years (1,095 Days)" to 1095,
                    "In 4 Years (1,460 Days)" to 1460
                )

                Column {
                    Text(
                        text = "Quick Horizon Preset",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    DropdownField(
                        label = "Choose Preset Horizon",
                        selectedValue = "Quick Date Presets (1 to 4 Years)",
                        options = presetOptions.map { it.first },
                        onSelect = { selectedLabel ->
                            val days = presetOptions.find { it.first == selectedLabel }?.second ?: 0
                            if (days > 0) {
                                applyPresetDaysAhead(days)
                            }
                        },
                        testTag = "preset_dropdown"
                    )
                }

                // Date Selectors: Month, Day, Year Dropdowns
                Column {
                    Text(
                        text = "Exam Date Dropdowns (Past 3+ Years)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Month Dropdown
                        DropdownField(
                            label = "Month",
                            selectedValue = monthNames[selectedMonth],
                            options = monthNames,
                            onSelect = { newMonthName ->
                                val newMonthIndex = monthNames.indexOf(newMonthName)
                                if (newMonthIndex != -1) {
                                    updateCalendarDate(selectedYear, newMonthIndex, selectedDay)
                                }
                            },
                            modifier = Modifier.weight(1.3f),
                            testTag = "month_dropdown"
                        )

                        // Day Dropdown
                        DropdownField(
                            label = "Day",
                            selectedValue = "$selectedDay",
                            options = daysInCurrentMonth.map { "$it" },
                            onSelect = { newDayStr ->
                                val newDay = newDayStr.toIntOrNull() ?: selectedDay
                                updateCalendarDate(selectedYear, selectedMonth, newDay)
                            },
                            modifier = Modifier.weight(0.9f),
                            testTag = "day_dropdown"
                        )

                        // Year Dropdown (goes past 3 years!)
                        DropdownField(
                            label = "Year",
                            selectedValue = "$selectedYear",
                            options = yearOptions.map { "$it" },
                            onSelect = { newYearStr ->
                                val newYear = newYearStr.toIntOrNull() ?: selectedYear
                                updateCalendarDate(newYear, selectedMonth, selectedDay)
                            },
                            modifier = Modifier.weight(1.0f),
                            testTag = "year_dropdown"
                        )
                    }
                }

                // Summary & Countdown Preview Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Target Exam Date",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Days remaining badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                val yearsDiff = daysLeft / 365.25f
                                val badgeText = when {
                                    daysLeft < 0 -> "${-daysLeft} Days Ago"
                                    daysLeft == 0L -> "Exam Today!"
                                    daysLeft == 1L -> "Tomorrow"
                                    daysLeft >= 365 -> "${daysLeft}d (~${"%.1f".format(yearsDiff)}y)"
                                    else -> "$daysLeft Days Left"
                                }
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(title.trim(), calendar.timeInMillis)
                    }
                },
                modifier = Modifier.testTag("save_exam_settings_button")
            ) {
                Text("Save Date")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_exam_settings_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun DropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .testTag(testTag),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedValue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedValue
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}
