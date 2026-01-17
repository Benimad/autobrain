package com.example.autobrain.presentation.screens.aiscore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autobrain.core.utils.Result
import com.example.autobrain.domain.model.*
import com.example.autobrain.domain.usecase.CalculateAIScoreUseCase
import com.example.autobrain.domain.usecase.MaintenanceData
import com.example.autobrain.domain.usecase.MarketData
import com.example.autobrain.domain.repository.AIScoreRepository
import com.example.autobrain.domain.repository.AuthRepository
import com.example.autobrain.data.local.entity.AIScore
import com.example.autobrain.data.repository.AudioDiagnosticRepository
import com.example.autobrain.data.repository.VideoDiagnosticRepository
import com.example.autobrain.domain.repository.CarLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIScoreUiState(
    val isLoading: Boolean = false,
    val isCalculating: Boolean = false,
    val scoreResult: AIScoreResult? = null,
    val carData: CarDisplayData? = null,
    val error: String? = null,
    val showBreakdownDialog: Boolean = false
)

data class CarDisplayData(
    val brand: String,
    val model: String,
    val year: Int,
    val fullName: String,
    val kilometers: Int,
    val imageUrl: String? = null
)

@HiltViewModel
class AIScoreViewModel @Inject constructor(
    private val calculateAIScoreUseCase: CalculateAIScoreUseCase,
    private val geminiAiRepository: com.example.autobrain.data.ai.GeminiAiRepository,
    private val aiScoreRepository: AIScoreRepository,
    private val authRepository: AuthRepository,
    private val audioDiagnosticRepository: AudioDiagnosticRepository,
    private val videoDiagnosticRepository: VideoDiagnosticRepository,
    private val carLogRepository: CarLogRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AIScoreUiState())
    val uiState: StateFlow<AIScoreUiState> = _uiState.asStateFlow()
    
    // Store diagnostic results
    private var currentEngineSoundResult: EngineSoundResult? = null
    private var currentVideoResult: VideoAnalysisResult? = null
    
    init {
        loadLatestScore()
    }
    
    /**
     * Load latest score from repository and refresh with new data if available
     */
    private fun loadLatestScore() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val userResult = authRepository.getCurrentUser()
            val user = if (userResult is Result.Success) userResult.data else null
            
            if (user != null) {
                // 1. Observe stored score
                launch {
                    aiScoreRepository.getLatestAIScore(user.uid).collect { aiScore ->
                        if (aiScore != null) {
                            val mappedResult = mapDomainToResult(aiScore)
                            _uiState.update { state ->
                                state.copy(
                                    isLoading = false,
                                    scoreResult = mappedResult,
                                    carData = CarDisplayData(
                                        brand = aiScore.carMake,
                                        model = aiScore.carModel,
                                        year = aiScore.carYear,
                                        fullName = "${aiScore.carMake} ${aiScore.carModel} • ${aiScore.carYear}",
                                        kilometers = aiScore.mileage
                                    )
                                )
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                }
                
                // 2. Fetch latest diagnostics to be ready for re-calculation
                refreshDiagnostics(user.uid)
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    private suspend fun refreshDiagnostics(userId: String) {
        // Assume single car for MVP or get from user profile
        // Here we just fetch the most recent diagnostic across all cars for the user
        // Ideally we filter by the specific car being analyzed
        
        // Fetch latest Audio Diagnostic
        val audioResult = audioDiagnosticRepository.getRecentDiagnostics(userId, 1)
        if (audioResult is Result.Success && audioResult.data.isNotEmpty()) {
            val latestAudio = audioResult.data.first()
            currentEngineSoundResult = mapAudioDiagnosticToEngineResult(latestAudio)
        }
        
        // Fetch latest Video Diagnostic
        val videoResult = videoDiagnosticRepository.getRecentDiagnostics(userId, 1)
        if (videoResult is Result.Success && videoResult.data.isNotEmpty()) {
            val latestVideo = videoResult.data.first()
            currentVideoResult = mapVideoDiagnosticToVideoResult(latestVideo)
        }
    }

    private fun mapAudioDiagnosticToEngineResult(diagnostic: com.example.autobrain.data.local.entity.AudioDiagnosticData): EngineSoundResult {
        // Best effort mapping
        return EngineSoundResult(
            mainIssue = EngineIssueType.entries.find { it.descriptionFr.equals(diagnostic.topSoundLabel, ignoreCase = true) } ?: EngineIssueType.UNKNOWN,
            confidence = diagnostic.topSoundConfidence,
            otherIssues = emptyList(),
            rawDescription = diagnostic.recommendations.joinToString("\n"),
            recordingDuration = (diagnostic.durationMs / 1000).toInt(),
            analysisTimestamp = diagnostic.createdAt
        )
    }

    private fun mapVideoDiagnosticToVideoResult(diagnostic: com.example.autobrain.data.local.entity.VideoDiagnosticData): VideoAnalysisResult {
        // Best effort mapping
        // We need to map VideoIssue (data layer) to String (domain layer expectation for otherObservations)
        val observations = diagnostic.detectedIssues.map { it.description }
        
        // Map SmokeType from String to Enum
        // Assuming SmokeType enum has matching names or descriptions
        val smokeTypeEnum = SmokeType.entries.find { it.name.equals(diagnostic.smokeType, ignoreCase = true) } ?: SmokeType.NONE
        
        return VideoAnalysisResult(
            smokeType = smokeTypeEnum,
            smokeSeverity = diagnostic.smokeSeverity,
            vibrationLevel = diagnostic.vibrationSeverity, // Use severity (int) instead of level (string) if needed, or map level
            otherObservations = observations,
            framesAnalyzed = diagnostic.totalFramesAnalyzed,
            analysisTimestamp = diagnostic.createdAt
        )
    }

    /**
     * Calculate AI Score from diagnostic results with Gemini Smart Analysis
     */
    fun calculateScore(
        carData: CarDataForAnalysis,
        maintenanceData: MaintenanceData,
        marketData: MarketData? = null,
        llmApiKey: String? = null,
        llmProvider: String = "gemini"  // Default to Gemini
    ) {
        _uiState.update { it.copy(isCalculating = true, error = null) }
        
        viewModelScope.launch {
            try {
                // Step 1: Calculate base score with Gemini
                val result = calculateAIScoreUseCase.execute(
                    carData = carData,
                    engineSoundResult = currentEngineSoundResult,
                    videoResult = currentVideoResult,
                    maintenanceData = maintenanceData,
                    marketData = marketData,
                    useGeminiEnhancement = true
                )
                
                // Step 2: Get Gemini smart analysis for enhanced insights
                val geminiCarDetails = com.example.autobrain.data.ai.CarDetails(
                    brand = carData.brand,
                    model = carData.model,
                    year = carData.year,
                    mileage = carData.currentKm,
                    fuelType = "Diesel",  // Default, can be from carData
                    transmission = "Manuelle"
                )
                
                // Convert currentEngineSoundResult to AudioAnalysisResult
                val audioResult = currentEngineSoundResult?.let { engineSound ->
                    com.example.autobrain.data.ai.AudioAnalysisResult(
                        classifications = emptyList(),  // Already processed
                        mainIssue = engineSound.mainIssue.descriptionFr,
                        possibleCauses = listOf(engineSound.rawDescription),
                        recommendations = emptyList(),
                        healthScore = (engineSound.confidence * 100).toInt()
                    )
                }
                
                // Convert currentVideoResult to VideoAnalysisResult for Gemini
                val videoResult = currentVideoResult?.let { video ->
                    com.example.autobrain.data.ai.VideoAnalysisResult(
                        detectedObjects = emptyList(),
                        anomalies = emptyList(),
                        frameCount = video.framesAnalyzed,
                        analysisTime = video.analysisTimestamp
                    )
                }
                
                // Call Gemini for smart analysis
                val geminiAnalysis = geminiAiRepository.performSmartAnalysis(
                    geminiCarDetails,
                    audioResult,
                    videoResult
                )
                
                // Merge Gemini insights with base result
                geminiAnalysis.fold(
                    onSuccess = { smartResult ->
                        val finalResult = result.copy(
                            buyerAdvice = smartResult.recommendation
                        )
                        updateStateWithResult(finalResult, carData)
                        saveScore(finalResult, carData)
                    },
                    onFailure = {
                        // If Gemini fails, still show base result
                        updateStateWithResult(result, carData)
                        saveScore(result, carData)
                    }
                )
                
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isCalculating = false,
                        error = e.message ?: "Error calculating score"
                    )
                }
            }
        }
    }

    private fun updateStateWithResult(result: AIScoreResult, carData: CarDataForAnalysis) {
        _uiState.update { state ->
            state.copy(
                isCalculating = false,
                scoreResult = result,
                carData = CarDisplayData(
                    brand = carData.brand,
                    model = carData.model,
                    year = carData.year,
                    fullName = "${carData.brand} ${carData.model} • ${carData.year}",
                    kilometers = carData.currentKm
                )
            )
        }
    }

    private suspend fun saveScore(result: AIScoreResult, carData: CarDataForAnalysis) {
        val userResult = authRepository.getCurrentUser()
        val user = if (userResult is Result.Success) userResult.data else null
        
        if (user == null) return
        
        // Map Result to Domain Entity
        val aiScoreDomain = AIScore(
            userId = user.uid,
            carId = "current_car", // Should be passed if we have multi-car support
            score = result.finalScore,
            condition = result.scoreCategory.displayName,
            riskLevel = result.riskLevel.displayName,
            engineScore = result.breakdown.technicalScore.engineSoundScore,
            transmissionScore = 0, // Not separately calculated yet
            chassisScore = 0,
            electricalScore = 0,
            bodyScore = result.breakdown.technicalScore.videoAnalysisScore, // Mapping video to body for now
            observations = result.issues.minor.map { it.title },
            recommendations = listOf(result.buyerAdvice),
            redFlags = result.issues.grave.map { it.title },
            confidence = 0.9f,
            carMake = carData.brand,
            carModel = carData.model,
            carYear = carData.year,
            mileage = carData.currentKm,
            analysisType = "FULL_SCAN",
            createdAt = System.currentTimeMillis()
        )
        
        aiScoreRepository.saveAIScore(aiScoreDomain)
    }

    private fun mapDomainToResult(domain: AIScore): AIScoreResult {
        // Reconstruct AIScoreResult from stored AIScore
        // This is a simplification as some data is lost in translation
        val category = ScoreCategory.entries.find { it.displayName == domain.condition } ?: getScoreCategoryFromScore(domain.score)
        val risk = RiskLevel.entries.find { it.displayName == domain.riskLevel } ?: getRiskLevelFromScore(domain.score)

        return AIScoreResult(
            finalScore = domain.score,
            scoreCategory = category,
            breakdown = AIScoreBreakdown(
                technicalScore = TechnicalScore(
                    engineSoundScore = domain.engineScore,
                    videoAnalysisScore = domain.bodyScore,
                    overallScore = (domain.engineScore + domain.bodyScore) / 2,
                    engineSoundResult = null,
                    videoResult = null
                ),
                maintenanceScore = MaintenanceScore(0, 0, MaintenanceStatus(StatusLevel.UNKNOWN, 0, ""), MaintenanceStatus(StatusLevel.UNKNOWN, 0, ""), MaintenanceStatus(StatusLevel.UNKNOWN, 0, ""), MileageConsistency(true, 0, 0, null, false, null)), // Placeholders
                marketScore = MarketScore(0, 0, PriceComparison(0, 0..0, PriceComparisonResult.BELOW_OR_EQUAL, 0), ModelPopularity("", false, 0)), // Placeholders
                rawWeightedScore = domain.score.toFloat(),
                llmAdjustment = 0
            ),
            llmAnalysis = null,
            priceEstimate = null,
            issues = IssuesList(
                grave = domain.redFlags.map { Issue(it, "Stored issue", IssueSeverity.GRAVE, null, IssueSource.LLM_ANALYSIS) },
                medium = emptyList(),
                minor = domain.observations.map { Issue(it, "Stored observation", IssueSeverity.MINOR, null, IssueSource.LLM_ANALYSIS) }
            ),
            buyerAdvice = domain.recommendations.firstOrNull() ?: "",
            riskLevel = risk,
            lastUpdated = domain.createdAt
        )
    }
    
    /**
     * Update engine sound analysis result
     */
    fun updateEngineSoundResult(result: EngineSoundResult) {
        currentEngineSoundResult = result
    }
    
    /**
     * Update video analysis result
     */
    fun updateVideoResult(result: VideoAnalysisResult) {
        currentVideoResult = result
    }
    
    /**
     * Toggle breakdown dialog visibility
     */
    fun toggleBreakdownDialog() {
        _uiState.update { it.copy(showBreakdownDialog = !it.showBreakdownDialog) }
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
