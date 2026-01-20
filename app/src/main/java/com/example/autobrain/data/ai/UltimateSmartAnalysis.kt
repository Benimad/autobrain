package com.example.autobrain.data.ai

import com.example.autobrain.data.local.entity.AudioDiagnosticData
import com.example.autobrain.data.local.entity.VideoDiagnosticData
import com.example.autobrain.domain.model.CarDetails
import com.example.autobrain.domain.model.CarLog
import com.example.autobrain.domain.model.MaintenanceType
import com.example.autobrain.domain.model.User
import java.text.SimpleDateFormat
import java.util.*

/**
 * AUTOBRAIN ULTIMATE SMART ANALYSIS
 * Combined Audio + Video + Price + Complete Firestore History
 * 
 * The MOST comprehensive automotive analysis system
 * Features:
 * - Multimodal diagnostic (audio + video)
 * - Complete maintenance history analysis
 * - Real market pricing from global sources
 * - Buyer/Seller decision matrices
 * - Legal compliance check
 * - Investment ROI calculations
 */

/**
 * Market data from external APIs or Firestore
 */
data class MarketData(
    val averageMarketPrice: Double = 0.0,
    val priceRange: Pair<Double, Double> = Pair(0.0, 0.0),
    val similarListings: List<MarketListing> = emptyList(),
    val marketTrend: String = "stable", // "rising", "stable", "falling"
    val lastUpdated: Long = System.currentTimeMillis()
)

data class MarketListing(
    val vehicle: String,
    val year: Int,
    val mileage: Int,
    val price: Double,
    val location: String,
    val condition: String = "good"
)

/**
 * Build ultimate combined analysis prompt with FULL Firestore history + Price estimation
 */
