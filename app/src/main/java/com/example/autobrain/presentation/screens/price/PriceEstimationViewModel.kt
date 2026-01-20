package com.example.autobrain.presentation.screens.price

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.ai.GeminiAiRepository
import com.example.autobrain.data.local.entity.AudioDiagnosticData
import com.example.autobrain.data.local.entity.VideoDiagnosticData
import com.example.autobrain.data.local.entity.toAudioDiagnosticData
import com.example.autobrain.data.local.entity.toVideoDiagnosticData
import com.example.autobrain.domain.model.CarDetails
import com.example.autobrain.domain.model.CarLog
import com.example.autobrain.domain.model.ComparableVehicle
import com.example.autobrain.domain.model.GeminiPriceEstimation
import com.example.autobrain.domain.model.PriceFactor
import com.example.autobrain.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Price Estimation ViewModel with Firebase + Gemini Integration
 * 
 * Features:
 * - Dynamic Firebase data integration
 * - Gemini AI for market analysis
 * - Real-time market pricing
 * - Share functionality
 */
@HiltViewModel
class PriceEstimationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geminiAiRepository: GeminiAiRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    
    private val TAG = "PriceEstimationVM"
    
    // State
    private val _uiState = MutableStateFlow<PriceEstimationUiState>(PriceEstimationUiState.Input)
    val uiState: StateFlow<PriceEstimationUiState> = _uiState.asStateFlow()
    
    private val _priceResult = MutableStateFlow<GeminiPriceEstimation?>(null)
    val priceResult: StateFlow<GeminiPriceEstimation?> = _priceResult.asStateFlow()
    
    /**
     * Calculate price with Gemini AI + Firebase data
     */
    fun calculatePrice(
        brand: String,
        model: String,
        year: String,
        mileage: String,
        condition: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = PriceEstimationUiState.Calculating
                
                Log.d(TAG, "🚀 Starting price estimation: $brand $model $year")
                
                val userId = auth.currentUser?.uid
                
                // Fetch additional context from Firebase
                val user = userId?.let { fetchUser(it) }
                val carLog = userId?.let { fetchCarLog(it) }
                val latestAudio = userId?.let { fetchLatestAudioDiagnostic(it) }
                val latestVideo = userId?.let { fetchLatestVideoDiagnostic(it) }
                
                // Build comprehensive price estimation prompt
                val prompt = buildDynamicPriceEstimationPrompt(
                    brand = brand,
                    model = model,
                    year = year.toIntOrNull() ?: 2020,
                    mileage = mileage.toIntOrNull() ?: 100000,
                    condition = condition,
                    user = user,
                    carLog = carLog,
                    audioData = latestAudio,
                    videoData = latestVideo
                )
                
                Log.d(TAG, "⭐ Gemini: Calling Gemini for price analysis...")
                
                // Call Gemini API
                val result = geminiAiRepository.performPriceEstimation(prompt)
                
                result.fold(
                    onSuccess = { estimation ->
                        Log.d(TAG, "✅ Price estimation complete!")
                        Log.d(TAG, "   Price Range: ${estimation.minPrice} - $${estimation.maxPrice}")
                        Log.d(TAG, "   Confidence: ${(estimation.confidence * 100).toInt()}%")
                        
                        _priceResult.value = estimation
                        _uiState.value = PriceEstimationUiState.Result(estimation)
                        
                        // Store in Firestore
                        userId?.let { storeEstimation(it, brand, model, year, mileage, estimation) }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ Price estimation failed: ${error.message}")
                        _uiState.value = PriceEstimationUiState.Error(
                            error.message ?: "Estimation error"
                        )
                    }
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error: ${e.message}", e)
                _uiState.value = PriceEstimationUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Share price estimation
     */
    fun shareEstimation(estimation: GeminiPriceEstimation, carInfo: String) {
        try {
            val shareText = buildShareText(estimation, carInfo)
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "AutoBrain - Vehicle Price Estimation")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            
            val chooserIntent = Intent.createChooser(shareIntent, "Share estimation via")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
            
            Log.d(TAG, "✅ Share intent launched")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Share failed: ${e.message}")
        }
    }
    
    /**
     * Reset to input state
     */
    fun resetToInput() {
        _uiState.value = PriceEstimationUiState.Input
        _priceResult.value = null
    }
    
    // =============================================================================
    // FIREBASE DATA FETCHING
    // =============================================================================
    
    private suspend fun fetchUser(userId: String): User? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user: ${e.message}")
            null
        }
    }
    
    private suspend fun fetchCarLog(userId: String): CarLog? {
        return try {
            val doc = firestore.collection("car_logs").document(userId).get().await()
            doc.toObject(CarLog::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching car log: ${e.message}")
            null
        }
    }
    
    private suspend fun fetchLatestAudioDiagnostic(userId: String): AudioDiagnosticData? {
        return try {
            val snapshot = firestore.collection("audio_diagnostics")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.data?.toAudioDiagnosticData()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching audio: ${e.message}")
            null
        }
    }
    
    private suspend fun fetchLatestVideoDiagnostic(userId: String): VideoDiagnosticData? {
        return try {
            val snapshot = firestore.collection("video_diagnostics")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.data?.toVideoDiagnosticData()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching video: ${e.message}")
            null
        }
    }
    
    // =============================================================================
    // FIRESTORE STORAGE
    // =============================================================================
    
    private suspend fun storeEstimation(
        userId: String,
        brand: String,
        model: String,
        year: String,
        mileage: String,
        estimation: GeminiPriceEstimation
    ) {
        try {
            val data = mapOf(
                "userId" to userId,
                "brand" to brand,
                "model" to model,
                "year" to year.toIntOrNull(),
                "mileage" to mileage.toIntOrNull(),
                "minPrice" to estimation.minPrice,
                "maxPrice" to estimation.maxPrice,
                "avgPrice" to estimation.avgPrice,
                "confidence" to estimation.confidence,
                "factors" to estimation.factors,
                "marketAnalysis" to estimation.marketAnalysis,
                "geminiInsights" to estimation.geminiInsights,
                "createdAt" to System.currentTimeMillis()
            )
            
            firestore.collection("price_estimations")
                .add(data)
                .await()
            
            Log.d(TAG, "✅ Estimation stored in Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error storing estimation: ${e.message}")
        }
    }
    
    // =============================================================================
    // HELPER FUNCTIONS
    // =============================================================================
    
    private fun buildShareText(estimation: GeminiPriceEstimation, carInfo: String): String {
        return """
🚗 AutoBrain - Vehicle Price Estimation

$carInfo

💰 Estimated Price: ${estimation.minPrice} - $${estimation.maxPrice}
📊 Average Price: $${estimation.avgPrice}
🎯 Confidence: ${(estimation.confidence * 100).toInt()}%

✅ Positive Factors:
${estimation.factors.filter { it.isPositive }.joinToString("\n") { "  • ${it.name}: ${it.value}" }}

${if (estimation.factors.any { !it.isPositive }) {
    "⚠️ Negative Factors:\n${estimation.factors.filter { !it.isPositive }.joinToString("\n") { "  • ${it.name}: ${it.value}" }}"
} else ""}

📈 Market Analysis:
${estimation.marketAnalysis}

⭐ AutoBrain AI Analysis - Gemini AI
Based on Firestore data + Gemini AI 2.5 Pro
2026 Automotive Market

---
Generated by AutoBrain AI
        """.trimIndent()
    }
}

