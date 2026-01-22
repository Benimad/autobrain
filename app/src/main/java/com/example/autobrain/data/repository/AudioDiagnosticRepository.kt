package com.example.autobrain.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.ai.AudioClassification
import com.example.autobrain.data.ai.ClassificationResult
import com.example.autobrain.data.ai.ComprehensiveAudioDiagnostic
import com.example.autobrain.data.ai.TfliteAudioClassifier
import com.example.autobrain.data.ai.GeminiAiRepository
import com.example.autobrain.data.ai.toFirestoreMap
import com.example.autobrain.data.local.dao.AudioDiagnosticDao
import com.example.autobrain.data.local.dao.CarLogDao
import com.example.autobrain.data.local.entity.AudioDiagnosticData
import com.example.autobrain.data.local.entity.IssueData
import com.example.autobrain.data.local.entity.toAudioDiagnosticData
import com.example.autobrain.data.local.entity.toEntity
import com.example.autobrain.data.local.entity.toFirestoreMap
import com.example.autobrain.data.local.entity.toDomain
import com.example.autobrain.domain.model.CarLog
import com.example.autobrain.domain.model.User
import com.example.autobrain.domain.usecase.AudioScoreResult
import com.example.autobrain.domain.usecase.AudioScoringUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.autobrain.core.utils.MediaCompressionUtils
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Audio Diagnostic Repository - Offline-First Architecture
 * 
 * Features:
 * - 100% offline operation for diagnosis
 * - Local Room database as source of truth
 * - Background sync to Firestore when online
 * - Audio file upload to Firebase Storage
 * - Conflict resolution by timestamp
 */
