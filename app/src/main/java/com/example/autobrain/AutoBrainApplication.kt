package com.example.autobrain

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.autobrain.data.worker.WorkManagerScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AutoBrainApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        
        try {
            WorkManager.getInstance(this)
            WorkManagerScheduler.scheduleDataSync(this)
            WorkManagerScheduler.scheduleReminderNotifications(this)
            WorkManagerScheduler.scheduleVideoCleanup(this)
        } catch (e: Exception) {
            Log.e("AutoBrainApp", "Failed to schedule workers: ${e.message}", e)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}
