package com.example.autobrain.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.autobrain.data.repository.AudioDiagnosticRepository
import com.example.autobrain.data.repository.VideoDiagnosticRepository
import com.example.autobrain.domain.repository.AIScoreRepository
import com.example.autobrain.domain.repository.ReminderRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val aiScoreRepository: AIScoreRepository,
    private val reminderRepository: ReminderRepository,
    private val audioDiagnosticRepository: AudioDiagnosticRepository,
    private val videoDiagnosticRepository: VideoDiagnosticRepository,
    private val auth: FirebaseAuth
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure()

            aiScoreRepository.syncAIScores(userId)
            reminderRepository.syncReminders(userId)
            audioDiagnosticRepository.syncUnsyncedDiagnostics()
            videoDiagnosticRepository.syncUnsyncedDiagnostics()

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "data_sync_worker"
    }
}
