package com.example.autobrain.data.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    fun scheduleDataSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
            2, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DataSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    fun scheduleReminderNotifications(context: Context) {
        val reminderRequest = PeriodicWorkRequestBuilder<ReminderNotificationWorker>(
            6, TimeUnit.HOURS
        )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ReminderNotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }

    fun scheduleVideoCleanup(context: Context) {
        val cleanupRequest = PeriodicWorkRequestBuilder<VideoCleanupWorker>(
            1, TimeUnit.DAYS
        )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            VideoCleanupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }

    fun scheduleOneTimeSyncNow(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeSyncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(oneTimeSyncRequest)
    }

    fun cancelAllWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DataSyncWorker.WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(ReminderNotificationWorker.WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(VideoCleanupWorker.WORK_NAME)
    }
}
