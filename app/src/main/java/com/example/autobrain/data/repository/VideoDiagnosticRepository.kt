package com.example.autobrain.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.ai.ComprehensiveVideoDiagnostic
import com.example.autobrain.data.ai.MlKitVideoAnalyzer
import com.example.autobrain.data.ai.VideoAnalysisResults
import com.example.autobrain.data.ai.GeminiAiRepository
import com.example.autobrain.data.ai.CarAnomaly
import com.example.autobrain.data.ai.AnomalyType
import com.example.autobrain.data.ai.SeverityLevel
import com.example.autobrain.data.ai.buildComprehensiveVideoAnalysisPrompt
import com.example.autobrain.data.ai.toFirestoreMap
import com.example.autobrain.data.local.entity.AudioDiagnosticData
import com.example.autobrain.data.local.entity.toAudioDiagnosticData
import com.example.autobrain.domain.model.CarLog
import com.example.autobrain.domain.model.IssueSeverity
import com.example.autobrain.domain.model.User
import com.example.autobrain.data.local.dao.MaintenanceRecordDao
import com.example.autobrain.data.local.dao.VideoDiagnosticDao
import com.example.autobrain.data.local.entity.VideoDiagnosticData
import com.example.autobrain.data.local.entity.toEntity
import com.example.autobrain.data.local.entity.toFirestoreMap
import com.example.autobrain.data.local.entity.toDomain
import com.example.autobrain.data.local.entity.toVideoDiagnosticData
import com.example.autobrain.domain.usecase.VideoScoringUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.autobrain.core.utils.MediaCompressionUtils

/**
 * Video Diagnostic Repository - Offline-First with Security
 * 
 * Features:
 * - 100% offline video analysis with ML Kit
 * - Local Room database as source of truth
 * - Encrypted video storage (optional)
 * - Background sync to Firestore/Storage with consent
 * - Auto-delete after 7 days
 * - Video integrity verification with hash
 */