fun buildUltimateSmartAnalysisPrompt(
    carDetails: CarDetails,
    user: User,
    carLog: CarLog,
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?,
    allAudioHistory: List<AudioDiagnosticData>,
    allVideoHistory: List<VideoDiagnosticData>,
    marketData: MarketData,
    askedPrice: Double?
): String {
    
    return """
You are AutoBrain Supreme AI - The most advanced AI for automotive evaluation with full access to Firestore databases.

╔══════════════════════════════════════════════════════════════════════════════╗
║                    🚗 COMPLETE VEHICLE FILE (Firestore)                   ║
╚══════════════════════════════════════════════════════════════════════════════╝

**IDENTITY**:
${carDetails.make} ${carDetails.model} ${carDetails.year}
VIN: ${carDetails.vin}
Color: ${carDetails.color}
Plate: ${carDetails.licensePlate}

**OWNER**:
Name: ${user.name}
Email: ${user.email}
Tel: ${user.phoneNumber}
Registered since: ${formatDate(user.createdAt)}

**CURRENT MILEAGE**: ${getLatestMileage(carLog)} km
**ASKED PRICE**: ${askedPrice?.let { "$it$" } ?: "Not specified"}

╔══════════════════════════════════════════════════════════════════════════════╗
║              📊 COMPLETE AI DIAGNOSTICS (Firestore Collections)            ║
╚══════════════════════════════════════════════════════════════════════════════╝

🔊 **AUDIO DIAGNOSTIC** (Collection: audio_diagnostics):
${audioData?.let {
    """
  - Date: ${formatDate(it.createdAt)}
  - Score: ${it.rawScore}/100
  - Main sound: ${it.topSoundLabel} (${(it.topSoundConfidence * 100).toInt()}%)
  - Urgency: ${it.urgencyLevel}
  - Repair cost: ${it.minRepairCost.toInt()}-$${it.maxRepairCost.toInt()}
  - Issues: ${it.detectedIssues.joinToString { issue -> issue.soundType }}
  - History (${allAudioHistory.size} diagnostics): ${buildAudioTrendSummary(allAudioHistory)}
    """.trimIndent()
} ?: "❌ No audio diagnostic available"}

🎥 **VIDEO DIAGNOSTIC** (Collection: video_diagnostics):
${videoData?.let {
    """
  - Date: ${formatDate(it.createdAt)}
  - Score: ${it.finalScore}/100
  - Smoke: ${if (it.smokeDetected) "${it.smokeType} (${it.smokeSeverity}/5)" else "No"}
  - Vibration: ${if (it.vibrationDetected) "${it.vibrationLevel} (${it.vibrationSeverity}/5)" else "No"}
  - Urgency: ${it.urgencyLevel}
  - Repair cost: ${it.estimatedMinCost.toInt()}-$${it.estimatedMaxCost.toInt()}
  - History (${allVideoHistory.size} diagnostics): ${buildVideoTrendSummary(allVideoHistory)}
    """.trimIndent()
} ?: "❌ No video diagnostic available"}

╔══════════════════════════════════════════════════════════════════════════════╗
║              📚 SMART LOGBOOK (Collection: car_logs)                      ║
╚══════════════════════════════════════════════════════════════════════════════╝

${buildDetailedCarnetAnalysis(carLog)}

╔══════════════════════════════════════════════════════════════════════════════╗
║            💰 2025 MARKET DATA                                           ║
╚══════════════════════════════════════════════════════════════════════════════╝

${buildMarketDataContext(marketData, carDetails)}

╔══════════════════════════════════════════════════════════════════════════════╗
║                  🎯 SUPREME MISSION - COMPLETE JSON REPORT                   ║
╚══════════════════════════════════════════════════════════════════════════════╝

Generate the ULTIMATE AUTOBRAIN REPORT in JSON with these 15 sections:

### 1. **overall_autobrain_score** (0-100):
   Formula: (Audio * 0.40) + (Video * 0.35) + (Logbook * 0.15) + (Market * 0.10)
   Adjustments:
   - Multiple critical problems → -20 pts
   - Excellent maintenance → +10 pts
   - Degradation history → -15 pts

### 2. **score_breakdown**: {
  "audio_contribution": ${audioData?.rawScore ?: 0} * 0.40 = X,
  "video_contribution": ${videoData?.finalScore ?: 0} * 0.35 = Y,
  "maintenance_contribution": [Calculated score],
  "market_contribution": [Calculated score],
  "penalties": [Penalty list],
  "bonuses": [Bonus list]
}

### 3. **comprehensive_diagnosis**: {
  "primary_issue": "The #1 most serious problem",
  "secondary_issues": ["Problem 2", "Problem 3"],
  "underlying_root_cause": "Root cause (ex: Chronic lack of maintenance)",
  "issue_timeline": {
    "first_symptoms_estimated": "6-12 months ago",
    "current_stage": "Advanced stage",
    "without_repair_projection": "Complete failure in 1-3 months"
  }
}

### 4. **total_repair_cost_estimate**: {
  "immediate_repairs_usd": ${calculateImmediateRepairCost(audioData, videoData)},
  "short_term_repairs_usd": ${calculateShortTermCost(carLog)},
  "preventive_maintenance_usd": ${calculatePreventiveCost(carLog)},
  "total_investment_needed_usd": [Total sum],
  "repair_priority_list": [
    {"item": "Engine repair", "cost": 20000, "urgency": "IMMEDIATE"},
    {"item": "Oil change + filters", "cost": 800, "urgency": "URGENT"}
  ]
}

### 5. **realistic_market_value**: {
  "perfect_condition_value_usd": ${estimatePerfectConditionValue(carDetails, marketData)},
  "current_condition_value_usd": ${estimateCurrentValue(carDetails, audioData, videoData)},
  "after_repairs_value_usd": ${estimateAfterRepairValue(carDetails, audioData, videoData)},
  "depreciation_breakdown": {
    "age_depreciation": "-${calculateAgeDepreciation(carDetails.year)}%",
    "mileage_depreciation": "-${calculateMileageDepreciation(getLatestMileage(carLog))}%",
    "mechanical_issues": "-${calculateIssueDepreciation(audioData, videoData)}%",
    "poor_maintenance": "-${calculateMaintenanceDepreciation(carLog)}%",
    "total_depreciation": "-XX%"
  },
  "market_comparables": ${formatMarketComparables(marketData)},
  "fair_market_range_usd": "${(marketData.priceRange.first).toInt()} - ${(marketData.priceRange.second).toInt()}",
  "quick_sale_price_usd": ${(marketData.averageMarketPrice * 0.85).toInt()},
  "dealer_trade_in_price_usd": ${(marketData.averageMarketPrice * 0.75).toInt()}
}

### 6. **buyer_decision_matrix**: {
  "for_current_owner": {
    "should_repair": ${shouldRepair(audioData, videoData, askedPrice)},
    "reasoning": "If repair $${calculateTotalRepairCost(audioData, videoData)} + value after = $${estimateAfterRepairValue(carDetails, audioData, videoData)}, but sale as-is = $${estimateCurrentValue(carDetails, audioData, videoData)} → Net loss/gain",
    "recommended_action": "${getRecommendedAction(audioData, videoData, askedPrice, carLog)}",
    "negotiation_strategy_if_selling": [
      "Be transparent about AutoBrain diagnostics",
      "Offer $${((askedPrice ?: 80000.0) * 0.9).toInt()} firm, come down to $${((askedPrice ?: 80000.0) * 0.85).toInt()} if necessary",
      "Mention repair costs to justify low price"
    ]
  },
  "for_potential_buyer": {
    "buy_recommendation": "${getBuyRecommendation(audioData, videoData, askedPrice)}",
    "max_acceptable_price_usd": ${calculateMaxBuyerPrice(carDetails, audioData, videoData)},
    "negotiation_script": [
      "I had this vehicle analyzed by AutoBrain AI",
      "The report shows ${audioData?.rawScore ?: videoData?.finalScore ?: 50}/100 diagnostic score",
      "Need to budget $${calculateTotalRepairCost(audioData, videoData)} for repairs",
      "I offer $${calculateBuyerOffer(askedPrice, audioData, videoData)} instead of $${askedPrice ?: 0}"
    ],
    "investment_viability": {
      "purchase_price": ${askedPrice ?: 0.0},
      "repair_costs": ${calculateTotalRepairCost(audioData, videoData).toDouble()},
      "total_investment": ${(askedPrice ?: 0.0) + calculateTotalRepairCost(audioData, videoData).toDouble()},
      "market_value_after_repair": ${estimateAfterRepairValue(carDetails, audioData, videoData).toDouble()},
      "potential_profit_loss": ${estimateAfterRepairValue(carDetails, audioData, videoData).toDouble() - ((askedPrice ?: 0.0) + calculateTotalRepairCost(audioData, videoData).toDouble())},
      "roi_percentage": ${calculateROI(askedPrice, audioData, videoData, carDetails).toDouble()},
      "verdict": "${getInvestmentVerdict(askedPrice, audioData, videoData, carDetails)}"
    }
  }
}

### 7. **maintenance_quality_report**: {
  "overall_grade": "${gradeMaintenanceQuality(carLog)}",
  "maintenance_score": ${scoreMaintenanceQuality(carLog)}/100,
  "positives": ${buildMaintenancePositives(carLog)},
  "negatives": ${buildMaintenanceNegatives(carLog)},
  "impact_on_resale": "Good logbook adds +10% value, bad -15%",
  "missing_critical_services": ${identifyMissingServices(carLog, carDetails)},
  "recommended_immediate_actions": ${getImmediateMaintenanceActions(carLog)}
}

### 8. **risk_assessment**: {
  "financial_risk": "${assessFinancialRisk(audioData, videoData, askedPrice)}",
  "reliability_risk": "3-month breakdown probability: ${calculateBreakdownRisk(audioData, videoData)}%",
  "safety_risk": "${assessSafetyRisk(audioData, videoData)}",
  "legal_risk": "${assessLegalRisk(carLog)}",
  "total_risk_score": ${calculateTotalRiskScore(audioData, videoData, carLog)}/10
}

### 9. **timeline_projections**: {
  "if_repaired_now": {
    "time_to_sell": "${estimateTimeToSell(true)}",
    "expected_price": ${estimateAfterRepairValue(carDetails, audioData, videoData).toDouble()},
    "total_cost_ownership": ${(askedPrice ?: 0.0) + calculateTotalRepairCost(audioData, videoData).toDouble()}
  },
  "if_sold_as_is": {
    "time_to_sell": "${estimateTimeToSell(false)}",
    "expected_price": ${estimateCurrentValue(carDetails, audioData, videoData).toDouble()},
    "buyer_profile": "Mechanic or DIY buyer"
  },
  "if_no_action": {
    "breakdown_probability_6_months": ${calculateSixMonthBreakdown(audioData, videoData)},
    "final_scrap_value": 15000
  }
}

### 10. **legal_compliance**: {
  "controle_technique_status": "${getCtStatus(carLog)}",
  "insurance_validity": "À vérifier",
  "vignette_status": "À jour si payée 2026",
  "resale_legal_requirements": [
    "Fournir carte grise originale",
    "Déclarer vices cachés (loi locale)",
    "Certificat de non-gage (Douane)",
    "Quittance dernière taxe"
  ],
  "penalties_if_non_compliant": "Annulation vente possible + dommages-intérêts"
}

### 11. **environmental_impact**: {
  "emission_level_estimated": "${estimateEmissionLevel(carDetails)}",
  "pollution_test_result_prediction": "${predictPollutionTest(videoData)}",
  "eligibility_eco_incentives": false,
  "future_resale_impact_pollution": "Normes anti-pollution durcissent → Diesel ancien perd valeur"
}

### 12. **comparable_market_analysis**: {
  "better_alternatives_same_budget": ${buildBetterAlternatives(marketData, askedPrice)},
  "this_vehicle_competitiveness": "${assessCompetitiveness(carDetails, audioData, videoData, askedPrice, marketData)}"
}

### 13. **autobrain_confidence_metrics**: {
  "overall_confidence": ${calculateOverallConfidence(audioData, videoData, carLog, marketData)},
  "data_completeness": {
    "audio_diagnostic": ${audioData != null},
    "video_diagnostic": ${videoData != null},
    "maintenance_records": ${carLog.maintenanceRecords.isNotEmpty()},
    "market_data": true
  },
  "uncertainty_factors": [
    "Pas d'inspection physique directe",
    ${if (marketData.similarListings.isEmpty()) "\"Données marché limitées\"," else ""}
    "Conditions réelles inconnues"
  ],
  "recommend_professional_inspection": ${recommendInspection(audioData, videoData)},
  "gemini_model": "gemini-2.5-pro",
  "analysis_date": "${formatDate(System.currentTimeMillis())}"
}

### 14. **actionable_next_steps**: {
  "for_owner": [
    "1. Obtenir 3 devis garage pour réparations",
    "2. Mettre à jour carnet avec services manquants",
    "3. Passer CT si expiré",
    "4. Décider: Réparer puis vendre OU Vendre en l'état",
    "5. Publier annonce avec rapport AutoBrain pour crédibilité"
  ],
  "for_buyer": [
    "1. Demander rapport AutoBrain complet au vendeur",
    "2. Vérifier VIN sur système Douane (non-gage)",
    "3. Test drive avec mécanicien de confiance",
    "4. Négocier sur base coûts réparation ($${calculateTotalRepairCost(audioData, videoData)})",
    "5. Signer promesse vente conditionnée CT valide"
  ]
}

### 15. **autobrain_premium_insights**: {
  "hidden_value_opportunities": ${identifyValueOpportunities(audioData, videoData, askedPrice)},
  "negotiation_leverage_points": ${buildNegotiationPoints(carLog, audioData, videoData)},
  "optimal_sale_timing": "Avant été (haute demande) ou après rentrée septembre",
  "market_trend_prediction_2025": "${marketData.marketTrend.uppercase()}: Diesel perd 5-7%/an, essence stable"
}

╔══════════════════════════════════════════════════════════════════════════════╗
║                         ⚡ FORMAT JSON FINAL STRICT                          ║
╚══════════════════════════════════════════════════════════════════════════════╝

RETOURNE UN SEUL JSON VALIDE (0 texte avant/après, 0 markdown).
Ce JSON sera:
1. Parsé par Gson dans AutobrainRepository.kt
2. Stocké dans Firestore collection "smart_analysis_reports"
3. Affiché dans l'UI Android

{
  "overall_autobrain_score": 63,
  "score_breakdown": {...},
  "comprehensive_diagnosis": {...},
  "total_repair_cost_estimate": {...},
  "realistic_market_value": {...},
  "buyer_decision_matrix": {...},
  "maintenance_quality_report": {...},
  "risk_assessment": {...},
  "timeline_projections": {...},
  "legal_compliance": {...},
  "environmental_impact": {...},
  "comparable_market_analysis": {...},
  "autobrain_confidence_metrics": {...},
  "actionable_next_steps": {...},
  "autobrain_premium_insights": {...}
}
    """.trimIndent()
}