// =============================================================================
// UI STATE
// =============================================================================

sealed class PriceEstimationUiState {
    object Input : PriceEstimationUiState()
    object Calculating : PriceEstimationUiState()
    data class Result(val estimation: GeminiPriceEstimation) : PriceEstimationUiState()
    data class Error(val message: String) : PriceEstimationUiState()
}

// =============================================================================
// PROMPT BUILDER (Firebase Dynamic Data)
// =============================================================================

private fun buildDynamicPriceEstimationPrompt(
    brand: String,
    model: String,
    year: Int,
    mileage: Int,
    condition: String,
    user: User?,
    carLog: CarLog?,
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): String {
    
    val maintenanceContext = carLog?.let {
        val recordsCount = it.maintenanceRecords.size
        val lastService = it.maintenanceRecords.maxByOrNull { rec -> rec.date }
        """
📚 MAINTENANCE HISTORY (Firestore):
  - Total interventions: $recordsCount
  - Last intervention: ${lastService?.description ?: "N/A"}
  - Total expenses: $${it.totalExpenses.toInt()}
  - Maintenance quality: ${if (recordsCount >= 5) "Good" else if (recordsCount >= 2) "Average" else "Low"}
        """.trimIndent()
    } ?: "No maintenance history available"
    
    val diagnosticContext = buildDiagnosticContext(audioData, videoData)
    
    return """
You are AutoBrain Price AI - Automotive estimation expert with access to Firestore data.

╔══════════════════════════════════════════════════════════════════════════════╗
║                      🚗 VEHICLE TO EVALUATE                                 ║
╚══════════════════════════════════════════════════════════════════════════════╝

Brand: $brand
Model: $model
Year: $year
Mileage: ${formatMileage(mileage)} km
Declared condition: $condition

${user?.let { "Owner: ${it.name} (${it.email})" } ?: ""}

╔══════════════════════════════════════════════════════════════════════════════╗
║            📚 FIREBASE CONTEXT (Real Data)                                   ║
╚══════════════════════════════════════════════════════════════════════════════╝

$maintenanceContext

$diagnosticContext

╔══════════════════════════════════════════════════════════════════════════════╗
║                  🎯 MISSION - PRICE ESTIMATION 2026                          ║
╚══════════════════════════════════════════════════════════════════════════════╝

Analyze the current automotive market and provide an HONEST and ACCURATE estimation.

**STRICT RULES**:
- Based on REAL market prices (no inflation)
- Consider AutoBrain diagnostics (audio/video)
- Adjust according to maintenance history
- Consider model popularity
- Full transparency on factors

**FORMAT JSON OBLIGATOIRE**:

{
  "min_price_usd": 170000,
  "max_price_usd": 210000,
  "avg_price_usd": 190000,
  "confidence": 0.82,
  "factors": [
    {
      "name": "Condition",
      "value": "$condition",
      "is_positive": true,
      "impact": "High"
    },
    {
      "name": "Mileage",
      "value": "${if (mileage.toInt() < 100000) "Below Avg." else if (mileage.toInt() < 150000) "Average" else "Above Avg."}",
      "is_positive": ${mileage.toInt() < 120000},
      "impact": "High"
    },
    {
      "name": "Service Records",
      "value": "${if ((carLog?.maintenanceRecords?.size ?: 0) >= 5) "Complete" else if ((carLog?.maintenanceRecords?.size ?: 0) >= 2) "Partial" else "Minimal"}",
      "is_positive": ${(carLog?.maintenanceRecords?.size ?: 0) >= 3},
      "impact": "Medium"
    },
    {
      "name": "Market Demand",
      "value": "${if (isPopular(brand)) "High" else "Medium"}",
      "is_positive": ${isPopular(brand)},
      "impact": "Medium"
    },
    {
      "name": "Diagnostic Score",
      "value": "${audioData?.let { "${it.rawScore}/100" } ?: "N/A"}",
      "is_positive": ${(audioData?.rawScore ?: 70) >= 70},
      "impact": "${if (audioData != null) "High" else "Low"}"
    }
  ],
  "market_analysis": "Detailed automotive market analysis for $brand $model $year. Include trends, demand, comparables.",
  "gemini_insights": [
    "Professional insight 1 on the market",
    "Insight 2 on depreciation",
    "Insight 3 on opportunities"
  ],
  "comparable_vehicles": [
    {
      "name": "Similar $brand $model",
      "year": $year,
      "mileage": 100000,
      "price": 185000,
      "location": "Casablanca"
    }
  ],
  "depreciation_factors": [
    "Age: -${calculateAgeDepreciation(year)}%",
    "Mileage: -${calculateMileageDepreciation(mileage.toInt())}%",
    ${if (audioData != null && audioData.rawScore < 70) "\"Mechanical problems: -15%\"," else ""}
    ${if ((carLog?.maintenanceRecords?.size ?: 0) < 3) "\"Incomplete history: -10%\"" else ""}
  ],
  "negotiation_tips": [
    "Negotiation tip 1",
    "Negotiation tip 2",
    "Negotiation tip 3"
  ]
}

Respond ONLY with valid JSON (no markdown, no text before/after).
    """.trimIndent()
}

