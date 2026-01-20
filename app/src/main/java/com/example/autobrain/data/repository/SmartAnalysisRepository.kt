package com.example.autobrain.data.repository

import android.util.Log
import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.ai.GeminiAiRepository
import com.example.autobrain.data.ai.MarketData
import com.example.autobrain.data.ai.UltimateSmartAnalysis
import com.example.autobrain.data.ai.buildUltimateSmartAnalysisPrompt
import com.example.autobrain.data.ai.toFirestoreMap
import com.example.autobrain.data.local.entity.AudioDiagnosticData
import com.example.autobrain.data.local.entity.VideoDiagnosticData
import com.example.autobrain.data.local.entity.toAudioDiagnosticData
import com.example.autobrain.data.local.entity.toVideoDiagnosticData
import com.example.autobrain.domain.model.CarDetails
import com.example.autobrain.domain.model.CarLog
import com.example.autobrain.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Smart Analysis Repository
 * Combines Audio + Video + Price + Complete Firestore History
 * 
 * The ULTIMATE automotive evaluation system
 */
@Singleton
class SmartAnalysisRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val geminiAiRepository: GeminiAiRepository
) {
    private val TAG = "SmartAnalysisRepo"
    
    companion object {
        private const val COLLECTION_SMART_ANALYSIS = "smart_analysis_reports"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_CAR_LOGS = "car_logs"
        private const val COLLECTION_AUDIO_DIAGNOSTICS = "audio_diagnostics"
        private const val COLLECTION_VIDEO_DIAGNOSTICS = "video_diagnostics"
        private const val COLLECTION_MARKET_DATA = "market_data"
    }
    
    /**
     * Perform ULTIMATE Smart Analysis combining ALL data sources
     * 
     * @param askedPrice Optional - price being asked for the car
     * @return Ultimate smart analysis with 15 comprehensive sections
     */
    suspend fun performUltimateSmartAnalysis(
        askedPrice: Double? = null
    ): Result<UltimateSmartAnalysis> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid 
                ?: return@withContext Result.Error(Exception("User not authenticated"))
            
            Log.d(TAG, "🚀 Starting ULTIMATE Smart Analysis")
            
            // Step 1: Fetch User Profile
            Log.d(TAG, "📥 Step 1: Fetching user profile...")
            val user = fetchUserProfile(userId)
            val carDetails = user.carDetails ?: CarDetails()
            Log.d(TAG, "✅ User: ${user.name}, Car: ${carDetails.make} ${carDetails.model} ${carDetails.year}")
            
            // Step 2: Fetch Car Log
            Log.d(TAG, "📥 Step 2: Fetching car log...")
            val carLog = fetchCarLog(userId)
            Log.d(TAG, "✅ CarLog: ${carLog.maintenanceRecords.size} records")
            
            // Step 3: Fetch Latest Audio Diagnostic
            Log.d(TAG, "📥 Step 3: Fetching latest audio diagnostic...")
            val audioData = fetchLatestAudioDiagnostic(userId)
            Log.d(TAG, "✅ Audio: ${if (audioData != null) "Score ${audioData.rawScore}/100" else "None"}")
            
            // Step 4: Fetch Latest Video Diagnostic
            Log.d(TAG, "📥 Step 4: Fetching latest video diagnostic...")
            val videoData = fetchLatestVideoDiagnostic(userId)
            Log.d(TAG, "✅ Video: ${if (videoData != null) "Score ${videoData.finalScore}/100" else "None"}")
            
            // Step 5: Fetch ALL Audio History
            Log.d(TAG, "📥 Step 5: Fetching all audio history...")
            val allAudioHistory = fetchAllAudioDiagnostics(userId)
            Log.d(TAG, "✅ Audio history: ${allAudioHistory.size} diagnostics")
            
            // Step 6: Fetch ALL Video History
            Log.d(TAG, "📥 Step 6: Fetching all video history...")
            val allVideoHistory = fetchAllVideoDiagnostics(userId)
            Log.d(TAG, "✅ Video history: ${allVideoHistory.size} diagnostics")
            
            // Step 7: Fetch Market Data
            Log.d(TAG, "📥 Step 7: Fetching market data...")
            val marketData = fetchMarketData(carDetails)
            Log.d(TAG, "✅ Market: Avg price $${marketData.averageMarketPrice.toInt()}")
            
            // Step 8: Build Ultimate Prompt
            Log.d(TAG, "🎯 Step 8: Building ultimate prompt...")
            val prompt = buildUltimateSmartAnalysisPrompt(
                carDetails = carDetails,
                user = user,
                carLog = carLog,
                audioData = audioData,
                videoData = videoData,
                allAudioHistory = allAudioHistory,
                allVideoHistory = allVideoHistory,
                marketData = marketData,
                askedPrice = askedPrice
            )
            
            // Step 9: Call Gemini AI
            Log.d(TAG, "⭐ Gemini: Step 9: Calling Gemini 2.5 Pro for ultimate analysis...")
            val result = geminiAiRepository.performUltimateSmartAnalysis(prompt)
            
            result.fold(
                onSuccess = { analysis ->
                    Log.d(TAG, "✅ Ultimate analysis complete!")
                    Log.d(TAG, "   📊 Overall AutoBrain Score: ${analysis.overallAutobrainScore}/100")
                    Log.d(TAG, "   🔴 Primary Issue: ${analysis.comprehensiveDiagnosis.primaryIssue}")
                    Log.d(TAG, "   💰 Total Repair Cost: $${analysis.totalRepairCostEstimate.totalInvestmentNeededUsd.toInt()}")
                    Log.d(TAG, "   📈 Current Value: $${analysis.realisticMarketValue.currentConditionValueUsd.toInt()}")
                    
                    // Step 10: Store in Firestore
                    Log.d(TAG, "💾 Step 10: Storing ultimate analysis...")
                    storeUltimateAnalysis(userId, analysis, askedPrice)
                    Log.d(TAG, "✅ Ultimate analysis stored!")
                    
                    Result.Success(analysis)
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ Ultimate analysis failed: ${error.message}")
                    Result.Error(Exception(error))
                }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ultimate smart analysis failed: ${e.message}", e)
            Result.Error(e)
        }
    }
    
    // =============================================================================
    // FIRESTORE DATA FETCHING
    // =============================================================================
    
    private suspend fun fetchUserProfile(userId: String): User {
        return try {
            val doc = firestore.collection(COLLECTION_USERS).document(userId).get().await()
            doc.toObject(User::class.java) ?: User(uid = userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user: ${e.message}")
            User(uid = userId)
        }
    }
    
    private suspend fun fetchCarLog(userId: String): CarLog {
        return try {
            val doc = firestore.collection(COLLECTION_CAR_LOGS).document(userId).get().await()
            doc.toObject(CarLog::class.java) ?: CarLog(userId = userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching car log: ${e.message}")
            CarLog(userId = userId)
        }
    }
    
    private suspend fun fetchLatestAudioDiagnostic(userId: String): AudioDiagnosticData? {
        return try {
            val snapshot = firestore.collection(COLLECTION_AUDIO_DIAGNOSTICS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.data?.toAudioDiagnosticData()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching latest audio: ${e.message}")
            null
        }
    }
    
    private suspend fun fetchLatestVideoDiagnostic(userId: String): VideoDiagnosticData? {
        return try {
            val snapshot = firestore.collection(COLLECTION_VIDEO_DIAGNOSTICS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.data?.toVideoDiagnosticData()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching latest video: ${e.message}")
            null
        }
    }
    
    private suspend fun fetchAllAudioDiagnostics(userId: String): List<AudioDiagnosticData> {
        return try {
            val snapshot = firestore.collection(COLLECTION_AUDIO_DIAGNOSTICS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { it.data?.toAudioDiagnosticData() }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all audio: ${e.message}")
            emptyList()
        }
    }
    
    private suspend fun fetchAllVideoDiagnostics(userId: String): List<VideoDiagnosticData> {
        return try {
            val snapshot = firestore.collection(COLLECTION_VIDEO_DIAGNOSTICS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { it.data?.toVideoDiagnosticData() }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all video: ${e.message}")
            emptyList()
        }
    }
    
    private suspend fun fetchMarketData(carDetails: CarDetails): MarketData {
        return try {
            // Try to fetch from Firestore market_data collection
            val doc = firestore.collection(COLLECTION_MARKET_DATA)
                .document("${carDetails.make}_${carDetails.model}_${carDetails.year}")
                .get()
                .await()
            
            if (doc.exists()) {
                doc.toObject(MarketData::class.java) ?: getDefaultMarketData(carDetails)
            } else {
                getDefaultMarketData(carDetails)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching market data: ${e.message}")
            getDefaultMarketData(carDetails)
        }
    }
    
    private fun getDefaultMarketData(carDetails: CarDetails): MarketData {
        // Estimate based on car details
        val baseValue = when {
            carDetails.year >= 2020 -> 150000.0
            carDetails.year >= 2015 -> 100000.0
            carDetails.year >= 2010 -> 60000.0
            else -> 35000.0
        }
        
        return MarketData(
            averageMarketPrice = baseValue,
            priceRange = Pair(baseValue * 0.85, baseValue * 1.15),
            similarListings = emptyList(),
            marketTrend = "stable",
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    // =============================================================================
    // FIRESTORE STORAGE
    // =============================================================================
    
    private suspend fun storeUltimateAnalysis(
        userId: String,
        analysis: UltimateSmartAnalysis,
        askedPrice: Double?
    ) {
        try {
            val data = analysis.toFirestoreMap().toMutableMap()
            data["userId"] = userId
            data["askedPrice"] = askedPrice
            data["createdAt"] = System.currentTimeMillis()
            
            val docId = "ultimate_${System.currentTimeMillis()}"
            
            firestore.collection(COLLECTION_SMART_ANALYSIS)
                .document(docId)
                .set(data)
                .await()
            
            Log.d(TAG, "✅ Ultimate analysis stored!")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error storing ultimate analysis: ${e.message}", e)
        }
    }
}
