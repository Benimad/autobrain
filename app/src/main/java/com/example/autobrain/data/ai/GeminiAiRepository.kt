package com.example.autobrain.data.ai

import android.content.Context
import android.util.Log
import com.example.autobrain.BuildConfig
import com.example.autobrain.data.local.entity.AudioDiagnosticData
import com.example.autobrain.domain.model.CarLog
import com.example.autobrain.domain.model.GeminiPriceEstimation
import com.example.autobrain.domain.model.MaintenanceReminder
import com.example.autobrain.domain.model.PriceFactor
import com.example.autobrain.domain.model.User
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Gemini AI Repository for AutoBrain
 * 
 * Integrates Gemini 2.5 Pro/Flash for:
 * - Audio diagnostics (engine sound analysis)
 * - Video diagnostics (smoke, vibration detection)
 * - Price estimation (market context)
 * - Combined smart analysis
 * 
 * Models: gemini-2.5-pro (main), gemini-2.5-flash (fast)
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
    
    // Gemini 2.5 Pro model for multimodal analysis (Latest, June 2025)
    private val diagnosticsModel = GenerativeModel(
        modelName = "gemini-2.5-pro",
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
    
    // Gemini 2.5 Flash for faster responses (Latest, June 2025)
    private val fastModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
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
     * Perform COMPREHENSIVE Audio Diagnostic Analysis with FULL Firebase Data
     * 
     * This is the COMPLETE implementation integrating:
     * - Real-time Firestore user profile & car details
     * - Complete maintenance history from CarLog
     * - Previous diagnostic trends
     * - Market context & legal compliance
     * 
     * @param audioData Current audio diagnostic data with TFLite classifications
     * @param carLog Complete car maintenance log from Firestore
     * @param user User profile with car details from Firestore
     * @param previousDiagnostics Historical audio diagnostics for trend analysis
     * @return Comprehensive diagnostic result with 11 detailed sections
     */
    suspend fun performComprehensiveAudioAnalysis(
        audioData: AudioDiagnosticData,
        carLog: CarLog,
        user: User,
        previousDiagnostics: List<AudioDiagnosticData>
    ): Result<ComprehensiveAudioDiagnostic> = withContext(Dispatchers.IO) {
        try {
            // Build the comprehensive prompt with ALL dynamic data
            val prompt = buildComprehensiveAudioAnalysisPrompt(
                audioData = audioData,
                carLog = carLog,
                user = user,
                previousDiagnostics = previousDiagnostics
            )
            
            Log.d(TAG, "Sending COMPREHENSIVE audio analysis to Gemini 2.5 Pro")
            Log.d(TAG, "Audio Score: ${audioData.rawScore}, Top Sound: ${audioData.topSoundLabel}")
            Log.d(TAG, "Car: ${user.carDetails?.make} ${user.carDetails?.model} ${user.carDetails?.year}")
            Log.d(TAG, "Maintenance Records: ${carLog.maintenanceRecords.size}, Previous Diagnostics: ${previousDiagnostics.size}")
            
            // Send to Gemini 2.5 Pro (with extended token limit for comprehensive response)
            val response = diagnosticsModel.generateContent(prompt)
            val responseText = response.text ?: return@withContext Result.failure(
                Exception("Empty response from Gemini AI")
            )
            
            Log.d(TAG, "Received comprehensive response from Gemini (${responseText.length} chars)")
            
            // Parse the JSON response
            val result = parseComprehensiveAudioResponse(responseText)
            
            Log.d(TAG, "✅ Comprehensive analysis complete!")
            Log.d(TAG, "Enhanced Health Score: ${result.enhancedHealthScore}/100")
            Log.d(TAG, "Primary Diagnosis: ${result.primaryDiagnosis.issue}")
            Log.d(TAG, "Severity: ${result.primaryDiagnosis.severity}")
            Log.d(TAG, "Repair Scenarios: ${result.detailedRepairPlan.repairScenarios.size}")
            
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Comprehensive audio analysis error: ${e.message}", e)
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
Tu es un expert en diagnostic automobile international avec 25 ans d'expérience. Analyse ces sons moteur détectés par un système d'IA TensorFlow Lite.

=== SONS DÉTECTÉS ===
$classificationsText

=== MISSION ===
Fournis une analyse HONNÊTE et PROFESSIONNELLE en format JSON :

1. Identifie le problème principal (main_issue)
2. Liste les causes possibles (possible_causes)
3. Donne des recommandations concrètes (recommendations)
4. Calcule un score de santé moteur entre 0-100 (health_score)
   - 90-100: Moteur excellent, son normal
   - 70-89: Bon état, maintenance préventive
   - 50-69: État moyen, problèmes à surveiller
   - 30-49: Mauvais état, réparations nécessaires
   - 0-29: État critique, ne pas conduire

RÈGLES STRICTES :
- Si "knocking" > 50% confidence → health_score MAX 60
- Si "grinding" > 50% confidence → health_score MAX 50
- Si "normal_engine" > 70% confidence → health_score MIN 80
- Sois HONNÊTE, ne gonfle JAMAIS le score

Réponds UNIQUEMENT en JSON valide :
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
            "Aucune anomalie détectée"
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

Réponds UNIQUEMENT en JSON valide :
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

=== DIAGNOSTICS IA ===
$diagnostics

=== MISSION ===
Estime un prix de revente RÉALISTE et HONNÊTE (pas d'inflation !) :

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

Réponds UNIQUEMENT en JSON valide :
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
            "Vidéo: $anomalyCount anomalies détectées"
        } ?: "Vidéo: Non analysée"
        
        return """
Tu es un expert AutoBrain international avec 25 ans d'expérience. Génère un rapport COMPLET et HONNÊTE.

=== VÉHICULE ===
${carDetails.brand} ${carDetails.model} ${carDetails.year} - ${carDetails.mileage} km

=== DIAGNOSTICS ===
$audioSummary
$videoSummary

Audio détails : ${audioResult?.possibleCauses?.joinToString(", ") ?: "N/A"}
Vidéo détails : ${videoResult?.anomalies?.joinToString { it.description } ?: "N/A"}

=== MISSION COMPLÈTE ===
Génère un rapport de pré-vente AutoBrain COMPLET incluant :

1. Score global AutoBrain 0-100 (overall_score)
2. Résumé des problèmes majeurs (major_issues)
3. Problèmes mineurs (minor_issues)
4. Actions urgentes requises (urgent_actions)
5. Estimation prix réaliste en$ (price_estimation)
6. Décision achat recommandée (recommendation)
   - "Acheter sans hésiter"
   - "Bon rapport qualité-prix si négociation"
   - "Risque modéré - inspection mécanicien requise"
   - "ÉVITER - trop de problèmes"
7. Explication détaillée professionnelle (detailed_explanation)

RÈGLES STRICTES :
- Score basé 60% audio + 40% vidéo
- Problèmes graves → score global < 60
- Transparence TOTALE : liste TOUS les problèmes
- Prix basé sur le marché RÉEL 2025
- Protège l'acheteur contre arnaques

Réponds UNIQUEMENT en JSON valide :
{
  "overall_score": 72,
  "major_issues": ["Problème 1", "Problème 2"],
  "minor_issues": ["Problème mineur 1"],
  "urgent_actions": ["Action urgente 1"],
  "price_estimation": "115000 - 135000$",
  "recommendation": "Catégorie de recommandation",
  "detailed_explanation": "Detailed professional explanation..."
}
        """.trimIndent()
    }
    
    private fun buildMaintenanceAnalysisPrompt(carDetails: CarDetails, history: String): String {
        return """
        Tu es un expert en maintenance automobile. Analyse l'historique d'entretien de ce véhicule pour prédire les risques et rappels futurs.
        
        === VÉHICULE ===
        ${carDetails.brand} ${carDetails.model} ${carDetails.year}
        Kilométrage: ${carDetails.mileage} km
        
        === HISTORIQUE MAINTENANCE ===
        $history
        
        === MISSION ===
        1. Identifie les entretiens manquants ou en retard (vidange, courroie, freins, etc.)
        2. Estime l'impact négatif sur le Score AI (0 à -20 points)
        3. Analyse les risques mécaniques liés aux retards
        
        Réponds UNIQUEMENT en JSON valide :
        {
          "suggested_reminders": ["Vidange moteur (Urgent)", "Contrôle freins"],
          "ai_score_impact": -10,
          "risk_analysis": "Risque élevé de casse moteur si vidange non faite rapidement.",
          "urgent_actions": ["Faire la vidange immédiatement"]
        }
        """.trimIndent()
    }

    private fun buildSmartReminderPrompt(reminder: MaintenanceReminder, daysLeft: Int): String {
        val status = if (daysLeft < 0) "EN RETARD de ${-daysLeft} jours" else "dans $daysLeft jours"
        val tone = if (daysLeft < 0) "URGENT et ALARMISTE" else "INFORMATIF et BIENVEILLANT"
        
        return """
        Tu es l'assistant intelligent AutoBrain. Génère une notification courte (max 150 caractères) pour un rappel d'entretien.
        
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
                recommendations = listOf("Consulter un mécanicien pour diagnostic complet"),
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
                buyerAdvice = jsonMap["buyer_advice"] as? String ?: "Consulter un expert",
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
                urgentActions = listOf("Consulter un mécanicien"),
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
     * Perform comprehensive video diagnostic analysis with Gemini
     * 
     * @param prompt Complete comprehensive video analysis prompt
     * @return Comprehensive video diagnostic with 10 sections
     */
    suspend fun performComprehensiveVideoAnalysis(
        prompt: String
    ): Result<ComprehensiveVideoDiagnostic> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎬 Sending COMPREHENSIVE video analysis to Gemini 2.5 Pro")
            
            // Send to Gemini 2.5 Pro
            val response = diagnosticsModel.generateContent(prompt)
            val responseText = response.text ?: return@withContext Result.failure(
                Exception("Empty response from Gemini AI")
            )
            
            Log.d(TAG, "Received comprehensive video response from Gemini (${responseText.length} chars)")
            
            // Parse the JSON response
            val result = parseComprehensiveVideoResponse(responseText)
            
            Log.d(TAG, "✅ Comprehensive video analysis complete!")
            Log.d(TAG, "Enhanced Visual Score: ${result.enhancedVisualScore}/100")
            Log.d(TAG, "Smoke Type: ${result.smokeDeepAnalysis.typeDetected}")
            Log.d(TAG, "Safety: ${result.safetyAssessment.roadworthiness}")
            
            Result.success(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Comprehensive video analysis error: ${e.message}", e)
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
                    immediateRisks = listOf("Consulter un mécanicien professionnel")
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
                    drivingRestrictions = listOf("Consulter un mécanicien avant de conduire"),
                    breakdownProbabilityNext30Days = 0.5f,
                    towingRecommendation = false,
                    insuranceClaimViability = "Indéterminé"
                ),
                marketImpactVisual = MarketImpactVisual(
                    buyerPerception = "Indéterminé",
                    negotiationLeverageSeller = "MOYEN",
                    priceReductionExpectedUsd = 0.0,
                    timeToSellEstimateDays = 90,
                    disclosureRequirement = "Consulter un expert"
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
                    geminiModel = "gemini-2.5-pro",
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
            Log.d(TAG, "🎯 Sending ULTIMATE smart analysis to Gemini 2.5 Pro")
            
            // Send to Gemini 2.5 Pro with extended tokens
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
            Log.d(TAG, "💰 Sending price estimation to Gemini 2.5 Pro")
            
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
                    immediateActions = listOf("Consulter un mécanicien professionnel"),
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
                    forCurrentOwner = listOf("Consulter un professionnel pour diagnostic complet"),
                    forPotentialBuyer = listOf("Faire inspecter par un mécanicien indépendant"),
                    forMechanic = listOf("Effectuer un diagnostic complet")
                ),
                autobrainAiConfidence = AutobrainAiConfidence(
                    analysisConfidence = 0.3f,
                    dataQualityScore = 0.5f,
                    tfliteModelAccuracy = "N/A",
                    factorsBoostingConfidence = emptyList(),
                    uncertaintyFactors = listOf("Erreur de parsing JSON", "Réponse Gemini invalide"),
                    recommendSecondOpinion = true,
                    geminiModelVersion = "gemini-2.5-pro",
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
