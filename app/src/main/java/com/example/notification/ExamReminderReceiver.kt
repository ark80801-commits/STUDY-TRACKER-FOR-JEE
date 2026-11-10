package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ExamReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (Intent.ACTION_BOOT_COMPLETED == action) {
            // Restore all active alarms on device restart
            ExamReminderManager.rescheduleAllActiveReminders(context)
            return
        }

        if (ExamReminderManager.ACTION_REMINDER == action) {
            val label = intent.getStringExtra(ExamReminderManager.EXTRA_REMINDER_LABEL) ?: ""

            // Asynchronously fetch current exam details from Room DB
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val settings = db.examSettingsDao().getSettingsImmediate()
                val now = System.currentTimeMillis()
                val targetDate = settings?.targetDateEpochMillis ?: (now + TimeUnit.DAYS.toMillis(90))
                val daysLeft = TimeUnit.MILLISECONDS.toDays(targetDate - now)
                val title = settings?.examTitle ?: "JEE Main & Advanced"

                ExamReminderManager.showReminderNotification(context, title, daysLeft, label)
            }
        }
    }
}
