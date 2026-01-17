package com.example.autobrain.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.autobrain.data.local.dao.VideoDiagnosticDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class VideoCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val videoDiagnosticDao: VideoDiagnosticDao
) : CoroutineWorker(context, params) {

    private val TAG = "VideoCleanupWorker"

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting video cleanup task...")
            
            val currentTime = System.currentTimeMillis()
            val expiredDiagnostics = videoDiagnosticDao.getExpiredDiagnostics(currentTime)
            
            Log.d(TAG, "Found ${expiredDiagnostics.size} expired videos to delete")
            
            var filesDeleted = 0
            var filesFailed = 0
            
            expiredDiagnostics.forEach { diagnostic ->
                try {
                    val videoFile = File(diagnostic.videoFilePath)
                    if (videoFile.exists()) {
                        val deleted = videoFile.delete()
                        if (deleted) {
                            filesDeleted++
                            Log.d(TAG, "✅ Deleted video file: ${videoFile.name}")
                        } else {
                            filesFailed++
                            Log.w(TAG, "⚠️ Failed to delete video file: ${videoFile.name}")
                        }
                    }
                } catch (e: Exception) {
                    filesFailed++
                    Log.e(TAG, "❌ Error deleting video file: ${e.message}")
                }
            }
            
            val dbDeleted = videoDiagnosticDao.deleteExpiredDiagnostics(currentTime)
            
            Log.d(TAG, """
                ✅ Video cleanup completed:
                - Files deleted: $filesDeleted
                - Files failed: $filesFailed
                - DB records deleted: $dbDeleted
            """.trimIndent())
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Video cleanup failed: ${e.message}", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "video_cleanup_worker"
    }
}
