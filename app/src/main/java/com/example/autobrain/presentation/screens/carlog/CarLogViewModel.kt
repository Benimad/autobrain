package com.example.autobrain.presentation.screens.carlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autobrain.core.utils.Result
import com.example.autobrain.domain.model.CarLog
import com.example.autobrain.domain.model.MaintenanceRecord
import com.example.autobrain.domain.model.MaintenanceReminder
import com.example.autobrain.domain.model.MaintenanceType
import com.example.autobrain.domain.repository.AuthRepository
import com.example.autobrain.domain.repository.CarLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CarLogViewModel @Inject constructor(
    private val carLogRepository: CarLogRepository,
    private val authRepository: AuthRepository,
    private val geminiAiRepository: com.example.autobrain.data.ai.GeminiAiRepository,
    private val geminiCarnetRepository: com.example.autobrain.data.ai.GeminiCarnetRepository
) : ViewModel() {
    
    // =========================================================================
    // ⭐ GEMINI 2.0 FLASH AI STATES
    // =========================================================================
    
    private val _smartRemindersState = MutableStateFlow<SmartRemindersState>(SmartRemindersState.Idle)
    val smartRemindersState: StateFlow<SmartRemindersState> = _smartRemindersState.asStateFlow()
    
    private val _aiAnalysisState = MutableStateFlow<AIAnalysisState>(AIAnalysisState.Idle)
    val aiAnalysisState: StateFlow<AIAnalysisState> = _aiAnalysisState.asStateFlow()
    
    private val _costPredictionState = MutableStateFlow<CostPredictionState>(CostPredictionState.Idle)
    val costPredictionState: StateFlow<CostPredictionState> = _costPredictionState.asStateFlow()
    
    private val _qualityEvaluationState = MutableStateFlow<QualityEvaluationState>(QualityEvaluationState.Idle)
    val qualityEvaluationState: StateFlow<QualityEvaluationState> = _qualityEvaluationState.asStateFlow()

    private val _carLogState = MutableStateFlow<CarLogState>(CarLogState.Loading)
    val carLogState: StateFlow<CarLogState> = _carLogState.asStateFlow()

    private val _remindersState = MutableStateFlow<RemindersState>(RemindersState.Loading)
    val remindersState: StateFlow<RemindersState> = _remindersState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<com.example.autobrain.domain.model.User?>(null)
    val currentUser: StateFlow<com.example.autobrain.domain.model.User?> = _currentUser.asStateFlow()

    private var currentUserId: String? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    _currentUser.value = result.data
                    currentUserId = result.data?.uid
                    android.util.Log.d("CarLogViewModel", "Current user loaded: ${result.data?.uid}")
                    loadCarLog()
                    loadReminders()
                }

                is Result.Error -> {
                    android.util.Log.e("CarLogViewModel", "Error loading current user: ${result.exception.message}")
                }

                else -> {}
            }
        }
    }

    private fun loadCarLog() {
        val userId = currentUserId ?: run {
            android.util.Log.e("CarLogViewModel", "Cannot load CarLog: userId is null")
            return
        }

        android.util.Log.d("CarLogViewModel", "Loading CarLog for user: $userId")

        viewModelScope.launch {
            carLogRepository.getCarLog(userId).collect { result ->
                android.util.Log.d("CarLogViewModel", "CarLog result: ${result::class.simpleName}")
                _carLogState.value = when (result) {
                    is Result.Success -> {
                        val carLog = result.data
                        android.util.Log.d("CarLogViewModel", "CarLog loaded: ${carLog?.maintenanceRecords?.size ?: 0} records")
                        if (carLog == null || carLog.maintenanceRecords.isEmpty()) {
                            android.util.Log.d("CarLogViewModel", "Showing Empty state")
                            CarLogState.Empty
                        } else {
                            android.util.Log.d("CarLogViewModel", "Showing Success state with ${carLog.maintenanceRecords.size} records")
                            CarLogState.Success(carLog)
                        }
                    }

                    is Result.Error -> {
                        android.util.Log.e("CarLogViewModel", "Error loading CarLog: ${result.exception.message}", result.exception)
                        CarLogState.Error(
                            result.exception.message ?: "Erreur de chargement"
                        )
                    }

                    is Result.Loading -> {
                        android.util.Log.d("CarLogViewModel", "Loading CarLog...")
                        CarLogState.Loading
                    }
                }
            }
        }
    }

    private fun loadReminders() {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            when (val result = carLogRepository.getUpcomingReminders(userId)) {
                is Result.Success -> {
                    _remindersState.value = if (result.data.isEmpty()) {
                        RemindersState.Empty
                    } else {
                        RemindersState.Success(result.data)
                    }
                }

                is Result.Error -> {
                    _remindersState.value = RemindersState.Error(
                        result.exception.message ?: "Erreur de chargement"
                    )
                }

                else -> {}
            }
        }
    }

    fun addMaintenanceRecord(
        type: MaintenanceType,
        description: String,
        date: Long,
        mileage: Int,
        cost: Double,
        serviceProvider: String,
        notes: String
    ) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            _saveState.value = SaveState.Saving

            val record = MaintenanceRecord(
                id = UUID.randomUUID().toString(),
                type = type,
                description = description,
                date = date,
                mileage = mileage,
                cost = cost,
                serviceProvider = serviceProvider,
                notes = notes
            )

            when (carLogRepository.addMaintenanceRecord(userId, record)) {
                is Result.Success -> {
                    _saveState.value = SaveState.Success
                    loadCarLog()
                    // Automatically trigger AI analysis after adding record
                    analyzeMaintenanceWithGemini()
                }

                is Result.Error -> {
                    _saveState.value = SaveState.Error("Erreur de sauvegarde")
                }

                else -> {}
            }
        }
    }

    fun addReminder(
        type: MaintenanceType,
        title: String,
        description: String,
        dueDate: Long,
        dueMileage: Int
    ) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            _saveState.value = SaveState.Saving

            val reminder = MaintenanceReminder(
                id = UUID.randomUUID().toString(),
                type = type,
                title = title,
                description = description,
                dueDate = dueDate,
                dueMileage = dueMileage
            )

            when (carLogRepository.addReminder(userId, reminder)) {
                is Result.Success -> {
                    _saveState.value = SaveState.Success
                    loadReminders()
                }

                is Result.Error -> {
                    _saveState.value = SaveState.Error("Erreur de sauvegarde")
                }

                else -> {}
            }
        }
    }

    fun markReminderCompleted(reminderId: String) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            carLogRepository.markReminderAsCompleted(userId, reminderId)
            loadReminders()
        }
    }

    fun deleteMaintenanceRecord(recordId: String) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            carLogRepository.deleteMaintenanceRecord(userId, recordId)
            loadCarLog()
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    fun refresh() {
        loadCarLog()
        loadReminders()
    }
    
    /**
     * Get Gemini smart analysis for maintenance planning using current user car data
     */
    fun getSmartMaintenanceAnalysis() {
        viewModelScope.launch {
            _smartRemindersState.value = SmartRemindersState.Loading
            
            try {
                val user = _currentUser.value
                val userCarDetails = user?.carDetails
                
                if (userCarDetails == null || userCarDetails.make.isEmpty()) {
                    _smartRemindersState.value = SmartRemindersState.Error(
                        "Veuillez ajouter les détails de votre véhicule dans votre profil"
                    )
                    return@launch
                }
                
                // Get current mileage from latest maintenance record or default to 0
                val carLogState = _carLogState.value
                val currentMileage = if (carLogState is CarLogState.Success) {
                    carLogState.carLog.maintenanceRecords.maxByOrNull { it.mileage }?.mileage ?: 0
                } else {
                    0
                }
                
                val carDetails = com.example.autobrain.data.ai.CarDetails(
                    brand = userCarDetails.make,
                    model = userCarDetails.model,
                    year = userCarDetails.year,
                    mileage = currentMileage
                )
                
                // Get current maintenance records summary
                val maintenanceSummary = if (carLogState is CarLogState.Success) {
                    "Derniers entretiens: ${carLogState.carLog.maintenanceRecords.take(5).joinToString("\n") { "- ${it.type} (${it.date} / ${it.mileage}km): ${it.description}" }}"
                } else {
                    "Aucun historique d'entretien"
                }
                
                val maintenanceResult = geminiAiRepository.analyzeMaintenance(
                    carDetails,
                    maintenanceSummary
                )
                
                maintenanceResult.fold(
                    onSuccess = { analysis ->
                        val allSuggestions = analysis.suggestedReminders + analysis.urgentActions
                        _smartRemindersState.value = SmartRemindersState.Success(
                            advice = analysis.riskAnalysis,
                            suggestedMaintenance = allSuggestions.distinct()
                        )
                    },
                    onFailure = { error ->
                        _smartRemindersState.value = SmartRemindersState.Error(
                            error.message ?: "Erreur analyse Gemini"
                        )
                    }
                )
            } catch (e: Exception) {
                _smartRemindersState.value = SmartRemindersState.Error(
                    e.message ?: "Erreur inattendue"
                )
            }
        }
    }
    
    fun resetSmartReminders() {
        _smartRemindersState.value = SmartRemindersState.Idle
    }
    
    /**
     * Comprehensive Gemini AI analysis of maintenance records
     * Automatically triggered after adding a new maintenance record
     */
    private fun analyzeMaintenanceWithGemini() {
        viewModelScope.launch {
            try {
                val carLogState = _carLogState.value
                if (carLogState !is CarLogState.Success) return@launch
                
                val carLog = carLogState.carLog
                val user = _currentUser.value
                val userCarDetails = user?.carDetails ?: return@launch
                
                // Prepare car details for Gemini
                val geminiCarDetails = com.example.autobrain.data.ai.GeminiCarDetails(
                    brand = userCarDetails.make,
                    model = userCarDetails.model,
                    year = userCarDetails.year,
                    mileage = carLog.maintenanceRecords.maxByOrNull { it.mileage }?.mileage ?: 0
                )
                
                // Prepare maintenance records for Gemini
                val maintenanceRecords = carLog.maintenanceRecords.map { record ->
                    com.example.autobrain.data.ai.MaintenanceRecordData(
                        date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE).format(record.date),
                        type = record.type.name.replace("_", " "),
                        mileage = record.mileage,
                        cost = record.cost.toInt(),
                        serviceProvider = record.serviceProvider,
                        notes = record.notes
                    )
                }
                
                // Trigger comprehensive AI analysis
                _aiAnalysisState.value = AIAnalysisState.Loading
                
                val analysisResult = geminiCarnetRepository.analyzeMaintenanceHistory(
                    carDetails = geminiCarDetails,
                    maintenanceRecords = maintenanceRecords
                )
                
                analysisResult.fold(
                    onSuccess = { analysis ->
                        _aiAnalysisState.value = AIAnalysisState.Success(analysis)
                        
                        // Also generate smart reminders
                        generateGeminiSmartReminders(geminiCarDetails, carLog.maintenanceRecords)
                    },
                    onFailure = { error ->
                        _aiAnalysisState.value = AIAnalysisState.Error(
                            error.message ?: "Erreur d'analyse Gemini"
                        )
                    }
                )
            } catch (e: Exception) {
                _aiAnalysisState.value = AIAnalysisState.Error(e.message ?: "Erreur inattendue")
            }
        }
    }
    
    /**
     * Generate smart reminders using Gemini 2.0 Flash
     */
    private suspend fun generateGeminiSmartReminders(
        carDetails: com.example.autobrain.data.ai.GeminiCarDetails,
        maintenanceRecords: List<MaintenanceRecord>
    ) {
        try {
            // Prepare last maintenance dates map
            val lastMaintenanceDates = maintenanceRecords
                .groupBy { it.type.name }
                .mapValues { (_, records) -> records.maxByOrNull { it.date }?.date ?: 0L }
            
            val currentMileage = maintenanceRecords.maxByOrNull { it.mileage }?.mileage ?: 0
            
            val remindersResult = geminiCarnetRepository.generateSmartReminders(
                carDetails = carDetails,
                currentMileage = currentMileage,
                lastMaintenanceDates = lastMaintenanceDates
            )
            
            remindersResult.fold(
                onSuccess = { reminders ->
                    _smartRemindersState.value = SmartRemindersState.Success(
                        advice = "Analyse IA terminée avec succès - ${reminders.size} rappels générés",
                        suggestedMaintenance = reminders.map {
                            "${it.title} - Dans ${it.dueInDays} jours (${it.dueAtKm}km) - ${it.priority}"
                        }
                    )
                },
                onFailure = { /* Silent failure for reminders */ }
            )
        } catch (e: Exception) {
            // Silent failure - analysis is more important
        }
    }
    
    /**
     * Manual trigger for comprehensive AI analysis
     */
    fun triggerComprehensiveAnalysis() {
        analyzeMaintenanceWithGemini()
    }
    
    // =========================================================================
    // 🚀 GEMINI 2.0 FLASH AI-POWERED FEATURES
    // =========================================================================
    
    /**
     * Comprehensive AI Analysis with Gemini 2.0 Flash
     */
    fun performComprehensiveAIAnalysis() {
        viewModelScope.launch {
            _aiAnalysisState.value = AIAnalysisState.Loading
            
            try {
                val carLogState = _carLogState.value
                if (carLogState !is CarLogState.Success) {
                    _aiAnalysisState.value = AIAnalysisState.Error("Aucune donnée disponible")
                    return@launch
                }
                
                val carLog = carLogState.carLog
                val carDetails = com.example.autobrain.data.ai.GeminiCarDetails(
                    brand = carLog.carDetails?.make ?: "Unknown",
                    model = carLog.carDetails?.model ?: "Unknown",
                    year = carLog.carDetails?.year ?: 2020,
                    mileage = 50000 // Get from latest maintenance record
                )
                
                val maintenanceRecords = carLog.maintenanceRecords.map { record ->
                    com.example.autobrain.data.ai.MaintenanceRecordData(
                        date = record.date.toString(),
                        type = record.type.name,
                        mileage = record.mileage,
                        cost = record.cost.toInt(),
                        serviceProvider = record.serviceProvider,
                        notes = record.notes
                    )
                }
                
                val result = geminiCarnetRepository.analyzeMaintenanceHistory(
                    carDetails, 
                    maintenanceRecords
                )
                
                result.fold(
                    onSuccess = { analysis ->
                        _aiAnalysisState.value = AIAnalysisState.Success(analysis)
                    },
                    onFailure = { error ->
                        _aiAnalysisState.value = AIAnalysisState.Error(
                            error.message ?: "Erreur d'analyse Gemini 2.0 Flash"
                        )
                    }
                )
                
            } catch (e: Exception) {
                _aiAnalysisState.value = AIAnalysisState.Error(
                    e.message ?: "Erreur inattendue"
                )
            }
        }
    }
    
    /**
     * Generate Smart Reminders with AI
     */
    fun generateGeminiSmartReminders(
        brand: String,
        model: String,
        year: Int,
        currentMileage: Int
    ) {
        viewModelScope.launch {
            _smartRemindersState.value = SmartRemindersState.Loading
            
            try {
                val carDetails = com.example.autobrain.data.ai.GeminiCarDetails(
                    brand = brand,
                    model = model,
                    year = year,
                    mileage = currentMileage
                )
                
                val carLogState = _carLogState.value
                val lastMaintenanceDates = if (carLogState is CarLogState.Success) {
                    carLogState.carLog.maintenanceRecords
                        .groupBy { it.type.name }
                        .mapValues { it.value.maxByOrNull { record -> record.date }?.date ?: 0L }
                } else {
                    emptyMap()
                }
                
                val result = geminiCarnetRepository.generateSmartReminders(
                    carDetails,
                    currentMileage,
                    lastMaintenanceDates
                )
                
                result.fold(
                    onSuccess = { reminders ->
                        _smartRemindersState.value = SmartRemindersState.Success(
                            advice = "Gemini 2.0 Flash a généré ${reminders.size} rappels intelligents",
                            suggestedMaintenance = reminders.map { it.title }
                        )
                    },
                    onFailure = { error ->
                        _smartRemindersState.value = SmartRemindersState.Error(
                            error.message ?: "Erreur génération reminders"
                        )
                    }
                )
                
            } catch (e: Exception) {
                _smartRemindersState.value = SmartRemindersState.Error(
                    e.message ?: "Erreur inattendue"
                )
            }
        }
    }
    
    /**
     * Predict Maintenance Costs
     */
    fun predictMaintenanceCosts(averageMonthlyKm: Int = 1000) {
        viewModelScope.launch {
            _costPredictionState.value = CostPredictionState.Loading
            
            try {
                val carLogState = _carLogState.value
                if (carLogState !is CarLogState.Success) {
                    _costPredictionState.value = CostPredictionState.Error("Aucune donnée")
                    return@launch
                }
                
                val carLog = carLogState.carLog
                val currentMileage = 50000 // Get from records or car details
                val carDetails = com.example.autobrain.data.ai.GeminiCarDetails(
                    brand = carLog.carDetails?.make ?: "Unknown",
                    model = carLog.carDetails?.model ?: "Unknown",
                    year = carLog.carDetails?.year ?: 2020,
                    mileage = currentMileage
                )
                
                val result = geminiCarnetRepository.predictMaintenanceCosts(
                    carDetails,
                    currentMileage,
                    averageMonthlyKm
                )
                
                result.fold(
                    onSuccess = { prediction ->
                        _costPredictionState.value = CostPredictionState.Success(prediction)
                    },
                    onFailure = { error ->
                        _costPredictionState.value = CostPredictionState.Error(
                            error.message ?: "Erreur prédiction"
                        )
                    }
                )
                
            } catch (e: Exception) {
                _costPredictionState.value = CostPredictionState.Error(
                    e.message ?: "Erreur inattendue"
                )
            }
        }
    }
    
    /**
     * Evaluate Maintenance Quality
     */
    fun evaluateMaintenanceQuality() {
        viewModelScope.launch {
            _qualityEvaluationState.value = QualityEvaluationState.Loading
            
            try {
                val carLogState = _carLogState.value
                if (carLogState !is CarLogState.Success) {
                    _qualityEvaluationState.value = QualityEvaluationState.Error("Aucune donnée")
                    return@launch
                }
                
                val carLog = carLogState.carLog
                val currentMileage = carLog.maintenanceRecords.maxByOrNull { it.mileage }?.mileage ?: 50000
                val carDetails = com.example.autobrain.data.ai.GeminiCarDetails(
                    brand = carLog.carDetails?.make ?: "Unknown",
                    model = carLog.carDetails?.model ?: "Unknown",
                    year = carLog.carDetails?.year ?: 2020,
                    mileage = currentMileage
                )
                
                val maintenanceRecords = carLog.maintenanceRecords.map { record ->
                    com.example.autobrain.data.ai.MaintenanceRecordData(
                        date = record.date.toString(),
                        type = record.type.name,
                        mileage = record.mileage,
                        cost = record.cost.toInt(),
                        serviceProvider = record.serviceProvider
                    )
                }
                
                val result = geminiCarnetRepository.evaluateMaintenanceQuality(
                    carDetails,
                    maintenanceRecords,
                    currentMileage
                )
                
                result.fold(
                    onSuccess = { evaluation ->
                        _qualityEvaluationState.value = QualityEvaluationState.Success(evaluation)
                    },
                    onFailure = { error ->
                        _qualityEvaluationState.value = QualityEvaluationState.Error(
                            error.message ?: "Erreur évaluation"
                        )
                    }
                )
                
            } catch (e: Exception) {
                _qualityEvaluationState.value = QualityEvaluationState.Error(
                    e.message ?: "Erreur inattendue"
                )
            }
        }
    }
    
    fun resetAIStates() {
        _aiAnalysisState.value = AIAnalysisState.Idle
        _costPredictionState.value = CostPredictionState.Idle
        _qualityEvaluationState.value = QualityEvaluationState.Idle
    }
}

