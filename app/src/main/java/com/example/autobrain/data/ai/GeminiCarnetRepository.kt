package com.example.autobrain.data.ai

import android.content.Context
import android.util.Log
import com.example.autobrain.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini 2.0 Flash Carnet Intelligent Repository
 * AI-Powered Smart Maintenance System for AutoBrain
 * 
 * Features:
 * - Auto-generated maintenance reminders
 * - Cost predictions with market data
 * - Quality analysis of maintenance history
 * - Optimal scheduling suggestions
 * - Risk assessment and prevention
 */
@Singleton
class GeminiCarnetRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "GeminiCarnetRepository"
    
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val gson = Gson()
    
    // Gemini 2.0 Flash - Faster, smarter, more efficient
    private val carnetModel = GenerativeModel(
        modelName = "gemini-2.0-flash-exp",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 1f  // Creative but controlled
            topK = 40
            topP = 0.95f
            maxOutputTokens = 8192
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE)
        )
    )
    
    /**
     * 🧠 COMPREHENSIVE MAINTENANCE ANALYSIS
     * Analyzes full maintenance history and provides detailed insights
     */
    suspend fun analyzeMaintenanceHistory(
        carDetails: GeminiCarDetails,
        maintenanceRecords: List<MaintenanceRecordData>
    ): Result<MaintenanceAnalysis> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildComprehensiveAnalysisPrompt(carDetails, maintenanceRecords)
            Log.d(TAG, "🔍 Analyzing maintenance with Gemini 2.0 Flash...")
            
            val response = carnetModel.generateContent(prompt)
            val responseText = response.text ?: return@withContext Result.failure(
                Exception("Empty Gemini response")
            )
            
            Log.d(TAG, "✅ Gemini analysis complete")
            val analysis = parseMaintenanceAnalysis(responseText, carDetails, maintenanceRecords)
            Result.success(analysis)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Gemini analysis error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 🔔 AUTO-GENERATE SMART REMINDERS
     * AI creates personalized maintenance reminders based on car data
     */
    suspend fun generateSmartReminders(
        carDetails: GeminiCarDetails,
        currentMileage: Int,
        lastMaintenanceDates: Map<String, Long>
    ): Result<List<SmartReminder>> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildSmartRemindersPrompt(carDetails, currentMileage, lastMaintenanceDates)
            Log.d(TAG, "🔔 Generating smart reminders with Gemini 2.0 Flash...")
            
            val response = carnetModel.generateContent(prompt)
            val responseText = response.text ?: return@withContext Result.failure(
                Exception("Empty response")
            )
            
            Log.d(TAG, "✅ Smart reminders generated")
            val reminders = parseSmartReminders(responseText, currentMileage)
            Result.success(reminders)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Reminders generation error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 💰 COST PREDICTION
     * Predicts maintenance costs for the next 12 months
     */
    suspend fun predictMaintenanceCosts(
        carDetails: GeminiCarDetails,
        currentMileage: Int,
        averageMonthlyKm: Int
    ): Result<CostPrediction> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Predict maintenance costs for the next 12 months:
                
                Véhicule : ${carDetails.brand} ${carDetails.model} ${carDetails.year}
                Kilométrage actuel : $currentMileage km
                Km moyens par mois : $averageMonthlyKm km
                
                Génère une prédiction détaillée au format JSON :
                {
                    "total_annual_cost_usd": 12000,
                    "monthly_breakdown": [
                        {
                            "month": "January",
                            "estimated_cost_usd": 500,
                            "maintenance_items": ["Oil change"],
                            "priority": "HIGH|MEDIUM|LOW"
                        }
                    ],
                    "major_services": [
                        {
                            "service": "Full service",
                            "when": "In 3 months",
                            "cost_usd": 2500,
                            "why": "Reason"
                        }
                    ],
                    "savings_tips": ["Tip 1", "Tip 2"],
                    "confidence_level": 85
                }
                
                Base les prix sur le marché réel.
            """.trimIndent()
            
            val response = carnetModel.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty response")
            
            val prediction = parseCostPrediction(responseText)
            Result.success(prediction)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Cost prediction error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 📊 MAINTENANCE QUALITY SCORE
     * Evaluates maintenance quality and impact on car value
     */
    suspend fun evaluateMaintenanceQuality(
        carDetails: GeminiCarDetails,
        maintenanceRecords: List<MaintenanceRecordData>,
        currentMileage: Int
    ): Result<QualityEvaluation> = withContext(Dispatchers.IO) {
        try {
            val recordsText = maintenanceRecords.sortedByDescending { it.date }
                .take(20)
                .joinToString("\n") { 
                    "- ${it.date}: ${it.type} à ${it.mileage} km ($${it.cost}) - ${it.serviceProvider}"
                }
            
            val prompt = """
                Evaluate the QUALITY of this vehicle's maintenance:

Vehicle: ${carDetails.brand} ${carDetails.model} ${carDetails.year}
Current mileage: $currentMileage km
Total maintenance: ${maintenanceRecords.size}

Recent history:
                ${recordsText.ifEmpty { "No history" }}

Generate a JSON evaluation:
                {
                    "overall_quality_score": 85,
                    "maintenance_consistency": "Excellent|Good|Average|Poor",
    "service_providers_quality": "Service provider analysis",
    "missing_critical_maintenance": ["Missing maintenance 1"],
    "positive_aspects": ["Positive point 1"],
    "red_flags": ["Alert 1"],
    "impact_on_resale_value": {
        "percentage_impact": 10,
        "description": "Impact on value"
    },
    "recommendations": ["Recommendation 1"],
                    "ai_score_impact": 15,
                    "detailed_report": "Detailed report..."
                }
            """.trimIndent()
            
            val response = carnetModel.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty response")
            
            val evaluation = parseQualityEvaluation(responseText)
            Result.success(evaluation)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Quality evaluation error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 📅 OPTIMAL SCHEDULING
     * AI suggests the best times to schedule maintenance
     */
    suspend fun suggestOptimalSchedule(
        carDetails: GeminiCarDetails,
        currentMileage: Int,
        upcomingReminders: List<SmartReminder>
    ): Result<OptimalSchedule> = withContext(Dispatchers.IO) {
        try {
            val remindersText = upcomingReminders.joinToString("\n") { 
                "- ${it.title}: dans ${it.dueInDays} jours ou à ${it.dueAtKm} km ($${it.estimatedCostDH})"
            }
            
            val prompt = """
                Create an OPTIMAL maintenance schedule for this car:
                
                Véhicule : ${carDetails.brand} ${carDetails.model} ${carDetails.year}
                Kilométrage : $currentMileage km
                
                Rappels à venir :
                ${remindersText.ifEmpty { "Aucun rappel" }}
                
                Génère un planning intelligent au format JSON :
                {
                    "recommended_schedule": [
                        {
                            "date_range": "15-30 January 2025",
                            "services": ["Oil change", "Filters"],
                            "combined_cost_usd": 800,
                            "reason": "Pourquoi grouper ces services",
                            "urgency": "HIGH|MEDIUM|LOW",
                            "savings_potential_usd": 200
                        }
                    ],
                    "service_grouping_benefits": "Avantages de grouper les services",
                    "priority_order": ["Service le plus urgent 1", "Service 2"],
                    "total_optimized_cost_usd": 5000,
                    "cost_without_optimization_usd": 5500,
                    "time_efficiency_gain": "Gain de temps estimé",
                    "best_practices": ["Pratique 1", "Pratique 2"]
                }
            """.trimIndent()
            
            val response = carnetModel.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty response")
            
            val schedule = parseOptimalSchedule(responseText)
            Result.success(schedule)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Optimal schedule error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 🎯 PERSONALIZED ADVICE
     * Get custom maintenance advice based on driving conditions
     */
    suspend fun getPersonalizedAdvice(
        carDetails: GeminiCarDetails,
        currentMileage: Int,
        drivingConditions: DrivingConditions
    ): Result<PersonalizedAdvice> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Generate PERSONALIZED automotive maintenance advice:
                
                Véhicule : ${carDetails.brand} ${carDetails.model} ${carDetails.year}
                Kilométrage : $currentMileage km
                
                Conditions de conduite :
                - Type : ${drivingConditions.type}
                - Climat : ${drivingConditions.climate}
                - Utilisation : ${drivingConditions.usage}
                - Terrain : ${drivingConditions.terrain}
                
                Génère des conseils au format JSON :
                {
                    "key_recommendations": ["Conseil clé 1", "Conseil clé 2"],
                    "driving_style_impact": "Impact du style de conduite",
                    "climate_specific_advice": "Climate-specific advice",
                    "frequency_adjustments": {
                        "oil_change_km": 10000,
                        "tire_check_months": 3,
                        "brake_inspection_km": 20000
                    },
                    "priority_maintenance": ["Entretien prioritaire 1"],
                    "cost_saving_tips": ["Astuce économie 1"],
                    "warning_signs": ["Signe d'alerte 1"],
                    "seasonal_advice": {
                        "summer": "Conseils été",
                        "winter": "Conseils hiver"
                    },
                    "detailed_guide": "Detailed guide..."
                }
            """.trimIndent()
            
            val response = carnetModel.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty response")
            
            val advice = parsePersonalizedAdvice(responseText)
            Result.success(advice)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Personalized advice error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * ⚠️ RISK ASSESSMENT
     * Identify potential risks and preventive actions
     */
    suspend fun assessMaintenanceRisks(
        carDetails: GeminiCarDetails,
        maintenanceRecords: List<MaintenanceRecordData>,
        currentMileage: Int
    ): Result<MaintenanceRiskAssessment> = withContext(Dispatchers.IO) {
        try {
            val recentRecords = maintenanceRecords.sortedByDescending { it.date }
                .take(10)
                .joinToString("\n") { "${it.type} à ${it.mileage} km" }
            
            val prompt = """
                Evaluate MAINTENANCE RISKS for this vehicle:

Vehicle: ${carDetails.brand} ${carDetails.model} ${carDetails.year}
Mileage: $currentMileage km

Recent maintenance:
                ${recentRecords.ifEmpty { "No history" }}

Identify risks in JSON format:
                {
                    "overall_risk_level": "LOW|MEDIUM|HIGH|CRITICAL",
                    "identified_risks": [
                        {
                            "risk": "Risk description",
    "severity": "LOW|MEDIUM|HIGH|CRITICAL",
    "probability": 75,
    "consequences": "Potential consequences",
    "prevention_cost_usd": 1500,
    "repair_cost_if_ignored_usd": 8000
                        }
                    ],
                    "immediate_actions": ["Urgent action 1"],
    "preventive_measures": ["Preventive measure 1"],
    "cost_benefit_analysis": "Cost/benefit analysis of prevention",
    "time_critical_issues": ["Urgent issue 1"],
    "ai_score_at_risk": -25,
    "detailed_assessment": "Detailed assessment..."
                }
            """.trimIndent()
            
            val response = carnetModel.generateContent(prompt)
            val responseText = response.text ?: throw Exception("Empty response")
            
            val assessment = parseRiskAssessment(responseText)
            Result.success(assessment)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Risk assessment error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // =========================================================================
    // PARSING METHODS
    // =========================================================================
    
    private fun buildComprehensiveAnalysisPrompt(
        carDetails: GeminiCarDetails,
        records: List<MaintenanceRecordData>
    ): String {
        val recordsText = if (records.isNotEmpty()) {
            records.sortedByDescending { it.date }
                .take(15)
                .joinToString("\n") { 
                    "- ${it.date}: ${it.type} à ${it.mileage} km ($${it.cost}) [${it.serviceProvider}]"
                }
        } else {
            "No maintenance history"
        }

        return """
            COMPLETE analysis of this vehicle's maintenance history:

            🚗 Vehicle: ${carDetails.brand} ${carDetails.model} ${carDetails.year}
            📊 Mileage: ${carDetails.mileage} km
            📝 Total maintenance: ${records.size}

            📋 Detailed history:
            $recordsText

            Provide a PROFESSIONAL and HONNEST analysis in JSON format:
            {
                "overall_maintenance_score": 85,
                "score_breakdown": {
                    "regularity": 90,
                    "quality": 80,
                    "completeness": 85
                },
                "positive_points": ["Positive point 1", "Positive point 2"],
    "concerns": ["Concern 1", "Concern 2"],
    "urgent_actions": ["Urgent action 1"],
    "recommended_next_steps": ["Step 1", "Step 2"],
    "estimated_annual_cost_usd": 8000,
    "maintenance_quality": "Excellent|Good|Average|Insufficient",
    "cost_efficiency": "Quality/price ratio analysis",
    "service_provider_analysis": "Analysis of service providers used",
                "impact_on_ai_score": 15,
                "detailed_analysis": "Detailed analysis with all the details..."
            }
            
            Base your analysis on automotive market standards.
        """.trimIndent()
    }
    
    private fun buildSmartRemindersPrompt(
        carDetails: GeminiCarDetails,
        currentMileage: Int,
        lastMaintenanceDates: Map<String, Long>
    ): String {
        val maintenanceInfo = lastMaintenanceDates.entries.joinToString("\n") { (type, timestamp) ->
            val daysAgo = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60 * 24)
            "- $type: $daysAgo days ago"
        }

        return """
            Generate INTELLIGENT and PERSONALIZED maintenance reminders:

            🚗 Vehicle: ${carDetails.brand} ${carDetails.model} ${carDetails.year}
            📊 Current mileage: $currentMileage km

            📅 Last maintenance performed:
            ${maintenanceInfo.ifEmpty { "No maintenance history registered" }}

            Create a PRIORITIZED reminder list in JSON format (array):
            [
                {
                    "title": "Engine oil change",
                    "priority": "CRITICAL|HIGH|MEDIUM|LOW",
                    "due_in_days": 15,
                    "due_at_km": ${currentMileage + 5000},
                    "estimated_cost_usd": 500,
                    "description": "Description détaillée du rappel et pourquoi c'est important",
                    "consequences_if_ignored": "Serious consequences if not done",
                    "impact_on_ai_score": -10,
                    "service_type": "OIL_CHANGE|BRAKE_SERVICE|TIRE_ROTATION|etc",
                    "urgency_reason": "Urgency reason"
                }
            ]
            
            Include MINIMUM these essential maintenance:
            1. Engine oil change (based on mileage)
            2. Contrôle technique annuel
            3. Assurance automobile (renouvellement)
            4. Filtres (air, habitacle, carburant)
            5. Freins et plaquettes
            6. Pneumatiques et géométrie
            
            Utilise les prix RÉELS du marché (garages certifiés).
            Réponds UNIQUEMENT en JSON valide, sans texte avant ou après.
        """.trimIndent()
    }
    
    private fun parseMaintenanceAnalysis(
        responseText: String,
        carDetails: GeminiCarDetails,
        records: List<MaintenanceRecordData>
    ): MaintenanceAnalysis {
        return try {
            val jsonText = extractJsonFromText(responseText)
            val type = object : TypeToken<Map<String, Any?>>() {}.type
            val jsonMap: Map<String, Any?> = gson.fromJson(jsonText, type)
            
            val scoreBreakdown: Map<String, Int> = (jsonMap["score_breakdown"] as? Map<*, *>)?.let { map ->
                mutableMapOf<String, Int>().apply {
                    put("regularity", (map["regularity"] as? Number)?.toInt() ?: 0)
                    put("quality", (map["quality"] as? Number)?.toInt() ?: 0)
                    put("completeness", (map["completeness"] as? Number)?.toInt() ?: 0)
                }
            } ?: emptyMap()
            
            MaintenanceAnalysis(
                overallScore = (jsonMap["overall_maintenance_score"] as? Number)?.toInt() ?: 70,
                scoreBreakdown = scoreBreakdown,
                positivePoints = (jsonMap["positive_points"] as? List<*>)
                    ?.filterIsInstance<String>() ?: listOf("Maintenance history present"),
                concerns = (jsonMap["concerns"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                urgentActions = (jsonMap["urgent_actions"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                recommendedNextSteps = (jsonMap["recommended_next_steps"] as? List<*>)
                    ?.filterIsInstance<String>() ?: listOf("Consulter un mécanicien certifié"),
                estimatedAnnualCostDH = (jsonMap["estimated_annual_cost_usd"] as? Number)?.toInt() ?: 6000,
                maintenanceQuality = jsonMap["maintenance_quality"] as? String ?: "Average",
                costEfficiency = jsonMap["cost_efficiency"] as? String ?: "To be evaluated",
                serviceProviderAnalysis = jsonMap["service_provider_analysis"] as? String ?: "",
                impactOnAIScore = (jsonMap["impact_on_ai_score"] as? Number)?.toInt() ?: 0,
                detailedAnalysis = jsonMap["detailed_analysis"] as? String ?: "Detailed analysis not available"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: ${e.message}")
            MaintenanceAnalysis(
                overallScore = if (records.isNotEmpty()) 65 else 30,
                scoreBreakdown = emptyMap(),
                positivePoints = if (records.isNotEmpty()) listOf("Active maintenance tracking") else emptyList(),
    concerns = if (records.isEmpty()) listOf("No maintenance history") else emptyList(),
    urgentActions = listOf("Create complete maintenance history"),
    recommendedNextSteps = listOf("Perform complete diagnostic"),
                estimatedAnnualCostDH = 6000,
                maintenanceQuality = "To be evaluated",
                costEfficiency = "Non disponible",
                serviceProviderAnalysis = "",
                impactOnAIScore = 0,
                detailedAnalysis = "Gemini analysis error. Please try again."
            )
        }
    }
    
    private fun parseSmartReminders(
        responseText: String,
        currentMileage: Int
    ): List<SmartReminder> {
        return try {
            val jsonText = extractJsonFromText(responseText)
            val type = object : TypeToken<List<Map<String, Any?>>>() {}.type
            val remindersList: List<Map<String, Any?>> = gson.fromJson(jsonText, type)
            
            remindersList.map { map ->
                SmartReminder(
                    title = map["title"] as? String ?: "Maintenance",
                    priority = ReminderPriority.valueOf(
                        (map["priority"] as? String ?: "MEDIUM").uppercase()
                            .replace("CRITICAL", "HIGH")
                    ),
                    dueInDays = (map["due_in_days"] as? Number)?.toInt() ?: 30,
                    dueAtKm = (map["due_at_km"] as? Number)?.toInt() ?: (currentMileage + 10000),
                    estimatedCostDH = (map["estimated_cost_usd"] as? Number)?.toInt() ?: 500,
                    description = map["description"] as? String ?: "",
                    consequencesIfIgnored = map["consequences_if_ignored"] as? String ?: "",
                    impactOnAIScore = (map["impact_on_ai_score"] as? Number)?.toInt() ?: -5,
                    serviceType = map["service_type"] as? String ?: "OTHER",
                    urgencyReason = map["urgency_reason"] as? String ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse reminders error: ${e.message}")
            // Return default essential reminders
            listOf(
                SmartReminder(
                    title = "Oil change",
                    priority = ReminderPriority.HIGH,
                    dueInDays = 30,
                    dueAtKm = currentMileage + 5000,
                    estimatedCostDH = 500,
                    description = "Engine oil change needed",
    consequencesIfIgnored = "Accelerated engine wear",
                    impactOnAIScore = -10,
                    serviceType = "OIL_CHANGE",
                    urgencyReason = "Preventive maintenance"
                )
            )
        }
    }
    
    private fun parseCostPrediction(responseText: String): CostPrediction {
        return try {
            val jsonText = extractJsonFromText(responseText)
            val type = object : TypeToken<Map<String, Any?>>() {}.type
            val jsonMap: Map<String, Any?> = gson.fromJson(jsonText, type)
            
            CostPrediction(
                totalAnnualCostDH = (jsonMap["total_annual_cost_usd"] as? Number)?.toInt() ?: 8000,
                monthlyBreakdown = emptyList(), // Simplified for now
                majorServices = emptyList(),
                savingsTips = (jsonMap["savings_tips"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                confidenceLevel = (jsonMap["confidence_level"] as? Number)?.toInt() ?: 75
            )
        } catch (e: Exception) {
            CostPrediction(
                totalAnnualCostDH = 8000,
                monthlyBreakdown = emptyList(),
                majorServices = emptyList(),
                savingsTips = emptyList(),
                confidenceLevel = 50
            )
        }
    }
    
    private fun parseQualityEvaluation(responseText: String): QualityEvaluation {
        return try {
            val jsonText = extractJsonFromText(responseText)
            val type = object : TypeToken<Map<String, Any?>>() {}.type
            val jsonMap: Map<String, Any?> = gson.fromJson(jsonText, type)
            
            QualityEvaluation(
                overallQualityScore = (jsonMap["overall_quality_score"] as? Number)?.toInt() ?: 70,
                maintenanceConsistency = jsonMap["maintenance_consistency"] as? String ?: "Average",
                serviceProvidersQuality = jsonMap["service_providers_quality"] as? String ?: "",
                missingCriticalMaintenance = (jsonMap["missing_critical_maintenance"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                positiveAspects = (jsonMap["positive_aspects"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                redFlags = (jsonMap["red_flags"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                impactOnResaleValue = 0,
                recommendations = (jsonMap["recommendations"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                aiScoreImpact = (jsonMap["ai_score_impact"] as? Number)?.toInt() ?: 0,
                detailedReport = jsonMap["detailed_report"] as? String ?: ""
            )
        } catch (e: Exception) {
            QualityEvaluation(
                overallQualityScore = 60,
                maintenanceConsistency = "To be evaluated",
                serviceProvidersQuality = "",
                missingCriticalMaintenance = emptyList(),
                positiveAspects = emptyList(),
                redFlags = emptyList(),
                impactOnResaleValue = 0,
                recommendations = listOf("Maintain regular maintenance history"),
                aiScoreImpact = 0,
                detailedReport = ""
            )
        }
    }
    
    private fun parseOptimalSchedule(responseText: String): OptimalSchedule {
        return try {
            val jsonText = extractJsonFromText(responseText)
            val type = object : TypeToken<Map<String, Any?>>() {}.type
            val jsonMap: Map<String, Any?> = gson.fromJson(jsonText, type)
            
            OptimalSchedule(
                recommendedSchedule = emptyList(),
                serviceGroupingBenefits = jsonMap["service_grouping_benefits"] as? String ?: "",
                priorityOrder = (jsonMap["priority_order"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                totalOptimizedCostDH = (jsonMap["total_optimized_cost_usd"] as? Number)?.toInt() ?: 5000,
                costWithoutOptimizationDH = (jsonMap["cost_without_optimization_usd"] as? Number)?.toInt() ?: 5500,
                timeEfficiencyGain = jsonMap["time_efficiency_gain"] as? String ?: "",
                bestPractices = (jsonMap["best_practices"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
            )
        } catch (e: Exception) {
            OptimalSchedule(
                recommendedSchedule = emptyList(),
                serviceGroupingBenefits = "",
                priorityOrder = emptyList(),
                totalOptimizedCostDH = 5000,
                costWithoutOptimizationDH = 5000,
                timeEfficiencyGain = "",
                bestPractices = emptyList()
            )
        }
    }
    
    private fun parsePersonalizedAdvice(responseText: String): PersonalizedAdvice {
        return try {
            val jsonText = extractJsonFromText(responseText)
            val type = object : TypeToken<Map<String, Any?>>() {}.type
            val jsonMap: Map<String, Any?> = gson.fromJson(jsonText, type)
            
            PersonalizedAdvice(
                keyRecommendations = (jsonMap["key_recommendations"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                drivingStyleImpact = jsonMap["driving_style_impact"] as? String ?: "",
                climateSpecificAdvice = jsonMap["climate_specific_advice"] as? String ?: "",
                frequencyAdjustments = emptyMap(),
                priorityMaintenance = (jsonMap["priority_maintenance"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                costSavingTips = (jsonMap["cost_saving_tips"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                warningSign = (jsonMap["warning_signs"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                seasonalAdvice = emptyMap(),
                detailedGuide = jsonMap["detailed_guide"] as? String ?: ""
            )
        } catch (e: Exception) {
            PersonalizedAdvice(
                keyRecommendations = emptyList(),
                drivingStyleImpact = "",
                climateSpecificAdvice = "",
                frequencyAdjustments = emptyMap(),
                priorityMaintenance = emptyList(),
                costSavingTips = emptyList(),
                warningSign = emptyList(),
                seasonalAdvice = emptyMap(),
                detailedGuide = ""
            )
        }
    }
    
    private fun parseRiskAssessment(responseText: String): MaintenanceRiskAssessment {
        return try {
            val jsonText = extractJsonFromText(responseText)
            val type = object : TypeToken<Map<String, Any?>>() {}.type
            val jsonMap: Map<String, Any?> = gson.fromJson(jsonText, type)
            
            MaintenanceRiskAssessment(
                overallRiskLevel = jsonMap["overall_risk_level"] as? String ?: "MEDIUM",
                identifiedRisks = emptyList(),
                immediateActions = (jsonMap["immediate_actions"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                preventiveMeasures = (jsonMap["preventive_measures"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                costBenefitAnalysis = jsonMap["cost_benefit_analysis"] as? String ?: "",
                timeCriticalIssues = (jsonMap["time_critical_issues"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                aiScoreAtRisk = (jsonMap["ai_score_at_risk"] as? Number)?.toInt() ?: 0,
                detailedAssessment = jsonMap["detailed_assessment"] as? String ?: ""
            )
        } catch (e: Exception) {
            MaintenanceRiskAssessment(
                overallRiskLevel = "LOW",
                identifiedRisks = emptyList(),
                immediateActions = emptyList(),
                preventiveMeasures = emptyList(),
                costBenefitAnalysis = "",
                timeCriticalIssues = emptyList(),
                aiScoreAtRisk = 0,
                detailedAssessment = ""
            )
        }
    }
    
    private fun extractJsonFromText(text: String): String {
        val cleanText = text.replace("```json", "").replace("```", "").trim()
        val jsonStart = cleanText.indexOf(if (cleanText.contains('[')) '[' else '{')
        val jsonEnd = cleanText.lastIndexOf(if (cleanText.contains(']')) ']' else '}')
        
        return if (jsonStart >= 0 && jsonEnd > jsonStart) {
            cleanText.substring(jsonStart, jsonEnd + 1)
        } else {
            cleanText
        }
    }
}

// ============================================================================
// DATA CLASSES
// ============================================================================

data class GeminiCarDetails(
    val brand: String,
    val model: String,
    val year: Int,
    val mileage: Int
)

data class MaintenanceRecordData(
    val date: String,
    val type: String,
    val mileage: Int,
    val cost: Int,
    val serviceProvider: String = "",
    val notes: String = ""
)

data class MaintenanceAnalysis(
    val overallScore: Int,
    val scoreBreakdown: Map<String, Int>,
    val positivePoints: List<String>,
    val concerns: List<String>,
    val urgentActions: List<String>,
    val recommendedNextSteps: List<String>,
    val estimatedAnnualCostDH: Int,
    val maintenanceQuality: String,
    val costEfficiency: String,
    val serviceProviderAnalysis: String,
    val impactOnAIScore: Int,
    val detailedAnalysis: String
)

data class SmartReminder(
    val title: String,
    val priority: ReminderPriority,
    val dueInDays: Int,
    val dueAtKm: Int,
    val estimatedCostDH: Int,
    val description: String,
    val consequencesIfIgnored: String,
    val impactOnAIScore: Int,
    val serviceType: String,
    val urgencyReason: String
)

data class CostPrediction(
    val totalAnnualCostDH: Int,
    val monthlyBreakdown: List<MonthlyMaintenance>,
    val majorServices: List<MajorService>,
    val savingsTips: List<String>,
    val confidenceLevel: Int
)

data class MonthlyMaintenance(
    val month: String,
    val estimatedCostDH: Int,
    val maintenanceItems: List<String>,
    val priority: String
)

data class MajorService(
    val service: String,
    val `when`: String,
    val costDH: Int,
    val why: String
)

data class QualityEvaluation(
    val overallQualityScore: Int,
    val maintenanceConsistency: String,
    val serviceProvidersQuality: String,
    val missingCriticalMaintenance: List<String>,
    val positiveAspects: List<String>,
    val redFlags: List<String>,
    val impactOnResaleValue: Int,
    val recommendations: List<String>,
    val aiScoreImpact: Int,
    val detailedReport: String
)

data class OptimalSchedule(
    val recommendedSchedule: List<ScheduledService>,
    val serviceGroupingBenefits: String,
    val priorityOrder: List<String>,
    val totalOptimizedCostDH: Int,
    val costWithoutOptimizationDH: Int,
    val timeEfficiencyGain: String,
    val bestPractices: List<String>
)

data class ScheduledService(
    val dateRange: String,
    val services: List<String>,
    val combinedCostDH: Int,
    val reason: String,
    val urgency: String,
    val savingsPotentialDH: Int
)

data class PersonalizedAdvice(
    val keyRecommendations: List<String>,
    val drivingStyleImpact: String,
    val climateSpecificAdvice: String,
    val frequencyAdjustments: Map<String, Int>,
    val priorityMaintenance: List<String>,
    val costSavingTips: List<String>,
    val warningSign: List<String>,
    val seasonalAdvice: Map<String, String>,
    val detailedGuide: String
)

data class MaintenanceRiskAssessment(
    val overallRiskLevel: String,
    val identifiedRisks: List<IdentifiedRisk>,
    val immediateActions: List<String>,
    val preventiveMeasures: List<String>,
    val costBenefitAnalysis: String,
    val timeCriticalIssues: List<String>,
    val aiScoreAtRisk: Int,
    val detailedAssessment: String
)

data class IdentifiedRisk(
    val risk: String,
    val severity: String,
    val probability: Int,
    val consequences: String,
    val preventionCostDH: Int,
    val repairCostIfIgnoredDH: Int
)

enum class ReminderPriority {
    HIGH,
    MEDIUM,
    LOW
}

data class DrivingConditions(
    val type: String,  // "Urbain", "Autoroute", "Mixte"
    val climate: String,  // "Chaud", "Tempéré", "Froid"
    val usage: String,  // "Quotidien", "Occasionnel", "Professionnel"
    val terrain: String = "Normal"  // "Normal", "Montagne", "Désert"
)
