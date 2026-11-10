package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.entity.ReminderTimeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object ExamReminderManager {

    const val CHANNEL_ID = "jee_daily_reminders"
    const val NOTIFICATION_ID = 1001
    const val ACTION_REMINDER = "com.example.ACTION_EXAM_REMINDER"
    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_REMINDER_LABEL = "extra_reminder_label"

    private val MOTIVATIONAL_MESSAGES = listOf(
        "\"Success is the sum of small efforts, repeated day in and day out.\" Review today's Physics & Chemistry formulas!",
        "\"Believe you can and you're halfway there.\" Crack those calculus problems today!",
        "Every focused chapter you complete today brings you one step closer to your dream IIT/NIT seat!",
        "Consistency beats intensity. Spend 2 solid hours revising your weak topics today!",
        "\"The secret of getting ahead is getting started.\" Open your syllabus and mark your PD/FD topics!",
        "Keep your momentum alive! Revision and mock practice are the twin keys to a top JEE rank.",
        "Your future self will thank you for the hard work you put in today. Stay sharp and stay confident!",
        "Master the fundamentals, solve past year questions, and trust the process. You can do this!"
    )

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily JEE Exam Reminders"
            val descriptionText = "Daily reminders and motivational updates on time remaining for JEE"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(context: Context, reminder: ReminderTimeEntity) {
        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ExamReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_REMINDER_LABEL, reminder.label)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ExamReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleAllActiveReminders(context: Context) {
        createNotificationChannel(context)
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val dao = db.reminderTimeDao()
            // Seed 2 default custom timings if empty
            if (dao.getCount() == 0) {
                dao.insertReminderTime(
                    ReminderTimeEntity(hour = 8, minute = 30, isEnabled = true, label = "Morning Study Session")
                )
                dao.insertReminderTime(
                    ReminderTimeEntity(hour = 19, minute = 0, isEnabled = true, label = "Evening Practice & Revision")
                )
            }

            val activeList = dao.getActiveReminderTimes()
            for (reminder in activeList) {
                scheduleReminder(context, reminder)
            }
        }
    }

    fun showReminderNotification(context: Context, examTitle: String, daysLeft: Long, customLabel: String = "") {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val labelPrefix = if (customLabel.isNotBlank()) "[$customLabel] " else ""

        val title = when {
            daysLeft < 0 -> "${labelPrefix}$examTitle Passed: Time to Review Results!"
            daysLeft == 0L -> "🎯 ${labelPrefix}$examTitle is TODAY! Give it your best!"
            daysLeft == 1L -> "⏳ ${labelPrefix}Tomorrow is $examTitle! Stay calm & review."
            daysLeft >= 365 -> "🎯 ${labelPrefix}$examTitle: $daysLeft Days Left (~${"%.1f".format(daysLeft / 365.25f)} Years)"
            else -> "🎯 ${labelPrefix}$examTitle: Only $daysLeft Days Remaining!"
        }

        val randomQuote = MOTIVATIONAL_MESSAGES[Random.nextInt(MOTIVATIONAL_MESSAGES.size)]
        val contentText = when {
            daysLeft in 1..7 -> "Final countdown! Revise high-yield topics and formula sheets today. Stay confident!"
            daysLeft > 7 -> randomQuote
            daysLeft == 0L -> "Breathe deep and trust all the hard work you've put in. Good luck!"
            else -> "Keep tracking your study progress and upcoming entrance exams!"
        }

        val largeIcon = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_app_icon_img)
        } catch (e: Exception) {
            null
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_icon_img)
            .apply {
                if (largeIcon != null) {
                    setLargeIcon(largeIcon)
                }
            }
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            // Use unique ID or default ID so multiple alerts can display or update
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun triggerInstantTestNotification(context: Context, label: String = "Test Reminder") {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val settings = db.examSettingsDao().getSettingsImmediate()
            val now = System.currentTimeMillis()
            val targetDate = settings?.targetDateEpochMillis ?: (now + TimeUnit.DAYS.toMillis(90))
            val daysLeft = TimeUnit.MILLISECONDS.toDays(targetDate - now)
            val title = settings?.examTitle ?: "JEE Main & Advanced"

            showReminderNotification(context, title, daysLeft, label)
        }
    }
}
