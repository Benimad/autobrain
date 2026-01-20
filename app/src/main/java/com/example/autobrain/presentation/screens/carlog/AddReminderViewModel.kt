package com.example.autobrain.presentation.screens.carlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.ai.GeminiCarDetails
import com.example.autobrain.data.ai.MaintenanceRecordData
import com.example.autobrain.data.local.entity.Reminder
import com.example.autobrain.data.local.entity.ReminderPriority
import com.example.autobrain.data.local.entity.ReminderType
import com.example.autobrain.domain.logic.IntelligentReminderEngine
import com.example.autobrain.domain.logic.ReminderCalculation
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

/**
 * 🧠 ADD REMINDER VIEW MODEL
 * 
 * Professional-grade logic for intelligent reminder creation
 * Features:
 * - Smart calculation of due dates and mileage
 * - Priority assessment
 * - Cost estimation
 * - Conflict detection
 * - Gemini AI recommendations
 */
@HiltViewModel
class AddReminderViewModel @Inject constructor(
    private val carLogRepository: CarLogRepository,
    private val authRepository: AuthRepository,
    private val geminiCarnetRepository: com.example.autobrain.data.ai.GeminiCarnetRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<AddReminderUiState>(AddReminderUiState.Idle)
    val uiState: StateFlow<AddReminderUiState> = _uiState.asStateFlow()
    
    private val _calculationState = MutableStateFlow<CalculationState>(CalculationState.Idle)
    val calculationState: StateFlow<CalculationState> = _calculationState.asStateFlow()
    
    private val _geminiSuggestions = MutableStateFlow<GeminiSuggestionsState>(GeminiSuggestionsState.Idle)
    val geminiSuggestions: StateFlow<GeminiSuggestionsState> = _geminiSuggestions.asStateFlow()
    
    private var currentUserId: String? = null
    private var currentMileage: Int = 0
    private var maintenanceHistory = listOf<com.example.autobrain.domain.model.MaintenanceRecord>()
    
    init {
        loadUserData()
    }
    
    /**
     * Load user and maintenance history for intelligent calculations
     */
    private fun loadUserData() {
        viewModelScope.launch {
            when (val userResult = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    currentUserId = userResult.data?.uid
                    currentUserId?.let { userId ->
                        loadMaintenanceHistory(userId)
                    }
                }
                else -> {
                    _uiState.value = AddReminderUiState.Error("User not logged in")
                }
            }
        }
    }
    
    /**
     * Load maintenance history for smart calculations
     */
    private suspend fun loadMaintenanceHistory(userId: String) {
        carLogRepository.getMaintenanceRecords(userId).let { result ->
            when (result) {
                is Result.Success -> {
                    maintenanceHistory = result.data
                    currentMileage = maintenanceHistory.maxOfOrNull { it.mileage } ?: 0
                    android.util.Log.d("AddReminderVM", "Loaded ${maintenanceHistory.size} records, current mileage: $currentMileage")
                }
                else -> {
                    android.util.Log.w("AddReminderVM", "Failed to load maintenance history")
                }
            }
        }
    }
    
    /**
     * 🧠 INTELLIGENT CALCULATION
     * Calculate optimal reminder based on maintenance type and history
     */
    fun calculateIntelligentReminder(maintenanceType: MaintenanceType) {
        viewModelScope.launch {
            _calculationState.value = CalculationState.Calculating
            
            try {
                // Use intelligent engine to calculate optimal reminder
                val calculation = IntelligentReminderEngine.calculateOptimalReminder(
                    maintenanceType = maintenanceType,
                    currentMileage = currentMileage,
                    maintenanceHistory = maintenanceHistory
                )
                
                android.util.Log.d("AddReminderVM", "Calculated reminder: $calculation")
                
                _calculationState.value = CalculationState.Success(calculation)
                
                // Automatically trigger Gemini suggestions
                getGeminiSuggestions(maintenanceType, calculation)
                
            } catch (e: Exception) {
                android.util.Log.e("AddReminderVM", "Calculation error: ${e.message}", e)
                _calculationState.value = CalculationState.Error("Calculation error: ${e.message}")
            }
        }
    }
    
    /**
     * ⭐ GEMINI AI SUGGESTIONS
     * Get intelligent recommendations from Gemini
     */
    private fun getGeminiSuggestions(
        maintenanceType: MaintenanceType,
        calculation: ReminderCalculation
    ) {
        viewModelScope.launch {
            _geminiSuggestions.value = GeminiSuggestionsState.Loading
            
            try {
                // Prepare data for Gemini
                val carDetails = com.example.autobrain.data.ai.GeminiCarDetails(
                    brand = "Generic", // TODO: Get from user profile
                    model = "Vehicle",
                    year = 2020,
                    mileage = currentMileage
                )
                
                val maintenanceRecords = maintenanceHistory.map { record ->
                    com.example.autobrain.data.ai.MaintenanceRecordData(
                        date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE)
                            .format(record.date),
                        type = record.type.name,
                        mileage = record.mileage,
                        cost = record.cost.toInt(),
                        serviceProvider = record.serviceProvider,
                        notes = record.notes
                    )
                }
                
                // Get Gemini analysis
                val analysisResult = geminiCarnetRepository.analyzeMaintenanceHistory(
                    carDetails = carDetails,
                    maintenanceRecords = maintenanceRecords
                )
                
                analysisResult.fold(
                    onSuccess = { analysis ->
                        _geminiSuggestions.value = GeminiSuggestionsState.Success(
                            suggestions = listOf(
                                "Health score: ${analysis.overallScore}/100",
                                "Estimation: ${calculation.estimatedCost.first.toInt()}-$${calculation.estimatedCost.second.toInt()}",
                                "Urgence: ${calculation.urgencyScore}/100"
                            ) + analysis.recommendedNextSteps.take(3)
                        )
                    },
                    onFailure = { error ->
                        _geminiSuggestions.value = GeminiSuggestionsState.Error(
                            error.message ?: "Gemini error"
                        )
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("AddReminderVM", "Gemini error: ${e.message}", e)
                _geminiSuggestions.value = GeminiSuggestionsState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * 💾 SAVE REMINDER
     * Save reminder to Room + Firebase with full validation
     */
    fun saveReminder(
        maintenanceType: MaintenanceType,
        customTitle: String? = null,
        customDescription: String? = null,
        customDueMileage: Int? = null,
        customDueDate: Long? = null,
        enableNotification: Boolean = true,
        notifyDaysBefore: Int = 7
    ) {
        viewModelScope.launch {
            _uiState.value = AddReminderUiState.Saving
            
            try {
                val userId = currentUserId
                if (userId == null) {
                    _uiState.value = AddReminderUiState.Error("User not logged in")
                    return@launch
                }
                
                // Get calculation (should already be calculated)
                val calculation = (_calculationState.value as? CalculationState.Success)?.calculation
                if (calculation == null) {
                    _uiState.value = AddReminderUiState.Error("Please calculate the reminder first")
                    return@launch
                }
                
                // Check for conflicts
                if (IntelligentReminderEngine.hasRecentMaintenance(
                        maintenanceType,
                        currentMileage,
                        maintenanceHistory
                    )) {
                    _uiState.value = AddReminderUiState.Warning(
                        "Attention: This service was recently performed. Do you really want to create a reminder?"
                    )
                    return@launch
                }
                
                // Convert MaintenanceType to ReminderType
                val reminderType = when (maintenanceType) {
                    MaintenanceType.OIL_CHANGE -> ReminderType.OIL_CHANGE
                    MaintenanceType.BRAKE_SERVICE -> ReminderType.BRAKE_SERVICE
                    MaintenanceType.TIRE_ROTATION -> ReminderType.TIRE_CHANGE
                    MaintenanceType.BATTERY_REPLACEMENT -> ReminderType.BATTERY_CHECK
                    else -> ReminderType.MAINTENANCE
                }
                
                // Convert priority
                val priority = when (calculation.priority) {
                    com.example.autobrain.domain.model.ReminderPriority.CRITICAL -> ReminderPriority.CRITICAL
                    com.example.autobrain.domain.model.ReminderPriority.HIGH -> ReminderPriority.HIGH
                    com.example.autobrain.domain.model.ReminderPriority.MEDIUM -> ReminderPriority.MEDIUM
                    com.example.autobrain.domain.model.ReminderPriority.LOW -> ReminderPriority.LOW
                }
                
                // Create reminder with intelligent data
                val reminder = Reminder(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    carId = userId, // Using userId as carId for now
                    type = reminderType,
                    title = customTitle ?: getDefaultTitle(maintenanceType),
                    description = customDescription ?: calculation.description,
                    dueDate = customDueDate ?: calculation.dueDate,
                    dueMileage = customDueMileage ?: calculation.dueMileage,
                    reminderDaysBefore = notifyDaysBefore,
                    isNotificationEnabled = enableNotification,
                    priority = priority,
                    isRecurring = false, // Can be enhanced later
                    createdAt = System.currentTimeMillis()
                )
                
                // Save to repository (Room + Firebase)
                when (carLogRepository.addReminder(userId, convertToMaintenanceReminder(reminder))) {
                    is Result.Success -> {
                        android.util.Log.d("AddReminderVM", "Reminder saved successfully")
                        _uiState.value = AddReminderUiState.Success
                    }
                    is Result.Error -> {
                        _uiState.value = AddReminderUiState.Error("Save error")
                    }
                    else -> {}
                }
                
            } catch (e: Exception) {
                android.util.Log.e("AddReminderVM", "Save error: ${e.message}", e)
                _uiState.value = AddReminderUiState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Override conflict warning and force save
     */
    fun forceOverrideConflict(
        maintenanceType: MaintenanceType,
        customTitle: String? = null,
        customDescription: String? = null
    ) {
        // Same as saveReminder but skip conflict check
        // Implementation similar to saveReminder without conflict detection
    }
    
    private fun getDefaultTitle(type: MaintenanceType): String {
        return when (type) {
            MaintenanceType.OIL_CHANGE -> "Oil change"
            MaintenanceType.TIRE_ROTATION -> "Tire rotation"
            MaintenanceType.BRAKE_SERVICE -> "Brake service"
            MaintenanceType.ENGINE_TUNE_UP -> "Engine tune-up"
            MaintenanceType.BATTERY_REPLACEMENT -> "Battery replacement"
            MaintenanceType.AIR_FILTER -> "Air filter replacement"
            MaintenanceType.TRANSMISSION_SERVICE -> "Transmission service"
            MaintenanceType.COOLANT_FLUSH -> "Coolant flush"
            MaintenanceType.GENERAL_INSPECTION -> "General inspection"
            MaintenanceType.REPAIR -> "Repair"
            MaintenanceType.OTHER -> "Other maintenance"
        }
    }
    
    private fun convertToMaintenanceReminder(reminder: Reminder): com.example.autobrain.domain.model.MaintenanceReminder {
        return com.example.autobrain.domain.model.MaintenanceReminder(
            id = reminder.id,
            type = MaintenanceType.valueOf(reminder.type.name),
            title = reminder.title,
            description = reminder.description,
            dueDate = reminder.dueDate,
            dueMileage = reminder.dueMileage,
            isCompleted = reminder.isCompleted,
            notificationDaysBefore = reminder.reminderDaysBefore,
            priority = com.example.autobrain.domain.model.ReminderPriority.valueOf(reminder.priority.name),
            createdAt = reminder.createdAt
        )
    }
    
    fun resetState() {
        _uiState.value = AddReminderUiState.Idle
        _calculationState.value = CalculationState.Idle
        _geminiSuggestions.value = GeminiSuggestionsState.Idle
    }
}

/**
 * UI States
 */
sealed class AddReminderUiState {
    object Idle : AddReminderUiState()
    object Saving : AddReminderUiState()
    object Success : AddReminderUiState()
    data class Error(val message: String) : AddReminderUiState()
    data class Warning(val message: String) : AddReminderUiState()
}

sealed class CalculationState {
    object Idle : CalculationState()
    object Calculating : CalculationState()
    data class Success(val calculation: ReminderCalculation) : CalculationState()
    data class Error(val message: String) : CalculationState()
}

sealed class GeminiSuggestionsState {
    object Idle : GeminiSuggestionsState()
    object Loading : GeminiSuggestionsState()
    data class Success(val suggestions: List<String>) : GeminiSuggestionsState()
    data class Error(val message: String) : GeminiSuggestionsState()
}
