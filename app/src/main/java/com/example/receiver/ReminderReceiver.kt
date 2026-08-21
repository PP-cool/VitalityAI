package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.util.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Received broadcast: ${intent.action}")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val dao = db.healthDao()
                val latestMetrics = dao.getLatestMetrics().firstOrNull()
                val allHabits = dao.getAllHabits().firstOrNull() ?: emptyList()

                // If broadcast is from BOOT_COMPLETED or package replaced, reschedule if enabled
                if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
                    if (latestMetrics?.isReminderEnabled == true) {
                        ReminderScheduler.scheduleDailyReminder(
                            context,
                            latestMetrics.reminderHour,
                            latestMetrics.reminderMinute
                        )
                    }
                    return@launch
                }

                // If user disabled reminders, skip notification
                if (latestMetrics != null && !latestMetrics.isReminderEnabled) {
                    return@launch
                }

                // Determine unfinished tasks
                val remainingMetrics = mutableListOf<String>()
                if (latestMetrics != null) {
                    if (!latestMetrics.isWaterCompleted && latestMetrics.waterCurrentMl < latestMetrics.waterTargetMl) {
                        remainingMetrics.add("Water (${latestMetrics.waterCurrentMl}/${latestMetrics.waterTargetMl}ml)")
                    }
                    if (!latestMetrics.isStepsCompleted && latestMetrics.stepCurrent < latestMetrics.stepTarget) {
                        remainingMetrics.add("Steps (${latestMetrics.stepCurrent}/${latestMetrics.stepTarget})")
                    }
                    if (!latestMetrics.isSleepCompleted && latestMetrics.sleepHours < latestMetrics.sleepTargetHours) {
                        remainingMetrics.add("Sleep")
                    }
                    if (!latestMetrics.isWorkoutCompleted && latestMetrics.workoutMinutes < latestMetrics.workoutTargetMinutes) {
                        remainingMetrics.add("Workout (${latestMetrics.workoutMinutes}/${latestMetrics.workoutTargetMinutes}m)")
                    }
                }

                val unfinishedHabits = allHabits.filter { !it.completed }

                val bodyText = when {
                    remainingMetrics.isNotEmpty() && unfinishedHabits.isNotEmpty() -> {
                        "You still have ${remainingMetrics.joinToString(", ")} and ${unfinishedHabits.size} habit(s) to finish today!"
                    }
                    remainingMetrics.isNotEmpty() -> {
                        "Target reminder: You still have ${remainingMetrics.joinToString(", ")} left to log today!"
                    }
                    unfinishedHabits.isNotEmpty() -> {
                        "Habit reminder: You have ${unfinishedHabits.size} habit(s) left: ${unfinishedHabits.take(2).joinToString { it.title }}"
                    }
                    else -> {
                        "All habits and metrics logged for today! Amazing consistency! 🔥"
                    }
                }

                // Ensure channel exists
                ReminderScheduler.createNotificationChannel(context)

                // Intent to open app
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val contentPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Time to complete your habits! ⚡")
                    .setContentText(bodyText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(contentPendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(longArrayOf(0, 250, 150, 250))
                    .build()

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.notify(ReminderScheduler.NOTIFICATION_ID, notification)

                // Reschedule for next day if enabled
                if (latestMetrics?.isReminderEnabled == true) {
                    ReminderScheduler.scheduleDailyReminder(
                        context,
                        latestMetrics.reminderHour,
                        latestMetrics.reminderMinute
                    )
                }
            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Error delivering habit reminder: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