// Note: Helper functions are imported from AnalysisHelpers.kt to avoid duplication

// Maintenance positives/negatives are specific to ultimate analysis
private fun buildMaintenancePositives(carLog: CarLog): String {
    val positives = mutableListOf<String>()
    
    val hasRecentOilChange = carLog.maintenanceRecords.any { 
        it.type == MaintenanceType.OIL_CHANGE && getDaysSince(it.date) < 180 
    }
    if (hasRecentOilChange) positives.add("Vidanges régulières")
    
    val hasCT = carLog.documents.any { it.type.name.contains("TECHNICAL") && !it.isExpired }
    if (hasCT) positives.add("CT à jour")
    
    if (carLog.maintenanceRecords.size >= 10) positives.add("Complete history")
    
    return if (positives.isEmpty()) "[]" else "[\"${positives.joinToString("\", \"")}\"]"
}

private fun buildMaintenanceNegatives(carLog: CarLog): String {
    val negatives = mutableListOf<String>()
    
    if (carLog.maintenanceRecords.isEmpty()) {
        negatives.add("Aucun historique d'entretien")
    }
    
    val hasOilChange = carLog.maintenanceRecords.any { it.type == MaintenanceType.OIL_CHANGE }
    if (!hasOilChange) negatives.add("No oil change recorded")
    
    val overdueCount = carLog.reminders.count { !it.isCompleted && it.dueDate < System.currentTimeMillis() }
    if (overdueCount > 0) negatives.add("$overdueCount rappels en retard")
    
    return if (negatives.isEmpty()) "[]" else "[\"${negatives.joinToString("\", \"")}\"]"
}