@Singleton
class AudioDiagnosticRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioClassifier: TfliteAudioClassifier,
    private val scoringUseCase: AudioScoringUseCase,
    private val audioDiagnosticDao: AudioDiagnosticDao,
    private val carLogDao: CarLogDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val geminiAiRepository: GeminiAiRepository
) {
    private val TAG = "AudioDiagnosticRepo"
    
    companion object {
        private const val COLLECTION_AUDIO_DIAGNOSTICS = "audio_diagnostics"
        private const val COLLECTION_COMPREHENSIVE_DIAGNOSTICS = "comprehensive_diagnostics"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_CAR_LOGS = "car_logs"
        private const val STORAGE_PATH_AUDIO = "audio_diagnostics"
    }
    
    // =============================================================================
    // MAIN DIAGNOSTIC FLOW
    // =============================================================================
    
    /**
     * Complete audio diagnostic flow:
     * 1. Verify permissions & car profile
     * 2. Record & classify audio
     * 3. Calculate smart score
     * 4. Save to Room
     * 5. Schedule background sync
     */
    suspend fun performAudioDiagnostic(
        carId: String,
        durationMs: Long = 12000L,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Result<AudioDiagnosticData> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.Error(Exception("User not authenticated"))
            
            // Get car log for maintenance integration
            val carLog = getCarLog(carId)
            
            // Step 1: Record and classify audio
            onProgress(0.1f, "Initialisation du micro...")
            val classificationResult = audioClassifier.recordAndClassify(
                durationMs = durationMs,
                onProgress = { progress, status ->
                    onProgress(0.1f + (progress * 0.6f), status)
                }
            )
            
            // Handle classification result
            when (classificationResult) {
                is ClassificationResult.Error -> {
                    return@withContext Result.Error(Exception(classificationResult.message))
                }
                is ClassificationResult.PoorQuality -> {
                    return@withContext Result.Error(Exception(classificationResult.message))
                }
                is ClassificationResult.Ambiguous -> {
                    return@withContext Result.Error(Exception(classificationResult.message))
                }
                is ClassificationResult.Success -> {
                    // Continue with scoring
                }
            }
            
            val successResult = classificationResult as ClassificationResult.Success
            
            // Step 2: Calculate smart score
            onProgress(0.7f, "Calcul du score...")
            val scoreResult = scoringUseCase.calculateScore(
                classifications = successResult.classifications,
                carLog = carLog
            )
            
            // Step 2.5: Gemini Analysis (Smart Audio)
            onProgress(0.8f, "Analyse IA Gemini...")
            var finalScoreResult = scoreResult
            
            try {
                // Only call Gemini if we have detections
                if (successResult.classifications.isNotEmpty()) {
                    val geminiResult = geminiAiRepository.analyzeAudio(successResult.classifications)
                    geminiResult.fold(
                        onSuccess = { analysis ->
                            // Merge Gemini insights into the result
                            // We update the scoreResult (which holds recommendations)
                            finalScoreResult = scoreResult.copy(
                                recommendations = (scoreResult.recommendations + analysis.recommendations).distinct(),
                                detectedIssues = scoreResult.detectedIssues // Issues logic could be updated too if Gemini returns specific issues
                            )
                            Log.d(TAG, "Gemini analysis merged: ${analysis.mainIssue}")
                        },
                        onFailure = { e ->
                            Log.w(TAG, "Gemini analysis failed: ${e.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini integration error", e)
            }
            
            // Step 3: Build diagnostic data
            val diagnosticData = buildDiagnosticData(
                userId = userId,
                carId = carId,
                classificationResult = successResult,
                scoreResult = finalScoreResult
            )
            
            // Step 4: Save to Room (offline-first)
            onProgress(0.9f, "Local save...")
            audioDiagnosticDao.insertAudioDiagnostic(diagnosticData.toEntity(isSynced = false))
            
            // Step 5: Queue background upload instead of blocking
            onProgress(0.95f, "Queuing upload...")
            queueBackgroundUpload(diagnosticData.id)
            
            onProgress(1.0f, "Terminé!")
            Result.Success(diagnosticData)
            
        } catch (e: Exception) {
            Log.e(TAG, "Diagnostic error: ${e.message}", e)
            Result.Error(e)
        }
    }
    
    /**
     * Build diagnostic data from classification and scoring results
     */
    private fun buildDiagnosticData(
        userId: String,
        carId: String,
        classificationResult: ClassificationResult.Success,
        scoreResult: AudioScoreResult
    ): AudioDiagnosticData {
        val topClassification = classificationResult.classifications.firstOrNull()
        
        return AudioDiagnosticData(
            id = UUID.randomUUID().toString(),
            userId = userId,
            carId = carId,
            audioFilePath = classificationResult.audioFilePath,
            audioUrl = "", // Will be set after upload
            durationMs = classificationResult.durationMs,
            topSoundLabel = topClassification?.label ?: "unknown",
            topSoundConfidence = topClassification?.confidence ?: 0f,
            allDetectedSounds = classificationResult.classifications.associate {
                it.label to it.confidence
            },
            rawScore = scoreResult.rawScore,
            normalizedScore = scoreResult.normalizedScore,
            healthStatus = scoreResult.healthStatus,
            urgencyLevel = scoreResult.urgencyLevel.name,
            detectedIssues = scoreResult.detectedIssues.map { issue ->
                IssueData(
                    soundType = issue.soundType,
                    confidence = issue.confidence,
                    severity = issue.severity.name,
                    description = issue.description,
                    minCost = issue.estimatedCost.minCost,
                    maxCost = issue.estimatedCost.maxCost
                )
            },
            recommendations = scoreResult.recommendations,
            criticalWarning = scoreResult.criticalWarning ?: "",
            minRepairCost = scoreResult.detectedIssues.minOfOrNull { it.estimatedCost.minCost } ?: 0.0,
            maxRepairCost = scoreResult.detectedIssues.maxOfOrNull { it.estimatedCost.maxCost } ?: 0.0,
            maintenancePenalty = 0f, // Can be extracted from scoreResult if needed
            overdueServices = emptyList(),
            createdAt = System.currentTimeMillis()
        )
    }
    
    // =============================================================================
    // LOCAL DATABASE OPERATIONS
    // =============================================================================
    
    /**
     * Get all diagnostics for user (Flow for reactive UI)
     */
    fun getUserDiagnostics(userId: String): Flow<List<AudioDiagnosticData>> {
        return audioDiagnosticDao.getAllByUserFlow(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Get diagnostics for a specific car
     */
    fun getCarDiagnostics(carId: String): Flow<List<AudioDiagnosticData>> {
        return audioDiagnosticDao.getByCarFlow(carId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Get diagnostic by ID
     */
    suspend fun getDiagnosticById(id: String): Result<AudioDiagnosticData?> =
        withContext(Dispatchers.IO) {
            try {
                val entity = audioDiagnosticDao.getById(id)
                Result.Success(entity?.toDomain())
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    
    /**
     * Get recent diagnostics
     */
    suspend fun getRecentDiagnostics(userId: String, limit: Int = 10): Result<List<AudioDiagnosticData>> =
        withContext(Dispatchers.IO) {
            try {
                val entities = audioDiagnosticDao.getRecentDiagnostics(userId, limit)
                Result.Success(entities.map { it.toDomain() })
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    
    /**
     * Get critical diagnostics
     */
    fun getCriticalDiagnostics(userId: String): Flow<List<AudioDiagnosticData>> {
        return audioDiagnosticDao.getCriticalDiagnostics(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * Delete diagnostic
     */
    suspend fun deleteDiagnostic(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            audioDiagnosticDao.deleteById(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Update diagnostic data after local modification
     * This marks the diagnostic as locally modified to trigger sync
     */
    suspend fun updateDiagnostic(diagnostic: AudioDiagnosticData): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Update the entity in Room
            audioDiagnosticDao.insertAudioDiagnostic(diagnostic.toEntity(isSynced = false))
            
            // Mark as locally modified (updates localModifiedAt and isSynced = false)
            audioDiagnosticDao.markAsLocallyModified(diagnostic.id)
            
            Log.d(TAG, "Diagnostic ${diagnostic.id} marked as locally modified")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update diagnostic: ${e.message}")
            Result.Error(e)
        }
    }
    
    // =============================================================================
    // SYNC OPERATIONS
    // =============================================================================
    
    /**
     * Try to sync a single diagnostic (opportunistic)
     * @return true if sync succeeded, false otherwise
     */
    private suspend fun trySyncDiagnostic(diagnostic: AudioDiagnosticData): Boolean {
        return try {
            // Upload audio file if exists
            val audioUrl = if (diagnostic.audioFilePath.isNotEmpty()) {
                uploadAudioFile(diagnostic.id, diagnostic.audioFilePath)
            } else {
                ""
            }
            
            // Update with audio URL
            val updatedDiagnostic = diagnostic.copy(audioUrl = audioUrl)
            
            // Upload to Firestore
            firestore.collection(COLLECTION_AUDIO_DIAGNOSTICS)
                .document(diagnostic.id)
                .set(updatedDiagnostic.toFirestoreMap())
                .await()
            
            // Mark as synced in Room
            audioDiagnosticDao.updateAudioUrl(diagnostic.id, audioUrl)
            audioDiagnosticDao.markAsSynced(diagnostic.id)
            audioDiagnosticDao.clearSyncError(diagnostic.id)
            
            Log.d(TAG, "Successfully synced diagnostic: ${diagnostic.id}")
            true  // Success
        } catch (e: Exception) {
            Log.w(TAG, "Sync failed for ${diagnostic.id}: ${e.message}")
            audioDiagnosticDao.recordSyncError(diagnostic.id, e.message ?: "Unknown error")
            false  // Failure
        }
    }
    
    /**
     * Sync all unsynced diagnostics (called by WorkManager)
     */
    suspend fun syncUnsyncedDiagnostics(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val unsynced = audioDiagnosticDao.getUnsyncedDiagnostics()
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
                    audioDiagnosticDao.recordSyncError(entity.id, e.message ?: "Unknown")
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
                val snapshot = firestore.collection(COLLECTION_AUDIO_DIAGNOSTICS)
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                
                val remoteDiagnostics = snapshot.documents.mapNotNull { doc ->
                    doc.data?.toAudioDiagnosticData()
                }
                
                // Merge: conflict resolution using modification timestamps
                remoteDiagnostics.forEach { remote ->
                    val local = audioDiagnosticDao.getById(remote.id)
                    
                    if (local == null) {
                        // New item - insert
                        audioDiagnosticDao.insertAudioDiagnostic(remote.toEntity(isSynced = true))
                    } else {
                        // Conflict resolution: compare modification times
                        if (remote.updatedAt > local.localModifiedAt) {
                            // Remote is newer - overwrite local
                            audioDiagnosticDao.insertAudioDiagnostic(remote.toEntity(isSynced = true))
                        } else {
                            // Local is newer - keep local, mark for upload
                            audioDiagnosticDao.markAsLocallyModified(local.id)
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
            val uploadWork = OneTimeWorkRequestBuilder<com.example.autobrain.data.worker.AudioUploadWorker>()
                .setInputData(workDataOf("DIAGNOSTIC_ID" to diagnosticId))
                .build()
            
            workManager.enqueue(uploadWork)
            Log.d(TAG, "Queued background upload for diagnostic: $diagnosticId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to queue upload: ${e.message}")
        }
    }
    
    /**
     * Upload audio file to Firebase Storage with compression
     */
    suspend fun uploadAudioFile(diagnosticId: String, localPath: String): String =
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext ""
                val file = File(localPath)
                if (!file.exists()) return@withContext ""
                
                // Compress audio before upload
                val compressedPath = "${context.cacheDir}/compressed_$diagnosticId.m4a"
                val compressionResult = MediaCompressionUtils.compressAudio(
                    inputPath = localPath,
                    outputPath = compressedPath,
                    targetBitrate = 64_000,
                    sampleRate = 16000
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
                val fileName = "audio_$diagnosticId${if (uploadFile == file) ".pcm" else ".m4a"}"
                val storageRef = storage.reference
                    .child(STORAGE_PATH_AUDIO)
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
                Log.e(TAG, "Audio upload failed: ${e.message}")
                ""
            }
        }
    
    // =============================================================================
    // HELPERS
    // =============================================================================
    
    /**
     * Get car log for maintenance integration
     */
    private suspend fun getCarLog(carId: String): CarLog? = withContext(Dispatchers.IO) {
        try {
            // For now, return null - car log integration can be added later
            // TODO: Implement proper CarLog fetching when needed
            null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get car log: ${e.message}")
            null
        }
    }
    
    /**
     * Get statistics for a car
     */
    suspend fun getCarStatistics(carId: String): Result<CarAudioStatistics> =
        withContext(Dispatchers.IO) {
            try {
                val count = audioDiagnosticDao.getCarDiagnosticsCount(carId)
                val avgScore = audioDiagnosticDao.getAverageScoreForCar(carId) ?: 0f
                val latest = audioDiagnosticDao.getLatestForCar(carId)?.toDomain()
                
                Result.Success(
                    CarAudioStatistics(
                        totalDiagnostics = count,
                        averageScore = avgScore,
                        latestDiagnostic = latest
                    )
                )
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    
    // =============================================================================
    // COMPREHENSIVE AUDIO DIAGNOSTIC (Complete Firebase Integration)
    // =============================================================================
    
    /**
     * Perform COMPREHENSIVE Audio Diagnostic with FULL Firebase Data Integration
     * 
     * This method integrates:
     * - Real-time Firestore user profile & car details
     * - Complete maintenance history from CarLog
     * - Previous diagnostic trends
     * - Market context & legal compliance
     * 
     * @param audioData Current audio diagnostic data with TFLite results
     * @return Comprehensive diagnostic result with 11 detailed sections
     */
    suspend fun performComprehensiveAudioAnalysis(
        audioData: AudioDiagnosticData
    ): Result<ComprehensiveAudioDiagnostic> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid 
                ?: return@withContext Result.Error(Exception("User not authenticated"))
            
            Log.d(TAG, "🚀 Starting COMPREHENSIVE Audio Diagnostic")
            
            // Step 1: Fetch User Profile from Firestore
            Log.d(TAG, "📥 Step 1: Fetching user profile...")
            val user = fetchUserProfile(userId)
            Log.d(TAG, "✅ User fetched: ${user.name}, Car: ${user.carDetails?.make} ${user.carDetails?.model}")
            
            // Step 2: Fetch Car Log (Maintenance History) from Firestore
            Log.d(TAG, "📥 Step 2: Fetching car maintenance log...")
            val carLog = fetchCarLogFromFirestore(userId)
            Log.d(TAG, "✅ CarLog fetched: ${carLog.maintenanceRecords.size} records, ${carLog.reminders.size} reminders")
            
            // Step 3: Fetch Previous Audio Diagnostics for Trend Analysis
            Log.d(TAG, "📥 Step 3: Fetching previous diagnostics...")
            val previousDiagnostics = fetchPreviousAudioDiagnostics(userId)
            Log.d(TAG, "✅ Found ${previousDiagnostics.size} previous diagnostics")
            
            // Step 4: Call Gemini AI with Comprehensive Prompt
            Log.d(TAG, "⭐ Gemini: Step 4: Calling Gemini 2.5 Pro for comprehensive analysis...")
            val result = geminiAiRepository.performComprehensiveAudioAnalysis(
                audioData = audioData,
                carLog = carLog,
                user = user,
                previousDiagnostics = previousDiagnostics
            )
            
            result.fold(
                onSuccess = { diagnostic ->
                    Log.d(TAG, "✅ Gemini analysis complete!")
                    Log.d(TAG, "   📊 Enhanced Health Score: ${diagnostic.enhancedHealthScore}/100")
                    Log.d(TAG, "   🔴 Primary Issue: ${diagnostic.primaryDiagnosis.issue}")
                    Log.d(TAG, "   ⚠️  Severity: ${diagnostic.primaryDiagnosis.severity}")
                    
                    // Step 5: Store Complete Result in Firestore
                    Log.d(TAG, "💾 Step 5: Storing comprehensive result in Firestore...")
                    storeComprehensiveResult(userId, audioData.id, diagnostic)
                    Log.d(TAG, "✅ Result stored successfully!")
                    
                    Result.Success(diagnostic)
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Gemini analysis failed: ${error.message}")
                    Result.Error(Exception(error))
                }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Comprehensive diagnostic failed: ${e.message}", e)
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
            User(uid = userId) // Return empty user as fallback
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
            CarLog(userId = userId) // Return empty log as fallback
        }
    }
    
    /**
     * Fetch previous audio diagnostics for trend analysis
     */
    private suspend fun fetchPreviousAudioDiagnostics(userId: String): List<AudioDiagnosticData> {
        return try {
            val snapshot = firestore.collection(COLLECTION_AUDIO_DIAGNOSTICS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10) // Last 10 diagnostics
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
            Log.e(TAG, "Error fetching previous diagnostics: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Store comprehensive diagnostic result in Firestore
     */
    private suspend fun storeComprehensiveResult(
        userId: String,
        diagnosticId: String,
        result: ComprehensiveAudioDiagnostic
    ) {
        try {
            // Store in comprehensive_diagnostics collection
            val data = result.toFirestoreMap().toMutableMap()
            data["userId"] = userId
            data["diagnosticId"] = diagnosticId
            data["createdAt"] = System.currentTimeMillis()
            
            firestore.collection(COLLECTION_COMPREHENSIVE_DIAGNOSTICS)
                .document(diagnosticId)
                .set(data)
                .await()
            
            Log.d(TAG, "✅ Comprehensive result stored successfully!")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error storing comprehensive result: ${e.message}", e)
            // Don't throw - this is not critical
        }
    }
    
    /**
     * Fetch comprehensive diagnostic result from Firestore
     */
    suspend fun getComprehensiveDiagnostic(diagnosticId: String): Result<ComprehensiveAudioDiagnostic?> = 
        withContext(Dispatchers.IO) {
            try {
                val doc = firestore.collection(COLLECTION_COMPREHENSIVE_DIAGNOSTICS)
                    .document(diagnosticId)
                    .get()
                    .await()
                
                if (doc.exists()) {
                    // Parse Firestore data to ComprehensiveAudioDiagnostic
                    // This would require a from Firestore converter
                    // For now, return null - can be implemented later
                    Result.Success(null)
                } else {
                    Result.Success(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching comprehensive diagnostic: ${e.message}")
                Result.Error(e)
            }
        }
}

// =============================================================================
// DATA CLASSES
// =============================================================================

data class CarAudioStatistics(
    val totalDiagnostics: Int,
    val averageScore: Float,
    val latestDiagnostic: AudioDiagnosticData?
)
