package com.example.autobrain.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.autobrain.data.local.dao.VideoDiagnosticDao
import com.example.autobrain.data.local.entity.toDomain
import com.example.autobrain.data.local.entity.toFirestoreMap
import com.example.autobrain.data.repository.VideoDiagnosticRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class VideoUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val videoDiagnosticDao: VideoDiagnosticDao,
    private val videoDiagnosticRepository: VideoDiagnosticRepository,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "VideoUploadWorker"
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
            val entity = videoDiagnosticDao.getById(diagnosticId)
            
            if (entity == null) {
                Log.e(TAG, "Diagnostic not found: $diagnosticId")
                return Result.failure()
            }

            val diagnostic = entity.toDomain()
            
            val videoUrl = if (diagnostic.videoFilePath.isNotEmpty()) {
                videoDiagnosticRepository.uploadVideoFile(diagnosticId, diagnostic.videoFilePath)
            } else {
                ""
            }

            val updatedDiagnostic = diagnostic.copy(videoUrl = videoUrl)

            firestore.collection("video_diagnostics")
                .document(diagnosticId)
                .set(updatedDiagnostic.toFirestoreMap())
                .await()

            videoDiagnosticDao.updateVideoUrl(diagnosticId, videoUrl)
            videoDiagnosticDao.markAsSynced(diagnosticId)
            videoDiagnosticDao.clearSyncError(diagnosticId)

            Log.d(TAG, "Successfully uploaded diagnostic: $diagnosticId")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed for $diagnosticId: ${e.message}", e)
            
            videoDiagnosticDao.recordSyncError(diagnosticId, e.message ?: "Unknown error")
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