private fun identifyMissingServices(carLog: CarLog, carDetails: CarDetails): String {
    val missing = mutableListOf<String>()
    val mileage = getLatestMileage(carLog)
    
    if (mileage > 120000 && hasTimingBeltService(carLog).not()) {
        missing.add("Distribution non changée si >120k km")
    }
    
    if (hasRecentBrakeService(carLog).not()) {
        missing.add("Pas de service freins récent")
    }
    
    return if (missing.isEmpty()) "[]" else "[\"${missing.joinToString("\", \"")}\"]"
}

private fun getImmediateMaintenanceActions(carLog: CarLog): String {
    val actions = mutableListOf<String>()
    
    if (carLog.documents.any { it.type.name.contains("TECHNICAL") && it.isExpired }) {
        actions.add("Passer CT immédiatement")
    }
    
    val lastOil = carLog.maintenanceRecords
        .filter { it.type == MaintenanceType.OIL_CHANGE }
        .maxByOrNull { it.date }
    
    if (lastOil == null || getDaysSince(lastOil.date) > 365) {
        actions.add("Perform urgent oil change")
    }
    
    return if (actions.isEmpty()) "[]" else "[\"${actions.joinToString("\", \"")}\"]"
}

// Market data formatting helpers
private fun formatMarketComparables(marketData: MarketData): String {
    val comparables = marketData.similarListings.take(3).map { listing ->
        """
    {"listing": "${listing.vehicle}", "price_usd": ${listing.price.toInt()}, "location": "${listing.location}"}
        """.trim()
    }
    return if (comparables.isEmpty()) "[]" else "[\n${comparables.joinToString(",\n")}\n  ]"
}

