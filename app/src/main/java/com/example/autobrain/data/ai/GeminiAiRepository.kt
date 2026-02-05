package com.example.autobrain.data.ai

import android.content.Context
import android.util.Log
import com.example.autobrain.BuildConfig
import com.example.autobrain.data.local.entity.AudioDiagnosticData
import com.example.autobrain.data.local.entity.VideoDiagnosticData
import com.example.autobrain.domain.model.CarLog
import com.example.autobrain.domain.model.GeminiPriceEstimation
import com.example.autobrain.domain.model.MaintenanceReminder
import com.example.autobrain.domain.model.PriceFactor
import com.example.autobrain.domain.model.User
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import java.io.File
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Gemini AI Repository for AutoBrain
 * 
 * Integrates Gemini 3 Pro/Flash for:
 * - Audio diagnostics (engine sound analysis)
 * - Video diagnostics (smoke, vibration detection)
 * - Price estimation (market context)
 * - Combined smart analysis
 * 
 * Models: gemini-3-pro-preview (main), gemini-3-flash-preview (fast)
 * Context: 1M tokens input, 65K tokens output
 * Free tier: 15 requests/min, 1,000 requests/day
 */
@Singleton
class GeminiAiRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "GeminiAiRepository"
    
    // Gemini API Key from BuildConfig (loaded from local.properties)
    private val apiKey = BuildConfig.GEMINI_API_KEY
    
    // Gemini 3 Pro model for multimodal analysis
    private val diagnosticsModel = GenerativeModel(
        modelName = "gemini-3-pro-preview",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.5f  // Lower for factual diagnostics
            topK = 64
            topP = 0.95f
            maxOutputTokens = 8192
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)
        )
    )
    
    // Gemini 3 Flash for faster responses
    private val fastModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 64
            topP = 0.95f
            maxOutputTokens = 8192
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)
        )
    )
    
    // =============================================================================
    // AUDIO ANALYSIS
    // =============================================================================
    
    /**
     * Analyze engine audio classifications with Gemini
     * 
     * @param classifications Audio classifications from TFLite
     * @return Enhanced audio analysis result
     */
    suspend fun analyzeAudio(
        classifications: List<AudioClassification>
    ): Result<AudioAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            if (classifications.isEmpty()) {
                return@withContext Result.failure(Exception("No audio classifications provided"))
            }
            
            val prompt = buildAudioAnalysisPrompt(classifications)
            Log.d(TAG, "Sending audio analysis to Gemini: ${classifications.size} classifications")
            
            val response = diagnosticsModel.generateContent(prompt)
            val responseText = response.text ?: return@withContext Result.failure(
                Exception("Empty response from Gemini")
            )
            
            Log.d(TAG, "Gemini audio response: $responseText")
            
            // Parse response and create result
            val result = parseAudioAnalysisResponse(responseText, classifications)
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Gemini audio analysis error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Analyze video description (smoke, vibration, etc.) with Gemini
     * 
     * @param description Video analysis description from ML Kit
     * @param anomalies Detected anomalies
     * @return Enhanced video analysis result
     */
    suspend fun analyzeVideo(
        description: String,
        anomalies: List<CarAnomaly>
    ): Result<VideoAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            if (description.isEmpty() && anomalies.isEmpty()) {
                return@withContext Result.failure(Exception("No video data provided"))
            }
            
            val prompt = buildVideoAnalysisPrompt(description, anomalies)
            Log.d(TAG, "Sending video analysis to Gemini")
            
            val response = diagnosticsModel.generateContent(prompt)
            val responseText = response.text ?: return@withContext Result.failure(
                Exception("Empty response from Gemini")
            )
            
            Log.d(TAG, "Gemini video response: $responseText")
            
            // Parse response
            val result = parseVideoAnalysisResponse(responseText, anomalies)
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Gemini video analysis error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // =============================================================================
    // PRICE CHECKING & MARKET ANALYSIS
    // =============================================================================
    
    /**
     * Check realistic car price for current market
     * 
     * @param carDetails Car make, model, year, mileage
     * @param diagnosticsSummary Summary of audio/video issues
     * @return Price estimation result
     */
    suspend fun checkPrice(
        carDetails: CarDetails,
        diagnosticsSummary: String
    ): Result<PriceEstimation> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildPriceCheckPrompt(carDetails, diagnosticsSummary)
            Log.d(TAG, "Requesting price check from Gemini for ${carDetails.brand} ${carDetails.model}")
            
            val response = diagnosticsModel.generateContent(prompt)
            val responseText = response.text ?: return@withContext Result.failure(
                Exception("Empty response from Gemini")
            )
            
            Log.d(TAG, "Gemini price response: $responseText")
            
            // Parse price estimation
            val result = parsePriceEstimationResponse(responseText, carDetails)
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Gemini price check error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // =============================================================================
    // MAINTENANCE ANALYSIS
    // =============================================================================
    
    /**
     * Generate a personalized smart reminder message
     */
    suspend fun generateSmartReminderMessage(
        reminder: MaintenanceReminder,
        daysLeft: Int
    ): String = withContext(Dispatchers.IO) {
        try {
            val prompt = buildSmartReminderPrompt(reminder, daysLeft)
            val response = diagnosticsModel.generateContent(prompt)
            response.text?.trim() ?: "Rappel: ${reminder.title} est dû."
        } catch (e: Exception) {
            Log.e(TAG, "Gemini reminder generation error: ${e.message}", e)
            "Rappel: ${reminder.title} est dû."
        }
    }

    /**
     * Analyze maintenance history and suggest smart reminders
     */
    suspend fun analyzeMaintenance(
        carDetails: CarDetails,
        maintenanceHistory: String
    ): Result<MaintenanceAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildMaintenanceAnalysisPrompt(carDetails, maintenanceHistory)
            Log.d(TAG, "Sending maintenance analysis to Gemini")
            
            val response = diagnosticsModel.generateContent(prompt)
            val responseText = response.text ?: return@withContext Result.failure(Exception("Empty response"))
            
            Log.d(TAG, "Gemini maintenance response: $responseText")
            
            val result = parseMaintenanceAnalysisResponse(responseText)
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini maintenance analysis error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =============================================================================
    // CAR IMAGE GENERATION
    // =============================================================================
    
    suspend fun generateCarImageUrl(
        make: String,
        model: String,
        year: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
You are a professional automotive photographer. Generate a high-quality, realistic car image URL for:
- Make: $make
- Model: $model
- Year: $year

Generate an Unsplash featured image URL in this EXACT format:
https://source.unsplash.com/featured/1200x800/?{query}

RULES:
1. Use professional automotive keywords: "$year+$make+$model+car+professional+studio+photography"
2. Replace ALL spaces with + signs
3. Use high resolution: 1200x800
4. Return ONLY the complete URL with no additional text
5. Example: https://source.unsplash.com/featured/1200x800/?2020+Toyota+Corolla+car+professional+studio+photography

Generate the URL now:
            """.trimIndent()
            
            Log.d(TAG, "Requesting premium car image URL from Gemini for $make $model $year")
            
            val response = fastModel.generateContent(prompt)
            val responseText = response.text?.trim() ?: return@withContext Result.failure(
                Exception("Empty response from Gemini")
            )
            
            Log.d(TAG, "Gemini car image URL response: $responseText")
            
            val imageUrl = if (responseText.startsWith("http")) {
                responseText
            } else {
                val query = "$year+$make+$model+car+professional+automotive"
                "https://source.unsplash.com/featured/1200x800/?$query"
            }
            
            Result.success(imageUrl)
            
        } catch (e: Exception) {
            Log.e(TAG, "Gemini car image generation error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // =============================================================================
    // COMPREHENSIVE AUDIO DIAGNOSTIC (Complete Firebase Integration)
    // =============================================================================
    
    /**
     * Perform COMPREHENSIVE Audio Diagnostic Analysis with MULTIMODAL INPUT
     * 
     * 🎯 ENHANCED ARCHITECTURE:
     * - Sends BOTH TFLite analysis (text) AND actual audio file (binary) to Gemini
     * - Gemini 3 Pro analyzes audio directly for superior accuracy
     * - Combines on-device TFLite insights with cloud AI audio analysis
     * - Real-time Firestore user profile & car details
     * - Complete maintenance history from CarLog
     * - Previous diagnostic trends
     * - Market context & legal compliance
     * 
     * @param audioData Current audio diagnostic data with TFLite classifications
     * @param audioFilePath Path to the actual WAV audio file
     * @param carLog Complete car maintenance log from Firestore
     * @param user User profile with car details from Firestore
     * @param previousDiagnostics Historical audio diagnostics for trend analysis
     * @return Comprehensive diagnostic result with 11 detailed sections
     */
    suspend fun performComprehensiveAudioAnalysis(
        audioData: AudioDiagnosticData,
        audioFilePath: String,
        carLog: CarLog,
        user: User,
        previousDiagnostics: List<AudioDiagnosticData>
    ): Result<ComprehensiveAudioDiagnostic> = withContext(Dispatchers.IO) {
        try {
            // Validate audio file exists
            val audioFile = File(audioFilePath)
            if (!audioFile.exists() || !audioFile.canRead()) {
                Log.e(TAG, "❌ Audio file not found or unreadable: $audioFilePath")
                return@withContext Result.failure(
                    Exception("Audio file not accessible: $audioFilePath")
                )
            }
            
            Log.d(TAG, "📁 Audio file validated: ${audioFile.length() / 1024}KB")
            
            // Build the comprehensive prompt with ALL dynamic data
            val textPrompt = buildComprehensiveAudioAnalysisPrompt(
                audioData = audioData,
                carLog = carLog,
                user = user,
                previousDiagnostics = previousDiagnostics
            )
            
            Log.d(TAG, "🎵 Sending MULTIMODAL analysis to Gemini 3 Pro")
            Log.d(TAG, "   - TFLite Score: ${audioData.rawScore}/100")
            Log.d(TAG, "   - Top Sound: ${audioData.topSoundLabel} (${(audioData.topSoundConfidence * 100).toInt()}%)")
            Log.d(TAG, "   - Audio File: ${audioFile.name} (${audioFile.length() / 1024}KB)")
            Log.d(TAG, "   - Car: ${user.carDetails?.make} ${user.carDetails?.model} ${user.carDetails?.year}")
            Log.d(TAG, "   - Maintenance: ${carLog.maintenanceRecords.size} records")
            Log.d(TAG, "   - History: ${previousDiagnostics.size} previous diagnostics")
            
            // Create multimodal content with BOTH audio file and text analysis
            val multimodalContent = content {
                // Add the audio file for Gemini to analyze directly
                blob("audio/wav", audioFile.readBytes())
                
                // Add the comprehensive text prompt with TFLite analysis
                text(textPrompt)
            }
            
            // Send to Gemini 3 Pro (multimodal analysis)
            val response = diagnosticsModel.generateContent(multimodalContent)
            val responseText = response.text ?: return@withContext Result.failure(
                Exception("Empty response from Gemini AI")
            )
            
            Log.d(TAG, "✅ Received multimodal response from Gemini (${responseText.length} chars)")
            
            // Parse the JSON response
            val result = parseComprehensiveAudioResponse(responseText)
            
            Log.d(TAG, "🎯 Comprehensive multimodal analysis complete!")
            Log.d(TAG, "   - Enhanced Health Score: ${result.enhancedHealthScore}/100")
            Log.d(TAG, "   - Primary Diagnosis: ${result.primaryDiagnosis.issue}")
            Log.d(TAG, "   - Severity: ${result.primaryDiagnosis.severity}")
            Log.d(TAG, "   - Confidence: ${(result.primaryDiagnosis.confidence * 100).toInt()}%")
            Log.d(TAG, "   - Repair Scenarios: ${result.detailedRepairPlan.repairScenarios.size}")
            
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Comprehensive multimodal analysis error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // =============================================================================
    // COMBINED SMART ANALYSIS
    // =============================================================================
    
    /**
     * Generate comprehensive smart analysis combining audio, video, and price
     * 
     * @param carDetails Car information
     * @param audioResult Audio analysis result
     * @param videoResult Video analysis result
     * @return Combined smart diagnosis with price estimation
     */
    suspend fun performSmartAnalysis(
        carDetails: CarDetails,
        audioResult: AudioAnalysisResult?,
        videoResult: VideoAnalysisResult?
    ): Result<SmartAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildSmartAnalysisPrompt(carDetails, audioResult, videoResult)
            Log.d(TAG, "Performing smart analysis with Gemini")
            
            val response = diagnosticsModel.generateContent(prompt)
            val responseText = response.text ?: return@withContext Result.failure(
                Exception("Empty response from Gemini")
            )
            
            Log.d(TAG, "Gemini smart analysis response: $responseText")
            
            // Parse comprehensive result
            val result = parseSmartAnalysisResponse(responseText, carDetails, audioResult, videoResult)
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Gemini smart analysis error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // =============================================================================
    // PROMPT BUILDERS
    // =============================================================================
    
    private fun buildAudioAnalysisPrompt(classifications: List<AudioClassification>): String {
        val classificationsText = classifications.joinToString("\n") { 
            "- ${it.label}: ${(it.confidence * 100).toInt()}% confidence"
        }
        
        return """
You are an international automotive diagnostic expert with 25 years of experience. Analyze these engine sounds detected by an AI TensorFlow Lite system.

=== DETECTED SOUNDS ===
$classificationsText

=== MISSION ===
Provide an HONEST and PROFESSIONNELLE analysis in JSON format :

1. Identifie le problème principal (main_issue)
2. Liste les causes possibles (possible_causes)
3. Donne des recommandations concrètes (recommendations)
4. Calcule un score de santé moteur entre 0-100 (health_score)
   - 90-100: Excellent engine, normal sound
   - 70-89: Good condition, preventive maintenance
   - 50-69: Average state, problems to monitor
   - 30-49: Poor condition, repairs required
   - 0-29: Critical state, do not drive

RÈGLES STRICTES :
- Si "knocking" > 50% confidence → health_score MAX 60
- Si "grinding" > 50% confidence → health_score MAX 50
- Si "normal_engine" > 70% confidence → health_score MIN 80
- Be HONEST, never inflate the score

Respond ONLY in valid JSON :
{
  "main_issue": "Main issue description",
  "possible_causes": ["Cause 1", "Cause 2", "Cause 3"],
  "recommendations": ["Action 1", "Action 2", "Action 3"],
  "health_score": 75
}
        """.trimIndent()
    }
    
    private fun buildVideoAnalysisPrompt(description: String, anomalies: List<CarAnomaly>): String {
        val anomaliesText = if (anomalies.isNotEmpty()) {
            anomalies.joinToString("\n") { 
                "- ${it.type.name}: ${it.description} (Sévérité: ${it.severity.name}, Confiance: ${(it.confidence * 100).toInt()}%)"
            }
        } else {
                "No anomaly detected"
        }
        
        return """
Tu es un expert en diagnostic automobile international. Analyse cette vidéo de diagnostic automobile.

=== DESCRIPTION VIDÉO ===
$description

=== ANOMALIES DÉTECTÉES (ML Kit) ===
$anomaliesText

=== MISSION ===
Fournis une analyse HONNÊTE en JSON :

1. Résumé des observations visuelles (visual_summary)
2. Problèmes critiques identifiés (critical_issues)
3. Recommandations d'actions (recommendations)
4. Score de santé visuelle 0-100 (visual_health_score)
5. Coût estimé des réparations en$ (repair_cost_range)

RÈGLES :
- Fumée noire épaisse → score MAX 55
- Fumée blanche (joint culasse) → score MAX 40
- Vibrations excessives → -20 points
- Fuites d'huile visibles → -15 points

Respond ONLY in valid JSON :
{
  "visual_summary": "Visual summary...",
  "critical_issues": ["Problème 1", "Problème 2"],
  "recommendations": ["Action 1", "Action 2"],
  "visual_health_score": 65,
  "repair_cost_range": "5000 - 15000"
}
        """.trimIndent()
    }
    
    private fun buildPriceCheckPrompt(carDetails: CarDetails, diagnostics: String): String {
        return """
Tu es un expert du marché automobile international en décembre 2025.

=== DÉTAILS VÉHICULE ===
Marque : ${carDetails.brand}
Modèle : ${carDetails.model}
Année : ${carDetails.year}
Kilométrage : ${carDetails.mileage} km
Carburant : ${carDetails.fuelType}
Transmission : ${carDetails.transmission}

=== AI DIAGNOSTICS ===
$diagnostics

=== MISSION ===
Estimate a REALISTIC and HONEST resale price (no inflation!) :

1. Prix estimé moyen en$ (estimated_price_usd)
2. Fourchette basse-haute en$ (price_range)
3. Facteurs de dépréciation (depreciation_factors)
4. Conseil acheteur (buyer_advice)

RÈGLES STRICTES :
- Base-toi sur les prix RÉELS du marché 2025
- Déduis FORTEMENT pour problèmes moteur/transmission
- Knocking moteur → -20% à -40% du prix normal
- Fumée excessive → -15% à -30%
- Kilométrage > 150,000 km → dépréciation additionnelle
- Sois HONNÊTE : protège l'acheteur contre les arnaques

Respond ONLY in valid JSON :
{
  "estimated_price_usd": 125000,
  "price_range": "110000 - 135000",
  "depreciation_factors": ["Facteur 1", "Facteur 2"],
  "buyer_advice": "Buyer advice...",
  "confidence_level": "Haute/Moyenne/Faible"
}
        """.trimIndent()
    }
    
    private fun buildSmartAnalysisPrompt(
        carDetails: CarDetails,
        audioResult: AudioAnalysisResult?,
        videoResult: VideoAnalysisResult?
    ): String {
        val audioSummary = audioResult?.let {
            "Audio: ${it.mainIssue ?: "OK"} (Score santé: ${it.healthScore}/100)"
        } ?: "Audio: Non analysé"
        
        val videoSummary = videoResult?.let {
            val anomalyCount = it.anomalies.size
            "Video: $anomalyCount anomalies detected"
        } ?: "Video: Not analyzed"

        return """
You are an international AutoBrain expert with 25 years of experience. Generate a COMPLETE and HONNEST report.

=== VEHICLE ===
${carDetails.brand} ${carDetails.model} ${carDetails.year} - ${carDetails.mileage} km

=== DIAGNOSTICS ===
$audioSummary
$videoSummary

Audio détails : ${audioResult?.possibleCauses?.joinToString(", ") ?: "N/A"}
Vidéo détails : ${videoResult?.anomalies?.joinToString { it.description } ?: "N/A"}

=== COMPLETE MISSION ===
Generate a COMPLETE AutoBrain pre-sale report including :

1. Global AutoBrain score 0-100 (overall_score)
2. Major problems summary (major_issues)
3. Minor problems (minor_issues)
4. Urgent actions required (urgent_actions)
5. Realistic price estimation in$ (price_estimation)
6. Recommended purchase decision (recommendation)
   - "Buy without hesitation"
   - "Good value-price if negotiation"
   - "Moderate risk - mechanic inspection required"
   - "AVOID - too many problems"
7. Explication détaillée professionnelle (detailed_explanation)

RÈGLES STRICTES :
- Score basé 60% audio + 40% vidéo
- Problèmes graves → score global < 60
- Transparence TOTALE : liste TOUS les problèmes
- Prix basé sur le marché RÉEL 2025
- Protège l'acheteur contre arnaques

Respond ONLY in valid JSON :
{
  "overall_score": 72,
  "major_issues": ["Issue 1", "Issue 2"],
    "minor_issues": ["Minor issue 1"],
    "urgent_actions": ["Urgent action 1"],
    "price_estimation": "115000 - 135000$",
    "recommendation": "Recommendation category",
    "detailed_explanation": "Detailed professional explanation..."
}
        """.trimIndent()
    }
    
    private fun buildMaintenanceAnalysisPrompt(carDetails: CarDetails, history: String): String {
        return """
        You are an automotive maintenance expert. Analyze this vehicle's maintenance history to predict future risks and reminders.

=== VEHICLE ===
        ${carDetails.brand} ${carDetails.model} ${carDetails.year}
        Mileage: ${carDetails.mileage} km

=== MAINTENANCE HISTORY ===
        $history
        
        === MISSION ===
        1. Identify missing or overdue maintenance (oil change, belt, brakes, etc.)
        2. Estime l'impact négatif sur le Score AI (0 à -20 points)
        3. Analyze mechanical risks related to delays
        
        Respond ONLY in valid JSON :
        {
          "suggested_reminders": ["Engine oil change (Urgent)", "Brake check"],
    "ai_score_impact": -10,
    "risk_analysis": "High risk of engine failure if oil change not done soon.",
    "urgent_actions": ["Perform oil change immediately"]
        }
        """.trimIndent()
    }

    private fun buildSmartReminderPrompt(reminder: MaintenanceReminder, daysLeft: Int): String {
        val status = if (daysLeft < 0) "EN RETARD de ${-daysLeft} jours" else "dans $daysLeft jours"
        val tone = if (daysLeft < 0) "URGENT et ALARMISTE" else "INFORMATIF et BIENVEILLANT"
        
        return """
        You are the intelligent AutoBrain assistant. Generate a short notification (max 150 characters) for a maintenance reminder.
        
        Sujet: ${reminder.title}
        Description: ${reminder.description}
        Statut: $status
        Ton: $tone
        
        Si c'est en retard, mentionne un risque pour le "AI Score" (-10 pts).
        Si c'est à venir, rappelle l'importance pour la revente.
        
        Notification courte :
        """.trimIndent()
    }
    
    // =============================================================================
    // RESPONSE PARSERS
    // =============================================================================
    
    private fun parseAudioAnalysisResponse(
        responseText: String,
        originalClassifications: List<AudioClassification>
    ): AudioAnalysisResult {
        return try {
            val jsonText = extractJsonFromText(responseText)
            val jsonMap = parseJsonToMap(jsonText)
            
            AudioAnalysisResult(
                classifications = originalClassifications,
                mainIssue = jsonMap["main_issue"] as? String,
                possibleCauses = (jsonMap["possible_causes"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                recommendations = (jsonMap["recommendations"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                healthScore = (jsonMap["health_score"] as? Number)?.toInt() ?: 70
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse audio response, using fallback: ${e.message}")
            // Fallback: create basic result from classifications
            val mainClassification = originalClassifications.maxByOrNull { it.confidence }
            AudioAnalysisResult(
                classifications = originalClassifications,
                mainIssue = mainClassification?.label?.let { 
                    EngineSoundTypes.descriptions[it] 
                },
                possibleCauses = mainClassification?.label?.let {
                    EngineSoundTypes.recommendations[it] ?: emptyList()
                } ?: emptyList(),
                recommendations = listOf("Consult a mechanic for complete diagnosis"),
                healthScore = 70
            )
        }
    }
    
    private fun parseVideoAnalysisResponse(
        responseText: String,
        originalAnomalies: List<CarAnomaly>
    ): VideoAnalysisResult {
        return try {
            val jsonText = extractJsonFromText(responseText)
            val jsonMap = parseJsonToMap(jsonText)
            
            VideoAnalysisResult(
                detectedObjects = emptyList(),
                anomalies = originalAnomalies,
                frameCount = 0,
                analysisTime = System.currentTimeMillis(),
                summary = jsonMap["visual_summary"] as? String ?: "Analyse terminée",
                recommendations = (jsonMap["recommendations"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                visualHealthScore = (jsonMap["visual_health_score"] as? Number)?.toInt() ?: 70
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse video response: ${e.message}")
            VideoAnalysisResult(
                detectedObjects = emptyList(),
                anomalies = originalAnomalies,
                frameCount = 0,
                analysisTime = System.currentTimeMillis(),
                summary = "Erreur lors de l'analyse détaillée",
                recommendations = emptyList(),
                visualHealthScore = 70
            )
        }
    }
    
    private fun parsePriceEstimationResponse(
        responseText: String,
        carDetails: CarDetails
    ): PriceEstimation {
        return try {
            val jsonText = extractJsonFromText(responseText)
            val jsonMap = parseJsonToMap(jsonText)
            
            PriceEstimation(
                estimatedPrice = (jsonMap["estimated_price_usd"] as? Number)?.toDouble() ?: 0.0,
                priceRange = jsonMap["price_range"] as? String ?: "Prix non disponible",
                depreciationFactors = (jsonMap["depreciation_factors"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                buyerAdvice = jsonMap["buyer_advice"] as? String ?: "Consult an expert",
                confidenceLevel = jsonMap["confidence_level"] as? String ?: "Moyenne",
                currency = "USD"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse price response: ${e.message}")
            PriceEstimation(
                estimatedPrice = 0.0,
                priceRange = "Non disponible",
                depreciationFactors = emptyList(),
                buyerAdvice = "Erreur d'estimation - consulter un expert",
                confidenceLevel = "Faible",
                currency = "USD"
            )
        }
    }
    
    private fun parseSmartAnalysisResponse(
        responseText: String,
        carDetails: CarDetails,
        audioResult: AudioAnalysisResult?,
        videoResult: VideoAnalysisResult?
    ): SmartAnalysisResult {
        return try {
            val jsonText = extractJsonFromText(responseText)
            val jsonMap = parseJsonToMap(jsonText)
            
            SmartAnalysisResult(
                overallScore = (jsonMap["overall_score"] as? Number)?.toInt() ?: 70,
                majorIssues = (jsonMap["major_issues"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                minorIssues = (jsonMap["minor_issues"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                urgentActions = (jsonMap["urgent_actions"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                priceEstimation = jsonMap["price_estimation"] as? String ?: "Non disponible",
                recommendation = jsonMap["recommendation"] as? String ?: "Inspection requise",
                detailedExplanation = jsonMap["detailed_explanation"] as? String ?: "Analyse non disponible",
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse smart analysis response: ${e.message}")
            SmartAnalysisResult(
                overallScore = 70,
                majorIssues = listOf("Erreur d'analyse"),
                minorIssues = emptyList(),
                urgentActions = listOf("Consult a mechanic"),
                priceEstimation = "Non disponible",
                recommendation = "Inspection professionnelle requise",
                detailedExplanation = "Erreur lors de l'analyse Gemini",
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    private fun parseMaintenanceAnalysisResponse(responseText: String): MaintenanceAnalysisResult {
        return try {
            val jsonText = extractJsonFromText(responseText)
            val jsonMap = parseJsonToMap(jsonText)
            
            MaintenanceAnalysisResult(
                suggestedReminders = (jsonMap["suggested_reminders"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                aiScoreImpact = (jsonMap["ai_score_impact"] as? Number)?.toInt() ?: 0,
                riskAnalysis = jsonMap["risk_analysis"] as? String ?: "Analyse non disponible",
                urgentActions = (jsonMap["urgent_actions"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
            )
        } catch (e: Exception) {
            MaintenanceAnalysisResult(
                suggestedReminders = emptyList(),
                aiScoreImpact = 0,
                riskAnalysis = "Erreur d'analyse",
                urgentActions = emptyList()
            )
        }
    }
    
    // =============================================================================
    // COMPREHENSIVE VIDEO DIAGNOSTIC
    // =============================================================================
    
    /**
     * Perform COMPREHENSIVE Video Diagnostic Analysis with MULTIMODAL INPUT
     * 
     * 🎯 ENHANCED ARCHITECTURE (Same as Audio):
     * - Sends BOTH ML Kit analysis (text) AND actual video file (binary) to Gemini
     * - Gemini 3 Pro analyzes video directly for superior accuracy
     * - Combines on-device ML Kit insights with cloud AI video analysis
     * - Real-time Firestore user profile & car details
     * - Complete maintenance history from CarLog
     * - Previous diagnostic trends
     * - Audio diagnostics for multimodal correlation
     * - Market context & legal compliance
     * 
     * @param videoData Current video diagnostic data with ML Kit results
     * @param videoFilePath Path to the actual MP4 video file
     * @param carLog Complete car maintenance log from Firestore
     * @param user User profile with car details from Firestore
     * @param previousVideoDiagnostics Historical video diagnostics for trend analysis
     * @param audioDiagnostics Audio diagnostics for multimodal correlation
     * @return Comprehensive diagnostic result with 10 detailed sections
     */
    suspend fun performComprehensiveVideoAnalysisMultimodal(
        videoData: VideoDiagnosticData,
        videoFilePath: String,
        carLog: CarLog,
        user: User,
        previousVideoDiagnostics: List<VideoDiagnosticData>,
        audioDiagnostics: List<AudioDiagnosticData>
    ): Result<ComprehensiveVideoDiagnostic> = withContext(Dispatchers.IO) {
        try {
            // Check if video file exists (optional for multimodal)
            val videoFile = if (videoFilePath.isNotEmpty()) File(videoFilePath) else null
            val hasVideoFile = videoFile?.exists() == true && videoFile.canRead()
            
            if (!hasVideoFile) {
                Log.w(TAG, "⚠️ No video file available, using text-only analysis")
            }
            
            Log.d(TAG, "📁 Video: ${if (hasVideoFile) "${videoFile!!.length() / 1024}KB" else "Not available"}")
            
            // Build the comprehensive prompt
            val textPrompt = buildComprehensiveVideoAnalysisPrompt(
                videoData = videoData,
                carLog = carLog,
                user = user,
                previousVideoDiagnostics = previousVideoDiagnostics,
                audioDiagnostics = audioDiagnostics
            )
            
            Log.d(TAG, "🎬 Sending ${if (hasVideoFile) "MULTIMODAL" else "TEXT-ONLY"} analysis to Gemini 3 Pro")
            Log.d(TAG, "   - ML Kit Score: ${videoData.rawScore}/100")
            Log.d(TAG, "   - Smoke: ${if (videoData.smokeDetected) "${videoData.smokeType} (${videoData.smokeSeverity}/5)" else "None"}")
            Log.d(TAG, "   - Vibration: ${if (videoData.vibrationDetected) "${videoData.vibrationLevel} (${videoData.vibrationSeverity}/5)" else "None"}")
            if (hasVideoFile) {
                Log.d(TAG, "   - Video File: ${videoFile!!.name} (${videoFile.length() / 1024}KB)")
            }
            Log.d(TAG, "   - Car: ${user.carDetails?.make} ${user.carDetails?.model} ${user.carDetails?.year}")
            Log.d(TAG, "   - Maintenance: ${carLog.maintenanceRecords.size} records")
            Log.d(TAG, "   - History: ${previousVideoDiagnostics.size} previous video diagnostics")
            Log.d(TAG, "   - Audio Correlation: ${audioDiagnostics.size} audio diagnostics")
            
            // Create content (multimodal if video exists, text-only otherwise)
            val content = if (hasVideoFile) {
                content {
                    blob("video/mp4", videoFile!!.readBytes())
                    text(textPrompt)
                }
            } else {
                content {
                    text(textPrompt)
                }
            }
            
            // Send to Gemini 3 Pro
            val response = diagnosticsModel.generateContent(content)
            val responseText = response.text ?: return@withContext Result.failure(
                Exception("Empty response from Gemini AI")
            )
            
            Log.d(TAG, "✅ Received ${if (hasVideoFile) "multimodal" else "text-only"} video response from Gemini (${responseText.length} chars)")
            
            // Parse the JSON response
            val result = parseComprehensiveVideoResponse(responseText)
            
            Log.d(TAG, "🎯 Comprehensive multimodal video analysis complete!")
            Log.d(TAG, "   - Enhanced Visual Score: ${result.enhancedVisualScore}/100")
            Log.d(TAG, "   - Smoke Type: ${result.smokeDeepAnalysis.typeDetected}")
            Log.d(TAG, "   - Safety: ${result.safetyAssessment.roadworthiness}")
            Log.d(TAG, "   - Confidence: ${(result.autobrainVideoConfidence.confidenceThisAnalysis * 100).toInt()}%")
            Log.d(TAG, "   - Repair Scenarios: ${result.repairScenariosVisual.size}")
            
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Comprehensive multimodal video analysis error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Parse comprehensive video diagnostic response from Gemini
     */
    private fun parseComprehensiveVideoResponse(responseText: String): ComprehensiveVideoDiagnostic {
        return try {
            val jsonText = extractJsonFromText(responseText)
            Log.d(TAG, "Parsing comprehensive video JSON: ${jsonText.take(200)}...")
            
            // Use Gson to parse the complex JSON structure
            val gson = Gson()
            val result = gson.fromJson(jsonText, ComprehensiveVideoDiagnostic::class.java)
            
            Log.d(TAG, "✅ Successfully parsed comprehensive video diagnostic")
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to parse comprehensive video response: ${e.message}", e)
            Log.e(TAG, "Response text (first 500 chars): ${responseText.take(500)}")
            
            // Return fallback comprehensive result
            ComprehensiveVideoDiagnostic(
                enhancedVisualScore = 50,
                smokeDeepAnalysis = SmokeDeepAnalysis(
                    typeDetected = "unknown",
                    technicalDiagnosis = "Erreur d'analyse - Données insuffisantes",
                    chemicalCompositionTheory = "N/A",
                    emissionPattern = "N/A",
                    smellPrediction = "N/A",
                    colorIntensity = "N/A",
                    rootCausesByProbability = emptyList(),
                    worstCaseScenario = "Analyse non disponible",
                    immediateRisks = listOf("Consult a professional mechanic")
                ),
                vibrationEngineeringAnalysis = VibrationEngineeringAnalysis(
                    vibrationFrequencyEstimation = "N/A",
                    vibrationSourceDiagnosis = "N/A",
                    phaseAnalysis = "N/A",
                    probableMechanicalCauses = emptyList(),
                    cascadingFailuresIfIgnored = emptyList()
                ),
                combinedAudioVideoDiagnosis = CombinedAudioVideoDiagnosis(
                    correlationScore = 0.5f,
                    multimodalInsights = listOf("Analyse non disponible"),
                    comprehensiveRootCause = "Impossible à déterminer",
                    confidenceBoost = "N/A"
                ),
                repairScenariosVisual = emptyList(),
                videoQualityAssessment = VideoQualityAssessment(
                    recordingQualityScore = "50/100",
                    technicalIssues = emptyList(),
                    recommendationForRerecording = true,
                    optimalRecordingConditions = listOf("Enregistrer en plein jour avec téléphone stabilisé")
                ),
                safetyAssessment = SafetyAssessment(
                    roadworthiness = "CAUTION",
                    drivingRestrictions = listOf("Consult a mechanic before driving"),
                    breakdownProbabilityNext30Days = 0.5f,
                    towingRecommendation = false,
                    insuranceClaimViability = "Indéterminé"
                ),
                marketImpactVisual = MarketImpactVisual(
                    buyerPerception = "Indéterminé",
                    negotiationLeverageSeller = "MOYEN",
                    priceReductionExpectedUsd = 0.0,
                    timeToSellEstimateDays = 90,
                    disclosureRequirement = "Consult an expert"
                ),
                environmentalCompliance = EnvironmentalCompliance(
                    emissionTestPassProbability = "0.5",
                    pollutionLevel = "Indéterminé",
                    controleTechniqueImpact = "Indéterminé",
                    vignettePollutionEligibility = "Indéterminé"
                ),
                autobrainVideoConfidence = AutobrainVideoConfidence(
                    mlKitAccuracy = "87%",
                    confidenceThisAnalysis = 0.3f,
                    factorsAffectingConfidence = listOf("Erreur de parsing JSON", "Réponse Gemini invalide"),
                    geminiModel = "gemini-3-pro-preview",
                    analysisTimestamp = System.currentTimeMillis()
                )
            )
        }
    }
    
    // =============================================================================
    // ULTIMATE SMART ANALYSIS (Combined Audio + Video + Price + History)
    // =============================================================================
    
    /**
     * Perform ultimate smart analysis combining ALL data sources
     * 
     * @param prompt Complete ultimate analysis prompt
     * @return Ultimate smart analysis with 15 sections
     */
    suspend fun performUltimateSmartAnalysis(
        prompt: String
    ): Result<UltimateSmartAnalysis> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎯 Sending ULTIMATE smart analysis to Gemini 3 Pro")
            
            // Send to Gemini 3 Pro with extended tokens
            val response = diagnosticsModel.generateContent(prompt)
            val responseText = response.text ?: return@withContext Result.failure(
                Exception("Empty response from Gemini AI")
            )
            
            Log.d(TAG, "Received ultimate response from Gemini (${responseText.length} chars)")
            
            // Parse the JSON response
            val result = parseUltimateSmartResponse(responseText)
            
            Log.d(TAG, "✅ Ultimate smart analysis complete!")
            Log.d(TAG, "Overall Score: ${result.overallAutobrainScore}/100")
            Log.d(TAG, "Primary Issue: ${result.comprehensiveDiagnosis.primaryIssue}")
            
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ultimate smart analysis error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Parse ultimate smart analysis response from Gemini
     */
    private fun parseUltimateSmartResponse(responseText: String): UltimateSmartAnalysis {
        return try {
            val jsonText = extractJsonFromText(responseText)
            Log.d(TAG, "Parsing ultimate smart JSON: ${jsonText.take(200)}...")
            
            // Use Gson to parse the complex JSON structure
            val gson = Gson()
            val result = gson.fromJson(jsonText, UltimateSmartAnalysis::class.java)
            
            Log.d(TAG, "✅ Successfully parsed ultimate smart analysis")
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to parse ultimate response: ${e.message}", e)
            
            // Return minimal fallback
            throw e // Rethrow to be caught by caller
        }
    }
    
    // =============================================================================
    // DYNAMIC PRICE ESTIMATION (Firebase + Gemini)
    // =============================================================================
    
    /**
     * Perform comprehensive price estimation with Firebase dynamic data
     * 
     * @param prompt Complete price estimation prompt with Firebase context
     * @return Gemini price estimation with market analysis
     */
    suspend fun performPriceEstimation(
        prompt: String
    ): Result<GeminiPriceEstimation> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "💰 Sending price estimation to Gemini 3 Pro")
            
            // Send to Gemini
            val response = diagnosticsModel.generateContent(prompt)
            val responseText = response.text ?: return@withContext Result.failure(
                Exception("Empty response from Gemini AI")
            )
            
            Log.d(TAG, "Received price response from Gemini (${responseText.length} chars)")
            
            // Parse JSON
            val result = parsePriceEstimationResponse(responseText)
            
            Log.d(TAG, "✅ Price estimation complete!")
            Log.d(TAG, "Price Range: ${result.minPrice} - $${result.maxPrice}")
            
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Price estimation error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Parse price estimation response from Gemini
     */
    private fun parsePriceEstimationResponse(
        responseText: String
    ): GeminiPriceEstimation {
        return try {
            val jsonText = extractJsonFromText(responseText)
            Log.d(TAG, "Parsing price JSON: ${jsonText.take(200)}...")
            
            val gson = Gson()
            val jsonMap = gson.fromJson<Map<String, Any?>>(jsonText, object : com.google.gson.reflect.TypeToken<Map<String, Any?>>() {}.type)
            
            // Parse factors
            val factorsList = (jsonMap["factors"] as? List<*>)?.mapNotNull { factorMap ->
                val map = factorMap as? Map<*, *>
                map?.let {
                    PriceFactor(
                        name = map["name"] as? String ?: "",
                        value = map["value"] as? String ?: "",
                        isPositive = map["is_positive"] as? Boolean ?: true,
                        impact = map["impact"] as? String ?: "Medium"
                    )
                }
            } ?: emptyList()
            
            GeminiPriceEstimation(
                minPrice = (jsonMap["min_price_usd"] as? Number)?.toInt() ?: 0,
                maxPrice = (jsonMap["max_price_usd"] as? Number)?.toInt() ?: 0,
                avgPrice = (jsonMap["avg_price_usd"] as? Number)?.toInt() ?: 0,
                confidence = (jsonMap["confidence"] as? Number)?.toFloat() ?: 0.5f,
                factors = factorsList,
                marketAnalysis = jsonMap["market_analysis"] as? String ?: "",
                geminiInsights = (jsonMap["gemini_insights"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                depreciationFactors = (jsonMap["depreciation_factors"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                negotiationTips = (jsonMap["negotiation_tips"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to parse price response: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Parse comprehensive audio diagnostic response from Gemini
     */
    private fun parseComprehensiveAudioResponse(responseText: String): ComprehensiveAudioDiagnostic {
        return try {
            val jsonText = extractJsonFromText(responseText)
            Log.d(TAG, "Parsing comprehensive JSON: ${jsonText.take(200)}...")
            
            // Use Gson to parse the complex JSON structure
            val gson = Gson()
            val result = gson.fromJson(jsonText, ComprehensiveAudioDiagnostic::class.java)
            
            Log.d(TAG, "✅ Successfully parsed comprehensive diagnostic")
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to parse comprehensive response: ${e.message}", e)
            Log.e(TAG, "Response text (first 500 chars): ${responseText.take(500)}")
            
            // Return fallback comprehensive result
            ComprehensiveAudioDiagnostic(
                enhancedHealthScore = 50,
                primaryDiagnosis = PrimaryDiagnosis(
                    issue = "Erreur d'analyse - Données insuffisantes",
                    technicalName = "Analysis Error",
                    confidence = 0.5f,
                    severity = "MEDIUM",
                    affectedComponents = listOf("Analyse non disponible")
                ),
                secondaryIssues = emptyList(),
                rootCauseAnalysis = RootCauseAnalysis(
                    mostLikelyCause = "Impossible à déterminer",
                    probability = 0.0f,
                    alternativeCauses = emptyList(),
                    evidence = listOf("Erreur lors de l'analyse Gemini AI")
                ),
                progressiveDamagePrediction = ProgressiveDamagePrediction(
                    currentStage = "Inconnu",
                    nextFailureTimeline = "Indéterminé",
                    finalFailureDescription = "Analyse non disponible",
                    cascadingFailures = emptyList()
                ),
                detailedRepairPlan = DetailedRepairPlan(
                    immediateActions = listOf("Consult a professional mechanic"),
                    repairScenarios = emptyList(),
                    recommendedGarageType = "Garage spécialisé",
                    negotiationTip = "Demander plusieurs devis"
                ),
                marketValueImpact = MarketValueImpact(
                    valueBeforeIssue = 0.0,
                    valueAfterRepair = 0.0,
                    valueAsIs = 0.0,
                    depreciationFactors = emptyList(),
                    resaleTimeline = "Indéterminé",
                    buyerNegotiationPower = "Indéterminé"
                ),
                maintenanceCorrelation = MaintenanceCorrelation(
                    oilChangeImpact = "Indéterminé",
                    mileageFactor = "Indéterminé",
                    serviceHistoryQuality = "UNKNOWN",
                    preventablePercentage = 0,
                    lessonsLearned = emptyList()
                ),
                intelligentRecommendations = IntelligentRecommendations(
                    forCurrentOwner = listOf("Consult a professional for complete diagnosis"),
    forPotentialBuyer = listOf("Have inspected by an independent mechanic"),
                    forMechanic = listOf("Effectuer un diagnostic complet")
                ),
                autobrainAiConfidence = AutobrainAiConfidence(
                    analysisConfidence = 0.3f,
                    dataQualityScore = 0.5f,
                    tfliteModelAccuracy = "N/A",
                    factorsBoostingConfidence = emptyList(),
                    uncertaintyFactors = listOf("Erreur de parsing JSON", "Réponse Gemini invalide"),
                    recommendSecondOpinion = true,
                    geminiModelVersion = "gemini-3-pro-preview",
                    analysisTimestampUtc = System.currentTimeMillis().toString()
                ),
                legalComplianceGeneral = LegalComplianceGeneral(
                    inspectionRequirements = "Indéterminé",
                    insuranceNotificationRequired = false,
                    roadworthiness = "UNKNOWN",
                    legalResaleObligations = emptyList()
                )
            )
        }
    }

    // =============================================================================
    // UTILITY METHODS
    // =============================================================================
    
    private fun extractJsonFromText(text: String): String {
        // Remove markdown code blocks if present
        val cleanText = text.replace("```json", "").replace("```", "").trim()
        
        // Find JSON object boundaries
        val jsonStart = cleanText.indexOf('{')
        val jsonEnd = cleanText.lastIndexOf('}')
        
        return if (jsonStart >= 0 && jsonEnd > jsonStart) {
            cleanText.substring(jsonStart, jsonEnd + 1)
        } else {
            cleanText
        }
    }
    
    private fun parseJsonToMap(jsonText: String): Map<String, Any?> {
        // Simple JSON parser (you could use Gson for production)
        return try {
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Any?>>() {}.type
            gson.fromJson<Map<String, Any?>>(jsonText, type)
        } catch (e: Exception) {
            Log.e(TAG, "JSON parsing error: ${e.message}")
            emptyMap()
        }
    }
}

// =============================================================================
// DATA MODELS FOR GEMINI INTEGRATION
// =============================================================================

data class CarDetails(
    val brand: String,
    val model: String,
    val year: Int,
    val mileage: Int,
    val fuelType: String = "Diesel",
    val transmission: String = "Manuelle"
)

data class PriceEstimation(
    val estimatedPrice: Double,
    val priceRange: String,
    val depreciationFactors: List<String>,
    val buyerAdvice: String,
    val confidenceLevel: String,
    val currency: String = "USD"
)

data class SmartAnalysisResult(
    val overallScore: Int,
    val majorIssues: List<String>,
    val minorIssues: List<String>,
    val urgentActions: List<String>,
    val priceEstimation: String,
    val recommendation: String,
    val detailedExplanation: String,
    val timestamp: Long
)

data class MaintenanceAnalysisResult(
    val suggestedReminders: List<String>,
    val aiScoreImpact: Int,
    val riskAnalysis: String,
    val urgentActions: List<String>
)
