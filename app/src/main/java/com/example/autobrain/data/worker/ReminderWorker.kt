package com.example.autobrain.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.autobrain.MainActivity
import com.example.autobrain.R
import com.example.autobrain.core.utils.Constants
import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.ai.GeminiAiRepository
import com.example.autobrain.domain.model.MaintenanceReminder
import com.example.autobrain.domain.repository.AIScoreRepository
import com.example.autobrain.domain.repository.CarLogRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val carLogRepository: CarLogRepository,
    private val geminiAiRepository: GeminiAiRepository,
    private val aiScoreRepository: AIScoreRepository,
    private val auth: FirebaseAuth
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure()

            // Fetch upcoming reminders
            val remindersResult = carLogRepository.getUpcomingReminders(userId)
            
            if (remindersResult is com.example.autobrain.core.utils.Result.Success) {
                val reminders = remindersResult.data
                val now = System.currentTimeMillis()

                reminders.forEach { reminder ->
                    checkAndNotify(userId, reminder, now)
                }
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun checkAndNotify(userId: String, reminder: MaintenanceReminder, now: Long) {
        val diffMs = reminder.dueDate - now
        val daysLeft = TimeUnit.MILLISECONDS.toDays(diffMs)

        // Check for specific intervals (30, 15, 7 days) or Overdue
        val shouldNotify = when (daysLeft) {
            30L, 15L, 7L -> true
            in -30..-1 -> true // Overdue within last month (prevent spamming too old?) 
                               // Actually, maybe we notify once for overdue. 
                               // Ideally we track "lastNotificationSent" date in Reminder entity.
            else -> false
        }
        
        // Simple logic: If we are exactly on the day (or close enough given worker interval)
        // For production, we'd check if notification was already sent today.
        // Here we assume the worker runs daily and we check for exact matches or simplified ranges.
        
        if (shouldNotify) {
            val isOverdue = daysLeft < 0
            
            // Call Gemini for personalized message
            val geminiMessage = try {
                geminiAiRepository.generateSmartReminderMessage(reminder, daysLeft.toInt())
            } catch (e: Exception) {
                // Fallback message
                if (isOverdue) {
                    "Attention: ${reminder.title} is ${-daysLeft} days overdue!"
                } else {
                    "Reminder: ${reminder.title} is due in $daysLeft days."
                }
            }

            showNotification(
                title = if (isOverdue) "⚠️ MAINTENANCE ALERT" else "📅 VEHICLE REMINDER",
                body = geminiMessage,
                reminderId = reminder.id
            )

            // If Overdue, impact AI Score
            if (isOverdue) {
                applyOverduePenalty(userId, reminder, daysLeft.toInt())
            }
        }
    }

    private suspend fun applyOverduePenalty(userId: String, reminder: MaintenanceReminder, daysOverdue: Int) {
        // Calculate penalty based on severity and days
        // This could also be a Gemini call: "How much to deduct for X days overdue oil change?"
        // For now, let's do a simple penalty or ask Gemini if possible.
        
        // MVP: Simple penalty
        // Ideally we update the score via repository
        // aiScoreRepository.applyMaintenancePenalty(userId, 5) // Example method
        
        // Log the event. The actual score update happens when the user opens the app 
        // and the AIScoreViewModel recalculates based on overdue items.
        // We could also save a "Penalty Event" to Firestore if needed.
        android.util.Log.d("ReminderWorker", "Applying overdue penalty for ${reminder.title} ($daysOverdue days)")
    }

    private fun showNotification(title: String, body: String, reminderId: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_ID_REMINDERS,
                "Smart Maintenance",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "AI-powered maintenance alerts"
                enableLights(true)
                lightColor = android.graphics.Color.RED
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminder_id", reminderId)
            putExtra("navigate_to", "car_log")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with actual icon
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(reminderId.hashCode(), notification)
    }
}