// =============================================================================
// AI STATES
// =============================================================================

sealed class AIAnalysisState {
    object Idle : AIAnalysisState()
    object Loading : AIAnalysisState()
    data class Success(val analysis: com.example.autobrain.data.ai.MaintenanceAnalysis) : AIAnalysisState()
    data class Error(val message: String) : AIAnalysisState()
}

sealed class CostPredictionState {
    object Idle : CostPredictionState()
    object Loading : CostPredictionState()
    data class Success(val prediction: com.example.autobrain.data.ai.CostPrediction) : CostPredictionState()
    data class Error(val message: String) : CostPredictionState()
}

sealed class QualityEvaluationState {
    object Idle : QualityEvaluationState()
    object Loading : QualityEvaluationState()
    data class Success(val evaluation: com.example.autobrain.data.ai.QualityEvaluation) : QualityEvaluationState()
    data class Error(val message: String) : QualityEvaluationState()
}

sealed class CarLogState {
    object Loading : CarLogState()
    data class Success(val carLog: CarLog) : CarLogState()
    object Empty : CarLogState()
    data class Error(val message: String) : CarLogState()
}

sealed class RemindersState {
    object Loading : RemindersState()
    data class Success(val reminders: List<MaintenanceReminder>) : RemindersState()
    object Empty : RemindersState()
    data class Error(val message: String) : RemindersState()
}

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

sealed class SmartRemindersState {
    object Idle : SmartRemindersState()
    object Loading : SmartRemindersState()
    data class Success(
        val advice: String,
        val suggestedMaintenance: List<String>
    ) : SmartRemindersState()
    data class Error(val message: String) : SmartRemindersState()
}