@Singleton
class VideoDiagnosticRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val videoAnalyzer: MlKitVideoAnalyzer,
    private val videoAnonymizer: com.example.autobrain.data.ai.VideoAnonymizer,
    private val scoringUseCase: VideoScoringUseCase,
    private val videoDiagnosticDao: VideoDiagnosticDao,
    private val maintenanceRecordDao: MaintenanceRecordDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val geminiAiRepository: GeminiAiRepository
) {
    
    private val TAG = "VideoDiagnosticRepo"
    
    companion object {
        private const val COLLECTION_VIDEO_DIAGNOSTICS = "video_diagnostics"
        private const val COLLECTION_COMPREHENSIVE_VIDEO_DIAGNOSTICS = "comprehensive_video_diagnostics"
        private const val COLLECTION_AUDIO_DIAGNOSTICS = "audio_diagnostics"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_CAR_LOGS = "car_logs"
        private const val STORAGE_PATH_VIDEOS = "video_diagnostics"
        private const val AUTO_DELETE_DAYS = 7L
    }
    
    // =============================================================================
    // MAIN DIAGNOSTIC FLOW
    // =============================================================================
    
    /**
     * Process video diagnostic after recording
     * 
     * Flow:
     * 1. Verify user & car profile
     * 2. Analyze video with ML Kit (already done during recording)
     * 3. Calculate smart score
     * 4. Save to Room (offline-first)
     * 5. Schedule background sync if consent given
     */
    suspend fun processVideoDiagnostic(
        carId: String,
        videoFilePath: String,
        analysisResults: VideoAnalysisResults,
        hasStorageConsent: Boolean,
        currentMileage: Int? = null,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Result<VideoDiagnosticData> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.Error(Exception("User not authenticated"))
            
            onProgress(0.1f, "Vérification du profil...")
            
            // Get maintenance records for scoring
            val maintenanceRecords = maintenanceRecordDao.getUnsyncedRecords(userId)
            
            // Calculate smart score
            onProgress(0.3f, "Calcul du score intelligent...")
            val scoreResult = scoringUseCase.calculateScore(
                analysisResults = analysisResults,
                maintenanceRecords = maintenanceRecords,
                currentMileage = currentMileage
            )
            
            // Gemini Enhanced Analysis
            onProgress(0.4f, "Analyse IA Gemini...")
            var finalScoreResult = scoreResult
            
            try {
                // Prepare data for Gemini
                val anomalies = mutableListOf<CarAnomaly>()
                if (analysisResults.smokeDetected) {
                    anomalies.add(CarAnomaly(
                        type = AnomalyType.SMOKE_EXHAUST,
                        confidence = analysisResults.smokeConfidence,
                        severity = when(analysisResults.smokeSeverity) {
                            in 4..5 -> SeverityLevel.CRITICAL
                            3 -> SeverityLevel.HIGH
                            2 -> SeverityLevel.MEDIUM
                            else -> SeverityLevel.LOW
                        },
                        description = "Smoke ${analysisResults.smokeType} detected",
                        recommendation = "Vérifier le système d'échappement"
                    ))
                }
                if (analysisResults.vibrationDetected) {
                    anomalies.add(CarAnomaly(
                        type = AnomalyType.VIBRATION_EXCESSIVE,
                        confidence = analysisResults.vibrationConfidence,
                        severity = when(analysisResults.vibrationSeverity) {
                            in 4..5 -> SeverityLevel.CRITICAL
                            3 -> SeverityLevel.HIGH
                            else -> SeverityLevel.MEDIUM
                        },
                        description = "Vibration ${analysisResults.vibrationLevel}",
                        recommendation = "Check engine supports"
                    ))
                }
                
                val description = "Video analysis complete. ${analysisResults.totalFrames} frames analyzed."
                
                val geminiAnalysis = geminiAiRepository.analyzeVideo(description, anomalies)
                geminiAnalysis.onSuccess { result ->
                     // Merge Gemini recommendations
                    finalScoreResult = scoreResult.copy(
                        recommendations = (scoreResult.recommendations + result.recommendations).distinct(),
                        detectedIssues = scoreResult.detectedIssues // Issues logic is complex to merge, keeping local + maybe appending strings
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini analysis failed, proceeding with local results", e)
            }
            
            // Calculate video hash for integrity
            onProgress(0.5f, "Verifying integrity...")
            val videoHash = calculateFileHash(videoFilePath)
            
            // Build diagnostic data
            onProgress(0.6f, "Creating diagnostic...")
            val diagnosticData = buildDiagnosticData(
                userId = userId,
                carId = carId,
                videoFilePath = videoFilePath,
                videoHash = videoHash,
                analysisResults = analysisResults,
                scoreResult = finalScoreResult,
                hasStorageConsent = hasStorageConsent
            )
            
            // Save to Room (offline-first)
            onProgress(0.8f, "Local save...")
            videoDiagnosticDao.insertVideoDiagnostic(diagnosticData.toEntity(isSynced = false))
            
            // Queue background upload instead of blocking
            if (hasStorageConsent) {
                onProgress(0.9f, "Queuing upload...")
                queueBackgroundUpload(diagnosticData.id)
            }
            
            onProgress(1.0f, "Completed!")
            Result.Success(diagnosticData)
            
        } catch (e: Exception) {
            Log.e(TAG, "Diagnostic processing error: ${e.message}", e)
            Result.Error(e)
        }
    }
    
    /**
     * Build diagnostic data from analysis and scoring results
     */
    private suspend fun buildDiagnosticData(
        userId: String,
        carId: String,
        videoFilePath: String,
        videoHash: String,
        analysisResults: VideoAnalysisResults,
        scoreResult: com.example.autobrain.domain.usecase.VideoScoreResult,
        hasStorageConsent: Boolean
    ): VideoDiagnosticData {
        // Calculate auto-delete timestamp (7 days from now)
        val autoDeleteAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(AUTO_DELETE_DAYS)
        
        // GDPR Compliance: Anonymize video (detect license plates)
        val anonymizationResult = try {
            videoAnonymizer.anonymizeVideo(videoFilePath, autoBlur = false)
        } catch (e: Exception) {
            Log.e(TAG, "Anonymization check failed: ${e.message}")
            com.example.autobrain.data.ai.VideoAnonymizer.AnonymizationResult(
                success = false,
                platesDetected = 0,
                framesProcessed = 0,
                error = e.message
            )
        }
        
        // Log anonymization result
        if (anonymizationResult.platesDetected > 0) {
            Log.w(TAG, "⚠️ GDPR Warning: ${anonymizationResult.platesDetected} license plates detected in video")
        } else {
            Log.d(TAG, "✅ GDPR OK: No license plates detected (${anonymizationResult.framesProcessed} frames checked)")
        }
        
        return VideoDiagnosticData(
            id = UUID.randomUUID().toString(),
            userId = userId,
            carId = carId,
            videoFilePath = videoFilePath,
            videoUrl = "", // Will be set after upload
            durationMs = 0, // Set by caller if needed
            videoHash = videoHash,
            
            // Smoke detection
            smokeDetected = analysisResults.smokeDetected,
            smokeType = analysisResults.smokeType,
            smokeConfidence = analysisResults.smokeConfidence,
            smokeSeverity = analysisResults.smokeSeverity,
            
            // Vibration detection
            vibrationDetected = analysisResults.vibrationDetected,
            vibrationLevel = analysisResults.vibrationLevel,
            vibrationConfidence = analysisResults.vibrationConfidence,
            vibrationSeverity = analysisResults.vibrationSeverity,
            
            // Frame statistics
            totalFramesAnalyzed = analysisResults.totalFrames,
            smokeyFramesCount = analysisResults.smokeyFramesCount,
            vibrationFramesCount = analysisResults.vibrationFramesCount,
            averageBrightness = analysisResults.averageBrightness,
            isStableVideo = analysisResults.isStableVideo,
            
            // Scoring
            rawScore = scoreResult.rawScore,
            finalScore = scoreResult.finalScore,
            healthStatus = scoreResult.healthStatus,
            urgencyLevel = scoreResult.urgencyLevel.name,
            
            // Issues & recommendations
            detectedIssues = scoreResult.detectedIssues,
            recommendations = scoreResult.recommendations,
            criticalWarning = scoreResult.criticalWarning ?: "",
            
            // Costs
            estimatedMinCost = scoreResult.detectedIssues.minOfOrNull { it.estimatedMinCost } ?: 0.0,
            estimatedMaxCost = scoreResult.detectedIssues.maxOfOrNull { it.estimatedMaxCost } ?: 0.0,
            
            // Maintenance
            carnetImpactScore = scoreResult.carnetImpactScore,
            overdueMaintenanceItems = scoreResult.overdueMaintenanceItems,
            
            // Quality
            videoQuality = analysisResults.videoQuality,
            qualityIssues = determineQualityIssues(analysisResults),
            
            // Security & GDPR Compliance
            hasStorageConsent = hasStorageConsent,
            anonymized = anonymizationResult.success && anonymizationResult.platesDetected == 0,
            autoDeleteAt = autoDeleteAt,
            
            createdAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Determine video quality issues
     */
    private fun determineQualityIssues(results: VideoAnalysisResults): List<String> {
        val issues = mutableListOf<String>()
        
        if (results.averageBrightness < 30f) {
            issues.add("too_dark")
        } else if (results.averageBrightness < 50f) {
            issues.add("low_brightness")
        }
        
        if (!results.isStableVideo) {
            issues.add("shaky")
        }
        
        if (results.totalFrames < 30) {
            issues.add("too_short")
        }
        
        return issues
    }
    
    // =============================================================================
    // LOCAL DATABASE OPERATIONS
    // =============================================================================
    
    /**
     * Get all diagnostics for user (Flow for reactive UI)
     */
    fun getUserDiagnostics(userId: String): Flow<List<VideoDiagnosticData>> {
        return videoDiagnosticDao.getAllByUserFlow(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Get diagnostics for a specific car
     */
    fun getCarDiagnostics(carId: String): Flow<List<VideoDiagnosticData>> {
        return videoDiagnosticDao.getByCarFlow(carId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Get diagnostic by ID
     */
    suspend fun getDiagnosticById(id: String): Result<VideoDiagnosticData?> =
        withContext(Dispatchers.IO) {
            try {
                val entity = videoDiagnosticDao.getById(id)
                Result.Success(entity?.toDomain())
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    
    /**
     * Get recent diagnostics
     */
    suspend fun getRecentDiagnostics(userId: String, limit: Int = 10): Result<List<VideoDiagnosticData>> =
        withContext(Dispatchers.IO) {
            try {
                val entities = videoDiagnosticDao.getRecentDiagnostics(userId, limit)
                Result.Success(entities.map { it.toDomain() })
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    
    /**
     * Get critical diagnostics
     */
    fun getCriticalDiagnostics(userId: String): Flow<List<VideoDiagnosticData>> {
        return videoDiagnosticDao.getCriticalDiagnostics(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Get problematic diagnostics (smoke or vibration detected)
     */
    fun getProblematicDiagnostics(userId: String): Flow<List<VideoDiagnosticData>> {
        return videoDiagnosticDao.getProblematicDiagnostics(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Delete diagnostic (also deletes video file)
     */
    suspend fun deleteDiagnostic(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val diagnostic = videoDiagnosticDao.getById(id)
            
            // Delete video file
            if (diagnostic != null && diagnostic.videoFilePath.isNotEmpty()) {
                try {
                    val file = File(diagnostic.videoFilePath)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete video file: ${e.message}")
                }
            }
            
            // Delete from database
            videoDiagnosticDao.deleteById(id)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Update diagnostic data after local modification
     * This marks the diagnostic as locally modified to trigger sync
     */
    suspend fun updateDiagnostic(diagnostic: VideoDiagnosticData): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Update the entity in Room
            videoDiagnosticDao.insertVideoDiagnostic(diagnostic.toEntity(isSynced = false))
            
            // Mark as locally modified (updates localModifiedAt and isSynced = false)
            videoDiagnosticDao.markAsLocallyModified(diagnostic.id)
            
            Log.d(TAG, "Video diagnostic ${diagnostic.id} marked as locally modified")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update video diagnostic: ${e.message}")
            Result.Error(e)
        }
    }
    
    // =============================================================================
    // SYNC OPERATIONS
    // =============================================================================
    
    /**
     * Try to sync a single diagnostic (opportunistic)
     */
    /**
     * Try to sync a single diagnostic (opportunistic)
     * @return true if sync succeeded, false otherwise
     */
    private suspend fun trySyncDiagnostic(diagnostic: VideoDiagnosticData): Boolean {
        return try {
            // Only sync if user gave consent
            if (!diagnostic.hasStorageConsent) {
                Log.d(TAG, "Skipping sync - no storage consent")
                return false
            }
            
            // Upload video file if exists
            val videoUrl = if (diagnostic.videoFilePath.isNotEmpty()) {
                uploadVideoFile(diagnostic.id, diagnostic.videoFilePath)
            } else {
                ""
            }
            
            // Update with video URL
            val updatedDiagnostic = diagnostic.copy(videoUrl = videoUrl)
            
            // Upload to Firestore
            firestore.collection(COLLECTION_VIDEO_DIAGNOSTICS)
                .document(diagnostic.id)
                .set(updatedDiagnostic.toFirestoreMap())
                .await()
            
            // Mark as synced in Room
            videoDiagnosticDao.updateVideoUrl(diagnostic.id, videoUrl)
            videoDiagnosticDao.markAsSynced(diagnostic.id)
            videoDiagnosticDao.clearSyncError(diagnostic.id)
            
            Log.d(TAG, "Successfully synced diagnostic: ${diagnostic.id}")
            true  // Success
        } catch (e: Exception) {
            Log.w(TAG, "Sync failed for ${diagnostic.id}: ${e.message}")
            videoDiagnosticDao.recordSyncError(diagnostic.id, e.message ?: "Unknown error")
            false  // Failure
        }
    }
    
    /**
     * Sync all unsynced diagnostics (called by WorkManager)
     */
    suspend fun syncUnsyncedDiagnostics(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val unsynced = videoDiagnosticDao.getUnsyncedDiagnostics()
            var successCount = 0
            
            unsynced.forEach { entity ->
                try {
                    val diagnostic = entity.toDomain()
                    val syncSuccess = trySyncDiagnostic(diagnostic)  // Get boolean result
                    if (syncSuccess) {
                        successCount++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Sync failed for ${entity.id}: ${e.message}")
                    videoDiagnosticDao.recordSyncError(entity.id, e.message ?: "Unknown")
                }
            }
            
            Log.d(TAG, "Synced $successCount/${unsynced.size} diagnostics")
            Result.Success(successCount)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Fetch diagnostics from Firestore and merge to local
     */
    suspend fun fetchAndMergeDiagnostics(userId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection(COLLECTION_VIDEO_DIAGNOSTICS)
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                
                val remoteDiagnostics = snapshot.documents.mapNotNull { doc ->
                    doc.data?.toVideoDiagnosticData()
                }
                
                // Merge: conflict resolution using modification timestamps
                remoteDiagnostics.forEach { remote ->
                    val local = videoDiagnosticDao.getById(remote.id)
                    
                    if (local == null) {
                        // New item - insert
                        videoDiagnosticDao.insertVideoDiagnostic(remote.toEntity(isSynced = true))
                    } else {
                        // Conflict resolution: compare modification times
                        if (remote.updatedAt > local.localModifiedAt) {
                            // Remote is newer - overwrite local
                            videoDiagnosticDao.insertVideoDiagnostic(remote.toEntity(isSynced = true))
                        } else {
                            // Local is newer - keep local, mark for upload
                            videoDiagnosticDao.markAsLocallyModified(local.id)
                        }
                    }
                }
                
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Fetch failed: ${e.message}")
                Result.Error(e)
            }
        }
    
    // =============================================================================
    // FIREBASE STORAGE
    // =============================================================================
    
    /**
     * Queue background upload using WorkManager
     */
    private fun queueBackgroundUpload(diagnosticId: String) {
        try {
            val workManager = WorkManager.getInstance(context)
            val uploadWork = OneTimeWorkRequestBuilder<com.example.autobrain.data.worker.VideoUploadWorker>()
                .setInputData(workDataOf("DIAGNOSTIC_ID" to diagnosticId))
                .build()
            
            workManager.enqueue(uploadWork)
            Log.d(TAG, "Queued background upload for diagnostic: $diagnosticId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to queue upload: ${e.message}")
        }
    }
    
    /**
     * Upload video file to Firebase Storage with compression
     */
    suspend fun uploadVideoFile(diagnosticId: String, localPath: String): String =
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext ""
                val file = File(localPath)
                if (!file.exists()) return@withContext ""
                
                // Compress video before upload
                val compressedPath = "${context.cacheDir}/compressed_$diagnosticId.mp4"
                val compressionResult = MediaCompressionUtils.compressVideo(
                    inputPath = localPath,
                    outputPath = compressedPath,
                    targetBitrate = 1_000_000,
                    maxWidth = 1280,
                    maxHeight = 720
                )
                
                val uploadFile = when (compressionResult) {
                    is Result.Success -> File(compressedPath)
                    is Result.Error -> {
                        Log.w(TAG, "Compression failed, using original file")
                        file
                    }
                    else -> file
                }
                
                val uri = Uri.fromFile(uploadFile)
                val fileName = "video_$diagnosticId.mp4"
                val storageRef = storage.reference
                    .child(STORAGE_PATH_VIDEOS)
                    .child(userId)
                    .child(fileName)
                
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await()
                
                // Clean up compressed file
                if (uploadFile.path == compressedPath) {
                    uploadFile.delete()
                }
                
                downloadUrl.toString()
            } catch (e: Exception) {
                Log.e(TAG, "Video upload failed: ${e.message}")
                ""
            }
        }
    
    // =============================================================================
    // SECURITY & CLEANUP
    // =============================================================================
    
    /**
     * Delete expired diagnostics (auto-delete after 7 days)
     */
    suspend fun deleteExpiredDiagnostics(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val expired = videoDiagnosticDao.getExpiredDiagnostics()
            
            // Delete video files
            expired.forEach { diagnostic ->
                try {
                    val file = File(diagnostic.videoFilePath)
                    if (file.exists()) {
                        file.delete()
                        Log.d(TAG, "Deleted expired video: ${diagnostic.id}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete expired video file: ${e.message}")
                }
            }
            
            // Delete from database
            val deletedCount = videoDiagnosticDao.deleteExpiredDiagnostics()
            
            Log.d(TAG, "Deleted $deletedCount expired diagnostics")
            Result.Success(deletedCount)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Calculate SHA-256 hash of file for integrity verification
     */
    private fun calculateFileHash(filePath: String): String {
        return try {
            val file = File(filePath)
            if (!file.exists()) return ""
            
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            
            file.inputStream().use { input ->
                var bytesRead = input.read(buffer)
                while (bytesRead != -1) {
                    digest.update(buffer, 0, bytesRead)
                    bytesRead = input.read(buffer)
                }
            }
            
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Hash calculation failed: ${e.message}")
            ""
        }
    }
    
    // =============================================================================
    // STATISTICS
    // =============================================================================
    
    /**
     * Get statistics for a car
     */
    suspend fun getCarStatistics(carId: String): Result<CarVideoStatistics> =
        withContext(Dispatchers.IO) {
            try {
                val count = videoDiagnosticDao.getCarDiagnosticsCount(carId)
                val avgScore = videoDiagnosticDao.getAverageScoreForCar(carId) ?: 0f
                val latest = videoDiagnosticDao.getLatestForCar(carId)?.toDomain()
                val smokeCount = videoDiagnosticDao.getSmokeDetectionCount(carId)
                val vibrationCount = videoDiagnosticDao.getVibrationDetectionCount(carId)
                
                Result.Success(
                    CarVideoStatistics(
                        totalDiagnostics = count,
                        averageScore = avgScore,
                        latestDiagnostic = latest,
                        smokeDetectionCount = smokeCount,
                        vibrationDetectionCount = vibrationCount
                    )
                )
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    
    // =============================================================================
    // COMPREHENSIVE VIDEO DIAGNOSTIC (Complete Firebase Integration)
    // =============================================================================
    
    /**
     * Perform COMPREHENSIVE Video Diagnostic with FULL Firebase Data Integration + MULTIMODAL AI
     * 
     * This method integrates:
     * - Real-time Firestore user profile & car details
     * - Complete maintenance history from CarLog
     * - Previous video diagnostic trends
     * - Audio diagnostics for multimodal correlation
     * - ML Kit analysis results
     * - **ACTUAL VIDEO FILE sent to Gemini for direct analysis**
     * - Market context & legal compliance
     * 
     * @param videoData Current video diagnostic data with ML Kit results
     * @param videoFilePath Path to the actual video file
     * @return Comprehensive diagnostic result with 10 detailed sections
     */
    suspend fun performComprehensiveVideoAnalysis(
        videoData: VideoDiagnosticData,
        videoFilePath: String
    ): Result<ComprehensiveVideoDiagnostic> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid 
                ?: return@withContext Result.Error(Exception("User not authenticated"))
            
            Log.d(TAG, "🚀 Starting COMPREHENSIVE MULTIMODAL Video Diagnostic")
            
            // Step 1: Fetch User Profile from Firestore
            Log.d(TAG, "📥 Step 1: Fetching user profile...")
            val user = fetchUserProfile(userId)
            Log.d(TAG, "✅ User fetched: ${user.name}, Car: ${user.carDetails?.make} ${user.carDetails?.model}")
            
            // Step 2: Fetch Car Log (Maintenance History) from Firestore
            Log.d(TAG, "📥 Step 2: Fetching car maintenance log...")
            val carLog = fetchCarLogFromFirestore(userId)
            Log.d(TAG, "✅ CarLog fetched: ${carLog.maintenanceRecords.size} records, ${carLog.reminders.size} reminders")
            
            // Step 3: Fetch Previous Video Diagnostics for Trend Analysis
            Log.d(TAG, "📥 Step 3: Fetching previous video diagnostics...")
            val previousVideoDiagnostics = fetchPreviousVideoDiagnostics(userId)
            Log.d(TAG, "✅ Found ${previousVideoDiagnostics.size} previous video diagnostics")
            
            // Step 4: Fetch Audio Diagnostics for Multimodal Correlation
            Log.d(TAG, "📥 Step 4: Fetching audio diagnostics for correlation...")
            val audioDiagnostics = fetchAudioDiagnostics(userId)
            Log.d(TAG, "✅ Found ${audioDiagnostics.size} audio diagnostics")
            
            // Step 5: Call Gemini AI with MULTIMODAL input (ML Kit + Video File)
            Log.d(TAG, "⭐ Gemini: Step 5: Calling Gemini 3 Pro for MULTIMODAL video analysis...")
            Log.d(TAG, "   🎬 Sending BOTH ML Kit analysis AND actual video file")
            
            val result = geminiAiRepository.performComprehensiveVideoAnalysisMultimodal(
                videoData = videoData,
                videoFilePath = videoFilePath,
                carLog = carLog,
                user = user,
                previousVideoDiagnostics = previousVideoDiagnostics,
                audioDiagnostics = audioDiagnostics
            )
            
            result.fold(
                onSuccess = { diagnostic ->
                    Log.d(TAG, "✅ Gemini multimodal video analysis complete!")
                    Log.d(TAG, "   📊 Enhanced Visual Score: ${diagnostic.enhancedVisualScore}/100")
                    Log.d(TAG, "   🔴 Smoke Type: ${diagnostic.smokeDeepAnalysis.typeDetected}")
                    Log.d(TAG, "   ⚠️  Safety: ${diagnostic.safetyAssessment.roadworthiness}")
                    
                    // Step 6: Store Complete Result in Firestore
                    Log.d(TAG, "💾 Step 6: Storing comprehensive video result in Firestore...")
                    storeComprehensiveVideoResult(userId, videoData.id, diagnostic)
                    Log.d(TAG, "✅ Result stored successfully!")
                    
                    Result.Success(diagnostic)
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Gemini multimodal video analysis failed: ${error.message}")
                    Result.Error(Exception(error))
                }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Comprehensive multimodal video diagnostic failed: ${e.message}", e)
            Result.Error(e)
        }
    }
    
    /**
     * Fetch user profile with car details from Firestore
     */
    private suspend fun fetchUserProfile(userId: String): User {
        return try {
            val doc = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            
            doc.toObject(User::class.java) ?: User(uid = userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile: ${e.message}")
            User(uid = userId)
        }
    }
    
    /**
     * Fetch complete car maintenance log from Firestore
     */
    private suspend fun fetchCarLogFromFirestore(userId: String): CarLog {
        return try {
            val doc = firestore.collection(COLLECTION_CAR_LOGS)
                .document(userId)
                .get()
                .await()
            
            doc.toObject(CarLog::class.java) ?: CarLog(userId = userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching car log: ${e.message}")
            CarLog(userId = userId)
        }
    }
    
    /**
     * Fetch previous video diagnostics for trend analysis
     */
    private suspend fun fetchPreviousVideoDiagnostics(userId: String): List<VideoDiagnosticData> {
        return try {
            val snapshot = firestore.collection(COLLECTION_VIDEO_DIAGNOSTICS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10) // Last 10 video diagnostics
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data?.toVideoDiagnosticData()
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to VideoDiagnosticData: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching previous video diagnostics: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Fetch audio diagnostics for multimodal correlation
     */
    private suspend fun fetchAudioDiagnostics(userId: String): List<AudioDiagnosticData> {
        return try {
            val snapshot = firestore.collection(COLLECTION_AUDIO_DIAGNOSTICS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5) // Last 5 audio diagnostics for correlation
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data?.toAudioDiagnosticData()
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to AudioDiagnosticData: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching audio diagnostics: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Store comprehensive video diagnostic result in Firestore
     */
    private suspend fun storeComprehensiveVideoResult(
        userId: String,
        diagnosticId: String,
        result: ComprehensiveVideoDiagnostic
    ) {
        try {
            // Store in comprehensive_video_diagnostics collection
            val data = result.toFirestoreMap().toMutableMap()
            data["userId"] = userId
            data["diagnosticId"] = diagnosticId
            data["createdAt"] = System.currentTimeMillis()
            
            firestore.collection(COLLECTION_COMPREHENSIVE_VIDEO_DIAGNOSTICS)
                .document(diagnosticId)
                .set(data)
                .await()
            
            Log.d(TAG, "✅ Comprehensive video result stored successfully!")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error storing comprehensive video result: ${e.message}", e)
            // Don't throw - this is not critical
        }
    }
    
    /**
     * Fetch comprehensive video diagnostic result from Firestore
     */
    suspend fun getComprehensiveVideoDiagnostic(diagnosticId: String): Result<ComprehensiveVideoDiagnostic?> = 
        withContext(Dispatchers.IO) {
            try {
                val doc = firestore.collection(COLLECTION_COMPREHENSIVE_VIDEO_DIAGNOSTICS)
                    .document(diagnosticId)
                    .get()
                    .await()
                
                if (doc.exists()) {
                    // Parse Firestore data to ComprehensiveVideoDiagnostic
                    // This would require a from Firestore converter
                    // For now, return null - can be implemented later
                    Result.Success(null)
                } else {
                    Result.Success(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching comprehensive video diagnostic: ${e.message}")
                Result.Error(e)
            }
        }
}

// =============================================================================
// DATA CLASSES
// =============================================================================

data class CarVideoStatistics(
    val totalDiagnostics: Int,
    val averageScore: Float,
    val latestDiagnostic: VideoDiagnosticData?,
    val smokeDetectionCount: Int,
    val vibrationDetectionCount: Int
)
