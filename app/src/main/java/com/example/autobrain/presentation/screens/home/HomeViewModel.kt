package com.example.autobrain.presentation.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.local.dao.AudioDiagnosticDao
import com.example.autobrain.data.local.dao.MaintenanceRecordDao
import com.example.autobrain.data.local.dao.VideoDiagnosticDao
import com.example.autobrain.domain.repository.AuthRepository
import com.example.autobrain.presentation.theme.ElectricTeal
import com.example.autobrain.presentation.theme.SuccessGreen
import com.example.autobrain.presentation.theme.WarningAmber
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "User",
    val userEmail: String = "",
    val userPhotoUrl: String? = null,
    val carMake: String = "Honda",
    val carModel: String = "CRV",
    val carYear: String = "2021",
    val carKilometers: Int = 45000,
    val aiScore: Int = 94,
    val riskLevel: String = "Low",
    val lastDiagnosticDate: String = "Dec 15, 2024",
    val daysUntilService: Int = 15,
    val kmUntilService: Int = 2500,
    val carImageUrl: String? = null,
    val recentDiagnostics: List<DiagnosticItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val audioDiagnosticDao: AudioDiagnosticDao,
    private val videoDiagnosticDao: VideoDiagnosticDao,
    private val maintenanceRecordDao: MaintenanceRecordDao,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Legacy support for existing code
    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _carModel = MutableStateFlow("Honda CRV")
    val carModel: StateFlow<String> = _carModel.asStateFlow()

    private val _carYear = MutableStateFlow("2021")
    val carYear: StateFlow<String> = _carYear.asStateFlow()

    init {
        loadUserData()
        loadRecentDiagnostics()
        loadMaintenanceReminders()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    result.data?.let { user ->
                        val name = user.name.ifEmpty { "User" }
                        val carMake = user.carDetails?.make ?: "Honda"
                        val carModelName = user.carDetails?.model ?: "CRV"
                        val carYearValue = user.carDetails?.year?.toString() ?: "2021"

                        _userName.value = name
                        _carModel.value = "$carMake $carModelName"
                        _carYear.value = carYearValue

                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                userName = name,
                                userEmail = user.email,
                                carMake = carMake,
                                carModel = carModelName,
                                carYear = carYearValue,
                                carImageUrl = user.carDetails?.carImageUrl,
                                carKilometers = 45000
                            )
                        }
                    }
                }

                is Result.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = result.exception.message
                        )
                    }
                }

                is Result.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    private fun loadRecentDiagnostics() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            
            try {
                // Fetch recent audio diagnostics from database
                val audioDiagnostics = audioDiagnosticDao.getRecentDiagnostics(userId, limit = 3)
                
                // Fetch recent video diagnostics from database
                val videoDiagnostics = videoDiagnosticDao.getRecentDiagnostics(userId, limit = 3)
                
                // Convert to DiagnosticItems
                val diagnosticItems = mutableListOf<DiagnosticItem>()
                
                // Add audio diagnostics
                audioDiagnostics.forEach { audio ->
                    diagnosticItems.add(
                        DiagnosticItem(
                            id = audio.id,
                            title = "Diagnostic Audio",
                            status = audio.healthStatus,
                            statusColor = when {
                                audio.rawScore >= 80 -> SuccessGreen
                                audio.rawScore >= 60 -> WarningAmber
                                else -> Color(0xFFFF5555)
                            },
                            icon = Icons.Outlined.Mic,
                            value = "${audio.rawScore}/100"
                        )
                    )
                }
                
                // Add video diagnostics
                videoDiagnostics.forEach { video ->
                    val statusText = when {
                        video.smokeDetected && video.vibrationDetected -> "Smoke + Vibrations"
                        video.smokeDetected -> "Smoke detected (${video.smokeType})"
                        video.vibrationDetected -> "Vibrations (${video.vibrationLevel})"
                        else -> video.healthStatus
                    }
                    
                    diagnosticItems.add(
                        DiagnosticItem(
                            id = video.id,
                            title = "Video Diagnostic",
                            status = statusText,
                            statusColor = when {
                                video.finalScore >= 80 -> SuccessGreen
                                video.finalScore >= 60 -> WarningAmber
                                video.finalScore >= 40 -> Color(0xFFFF8C00) // Orange
                                else -> Color(0xFFFF5555) // Red
                            },
                            icon = Icons.Outlined.Videocam,
                            value = "${video.finalScore}/100"
                        )
                    )
                }
                
                // If no diagnostics, show empty state
                val finalDiagnostics = if (diagnosticItems.isEmpty()) {
                    listOf(
                        DiagnosticItem(
                            id = "empty",
                            title = "No diagnostic",
                            status = "Start your first diagnostic",
                            statusColor = ElectricTeal,
                            icon = Icons.Outlined.Mic
                        )
                    )
                } else {
                    diagnosticItems.take(3) // Show max 3 recent
                }
                
                _uiState.update { state ->
                    state.copy(recentDiagnostics = finalDiagnostics)
                }
                
                // Update AI score from latest diagnostic (prioritize most recent across all types)
                val allScores = mutableListOf<Pair<Long, Int>>()
                
                audioDiagnostics.forEach { allScores.add(Pair(it.createdAt, it.rawScore)) }
                videoDiagnostics.forEach { allScores.add(Pair(it.createdAt, it.finalScore)) }
                
                if (allScores.isNotEmpty()) {
                    // Get the most recent score
                    val latestScore = allScores.maxByOrNull { it.first }?.second ?: 0
                    _uiState.update { state ->
                        state.copy(
                            aiScore = latestScore,
                            riskLevel = when {
                                latestScore >= 80 -> "Low"
                                latestScore >= 60 -> "Medium"
                                else -> "High"
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                // If database fetch fails, show empty state
                _uiState.update { state ->
                    state.copy(
                        recentDiagnostics = listOf(
                            DiagnosticItem(
                                id = "error",
                                title = "Loading error",
                                status = "Try again later",
                                statusColor = Color(0xFFFF5555),
                                icon = Icons.Outlined.Mic
                            )
                        )
                    )
                }
            }
        }
    }

    /**
     * Load maintenance reminders and calculate service due dates
     */
    private fun loadMaintenanceReminders() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            
            try {
                // Get all maintenance records
                val maintenanceRecords = maintenanceRecordDao.getUnsyncedRecords(userId)
                
                if (maintenanceRecords.isEmpty()) {
                    // Default values if no records
                    _uiState.update { state ->
                        state.copy(
                            daysUntilService = 30,
                            kmUntilService = 5000
                        )
                    }
                    return@launch
                }
                
                // Find last oil change
                val lastOilChange = maintenanceRecords
                    .filter { it.type == "OIL_CHANGE" }
                    .maxByOrNull { it.date }
                
                if (lastOilChange != null) {
                    val currentKm = uiState.value.carKilometers
                    val kmSinceOilChange = currentKm - lastOilChange.mileage
                    val kmUntilNextService = (10000 - kmSinceOilChange).coerceAtLeast(0)
                    
                    val daysSinceOilChange = ((System.currentTimeMillis() - lastOilChange.date) / (1000 * 60 * 60 * 24)).toInt()
                    val daysUntilNextService = (180 - daysSinceOilChange).coerceAtLeast(0) // 6 months
                    
                    _uiState.update { state ->
                        state.copy(
                            daysUntilService = daysUntilNextService,
                            kmUntilService = kmUntilNextService
                        )
                    }
                } else {
                    // No oil change record - recommend service
                    _uiState.update { state ->
                        state.copy(
                            daysUntilService = 0,
                            kmUntilService = 0
                        )
                    }
                }
            } catch (e: Exception) {
                // Default values on error
                _uiState.update { state ->
                    state.copy(
                        daysUntilService = 15,
                        kmUntilService = 2500
                    )
                }
            }
        }
    }

    fun refreshData() {
        loadUserData()
        loadRecentDiagnostics()
        loadMaintenanceReminders()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
