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
                Prédis les coûts d'entretien pour les 12 prochains mois :
                
                Véhicule : ${carDetails.brand} ${carDetails.model} ${carDetails.year}
                Kilométrage actuel : $currentMileage km
                Km moyens par mois : $averageMonthlyKm km
                
                Génère une prédiction détaillée au format JSON :
                {
                    "total_annual_cost_dh": 12000,
                    "monthly_breakdown": [
                        {
                            "month": "Janvier",
                            "estimated_cost_dh": 500,
                            "maintenance_items": ["Vidange d'huile"],
                            "priority": "HIGH|MEDIUM|LOW"
                        }
                    ],
                    "major_services": [
                        {
                            "service": "Révision complète",
                            "when": "Dans 3 mois",
                            "cost_dh": 2500,
                            "why": "Raison"
                        }
                    ],
                    "savings_tips": ["Conseil 1", "Conseil 2"],
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
                Évalue la QUALITÉ de l'entretien de ce véhicule :
                
                Véhicule : ${carDetails.brand} ${carDetails.model} ${carDetails.year}
                Kilométrage actuel : $currentMileage km
                Total d'entretiens : ${maintenanceRecords.size}
                
                Historique récent :
                ${recordsText.ifEmpty { "Aucun historique" }}
                
                Génère une évaluation au format JSON :
                {
                    "overall_quality_score": 85,
                    "maintenance_consistency": "Excellent|Bon|Moyen|Mauvais",
                    "service_providers_quality": "Analyse des prestataires",
                    "missing_critical_maintenance": ["Entretien manquant 1"],
                    "positive_aspects": ["Point positif 1"],
                    "red_flags": ["Alerte 1"],
                    "impact_on_resale_value": {
                        "percentage_impact": 10,
                        "description": "Impact sur la valeur"
                    },
                    "recommendations": ["Recommandation 1"],
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
                Crée un planning OPTIMAL d'entretien pour cette voiture :
                
                Véhicule : ${carDetails.brand} ${carDetails.model} ${carDetails.year}
                Kilométrage : $currentMileage km
                
                Rappels à venir :
                ${remindersText.ifEmpty { "Aucun rappel" }}
                
                Génère un planning intelligent au format JSON :
                {
                    "recommended_schedule": [
                        {
                            "date_range": "15-30 Janvier 2025",
                            "services": ["Vidange", "Filtres"],
                            "combined_cost_dh": 800,
                            "reason": "Pourquoi grouper ces services",
                            "urgency": "HIGH|MEDIUM|LOW",
                            "savings_potential_dh": 200
                        }
                    ],
                    "service_grouping_benefits": "Avantages de grouper les services",
                    "priority_order": ["Service le plus urgent 1", "Service 2"],
                    "total_optimized_cost_dh": 5000,
                    "cost_without_optimization_dh": 5500,
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
                Génère des conseils PERSONNALISÉS d'entretien automobile :
                
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
                    "climate_specific_advice": "Conseils selon le climat local",
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
                Évalue les RISQUES liés à l'entretien de ce véhicule :
                
                Véhicule : ${carDetails.brand} ${carDetails.model} ${carDetails.year}
                Kilométrage : $currentMileage km
                
                Entretiens récents :
                ${recentRecords.ifEmpty { "Aucun historique" }}
                
                Identifie les risques au format JSON :
                {
                    "overall_risk_level": "LOW|MEDIUM|HIGH|CRITICAL",
                    "identified_risks": [
                        {
                            "risk": "Description du risque",
                            "severity": "LOW|MEDIUM|HIGH|CRITICAL",
                            "probability": 75,
                            "consequences": "Conséquences potentielles",
                            "prevention_cost_dh": 1500,
                            "repair_cost_if_ignored_dh": 8000
                        }
                    ],
                    "immediate_actions": ["Action urgente 1"],
                    "preventive_measures": ["Mesure préventive 1"],
                    "cost_benefit_analysis": "Analyse coût/bénéfice de la prévention",
                    "time_critical_issues": ["Problème urgent 1"],
                    "ai_score_at_risk": -25,
                    "detailed_assessment": "Évaluation détaillée..."
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
            "Aucun historique d'entretien"
        }
        
        return """
            Analyse COMPLÈTE de l'historique d'entretien de ce véhicule :
            
            🚗 Véhicule : ${carDetails.brand} ${carDetails.model} ${carDetails.year}
            📊 Kilométrage : ${carDetails.mileage} km
            📝 Total d'entretiens : ${records.size}
            
            📋 Historique détaillé :
            $recordsText
            
            Fournis une analyse PROFESSIONNELLE et HONNÊTE au format JSON :
            {
                "overall_maintenance_score": 85,
                "score_breakdown": {
                    "regularity": 90,
                    "quality": 80,
                    "completeness": 85
                },
                "positive_points": ["Point positif 1", "Point positif 2"],
                "concerns": ["Préoccupation 1", "Préoccupation 2"],
                "urgent_actions": ["Action urgente 1"],
                "recommended_next_steps": ["Étape 1", "Étape 2"],
                "estimated_annual_cost_dh": 8000,
                "maintenance_quality": "Excellent|Bon|Moyen|Insuffisant",
                "cost_efficiency": "Analyse du rapport qualité/prix",
                "service_provider_analysis": "Analyse des prestataires utilisés",
                "impact_on_ai_score": 15,
                "detailed_analysis": "Detailed analysis with all the details..."
            }
            
            Base ton analyse sur les standards du marché automobile marocain.
        """.trimIndent()
    }
    
    private fun buildSmartRemindersPrompt(
        carDetails: GeminiCarDetails,
        currentMileage: Int,
        lastMaintenanceDates: Map<String, Long>
    ): String {
        val maintenanceInfo = lastMaintenanceDates.entries.joinToString("\n") { (type, timestamp) ->
            val daysAgo = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60 * 24)
            "- $type: il y a $daysAgo jours"
        }
        
        return """
            Génère des rappels d'entretien INTELLIGENTS et PERSONNALISÉS pour le Maroc :
            
            🚗 Véhicule : ${carDetails.brand} ${carDetails.model} ${carDetails.year}
            📊 Kilométrage actuel : $currentMileage km
            
            📅 Derniers entretiens effectués :
            ${maintenanceInfo.ifEmpty { "Aucun historique d'entretien enregistré" }}
            
            Crée une liste de rappels PRIORITAIRES au format JSON (array) :
            [
                {
                    "title": "Vidange d'huile moteur",
                    "priority": "CRITICAL|HIGH|MEDIUM|LOW",
                    "due_in_days": 15,
                    "due_at_km": ${currentMileage + 5000},
                    "estimated_cost_dh": 500,
                    "description": "Description détaillée du rappel et pourquoi c'est important",
                    "consequences_if_ignored": "Conséquences graves si non effectué",
                    "impact_on_ai_score": -10,
                    "service_type": "OIL_CHANGE|BRAKE_SERVICE|TIRE_ROTATION|etc",
                    "urgency_reason": "Raison de l'urgence"
                }
            ]
            
            Inclus MINIMUM ces entretiens essentiels :
            1. Vidange d'huile moteur (selon kilométrage)
            2. Contrôle technique annuel (obligatoire au Maroc)
            3. Assurance automobile (renouvellement)
            4. Filtres (air, habitacle, carburant)
            5. Freins et plaquettes
            6. Pneumatiques et géométrie
            
            Utilise les prix RÉELS du marché marocain (garages certifiés).
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
                    ?.filterIsInstance<String>() ?: listOf("Historique d'entretien présent"),
                concerns = (jsonMap["concerns"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                urgentActions = (jsonMap["urgent_actions"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                recommendedNextSteps = (jsonMap["recommended_next_steps"] as? List<*>)
                    ?.filterIsInstance<String>() ?: listOf("Consulter un mécanicien certifié"),
                estimatedAnnualCostDH = (jsonMap["estimated_annual_cost_dh"] as? Number)?.toInt() ?: 6000,
                maintenanceQuality = jsonMap["maintenance_quality"] as? String ?: "Moyen",
                costEfficiency = jsonMap["cost_efficiency"] as? String ?: "À évaluer",
                serviceProviderAnalysis = jsonMap["service_provider_analysis"] as? String ?: "",
                impactOnAIScore = (jsonMap["impact_on_ai_score"] as? Number)?.toInt() ?: 0,
                detailedAnalysis = jsonMap["detailed_analysis"] as? String ?: "Analyse détaillée non disponible"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: ${e.message}")
            MaintenanceAnalysis(
                overallScore = if (records.isNotEmpty()) 65 else 30,
                scoreBreakdown = emptyMap(),
                positivePoints = if (records.isNotEmpty()) listOf("Suivi d'entretien actif") else emptyList(),
                concerns = if (records.isEmpty()) listOf("Pas d'historique d'entretien") else emptyList(),
                urgentActions = listOf("Créer un historique d'entretien complet"),
                recommendedNextSteps = listOf("Effectuer un diagnostic complet"),
                estimatedAnnualCostDH = 6000,
                maintenanceQuality = "À évaluer",
                costEfficiency = "Non disponible",
                serviceProviderAnalysis = "",
                impactOnAIScore = 0,
                detailedAnalysis = "Erreur lors de l'analyse Gemini. Veuillez réessayer."
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
                    title = map["title"] as? String ?: "Entretien",
                    priority = ReminderPriority.valueOf(
                        (map["priority"] as? String ?: "MEDIUM").uppercase()
                            .replace("CRITICAL", "HIGH")
                    ),
                    dueInDays = (map["due_in_days"] as? Number)?.toInt() ?: 30,
                    dueAtKm = (map["due_at_km"] as? Number)?.toInt() ?: (currentMileage + 10000),
                    estimatedCostDH = (map["estimated_cost_dh"] as? Number)?.toInt() ?: 500,
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
                    title = "Vidange d'huile",
                    priority = ReminderPriority.HIGH,
                    dueInDays = 30,
                    dueAtKm = currentMileage + 5000,
                    estimatedCostDH = 500,
                    description = "Vidange d'huile moteur nécessaire",
                    consequencesIfIgnored = "Usure moteur accélérée",
                    impactOnAIScore = -10,
                    serviceType = "OIL_CHANGE",
                    urgencyReason = "Maintenance préventive"
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
                totalAnnualCostDH = (jsonMap["total_annual_cost_dh"] as? Number)?.toInt() ?: 8000,
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
                maintenanceConsistency = jsonMap["maintenance_consistency"] as? String ?: "Moyen",
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
                maintenanceConsistency = "À évaluer",
                serviceProvidersQuality = "",
                missingCriticalMaintenance = emptyList(),
                positiveAspects = emptyList(),
                redFlags = emptyList(),
                impactOnResaleValue = 0,
                recommendations = listOf("Maintenir un historique d'entretien régulier"),
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
                totalOptimizedCostDH = (jsonMap["total_optimized_cost_dh"] as? Number)?.toInt() ?: 5000,
                costWithoutOptimizationDH = (jsonMap["cost_without_optimization_dh"] as? Number)?.toInt() ?: 5500,
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