private fun buildDiagnosticContext(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): String {
    val audioContext = audioData?.let {
        "🔊 Audio Diagnostic: Score ${it.rawScore}/100, ${it.urgencyLevel}"
    } ?: "No audio diagnostic"

    val videoContext = videoData?.let {
        "🎥 Video Diagnostic: Score ${it.finalScore}/100, ${if (it.smokeDetected) "Smoke detected" else "OK"}"
    } ?: "No video diagnostic"
    
    return """
🔍 AUTOBRAIN DIAGNOSTICS:
  $audioContext
  $videoContext
  Price impact: ${if ((audioData?.rawScore ?: 100) < 50 || (videoData?.finalScore ?: 100) < 50) "-15% to -30%" else "Minimal"}
    """.trimIndent()
}

private fun formatMileage(mileage: Int): String {
    return mileage.toString().reversed().chunked(3).joinToString(" ").reversed()
}

private fun isPopular(brand: String): Boolean {
    val popular = listOf("DACIA", "RENAULT", "PEUGEOT", "CITROEN", "VOLKSWAGEN", "TOYOTA", "HYUNDAI", "KIA", "FIAT")
    return popular.any { it.equals(brand, ignoreCase = true) }
}

private fun calculateAgeDepreciation(year: Int): Int {
    val age = 2026 - year
    return when {
        age <= 2 -> 10
        age <= 5 -> 20
        age <= 10 -> 35
        else -> 50
    }
}

private fun calculateMileageDepreciation(mileage: Int): Int {
    return when {
        mileage < 50000 -> 5
        mileage < 100000 -> 15
        mileage < 150000 -> 25
        mileage < 200000 -> 40
        else -> 60
    }
}
