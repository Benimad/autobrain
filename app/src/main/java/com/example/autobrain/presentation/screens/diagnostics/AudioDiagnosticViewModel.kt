package com.example.autobrain.presentation.screens.diagnostics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.ai.ComprehensiveAudioDiagnostic
import com.example.autobrain.data.ai.TfliteAudioClassifier
import com.example.autobrain.data.local.entity.AudioDiagnosticData
import com.example.autobrain.data.repository.AudioDiagnosticRepository
import com.example.autobrain.data.worker.AudioDiagnosticSyncWorker
import com.example.autobrain.data.ai.UrgencyLevel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Audio Diagnostic ViewModel with Sealed State Management
 * 
 * Manages complete audio diagnostic flow:
 * - Permission handling
 * - Recording & analysis
 * - Real-time progress
 * - Results display
 * - History management
 */
@HiltViewModel
class AudioDiagnosticViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioClassifier: TfliteAudioClassifier,
    private val audioDiagnosticRepository: AudioDiagnosticRepository,
    private val auth: FirebaseAuth,
    private val workManager: WorkManager,
    private val geminiAiRepository: com.example.autobrain.data.ai.GeminiAiRepository
) : ViewModel() {
    
    // State Management
    private val _uiState = MutableStateFlow<AudioDiagnosticState>(AudioDiagnosticState.Idle)
    val uiState: StateFlow<AudioDiagnosticState> = _uiState.asStateFlow()
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    
    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()
    
    // Waveform visualization
    val waveformData: StateFlow<List<Float>> = audioClassifier.waveformData
    
    // Audio quality
    val audioQuality = audioClassifier.audioQuality
    
    // Diagnostics history
    private val _diagnosticsHistory = MutableStateFlow<List<AudioDiagnosticData>>(emptyList())
    val diagnosticsHistory: StateFlow<List<AudioDiagnosticData>> = _diagnosticsHistory.asStateFlow()
    
    // Comprehensive diagnostic result
    private val _comprehensiveDiagnostic = MutableStateFlow<ComprehensiveAudioDiagnostic?>(null)
    val comprehensiveDiagnostic: StateFlow<ComprehensiveAudioDiagnostic?> = _comprehensiveDiagnostic.asStateFlow()
    
    private val _isComprehensiveAnalyzing = MutableStateFlow(false)
    val isComprehensiveAnalyzing: StateFlow<Boolean> = _isComprehensiveAnalyzing.asStateFlow()
    
    // Current car ID
    private var currentCarId: String = ""
    
    init {
        initializeClassifier()
        setupAudioSyncWorker()
    }
    
    // =============================================================================
    // INITIALIZATION
    // =============================================================================
    
    private fun initializeClassifier() {
        viewModelScope.launch {
            audioClassifier.initialize()
        }
    }
    
    private fun setupAudioSyncWorker() {
        AudioDiagnosticSyncWorker.schedule(workManager)
    }
    
    // =============================================================================
    // PERMISSION HANDLING
    // =============================================================================
    
    fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            _uiState.value = AudioDiagnosticState.Ready
        } else {
            _uiState.value = AudioDiagnosticState.Error(
                "Permission microphone requise pour analyser le son du moteur"
            )
        }
    }
    
    // =============================================================================
    // CAR PROFILE VERIFICATION
    // =============================================================================
    
    fun setCarProfile(carId: String) {
        currentCarId = carId
        loadDiagnosticsHistory(carId)
        
        if (carId.isEmpty()) {
            _uiState.value = AudioDiagnosticState.Error(
                "Veuillez d'abord configurer votre profil voiture"
            )
        } else {
            _uiState.value = AudioDiagnosticState.Ready
        }
    }
    
    private fun loadDiagnosticsHistory(carId: String) {
        viewModelScope.launch {
            audioDiagnosticRepository.getCarDiagnostics(carId)
                .collect { diagnostics ->
                    _diagnosticsHistory.value = diagnostics
                }
        }
    }
    
    // =============================================================================
    // MAIN DIAGNOSTIC FLOW
    // =============================================================================
    
    fun startDiagnostic(durationMs: Long = 12000L) {
        if (currentCarId.isEmpty()) {
            _uiState.value = AudioDiagnosticState.Error(
                "Aucune voiture sélectionnée"
            )
            return
        }
        
        if (!checkPermissions()) {
            _uiState.value = AudioDiagnosticState.Error(
                "Permission microphone requise"
            )
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = AudioDiagnosticState.Recording
                _progress.value = 0f
                
                val result = audioDiagnosticRepository.performAudioDiagnostic(
                    carId = currentCarId,
                    durationMs = durationMs,
                    onProgress = { progress, status ->
                        _progress.value = progress
                        _statusMessage.value = status
                    }
                )
                
                when (result) {
                    is Result.Success -> {
                        var diagnostic = result.data
                        
                        // Gemini analysis is now handled in the Repository
                        
                        _uiState.value = AudioDiagnosticState.Success(diagnostic)
                        
                        // Send notification if critical
                        if (diagnostic.urgencyLevel == UrgencyLevel.CRITICAL.name) {
                            sendCriticalAlert(diagnostic)
                        }
                        
                        // Trigger immediate sync
                        AudioDiagnosticSyncWorker.triggerImmediateSync(workManager)
                    }
                    is Result.Error -> {
                        _uiState.value = AudioDiagnosticState.Error(
                            result.exception.message ?: "Erreur lors du diagnostic"
                        )
                    }
                    is Result.Loading -> {
                        // Should not happen here, but handle it
                        _uiState.value = AudioDiagnosticState.Analyzing
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AudioDiagnosticState.Error(
                    e.message ?: "Erreur inconnue"
                )
            }
        }
    }
    
    fun stopRecording() {
        audioClassifier.release()
        _uiState.value = AudioDiagnosticState.Idle
    }
    
    fun resetToIdle() {
        _uiState.value = AudioDiagnosticState.Idle
        _progress.value = 0f
        _statusMessage.value = ""
    }
    
    fun resetToReady() {
        _uiState.value = AudioDiagnosticState.Ready
        _progress.value = 0f
        _statusMessage.value = ""
    }
    
    // =============================================================================
    // HISTORY & STATISTICS
    // =============================================================================
    
    fun loadUserDiagnostics() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            audioDiagnosticRepository.getUserDiagnostics(userId)
                .collect { diagnostics ->
                    _diagnosticsHistory.value = diagnostics
                }
        }
    }
    
    fun getDiagnosticById(id: String, onResult: (AudioDiagnosticData?) -> Unit) {
        viewModelScope.launch {
            when (val result = audioDiagnosticRepository.getDiagnosticById(id)) {
                is Result.Success -> onResult(result.data)
                is Result.Error -> onResult(null)
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }
    
    fun deleteDiagnostic(id: String) {
        viewModelScope.launch {
            audioDiagnosticRepository.deleteDiagnostic(id)
        }
    }
    
    fun getCarStatistics(carId: String, onResult: (CarStatistics?) -> Unit) {
        viewModelScope.launch {
            when (val result = audioDiagnosticRepository.getCarStatistics(carId)) {
                is Result.Success -> {
                    val stats = result.data
                    onResult(
                        CarStatistics(
                            totalDiagnostics = stats.totalDiagnostics,
                            averageScore = stats.averageScore.toInt(),
                            trend = calculateTrend(stats.averageScore)
                        )
                    )
                }
                is Result.Error -> onResult(null)
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }
    
    private fun calculateTrend(avgScore: Float): String {
        return when {
            avgScore >= 80 -> "Excellent état"
            avgScore >= 60 -> "Bon état"
            avgScore >= 40 -> "Nécessite attention"
            else -> "État critique"
        }
    }
    
    // =============================================================================
    // COMPREHENSIVE ANALYSIS
    // =============================================================================
    
    /**
     * Perform comprehensive Gemini AI analysis on audio diagnostic
     * 
     * This fetches:
     * - User profile with car details
     * - Complete maintenance history
     * - Previous diagnostic trends
     * And generates 11-section comprehensive report
     */
    fun performComprehensiveAnalysis(audioData: AudioDiagnosticData) {
        viewModelScope.launch {
            _isComprehensiveAnalyzing.value = true
            
            try {
                _statusMessage.value = "⭐ Gemini: Analyse complète en cours..."
                
                val result = audioDiagnosticRepository.performComprehensiveAudioAnalysis(audioData)
                
                when (result) {
                    is Result.Success -> {
                        _comprehensiveDiagnostic.value = result.data
                        _statusMessage.value = "✅ Analyse complète terminée!"
                        
                        android.util.Log.d(
                            "AudioDiagnostic",
                            "Comprehensive analysis successful! Score: ${result.data.enhancedHealthScore}/100"
                        )
                    }
                    is Result.Error -> {
                        _statusMessage.value = "❌ Erreur: ${result.exception.message}"
                        android.util.Log.e(
                            "AudioDiagnostic",
                            "Comprehensive analysis failed: ${result.exception.message}"
                        )
                    }
                    is Result.Loading -> {
                        // Should not happen
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.e("AudioDiagnostic", "Error during comprehensive analysis: ${e.message}", e)
                _statusMessage.value = "Erreur d'analyse"
            } finally {
                _isComprehensiveAnalyzing.value = false
            }
        }
    }
    
    /**
     * Clear comprehensive diagnostic result
     */
    fun clearComprehensiveDiagnostic() {
        _comprehensiveDiagnostic.value = null
    }
    
    // =============================================================================
    // NOTIFICATIONS
    // =============================================================================
    
    private fun sendCriticalAlert(diagnostic: AudioDiagnosticData) {
        // TODO: Integrate with FCM for push notifications
        // For now, just log
        android.util.Log.w(
            "AudioDiagnostic",
            "CRITICAL: Score ${diagnostic.rawScore} - ${diagnostic.criticalWarning}"
        )
    }
    
    // =============================================================================
    // CLEANUP
    // =============================================================================
    
    override fun onCleared() {
        super.onCleared()
        audioClassifier.release()
    }
}

// =============================================================================
// SEALED STATE CLASS
// =============================================================================

/**
 * Sealed class representing all possible states of audio diagnostic flow
 */
sealed class AudioDiagnosticState {
    /**
     * Initial state - no action taken
     */
    object Idle : AudioDiagnosticState()
    
    /**
     * Ready to start recording (permissions granted, car selected)
     */
    object Ready : AudioDiagnosticState()
    
    /**
     * Currently recording audio with real-time waveform
     */
    object Recording : AudioDiagnosticState()
    
    /**
     * Analyzing recorded audio
     */
    object Analyzing : AudioDiagnosticState()
    
    /**
     * Diagnostic complete with results
     */
    data class Success(val diagnostic: AudioDiagnosticData) : AudioDiagnosticState()
    
    /**
     * Error occurred during any phase
     */
    data class Error(val message: String) : AudioDiagnosticState()
}

// =============================================================================
// DATA CLASSES
// =============================================================================

data class CarStatistics(
    val totalDiagnostics: Int,
    val averageScore: Int,
    val trend: String
)
