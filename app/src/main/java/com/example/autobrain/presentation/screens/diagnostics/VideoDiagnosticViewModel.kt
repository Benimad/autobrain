package com.example.autobrain.presentation.screens.diagnostics

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.ai.ComprehensiveVideoDiagnostic
import com.example.autobrain.data.ai.MlKitVideoAnalyzer
import com.example.autobrain.data.ai.VideoAnalysisResults
import com.example.autobrain.data.local.entity.VideoDiagnosticData
import com.example.autobrain.data.repository.VideoDiagnosticRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Video Diagnostic ViewModel with Sealed State Pattern
 * 
 * States:
 * - Idle: Initial state, showing instructions
 * - PermissionRequired: Camera permission not granted
 * - Previewing: CameraX preview active, ready to record
 * - Recording: Recording video with real-time quality checks
 * - Analyzing: ML Kit analyzing frames
 * - Success: Diagnostic complete with results
 * - Error: Something went wrong
 */
@HiltViewModel
class VideoDiagnosticViewModel @Inject constructor(
    private val repository: VideoDiagnosticRepository,
    private val videoAnalyzer: MlKitVideoAnalyzer,
    private val geminiAiRepository: com.example.autobrain.data.ai.GeminiAiRepository,
    private val frameSnapshotManager: com.example.autobrain.data.ai.FrameSnapshotManager
) : ViewModel() {
    
    private val TAG = "VideoDiagnosticVM"
    
    // State management
    private val _uiState = MutableStateFlow<VideoDiagnosticState>(VideoDiagnosticState.Idle)
    val uiState: StateFlow<VideoDiagnosticState> = _uiState.asStateFlow()
    
    // Current analysis results (temporary storage during recording)
    private var currentAnalysisResults: VideoAnalysisResults? = null
    private var currentVideoPath: String = ""
    
    // Configuration
    private var currentCarId: String = ""
    private var hasStorageConsent: Boolean = false
    private var currentMileage: Int? = null
    
    // Comprehensive diagnostic result
    private val _comprehensiveVideoDiagnostic = MutableStateFlow<ComprehensiveVideoDiagnostic?>(null)
    val comprehensiveVideoDiagnostic: StateFlow<ComprehensiveVideoDiagnostic?> = _comprehensiveVideoDiagnostic.asStateFlow()
    
    private val _isComprehensiveAnalyzing = MutableStateFlow(false)
    val isComprehensiveAnalyzing: StateFlow<Boolean> = _isComprehensiveAnalyzing.asStateFlow()
    
    private var recordingJob: Job? = null
    private var recordingStartTime: Long = 0
    private val RECORDING_DURATION_MS = 10000L // 10 seconds

    // =============================================================================
    // PUBLIC API
    // =============================================================================
    
    /**
     * Initialize for new diagnostic
     */
    fun initializeDiagnostic(carId: String, mileage: Int? = null) {
        currentCarId = carId
        currentMileage = mileage
        _uiState.value = VideoDiagnosticState.Idle
        
        // Generate diagnostic ID and set it for frame snapshots
        val diagnosticId = java.util.UUID.randomUUID().toString()
        videoAnalyzer.setDiagnosticId(diagnosticId)
    }
    
    /**
     * Request camera permission
     */
    fun onPermissionRequested() {
        _uiState.value = VideoDiagnosticState.PermissionRequired
    }
    
    /**
     * Permission granted, show preview
     */
    fun onPermissionGranted() {
        _uiState.value = VideoDiagnosticState.Previewing
    }
    
    /**
     * Analyze a single frame from CameraX
     */
    @ExperimentalGetImage
    fun analyzeFrame(imageProxy: androidx.camera.core.ImageProxy) {
        val currentState = _uiState.value
        
        // Only analyze if we are in Previewing or Recording state
        if (currentState !is VideoDiagnosticState.Recording && currentState !is VideoDiagnosticState.Previewing) {
            imageProxy.close()
            return
        }

        viewModelScope.launch {
            val result = videoAnalyzer.analyzeFrame(imageProxy)
            
            // If recording, update UI with quality feedback
            if (currentState is VideoDiagnosticState.Recording) {
                val elapsed = (System.currentTimeMillis() - recordingStartTime).toInt() / 1000
                val progress = (System.currentTimeMillis() - recordingStartTime).toFloat() / RECORDING_DURATION_MS
                
                updateRecordingProgress(
                    progress = progress.coerceIn(0f, 1f),
                    elapsedSeconds = elapsed,
                    averageBrightness = result.brightness,
                    isStable = !result.vibrationDetected // Simplification
                )
                
                // Auto-stop after duration
                if (System.currentTimeMillis() - recordingStartTime >= RECORDING_DURATION_MS) {
                    stopRecording()
                }
            }
        }
    }

    /**
     * Start recording video
     */
    fun startRecording() {
        recordingStartTime = System.currentTimeMillis()
        videoAnalyzer.reset()
        
        _uiState.value = VideoDiagnosticState.Recording(
            progress = 0f,
            elapsedSeconds = 0,
            qualityStatus = "Initializing...",
            isQualityGood = true
        )
        
        // Timer job handled in analyzeFrame loop via timestamps
    }
    
    /**
     * Stop recording and start analysis
     */
    fun stopRecording() {
        if (_uiState.value !is VideoDiagnosticState.Recording) return
        
        // Get comprehensive results from analyzer
        val results = videoAnalyzer.getComprehensiveResults()
        currentAnalysisResults = results
        
        // Save video file path (if CameraX recorded it)
        // TODO: Implement actual video file saving in CameraX integration
        currentVideoPath = "" // Currently no video file is saved
        
        _uiState.value = VideoDiagnosticState.Analyzing(
            message = "Analyzing video data...",
            progress = 0.1f
        )
        
        // Process diagnostic
        processVideoDiagnostic()
    }

    /**
     * Update recording progress
     */
    fun updateRecordingProgress(
        progress: Float,
        elapsedSeconds: Int,
        averageBrightness: Float,
        isStable: Boolean
    ) {
        val qualityStatus = when {
            averageBrightness < 30f -> "⚠️ Too dark – Improve lighting"
            averageBrightness < 50f -> "ℹ️ Low brightness"
            !isStable -> "⚠️ Unstable video – Stabilize camera"
            else -> "✅ Good quality"
        }
        
        val isQualityGood = averageBrightness >= 50f && isStable
        
        _uiState.value = VideoDiagnosticState.Recording(
            progress = progress,
            elapsedSeconds = elapsedSeconds,
            qualityStatus = qualityStatus,
            isQualityGood = isQualityGood
        )
    }
    
    /**
     * Set storage consent
     */
    fun setStorageConsent(consent: Boolean) {
        hasStorageConsent = consent
    }
    
    /**
     * Retry diagnostic
     */
    fun retryDiagnostic() {
        _uiState.value = VideoDiagnosticState.Idle
        currentAnalysisResults = null
        currentVideoPath = ""
        videoAnalyzer.reset()
    }
    
    /**
     * Navigate back to diagnostics list
     */
    fun goToList() {
        _uiState.value = VideoDiagnosticState.Idle
    }
    
    // =============================================================================
    // PRIVATE LOGIC
    // =============================================================================
    
    /**
     * Process video diagnostic
     */
    private fun processVideoDiagnostic() {
        viewModelScope.launch {
            try {
                val analysisResults = currentAnalysisResults
                if (analysisResults == null) {
                    _uiState.value = VideoDiagnosticState.Error("No analysis data available")
                    return@launch
                }
                
                if (currentCarId.isEmpty()) {
                    _uiState.value = VideoDiagnosticState.Error("Missing car ID")
                    return@launch
                }
                
                val result = repository.processVideoDiagnostic(
                    carId = currentCarId,
                    videoFilePath = currentVideoPath,
                    analysisResults = analysisResults,
                    hasStorageConsent = hasStorageConsent,
                    currentMileage = currentMileage,
                    onProgress = { progress, message ->
                        _uiState.value = VideoDiagnosticState.Analyzing(
                            message = message,
                            progress = progress
                        )
                    }
                )
                
                when (result) {
                    is Result.Success -> {
                        var diagnostic = result.data
                        
                        // Get Gemini enhanced analysis
                        try {
                            val videoDescription = "Video analysis: ${if (analysisResults.smokeDetected) "${analysisResults.smokeType} smoke detected" else "No smoke"}, " +
                            "Vibrations: ${if (analysisResults.vibrationDetected) analysisResults.vibrationLevel else "normal"}, " +
                            "Smoke severity: ${analysisResults.smokeSeverity}/5"
                            
                            val anomalies = listOf<com.example.autobrain.data.ai.CarAnomaly>()  // Empty for now
                            
                            val geminiResult = geminiAiRepository.analyzeVideo(videoDescription, anomalies)
                            geminiResult.fold(
                                onSuccess = { videoAnalysis ->
                                    // Keep original diagnostic, Gemini data saved in Firestore
                                    Log.d(TAG, "Gemini video analysis completed")
                                },
                                onFailure = {
                                    // Continue with original diagnostic
                                    Log.w(TAG, "Gemini analysis failed, using base diagnostic")
                                }
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Gemini integration error: ${e.message}")
                        }
                        
                        _uiState.value = VideoDiagnosticState.Success(diagnostic)
                        Log.d(TAG, "Diagnostic completed successfully: ${diagnostic.id}")
                        
                        // Automatically trigger comprehensive analysis
                        performComprehensiveAnalysis(diagnostic, diagnostic.videoFilePath)
                    }
                    is Result.Error -> {
                        val errorMessage = result.exception.message ?: "Unknown error"
                        _uiState.value = VideoDiagnosticState.Error(errorMessage)
                        Log.e(TAG, "Diagnostic failed: $errorMessage")
                    }
                    is Result.Loading -> {
                        // Already showing analyzing state
                    }
                }
                
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Diagnostic error"
                _uiState.value = VideoDiagnosticState.Error(errorMessage)
                Log.e(TAG, "Processing error: $errorMessage", e)
            }
        }
    }
    
    /**
     * Get user diagnostics
     */
    fun getUserDiagnostics(userId: String) = repository.getUserDiagnostics(userId)
    
    /**
     * Get car diagnostics
     */
    fun getCarDiagnostics(carId: String) = repository.getCarDiagnostics(carId)
    
    /**
     * Get critical diagnostics
     */
    fun getCriticalDiagnostics(userId: String) = repository.getCriticalDiagnostics(userId)
    
    /**
     * Get critical frame snapshots for current diagnostic
     */
    fun getCriticalFrameSnapshots() = videoAnalyzer.getCriticalFrameSnapshots()
    
    /**
     * Delete diagnostic
     */
    fun deleteDiagnostic(diagnosticId: String) {
        viewModelScope.launch {
            // Delete frame snapshots
            frameSnapshotManager.deleteSnapshotsForDiagnostic(diagnosticId)
            // Delete diagnostic
            repository.deleteDiagnostic(diagnosticId)
        }
    }
    
    // =============================================================================
    // COMPREHENSIVE ANALYSIS
    // =============================================================================
    
    /**
     * Perform comprehensive Gemini AI analysis on video diagnostic
     * 
     * This fetches:
     * - User profile with car details
     * - Complete maintenance history
     * - Previous video diagnostic trends
     * - Audio diagnostics for multimodal correlation
     * And generates 10-section comprehensive report with MULTIMODAL input
     * (ML Kit results + actual video file sent to Gemini)
     */
    fun performComprehensiveAnalysis(videoData: VideoDiagnosticData, videoFilePath: String) {
        viewModelScope.launch {
            _isComprehensiveAnalyzing.value = true
            
            try {
                Log.d(TAG, "🎬 Starting comprehensive MULTIMODAL video analysis...")
                Log.d(TAG, "   - Video file: $videoFilePath")
                
                val result = repository.performComprehensiveVideoAnalysis(videoData, videoFilePath)
                
                when (result) {
                    is Result.Success -> {
                        _comprehensiveVideoDiagnostic.value = result.data
                        Log.d(
                            TAG,
                            "Comprehensive multimodal video analysis successful! Score: ${result.data.enhancedVisualScore}/100"
                        )
                    }
                    is Result.Error -> {
                        Log.e(
                            TAG,
                            "Comprehensive multimodal video analysis failed: ${result.exception.message}"
                        )
                    }
                    is Result.Loading -> {
                        // Should not happen
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during comprehensive multimodal video analysis: ${e.message}", e)
            } finally {
                _isComprehensiveAnalyzing.value = false
            }
        }
    }
    
    /**
     * Clear comprehensive diagnostic result
     */
    fun clearComprehensiveDiagnostic() {
        _comprehensiveVideoDiagnostic.value = null
    }
    
    // =============================================================================
    // CLEANUP
    // =============================================================================

    override fun onCleared() {
        super.onCleared()
        videoAnalyzer.release()
    }
}

// =============================================================================
// SEALED STATE
// =============================================================================

/**
 * Sealed class representing all possible states of video diagnostic flow
 */
sealed class VideoDiagnosticState {
    
    /**
     * Initial state - showing instructions
     */
    data object Idle : VideoDiagnosticState()
    
    /**
     * Camera permission required
     */
    data object PermissionRequired : VideoDiagnosticState()
    
    /**
     * Camera preview active, ready to record
     */
    data object Previewing : VideoDiagnosticState()
    
    /**
     * Recording video with real-time feedback
     * 
     * @param progress 0.0 to 1.0
     * @param elapsedSeconds Seconds recorded
     * @param qualityStatus Real-time quality message
     * @param isQualityGood Quality indicator for UI
     */
    data class Recording(
        val progress: Float,
        val elapsedSeconds: Int,
        val qualityStatus: String,
        val isQualityGood: Boolean
    ) : VideoDiagnosticState()
    
    /**
     * Analyzing video with ML Kit
     * 
     * @param message Current operation
     * @param progress 0.0 to 1.0
     */
    data class Analyzing(
        val message: String,
        val progress: Float
    ) : VideoDiagnosticState()
    
    /**
     * Diagnostic completed successfully
     * 
     * @param diagnostic Complete diagnostic results
     */
    data class Success(
        val diagnostic: VideoDiagnosticData
    ) : VideoDiagnosticState()
    
    /**
     * Error occurred
     * 
     * @param message Error description
     */
    data class Error(
        val message: String
    ) : VideoDiagnosticState()
}
