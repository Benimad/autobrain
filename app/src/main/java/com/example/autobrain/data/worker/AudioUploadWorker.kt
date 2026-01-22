package com.example.autobrain.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.autobrain.data.local.dao.AudioDiagnosticDao
import com.example.autobrain.data.local.entity.toDomain
import com.example.autobrain.data.local.entity.toFirestoreMap
import com.example.autobrain.data.repository.AudioDiagnosticRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class AudioUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val audioDiagnosticDao: AudioDiagnosticDao,
    private val audioDiagnosticRepository: AudioDiagnosticRepository,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "AudioUploadWorker"
        const val KEY_DIAGNOSTIC_ID = "DIAGNOSTIC_ID"
    }

    override suspend fun doWork(): Result {
        val diagnosticId = inputData.getString(KEY_DIAGNOSTIC_ID)
        
        if (diagnosticId == null) {
            Log.e(TAG, "No diagnostic ID provided")
            return Result.failure()
        }

        Log.d(TAG, "Starting background upload for diagnostic: $diagnosticId")

        return try {
            val entity = audioDiagnosticDao.getById(diagnosticId)
            
            if (entity == null) {
                Log.e(TAG, "Diagnostic not found: $diagnosticId")
                return Result.failure()
            }

            val diagnostic = entity.toDomain()
            
            val audioUrl = if (diagnostic.audioFilePath.isNotEmpty()) {
                audioDiagnosticRepository.uploadAudioFile(diagnosticId, diagnostic.audioFilePath)
            } else {
                ""
            }

            val updatedDiagnostic = diagnostic.copy(audioUrl = audioUrl)

            firestore.collection("audio_diagnostics")
                .document(diagnosticId)
                .set(updatedDiagnostic.toFirestoreMap())
                .await()

            audioDiagnosticDao.updateAudioUrl(diagnosticId, audioUrl)
            audioDiagnosticDao.markAsSynced(diagnosticId)
            audioDiagnosticDao.clearSyncError(diagnosticId)

            Log.d(TAG, "Successfully uploaded diagnostic: $diagnosticId")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed for $diagnosticId: ${e.message}", e)
            
            audioDiagnosticDao.recordSyncError(diagnosticId, e.message ?: "Unknown error")
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
