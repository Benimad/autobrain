package com.example.autobrain.domain.usecase

import com.example.autobrain.data.ai.GeminiAiRepository
import com.example.autobrain.data.ai.CarDetails
import com.example.autobrain.domain.model.*
import com.example.autobrain.domain.model.IssueSeverity as DomainIssueSeverity
import javax.inject.Inject

/**
 * AI Score Calculator for AutoBrain
 * Implements the complete scoring formula:
 * AI Score = (Technical × 70%) + (Carnet × 20%) + (Market × 10%) + Gemini Adjustment (±10)
 */
class CalculateAIScoreUseCase @Inject constructor(
    private val geminiAiRepository: GeminiAiRepository
) {
    
    /**
     * Calculate complete AI Score from all inputs using Gemini AI
     */
    suspend fun execute(
        carData: CarDataForAnalysis,
        engineSoundResult: EngineSoundResult?,
        videoResult: VideoAnalysisResult?,
        maintenanceData: MaintenanceData,
        marketData: MarketData?,
        useGeminiEnhancement: Boolean = true
    ): AIScoreResult {
        // 1. Calculate Technical Score (70%)
        val technicalScore = calculateTechnicalScore(engineSoundResult, videoResult)
        
        // 2. Calculate Maintenance Score (20%)
        val maintenanceScore = calculateMaintenanceScore(maintenanceData)
        
        // 3. Calculate Market Score (10%)
        val marketScore = calculateMarketScore(carData, marketData)
        
        // 4. Calculate raw weighted score
        val rawWeightedScore = (technicalScore.overallScore * AIScoreBreakdown.TECHNICAL_WEIGHT) +
                               (maintenanceScore.overallScore * AIScoreBreakdown.MAINTENANCE_WEIGHT) +
                               (marketScore.overallScore * AIScoreBreakdown.MARKET_WEIGHT)
        
        // 5. Get Gemini AI analysis and adjustment (optional)
        var geminiAnalysis: GeminiAnalysisResult? = null
        var geminiAdjustment = 0
        
        if (useGeminiEnhancement) {
            // Convert to CarDetails for Gemini
            val carDetails = CarDetails(
                brand = carData.brand,
                model = carData.model,
                year = carData.year,
                mileage = carData.currentKm,
                fuelType = "Diesel", // Default, can be extracted from carData
                transmission = "Manuelle"
            )
            
            // Convert engineSoundResult to AudioAnalysisResult for Gemini
            val audioResult = engineSoundResult?.let { result ->
                com.example.autobrain.data.ai.AudioAnalysisResult(
                    classifications = emptyList(), // Will be filled by actual TFLite data
                    mainIssue = result.mainIssue.descriptionFr,
                    possibleCauses = listOf(result.rawDescription),
                    recommendations = emptyList(),
                    healthScore = result.mainIssue.score
                )
            }
            
            // Convert videoResult to Gemini VideoAnalysisResult
            val geminiVideoResult = videoResult?.let {
                com.example.autobrain.data.ai.VideoAnalysisResult(
                    detectedObjects = emptyList(),
                    anomalies = emptyList(),
                    frameCount = it.framesAnalyzed,
                    analysisTime = it.analysisTimestamp
                )
            }
            
            // Call Gemini for smart analysis
            val geminiResult = geminiAiRepository.performSmartAnalysis(
                carDetails = carDetails,
                audioResult = audioResult,
                videoResult = geminiVideoResult
            )
            
            geminiResult.onSuccess { result ->
                geminiAnalysis = GeminiAnalysisResult(
                    overallScore = result.overallScore,
                    majorIssues = result.majorIssues,
                    minorIssues = result.minorIssues,
                    priceEstimation = result.priceEstimation,
                    recommendation = result.recommendation,
                    detailedExplanation = result.detailedExplanation
                )
                
                // Calculate adjustment based on Gemini score vs raw score
                val scoreDiff = result.overallScore - rawWeightedScore.toInt()
                geminiAdjustment = scoreDiff.coerceIn(-10, 10)
            }
        }
        
        // 6. Calculate final score
        val finalScore = (rawWeightedScore + geminiAdjustment).toInt().coerceIn(0, 100)
        
        // 7. Build issues list
        val issues = buildIssuesList(engineSoundResult, videoResult, maintenanceData, geminiAnalysis)
        
        // 8. Determine category and risk level
        val scoreCategory = getScoreCategoryFromScore(finalScore)
        val riskLevel = getRiskLevelFromScore(finalScore)
        
        // 9. Build price estimate
        val priceEstimate = buildPriceEstimate(geminiAnalysis, marketData)
        
        // 10. Get buyer advice
        val buyerAdvice = geminiAnalysis?.recommendation ?: getDefaultBuyerAdvice(finalScore, issues)
        
        return AIScoreResult(
            finalScore = finalScore,
            scoreCategory = scoreCategory,
            breakdown = AIScoreBreakdown(
                technicalScore = technicalScore,
                maintenanceScore = maintenanceScore,
                marketScore = marketScore,
                rawWeightedScore = rawWeightedScore,
                llmAdjustment = geminiAdjustment
            ),
            llmAnalysis = geminiAnalysis?.toLlmAnalysisResult(),
            priceEstimate = priceEstimate,
            issues = issues,
            buyerAdvice = buyerAdvice,
            riskLevel = riskLevel
        )
    }
    
    /**
     * Calculate Technical Score (70% of total)
     * Engine Sound: 40%, Video Analysis: 30%
     */
    private fun calculateTechnicalScore(
        engineSoundResult: EngineSoundResult?,
        videoResult: VideoAnalysisResult?
    ): TechnicalScore {
        // Engine Sound Score (40% of technical)
        val engineScore = engineSoundResult?.let { result ->
            var score = result.mainIssue.score
            
            // Reduce for additional issues
            result.otherIssues.forEach { issue ->
                score -= (100 - issue.score) / 4
            }
            
            // Adjust based on confidence
            if (result.confidence < 0.7f) {
                score = (score + 75) / 2 // Move towards neutral if low confidence
            }
            
            score.coerceIn(0, 100)
        } ?: 75 // Default if no analysis
        
        // Video Analysis Score (30% of technical)
        val videoScore = videoResult?.let { result ->
            var score = 100
            
            // Deduct for smoke
            score -= result.smokeType.scorePenalty
            
            // Deduct for vibrations (0-5 scale, each point = -10)
            score -= result.vibrationLevel * 10
            
            // Deduct for other observations
            score -= result.otherObservations.size * 5
            
            score.coerceIn(0, 100)
        } ?: 75 // Default if no analysis
        
        // Calculate weighted technical score
        val overallScore = ((engineScore * TechnicalScore.ENGINE_SOUND_WEIGHT) +
                           (videoScore * TechnicalScore.VIDEO_ANALYSIS_WEIGHT)) /
                          (TechnicalScore.ENGINE_SOUND_WEIGHT + TechnicalScore.VIDEO_ANALYSIS_WEIGHT)
        
        return TechnicalScore(
            engineSoundScore = engineScore,
            videoAnalysisScore = videoScore,
            overallScore = overallScore.toInt(),
            engineSoundResult = engineSoundResult,
            videoResult = videoResult
        )
    }
    
    /**
     * Calculate Maintenance Score (20% of total)
     * Based on oil change, technical inspection, insurance, mileage consistency
     */
    private fun calculateMaintenanceScore(data: MaintenanceData): MaintenanceScore {
        var totalPoints = 0
        
        // Oil Change Status (max 20 points)
        val oilChangeStatus = when {
            data.kmSinceLastOilChange < 10000 && data.monthsSinceLastOilChange < 12 -> {
                totalPoints += 20
                MaintenanceStatus(StatusLevel.GOOD, 20, "Vidange récente")
            }
            data.kmSinceLastOilChange < 15000 && data.monthsSinceLastOilChange < 18 -> {
                totalPoints += 10
                MaintenanceStatus(StatusLevel.WARNING, 10, "Vidange à prévoir bientôt")
            }
            else -> {
                MaintenanceStatus(StatusLevel.EXPIRED, 0, "Vidange en retard")
            }
        }
        
        // Technical Inspection (max 20 points)
        val technicalStatus = when {
            data.monthsSinceTechnicalInspection < 0 -> { // Valid
                totalPoints += 20
                MaintenanceStatus(StatusLevel.GOOD, 20, "Contrôle technique valide")
            }
            data.monthsSinceTechnicalInspection < 3 -> { // Recently expired
                totalPoints += 10
                MaintenanceStatus(StatusLevel.WARNING, 10, "CT expiré récemment")
            }
            else -> {
                MaintenanceStatus(StatusLevel.EXPIRED, 0, "CT expiré depuis plus de 3 mois")
            }
        }
        
        // Insurance Status (max 10 points)
        val insuranceStatus = if (data.hasValidInsurance) {
            totalPoints += 10
            MaintenanceStatus(StatusLevel.GOOD, 10, "Assurance à jour")
        } else {
            MaintenanceStatus(StatusLevel.EXPIRED, 0, "Assurance expirée")
        }
        
        // Mileage Consistency (max 20 points, can be negative)
        val mileageConsistency = if (data.mileageIsConsistent) {
            totalPoints += 10
            MileageConsistency(
                isConsistent = true,
                points = 10,
                currentKm = data.currentKm,
                expectedKmRange = data.expectedKmRange,
                anomalyDetected = false,
                anomalyDescription = null
            )
        } else {
            totalPoints -= 20 // Penalty for suspicious mileage
            MileageConsistency(
                isConsistent = false,
                points = -20,
                currentKm = data.currentKm,
                expectedKmRange = data.expectedKmRange,
                anomalyDetected = true,
                anomalyDescription = "Kilométrage suspect - possible manipulation"
            )
        }
        
        // Convert to 0-100 scale (max raw is 70, min is -20)
        val overallScore = ((totalPoints + 20) / 90.0 * 100).toInt().coerceIn(0, 100)
        
        return MaintenanceScore(
            overallScore = overallScore,
            rawPoints = totalPoints,
            oilChangeStatus = oilChangeStatus,
            technicalInspectionStatus = technicalStatus,
            insuranceStatus = insuranceStatus,
            mileageConsistency = mileageConsistency
        )
    }
    
    /**
     * Calculate Market Score (10% of total)
     * Based on price comparison and model popularity
     */
    private fun calculateMarketScore(
        carData: CarDataForAnalysis,
        marketData: MarketData?
    ): MarketScore {
        var totalPoints = 0
        
        // Price Comparison (max 10 points)
        val priceComparison = marketData?.let { data ->
            val askedPrice = carData.askedPrice ?: data.averagePrice
            val estimatedMid = (data.lowPrice + data.highPrice) / 2
            
            when {
                askedPrice <= estimatedMid -> {
                    totalPoints += 10
                    PriceComparison(
                        askedPrice = askedPrice,
                        estimatedPrice = data.lowPrice..data.highPrice,
                        comparisonResult = PriceComparisonResult.BELOW_OR_EQUAL,
                        points = 10
                    )
                }
                askedPrice <= estimatedMid * 1.2 -> {
                    totalPoints += 5
                    PriceComparison(
                        askedPrice = askedPrice,
                        estimatedPrice = data.lowPrice..data.highPrice,
                        comparisonResult = PriceComparisonResult.SLIGHTLY_ABOVE,
                        points = 5
                    )
                }
                else -> {
                    PriceComparison(
                        askedPrice = askedPrice,
                        estimatedPrice = data.lowPrice..data.highPrice,
                        comparisonResult = PriceComparisonResult.WAY_ABOVE,
                        points = 0
                    )
                }
            }
        } ?: PriceComparison(
            askedPrice = 0,
            estimatedPrice = 0..0,
            comparisonResult = PriceComparisonResult.BELOW_OR_EQUAL,
            points = 5 // Neutral if no data
        )
        
        // Model Popularity (max 5 points)
        val isPopular = ModelPopularity.POPULAR_BRANDS
            .any { it.equals(carData.brand, ignoreCase = true) }
        
        val modelPopularity = if (isPopular) {
            totalPoints += 5
            ModelPopularity(carData.brand, true, 5)
        } else {
            ModelPopularity(carData.brand, false, 0)
        }
        
        // Convert to 0-100 scale (max raw is 15)
        val overallScore = (totalPoints / 15.0 * 100).toInt().coerceIn(0, 100)
        
        return MarketScore(
            overallScore = overallScore,
            rawPoints = totalPoints,
            priceComparison = priceComparison,
            modelPopularity = modelPopularity
        )
    }
    
    /**
     * Build consolidated issues list from all sources including Gemini AI
     */
    private fun buildIssuesList(
        engineSoundResult: EngineSoundResult?,
        videoResult: VideoAnalysisResult?,
        maintenanceData: MaintenanceData,
        geminiAnalysis: GeminiAnalysisResult?
    ): IssuesList {
        val grave = mutableListOf<Issue>()
        val medium = mutableListOf<Issue>()
        val minor = mutableListOf<Issue>()
        
        // Add engine sound issues
        engineSoundResult?.let { result ->
            when (result.mainIssue.severity) {
                DomainIssueSeverity.GRAVE -> grave.add(
                    Issue(
                        title = result.mainIssue.descriptionFr,
                        description = result.rawDescription,
                        severity = DomainIssueSeverity.GRAVE,
                        estimatedRepairCost = 20000..50000,
                        source = IssueSource.ENGINE_SOUND_ANALYSIS
                    )
                )
                DomainIssueSeverity.MEDIUM -> medium.add(
                    Issue(
                        title = result.mainIssue.descriptionFr,
                        description = result.rawDescription,
                        severity = DomainIssueSeverity.MEDIUM,
                        estimatedRepairCost = 5000..15000,
                        source = IssueSource.ENGINE_SOUND_ANALYSIS
                    )
                )
                DomainIssueSeverity.MINOR -> minor.add(
                    Issue(
                        title = result.mainIssue.descriptionFr,
                        description = result.rawDescription,
                        severity = DomainIssueSeverity.MINOR,
                        estimatedRepairCost = 500..3000,
                        source = IssueSource.ENGINE_SOUND_ANALYSIS
                    )
                )
                else -> {}
            }
        }
        
        // Add video issues
        videoResult?.let { result ->
            if (result.smokeType != SmokeType.NONE) {
                when (result.smokeType.severity) {
                    DomainIssueSeverity.GRAVE -> grave.add(
                        Issue(
                            title = result.smokeType.descriptionFr,
                            description = "Détecté dans l'analyse vidéo",
                            severity = DomainIssueSeverity.GRAVE,
                            estimatedRepairCost = 15000..40000,
                            source = IssueSource.VIDEO_ANALYSIS
                        )
                    )
                    DomainIssueSeverity.MEDIUM -> medium.add(
                        Issue(
                            title = result.smokeType.descriptionFr,
                            description = "Détecté dans l'analyse vidéo",
                            severity = DomainIssueSeverity.MEDIUM,
                            estimatedRepairCost = 5000..15000,
                            source = IssueSource.VIDEO_ANALYSIS
                        )
                    )
                    else -> minor.add(
                        Issue(
                            title = result.smokeType.descriptionFr,
                            description = "Observation mineure",
                            severity = DomainIssueSeverity.MINOR,
                            estimatedRepairCost = null,
                            source = IssueSource.VIDEO_ANALYSIS
                        )
                    )
                }
            }
        }
        
        // Add maintenance issues
        if (!maintenanceData.mileageIsConsistent) {
            grave.add(
                Issue(
                    title = "Kilométrage suspect",
                    description = "Possible manipulation du compteur",
                    severity = DomainIssueSeverity.GRAVE,
                    estimatedRepairCost = null,
                    source = IssueSource.MAINTENANCE_LOG
                )
            )
        }
        
        // Add Gemini AI identified issues
        geminiAnalysis?.let { analysis ->
            analysis.majorIssues.forEach { issue ->
                if (issue.lowercase() != "aucun") {
                    grave.add(
                        Issue(
                            title = issue,
                            description = "Identifié par Gemini AI",
                            severity = DomainIssueSeverity.GRAVE,
                            estimatedRepairCost = 20000..50000,
                            source = IssueSource.LLM_ANALYSIS
                        )
                    )
                }
            }
            analysis.minorIssues.forEach { issue ->
                minor.add(
                    Issue(
                        title = issue,
                        description = "Identifié par Gemini AI",
                        severity = DomainIssueSeverity.MINOR,
                        estimatedRepairCost = null,
                        source = IssueSource.LLM_ANALYSIS
                    )
                )
            }
        }
        
        return IssuesList(grave = grave, medium = medium, minor = minor)
    }
    
    private fun buildPriceEstimate(
        geminiAnalysis: GeminiAnalysisResult?,
        marketData: MarketData?
    ): PriceEstimate? {
        return geminiAnalysis?.let { analysis ->
            try {
                val prices = analysis.priceEstimation.replace(" ", "").replace("$", "").replace("USD", "").split("-")
                if (prices.size == 2) {
                    PriceEstimate(
                        lowPrice = prices[0].replace("[^0-9]".toRegex(), "").toInt(),
                        highPrice = prices[1].replace("[^0-9]".toRegex(), "").toInt(),
                        confidence = ConfidenceLevel.HIGH,
                        basedOn = listOf("Gemini AI", "Global Market Data"),
                        lastUpdated = System.currentTimeMillis()
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        } ?: marketData?.let {
            PriceEstimate(
                lowPrice = it.lowPrice,
                highPrice = it.highPrice,
                confidence = ConfidenceLevel.MEDIUM,
                basedOn = listOf("Estimation locale"),
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
    
    private fun getDefaultBuyerAdvice(score: Int, issues: IssuesList): String {
        return when {
            score >= 90 -> "Acheter sans hésiter - Excellent état"
            score >= 70 && !issues.hasGraveIssues() -> "Bon rapport qualité-prix, à vérifier chez un mécanicien"
            score >= 50 && !issues.hasGraveIssues() -> "Négocier le prix en fonction des défauts constatés"
            issues.hasGraveIssues() -> "Éviter cette voiture – risque élevé de réparation coûteuse"
            else -> "Faire inspecter par un mécanicien avant toute décision"
        }
    }
}

// Helper data classes for input
data class MaintenanceData(
    val kmSinceLastOilChange: Int,
    val monthsSinceLastOilChange: Int,
    val monthsSinceTechnicalInspection: Int, // Negative if valid
    val hasValidInsurance: Boolean,
    val currentKm: Int,
    val mileageIsConsistent: Boolean,
    val expectedKmRange: IntRange? = null
)

data class MarketData(
    val lowPrice: Int,
    val highPrice: Int,
    val averagePrice: Int
)

// Gemini AI Result wrapper for use case
data class GeminiAnalysisResult(
    val overallScore: Int,
    val majorIssues: List<String>,
    val minorIssues: List<String>,
    val priceEstimation: String,
    val recommendation: String,
    val detailedExplanation: String
) {
    fun toLlmAnalysisResult(): LlmAnalysisResult {
        return LlmAnalysisResult(
            aiScore = overallScore,
            scoreCategory = when {
                overallScore >= 90 -> "Excellent"
                overallScore >= 70 -> "Très bon état"
                overallScore >= 50 -> "État moyen"
                overallScore >= 30 -> "État médiocre"
                else -> "À éviter"
            },
            graveIssues = majorIssues,
            mediumIssues = emptyList(), // Gemini uses major/minor only
            minorIssues = minorIssues,
            priceRangeDh = priceEstimation,
            buyerAdvice = recommendation,
            detailedExplanation = detailedExplanation,
            adjustment = 0,
            llmProvider = "gemini"
        )
    }
}