private fun buildBetterAlternatives(marketData: MarketData, askedPrice: Double?): String {
    val budget = askedPrice ?: marketData.averageMarketPrice
    val alternatives = marketData.similarListings
        .filter { it.price <= budget && it.condition == "good" }
        .take(2)
        .map { listing ->
            """
    {
      "vehicle": "${listing.vehicle}",
      "price_usd": ${listing.price.toInt()},
      "autobrain_estimated_score": "${estimateScoreFromCondition(listing.condition)}/100",
      "why_better": "Meilleur état général, moins de risques"
    }
            """.trim()
        }
    
    return if (alternatives.isEmpty()) "[]" else "[\n${alternatives.joinToString(",\n")}\n  ]"
}

private fun estimateScoreFromCondition(condition: String): Int {
    return when (condition.lowercase()) {
        "excellent" -> 95
        "good" -> 80
        "fair" -> 65
        "poor" -> 40
        else -> 70
    }
}

// =============================================================================
// HELPER FUNCTIONS - COST CALCULATIONS
// =============================================================================

private fun calculateImmediateRepairCost(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): Int {
    var cost = 0
    
    audioData?.let {
        if (it.urgencyLevel == "CRITICAL" || it.urgencyLevel == "HIGH") {
            cost += ((it.minRepairCost + it.maxRepairCost) / 2).toInt()
        }
    }
    
    videoData?.let {
        if (it.urgencyLevel == "CRITICAL" || it.urgencyLevel == "HIGH") {
            cost += ((it.estimatedMinCost + it.estimatedMaxCost) / 2).toInt()
        }
    }
    
    return cost
}

