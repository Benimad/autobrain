package com.example.autobrain.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.repository.AudioDiagnosticRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * WorkManager Worker for Background Audio Diagnostic Sync
 * 
 * Features:
 * - Periodic sync of unsynced diagnostics
 * - Retry with exponential backoff
 * - Network-aware (only runs when online)
 * - Battery-efficient constraints
 */
@HiltWorker
class AudioDiagnosticSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val audioDiagnosticRepository: AudioDiagnosticRepository
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        private const val TAG = "AudioDiagnosticSync"
        const val WORK_NAME = "audio_diagnostic_sync"
        private const val PERIODIC_INTERVAL_HOURS = 6L // Sync every 6 hours
        
        /**
         * Schedule periodic sync work
         */
        fun schedule(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Only when online
                .setRequiresBatteryNotLow(true) // Don't drain battery
                .build()
            
            val periodicWork = PeriodicWorkRequestBuilder<AudioDiagnosticSyncWorker>(
                PERIODIC_INTERVAL_HOURS,
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(WORK_NAME)
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
                periodicWork
            )
            
            Log.d(TAG, "Periodic sync scheduled")
        }
        
        /**
         * Trigger immediate one-time sync
         */
        fun triggerImmediateSync(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val oneTimeWork = OneTimeWorkRequestBuilder<AudioDiagnosticSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            
            workManager.enqueue(oneTimeWork)
            
            Log.d(TAG, "Immediate sync triggered")
        }
        
        /**
         * Cancel all sync work
         */
        fun cancel(workManager: WorkManager) {
            workManager.cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Sync work cancelled")
        }
    }
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting audio diagnostic sync...")
        
        return try {
            // Sync unsynced diagnostics
            val syncResult = audioDiagnosticRepository.syncUnsyncedDiagnostics()
            
            when (syncResult) {
                is com.example.autobrain.core.utils.Result.Success -> {
                    val syncedCount = syncResult.data
                    Log.d(TAG, "Successfully synced $syncedCount diagnostics")
                    
                    // Return success
                    Result.success()
                }
                is com.example.autobrain.core.utils.Result.Error -> {
                    Log.e(TAG, "Sync failed: ${syncResult.exception.message}")
                    
                    // Retry on failure
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
                is com.example.autobrain.core.utils.Result.Loading -> {
                    // Should not happen, but handle it
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync worker error: ${e.message}", e)
            
            // Retry with backoff
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}

/**
 * Extension function to setup audio diagnostic sync
 */
fun WorkManager.setupAudioDiagnosticSync() {
    AudioDiagnosticSyncWorker.schedule(this)
}
