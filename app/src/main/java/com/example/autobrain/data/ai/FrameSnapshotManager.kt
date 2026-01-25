package com.example.autobrain.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Frame Snapshot Manager
 * Saves critical frames where smoke or vibration is detected
 */
@Singleton
class FrameSnapshotManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "FrameSnapshotManager"
    private val snapshotsDir = File(context.filesDir, "frame_snapshots")
    
    init {
        if (!snapshotsDir.exists()) {
            snapshotsDir.mkdirs()
        }
    }
    
    /**
     * Save a critical frame with metadata
     */
    suspend fun saveCriticalFrame(
        diagnosticId: String,
        frameNumber: Int,
        bitmap: Bitmap,
        reason: String, // "smoke_detected" or "vibration_detected"
        confidence: Float
    ): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "${diagnosticId}_frame_${frameNumber}_${reason}.jpg"
            val file = File(snapshotsDir, fileName)
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            
            Log.d(TAG, "Saved critical frame: $fileName (confidence: ${(confidence * 100).toInt()}%)")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save frame: ${e.message}")
            null
        }
    }
    
    /**
     * Get all snapshots for a diagnostic
     */
    fun getSnapshotsForDiagnostic(diagnosticId: String): List<File> {
        return snapshotsDir.listFiles { file ->
            file.name.startsWith(diagnosticId)
        }?.toList() ?: emptyList()
    }
    
    /**
     * Delete snapshots for a diagnostic
     */
    fun deleteSnapshotsForDiagnostic(diagnosticId: String) {
        getSnapshotsForDiagnostic(diagnosticId).forEach { file ->
            try {
                file.delete()
                Log.d(TAG, "Deleted snapshot: ${file.name}")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete snapshot: ${e.message}")
            }
        }
    }
    
    /**
     * Delete old snapshots (older than 7 days)
     */
    fun deleteOldSnapshots() {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        
        snapshotsDir.listFiles()?.forEach { file ->
            if (file.lastModified() < sevenDaysAgo) {
                try {
                    file.delete()
                    Log.d(TAG, "Deleted old snapshot: ${file.name}")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete old snapshot: ${e.message}")
                }
            }
        }
    }
}

/**
 * Enhanced Frame Analysis Result with snapshot path
 */
data class FrameAnalysisResultWithSnapshot(
    val frameNumber: Int,
    val timestamp: Long,
    val brightness: Float = 0f,
    val smokeDetected: Boolean = false,
    val smokeType: String = "",
    val smokeConfidence: Float = 0f,
    val vibrationDetected: Boolean = false,
    val vibrationLevel: Float = 0f,
    val snapshotPath: String? = null, // Path to saved frame image
    val error: String? = null
)