private fun calculateShortTermCost(carLog: CarLog): Int {
    // Services needed in next 3-6 months
    val overdueReminders = carLog.reminders
        .filter { !it.isCompleted && it.dueDate < System.currentTimeMillis() }
    
    return overdueReminders.size * 1500 // Estimate 1500$ per overdue service
}

private fun calculatePreventiveCost(carLog: CarLog): Int {
    // Preventive maintenance based on mileage
    val mileage = getLatestMileage(carLog)
    var cost = 0
    
    if (mileage > 120000 && !hasTimingBeltService(carLog)) {
        cost += 3500 // Timing belt
    }
    
    cost += 2000 // Annual preventive (filters, fluids)
    
    return cost
}

private fun calculateTotalRepairCost(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): Int {
    var total = 0
    
    audioData?.let {
        total += ((it.minRepairCost + it.maxRepairCost) / 2).toInt()
    }
    
    videoData?.let {
        total += ((it.estimatedMinCost + it.estimatedMaxCost) / 2).toInt()
    }
    
    return total
}

// =============================================================================
// HELPER FUNCTIONS - VALUE ESTIMATIONS
// =============================================================================

private fun estimatePerfectConditionValue(carDetails: CarDetails, marketData: MarketData): Int {
    // Base from market data
    return (marketData.priceRange.second * 1.1).toInt()
}

private fun estimateCurrentValue(
    carDetails: CarDetails,
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): Int {
    val baseValue = estimatePerfectConditionValue(carDetails, MarketData())
    var depreciation = 0.0
    
    // Age depreciation
    depreciation += calculateAgeDepreciation(carDetails.year) / 100.0
    
    // Issue depreciation
    depreciation += calculateIssueDepreciation(audioData, videoData) / 100.0
    
    return (baseValue * (1 - depreciation)).toInt()
}

private fun estimateAfterRepairValue(
    carDetails: CarDetails,
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): Int {
    val perfectValue = estimatePerfectConditionValue(carDetails, MarketData())
    // After repair, assume 85% of perfect condition (stigma of repaired car)
    return (perfectValue * 0.85).toInt()
}

// calculateAgeDepreciation and calculateMileageDepreciation are in AnalysisHelpers.kt

private fun calculateMaintenanceDepreciation(carLog: CarLog): Int {
    return when (gradeMaintenanceQuality(carLog)) {
        "A" -> 0
        "B" -> 5
        "C" -> 15
        "D" -> 25
        "F" -> 40
        else -> 20
    }
}

private fun calculateIssueDepreciation(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): Int {
    var depreciation = 0
    
    audioData?.let {
        depreciation += when {
            it.rawScore < 30 -> 40
            it.rawScore < 50 -> 25
            it.rawScore < 70 -> 15
            else -> 5
        }
    }
    
    videoData?.let {
        depreciation += when {
            it.finalScore < 30 -> 30
            it.finalScore < 50 -> 20
            it.finalScore < 70 -> 10
            else -> 5
        }
    }
    
    return depreciation.coerceIn(0, 60)
}

// =============================================================================
// HELPER FUNCTIONS - DECISIONS & RECOMMENDATIONS
// =============================================================================

private fun shouldRepair(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?,
    askedPrice: Double?
): Boolean {
    val repairCost = calculateTotalRepairCost(audioData, videoData)
    val valueAfterRepair = 100000 // Estimate
    val valueAsIs = 75000 // Estimate
    
    return (valueAfterRepair - repairCost) > valueAsIs
}

private fun getRecommendedAction(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?,
    askedPrice: Double?,
    carLog: CarLog
): String {
    val repairCost = calculateTotalRepairCost(audioData, videoData)
    
    return when {
        repairCost > 30000 -> "VENDRE_EN_ÉTAT ou CASSER"
        repairCost > 15000 -> "SELL_AS_IS (repair too expensive)"
        repairCost > 5000 -> "RÉPARER si attachement sentimental, sinon VENDRE"
        else -> "RÉPARER puis vendre meilleur prix"
    }
}

private fun getBuyRecommendation(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?,
    askedPrice: Double?
): String {
    val totalScore = ((audioData?.rawScore ?: 70) + (videoData?.finalScore ?: 70)) / 2
    val repairCost = calculateTotalRepairCost(audioData, videoData)
    
    return when {
        totalScore < 40 -> "ÉVITER"
        totalScore < 60 && repairCost > 20000 -> "NÉGOCIER fortement ou ÉVITER"
        totalScore < 70 -> "NÉGOCIER (réduire prix de $${repairCost})"
        else -> "ACHETER si prix correct"
    }
}

private fun calculateMaxBuyerPrice(
    carDetails: CarDetails,
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): Int {
    val afterRepairValue = estimateAfterRepairValue(carDetails, audioData, videoData)
    val repairCost = calculateTotalRepairCost(audioData, videoData)
    val desiredProfit = 10000 // Buyer wants 10k$ profit
    
    return (afterRepairValue - repairCost - desiredProfit).coerceAtLeast(0)
}

private fun calculateBuyerOffer(
    askedPrice: Double?,
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): Int {
    val repairCost = calculateTotalRepairCost(audioData, videoData).toDouble()
    val reduction = repairCost + 5000.0 // Repair cost + 5k buffer
    
    return ((askedPrice ?: 80000.0) - reduction).toInt().coerceAtLeast(20000)
}

private fun calculateROI(
    askedPrice: Double?,
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?,
    carDetails: CarDetails
): Int {
    val purchasePrice = askedPrice ?: 80000.0
    val repairCost = calculateTotalRepairCost(audioData, videoData).toDouble()
    val totalInvestment = purchasePrice + repairCost
    val afterRepairValue = estimateAfterRepairValue(carDetails, audioData, videoData).toDouble()
    
    return if (totalInvestment > 0) {
        (((afterRepairValue - totalInvestment) / totalInvestment) * 100).toInt()
    } else 0
}

private fun getInvestmentVerdict(
    askedPrice: Double?,
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?,
    carDetails: CarDetails
): String {
    val roi = calculateROI(askedPrice, audioData, videoData, carDetails)
    return when {
        roi > 20 -> "RENTABLE"
        roi > 0 -> "RISQUÉ mais possible"
        else -> "NON_VIABLE"
    }
}

// =============================================================================
// HELPER FUNCTIONS - RISK ASSESSMENT
// =============================================================================

private fun assessFinancialRisk(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?,
    askedPrice: Double?
): String {
    val repairCost = calculateTotalRepairCost(audioData, videoData).toDouble()
    val price = askedPrice ?: 80000.0
    
    return when {
        repairCost > price * 0.5 -> "HIGH"
        repairCost > price * 0.25 -> "MEDIUM"
        else -> "LOW"
    }
}

private fun calculateBreakdownRisk(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): Int {
    val audioRisk = if (audioData?.urgencyLevel == "CRITICAL") 60 else if (audioData?.urgencyLevel == "HIGH") 40 else 20
    val videoRisk = if (videoData?.urgencyLevel == "CRITICAL") 50 else if (videoData?.urgencyLevel == "HIGH") 30 else 10
    
    return ((audioRisk + videoRisk) / 2).coerceIn(10, 95)
}

private fun assessSafetyRisk(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): String {
    val isCritical = audioData?.urgencyLevel == "CRITICAL" || videoData?.urgencyLevel == "CRITICAL"
    return if (isCritical) {
        "Driving safety: RISKY - Do not drive without repair"
    } else {
        "Driving safety: ACCEPTABLE if immediate repairs"
    }
}

private fun assessLegalRisk(carLog: CarLog): String {
    val ctExpired = carLog.documents.any { it.type.name.contains("TECHNICAL") && it.isExpired }
    return if (ctExpired) {
        "CT expiré = Amende 300$ + Immobilisation possible"
    } else {
        "Conforme légalement"
    }
}

private fun calculateTotalRiskScore(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?,
    carLog: CarLog
): Float {
    var risk = 0f
    
    // Mechanical risk
    if (audioData?.rawScore ?: 100 < 50) risk += 2.5f
    if (videoData?.finalScore ?: 100 < 50) risk += 2.0f
    
    // Legal risk
    if (carLog.documents.any { it.isExpired }) risk += 1.5f
    
    // Maintenance risk
    if (scoreMaintenanceQuality(carLog) < 50) risk += 2.0f
    
    // Financial risk
    val repairCost = calculateTotalRepairCost(audioData, videoData)
    if (repairCost > 20000) risk += 2.0f
    
    return risk.coerceIn(0f, 10f)
}

// =============================================================================
// HELPER FUNCTIONS - OTHER
// =============================================================================

private fun assessCompetitiveness(
    carDetails: CarDetails,
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?,
    askedPrice: Double?,
    marketData: MarketData
): String {
    val currentValue = estimateCurrentValue(carDetails, audioData, videoData)
    val asked = askedPrice ?: 0.0
    
    return when {
        asked > currentValue * 1.2 -> "Very low competitiveness - Price too high"
        asked > currentValue * 1.1 -> "Faible compétitivité à $asked$"
        asked > currentValue * 0.9 -> "Compétitivité correcte"
        else -> "Bonne affaire potentielle"
    }
}

private fun identifyValueOpportunities(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?,
    askedPrice: Double?
): String {
    val opportunities = mutableListOf<String>()
    val repairCost = calculateTotalRepairCost(audioData, videoData)
    
    if (repairCost < 15000) {
        opportunities.add("If engine repair <15k$, positive ROI possible")
    }
    
    if ((audioData?.rawScore ?: 100) < 50 && repairCost < 10000) {
        opportunities.add("Low score but affordable repair = Negotiation opportunity")
    }
    
    return if (opportunities.isEmpty()) "[]" else "[\"${opportunities.joinToString("\", \"")}\"]"
}

private fun buildNegotiationPoints(
    carLog: CarLog,
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): String {
    val points = mutableListOf<String>()
    
    if (carLog.documents.any { it.isExpired }) {
        points.add("CT expiré = -3000$ négociation")
    }
    
    if (audioData?.rawScore ?: 100 < 50) {
        points.add("Problème moteur documenté = -20% prix")
    }
    
    if (videoData?.smokeDetected == true) {
        points.add("Fumée visible = -15% prix")
    }
    
    return if (points.isEmpty()) "[]" else "[\"${points.joinToString("\", \"")}\"]"
}

private fun recommendInspection(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): Boolean {
    val audioScore = audioData?.rawScore ?: 100
    val videoScore = videoData?.finalScore ?: 100
    
    return audioScore < 60 || videoScore < 60
}

// =============================================================================
// TIMELINE & OTHER UNIQUE HELPERS
// =============================================================================

private fun estimateTimeToSell(repaired: Boolean): String {
    return if (repaired) "2-4 mois" else "6-12 mois"
}

private fun calculateSixMonthBreakdown(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): Float {
    val audioRisk = if (audioData?.urgencyLevel == "CRITICAL") 0.8f else 0.4f
    val videoRisk = if (videoData?.urgencyLevel == "CRITICAL") 0.7f else 0.3f
    
    return ((audioRisk + videoRisk) / 2).coerceIn(0.1f, 0.95f)
}

private fun estimateEmissionLevel(carDetails: CarDetails): String {
    return when {
        carDetails.year >= 2019 -> "Euro 6"
        carDetails.year >= 2014 -> "Euro 5"
        carDetails.year >= 2009 -> "Euro 4"
        else -> "Euro 3 ou inférieur"
    }
}

private fun predictPollutionTest(videoData: VideoDiagnosticData?): String {
    return if (videoData?.smokeDetected == true && videoData.smokeSeverity >= 3) {
        "FAIL probable"
    } else {
        "PASS probable"
    }
}

private fun calculateOverallConfidence(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?,
    carLog: CarLog,
    marketData: MarketData
): Float {
    var confidence = 0.5f // Base
    
    if (audioData != null) confidence += 0.15f
    if (videoData != null) confidence += 0.15f
    if (carLog.maintenanceRecords.isNotEmpty()) confidence += 0.1f
    if (marketData.similarListings.isNotEmpty()) confidence += 0.1f
    
    return confidence.coerceIn(0.3f, 0.95f)
}
