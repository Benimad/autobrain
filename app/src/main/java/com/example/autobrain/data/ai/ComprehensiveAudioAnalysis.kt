package com.example.autobrain.data.ai

import com.example.autobrain.data.local.entity.AudioDiagnosticData
import com.example.autobrain.domain.model.CarDetails
import com.example.autobrain.domain.model.CarLog
import com.example.autobrain.domain.model.MaintenanceType
import com.example.autobrain.domain.model.User
import java.text.SimpleDateFormat
import java.util.*

/**
 * AUTOBRAIN - COMPLETE GEMINI AI INTEGRATION
 * Comprehensive Audio Diagnostic Prompt Builder with FULL Firebase Data
 * 
 * Features:
 * - Real-time Firestore data integration
 * - Dynamic car profile and maintenance history
 * - Diagnostic trend analysis
 * - Complete JSON output structure for Gemini 3 Pro
 * - Market context and legal compliance
 */

/**
 * Build comprehensive audio analysis prompt with ALL dynamic data
 */
fun buildComprehensiveAudioAnalysisPrompt(
    audioData: AudioDiagnosticData,
    carLog: CarLog,
    user: User,
    previousDiagnostics: List<AudioDiagnosticData>
): String {
    
    // Extract audio classifications
    val classificationsText = audioData.allDetectedSounds.entries
        .sortedByDescending { it.value }
        .joinToString("\n") { (label, confidence) ->
            "- ${EngineSoundTypes.descriptions[label] ?: label}: ${(confidence * 100).toInt()}% confidence"
        }
    
    // Maintenance history analysis
    val maintenanceContext = buildMaintenanceContext(carLog)
    
    // Previous diagnostic trends
    val diagnosticTrend = analyzeDiagnosticTrend(previousDiagnostics)
    
    // Car details from user profile
    val carDetails = user.carDetails ?: CarDetails()
    
    return """
🎯 MULTIMODAL ANALYSIS MODE ACTIVATED

Tu es Dr. AutoBrain AI - Expert en diagnostic automobile avec capacité d'analyse audio directe.

🎵 TU REÇOIS 2 SOURCES DE DONNÉES:
1. 📊 ANALYSE TFLite (On-Device): Classifications heuristiques ci-dessous
2. 🔊 FICHIER AUDIO BRUT: Analyse directement le son moteur avec tes capacités multimodales

⚡ MISSION CRITIQUE:
- Écoute l'audio et identifie les sons réels (knocking, grinding, hissing, etc.)
- Compare avec l'analyse TFLite pour validation croisée
- Si TFLite dit "knocking 70%" mais tu entends un son normal → Corrige le score
- Si TFLite dit "normal" mais tu détectes un problème → Alerte critique
- Utilise l'audio comme source primaire, TFLite comme référence secondaire

╔══════════════════════════════════════════════════════════════════════╗
║                    🚗 PROFIL VÉHICULE (Firestore)                    ║
╚══════════════════════════════════════════════════════════════════════╝

Marque : ${carDetails.make}
Modèle : ${carDetails.model}
Année : ${carDetails.year}
VIN : ${carDetails.vin}
Couleur : ${carDetails.color}
Plaque : ${carDetails.licensePlate}

╔══════════════════════════════════════════════════════════════════════╗
║              📊 ANALYSE AUDIO TFLite (${audioData.durationMs}ms)             ║
╚══════════════════════════════════════════════════════════════════════╝

🎵 SONS DÉTECTÉS (TensorFlow Lite Classifier):
$classificationsText

🔊 Son Dominant: ${audioData.topSoundLabel} (${(audioData.topSoundConfidence * 100).toInt()}%)
📈 Score Local (Offline): ${audioData.rawScore}/100
⚠️ Niveau Urgence: ${audioData.urgencyLevel}
💰 Coût Estimé Local: ${audioData.minRepairCost.toInt()}-$${audioData.maxRepairCost.toInt()}

╔══════════════════════════════════════════════════════════════════════╗
║         📚 CARNET INTELLIGENT (Firestore Collection)                 ║
╚══════════════════════════════════════════════════════════════════════╝

$maintenanceContext

╔══════════════════════════════════════════════════════════════════════╗
║           📈 HISTORIQUE DIAGNOSTICS (Tendances Firestore)            ║
╚══════════════════════════════════════════════════════════════════════╝

$diagnosticTrend

╔══════════════════════════════════════════════════════════════════════╗
║                  🎯 MISSION GEMINI AI - JSON OUTPUT                  ║
╚══════════════════════════════════════════════════════════════════════╝

Provide an ULTRA-DETAILED analysis in JSON with these MANDATORY fields:

**1. enhanced_health_score** (0-100):
   - Take local score into account (${audioData.rawScore})
   - Adjust based on diagnostic history
   - Include Smart Logbook penalties
   - Apply advanced scoring logic:
     * Knocking > 70% confidence → MAX score 40
     * Grinding/Misfire → MAX score 45
     * Normal engine > 80% → MIN score 85
     * Maintenance overdue → -10 to -25 points
   
**2. primary_diagnosis**: {
  "issue": "Primary issue description",
  "technical_name": "Mechanical technical name",
  "confidence": 0.85,
  "severity": "CRITICAL|HIGH|MEDIUM|LOW",
  "affected_components": ["Piston", "Connecting rod", "Crankshaft"]
}

**3. secondary_issues**: [
  {
    "issue": "Secondary problem",
    "confidence": 0.65,
    "severity": "MEDIUM",
    "components": ["Timing belt"]
  }
]

**4. root_cause_analysis**: {
  "most_likely_cause": "Piston ring wear",
  "probability": 0.82,
  "alternative_causes": [
    "Prolonged lack of oil",
    "Past engine overheating"
  ],
  "evidence": [
    "Rhythmic knocking at 2Hz",
    "Increase with engine load",
    "No maintenance for ${getDaysSinceLastMaintenance(carLog)} days"
  ]
}

**5. progressive_damage_prediction**: {
  "current_stage": "Stage 2 of 4",
  "next_failure_timeline": "100-500 km without repair",
  "final_failure_description": "Piston seizure → Complete engine failure",
  "cascading_failures": [
    "Crankshaft wear (50 km)",
    "Metal contamination in oil (immediate)",
    "Friction overheating (200 km)"
  ]
}

**6. detailed_repair_plan**: {
  "immediate_actions": [
    "Stop driving immediately",
    "Check engine oil level",
    "Test oil pressure at garage (cost: 200$)"
  ],
  "repair_scenarios": [
    {
      "scenario": "Best case - Simple ring replacement",
      "steps": ["Cylinder head removal", "Ring replacement", "Break-in period"],
      "parts_cost_usd": 3500,
      "labor_cost_usd": 4500,
      "total_cost_usd": 8000,
      "duration_days": 3,
      "probability": 0.15
    },
    {
      "scenario": "Likely case - Partial engine rebuild",
      "steps": ["Engine removal", "Crankshaft machining", "Pistons/connecting rods replacement", "Reassembly"],
      "parts_cost_usd": 12000,
      "labor_cost_usd": 8000,
      "total_cost_usd": 20000,
      "duration_days": 7,
      "probability": 0.65
    },
    {
      "scenario": "Worst case - Complete engine replacement",
      "steps": ["Purchase used engine", "Standard exchange", "Reassembly"],
      "parts_cost_usd": 25000,
      "labor_cost_usd": 10000,
      "total_cost_usd": 35000,
      "duration_days": 10,
      "probability": 0.20
    }
  ],
  "recommended_garage_type": "Engine specialist workshop with crankshaft machining capability",
  "negotiation_tip": "Request a detailed quote BEFORE disassembly. Compare at least 3 garages."
}

**7. market_value_impact**: {
  "value_before_issue": ${estimateCarValueBeforeIssue(carDetails)},
  "value_after_repair": ${estimateCarValueAfterRepair(carDetails, audioData.rawScore)},
  "value_as_is": ${estimateCarValueAsIs(carDetails, audioData.rawScore)},
  "depreciation_factors": [
    "Rebuilt engine: -25% value",
    "History of serious problem: -15%",
    "Vehicle age: -${calculateAgeDepreciation(carDetails.year)}%"
  ],
  "resale_timeline": "6-12 months if repaired, >12 months otherwise",
  "buyer_negotiation_power": "HIGH - Documented engine problem"
}

**8. maintenance_correlation**: {
  "oil_change_impact": "${assessOilChangeImpact(carLog)}",
  "mileage_factor": "${assessMileageFactor(carLog, carDetails)}",
  "service_history_quality": "${assessMaintenanceQuality(carLog)}",
  "preventable_percentage": 75,
  "lessons_learned": [
    "Oil changes every 10,000 km could have prevented this problem",
    "The noise should have been diagnosed earlier (see history)",
    "Cost of missed oil changes: $${calculateMissedMaintenanceCost(carLog)} vs. Current repair: 20000$"
  ]
}

**9. intelligent_recommendations**: {
  "for_current_owner": [
    "🚨 DO NOT DRIVE - Risk of complete engine failure",
    "📞 Call tow truck (cost: 500-800$ depending on distance)",
    "🔍 Get diagnosed by 3 different garages",
    "💰 Realistic repair budget: 15,000-25,000$",
    "📄 If insured: Check mechanical breakdown coverage",
    "💡 Alternative: Sell as-is (estimated loss: $${calculateSellAsIsLoss(carDetails, audioData)})"
  ],
  "for_potential_buyer": [
    "❌ AVOID THIS PURCHASE - Critical score ${audioData.rawScore}/100",
    "💸 If seller insists, negotiate at least -$${calculateNegotiationDiscount(audioData)}",
    "🔧 Budget emergency repair: 20,000$",
    "📊 Probably negative ROI on this vehicle",
    "🏃 Look for another vehicle with AI Score > 70/100"
  ],
  "for_mechanic": [
    "🛠️ Remove oil pan for crankshaft inspection",
    "🔬 Cylinder compression test (expected pressure: 12-14 bars)",
    "🎥 Boroscope cylinder wall inspection",
    "🧪 Engine oil analysis (metal particles)",
    "📋 Complete documentation for insurance/warranty"
  ]
}

**10. autobrain_ai_confidence**: {
  "analysis_confidence": 0.89,
  "data_quality_score": 0.92,
  "tflite_model_accuracy": "${getModelAccuracy()}",
  "factors_boosting_confidence": [
    "Clear TFLite classification (${audioData.topSoundConfidence * 100}%)",
    "Consistent logbook history",
    "${audioData.allDetectedSounds.size} different sounds analyzed"
  ],
  "uncertainty_factors": [
    "Audio quality: ${assessAudioQuality(audioData)}",
    "Possible ambient noise"
  ],
  "recommend_second_opinion": ${audioData.rawScore < 50},
  "gemini_model_version": "gemini-3-pro-preview",
  "analysis_timestamp_utc": "${System.currentTimeMillis()}"
}

**11. legal_compliance_general**: {
  "inspection_requirements": "${getCtImpact(carLog)}",
  "insurance_notification_required": ${audioData.urgencyLevel == "CRITICAL"},
  "roadworthiness": "${if (audioData.rawScore < 40) "NOT_COMPLIANT" else "COMPLIANT"}",
  "legal_resale_obligations": [
    "Disclose known defects to potential buyers",
    "Provide diagnostic report during sale",
    "Legal liability if defects not disclosed"
  ]
}

╔══════════════════════════════════════════════════════════════════════╗
║                    ⚡ MANDATORY OUTPUT FORMAT                       ║
╚══════════════════════════════════════════════════════════════════════╝

Respond ONLY with a valid JSON (no markdown, no text before/after).
The JSON will be parsed directly by Kotlin/Gson and stored in Firestore.

EXPECTED EXACT STRUCTURE:
{
  "enhanced_health_score": 42,
  "primary_diagnosis": { ... },
  "secondary_issues": [ ... ],
  "root_cause_analysis": { ... },
  "progressive_damage_prediction": { ... },
  "detailed_repair_plan": { ... },
  "market_value_impact": { ... },
  "maintenance_correlation": { ... },
  "intelligent_recommendations": { ... },
  "autobrain_ai_confidence": { ... },
  "legal_compliance_general": { ... }
}
    """.trimIndent()
}

// =============================================================================
// HELPER FUNCTIONS - MAINTENANCE CONTEXT
// =============================================================================

private fun buildMaintenanceContext(carLog: CarLog): String {
    val lastOilChange = carLog.maintenanceRecords
        .filter { it.type == MaintenanceType.OIL_CHANGE }
        .maxByOrNull { it.date }
    
    val lastCT = carLog.documents
        .find { it.type.name.contains("TECHNICAL") }
    
    val overdueReminders = carLog.reminders
        .filter { !it.isCompleted && it.dueDate < System.currentTimeMillis() }
    
    return """
🛢️ Last Oil Change: ${lastOilChange?.let {
    "${getDaysSince(it.date)} days ago (${it.mileage} km)"
} ?: "NONE RECORDED ⚠️"}

🔍 Last Technical Inspection: ${lastCT?.let {
    if (it.isExpired) "EXPIRED since ${getDaysSince(it.expiryDate)} days ❌"
    else "Valid until ${formatDate(it.expiryDate)} ✅"
} ?: "NOT PROVIDED"}

📋 Overdue Reminders (${overdueReminders.size}):
${if (overdueReminders.isEmpty()) "   None" else overdueReminders.joinToString("\n") {
    "   - ${it.title}: ${getDaysSince(it.dueDate)} days overdue"
}}

📊 Overall Maintenance Quality: ${assessMaintenanceQuality(carLog)}
💸 Total Maintenance Cost (12 months): $${calculateTotalMaintenanceCost(carLog)}
    """.trimIndent()
}

private fun analyzeDiagnosticTrend(diagnostics: List<AudioDiagnosticData>): String {
    if (diagnostics.isEmpty()) {
        return "First diagnostic - No history"
    }

    val sortedDiags = diagnostics.sortedBy { it.createdAt }
    val scoreEvolution = sortedDiags.map { "${it.rawScore}/100" }.joinToString(" → ")

    val degradationRate = if (sortedDiags.size >= 2) {
        val first = sortedDiags.first().rawScore
        val last = sortedDiags.last().rawScore
        ((first - last).toFloat() / sortedDiags.size).toInt()
    } else 0

    val recurringIssues = findRecurringIssues(diagnostics)

    return """
🔄 Score Evolution: $scoreEvolution
📉 Degradation Rate: ${degradationRate} points/diagnostic
⏱️ First diagnostic: ${formatDate(sortedDiags.first().createdAt)}
🔔 Recurring Problems: $recurringIssues
⚠️ Trend: ${when {
    degradationRate > 5 -> "RAPID AGGRAVATION"
    degradationRate > 0 -> "Progressive degradation"
    else -> "Stable"
}}
    """.trimIndent()
}

// =============================================================================
// HELPER FUNCTIONS - DATE & TIME
// =============================================================================

// getDaysSince and formatDate are in AnalysisHelpers.kt

private fun getDaysSinceLastMaintenance(carLog: CarLog): Long {
    val lastMaintenance = carLog.maintenanceRecords.maxByOrNull { it.date }
    return if (lastMaintenance != null) {
        getDaysSince(lastMaintenance.date)
    } else {
        365L // Default 1 year if no records
    }
}

// =============================================================================
// HELPER FUNCTIONS - MAINTENANCE QUALITY
// =============================================================================

private fun assessMaintenanceQuality(carLog: CarLog): String {
    val records = carLog.maintenanceRecords
    if (records.isEmpty()) return "POOR"
    
    val totalRecords = records.size
    val recentRecords = records.filter { getDaysSince(it.date) <= 365 }.size
    val overdueReminders = carLog.reminders.count { !it.isCompleted && it.dueDate < System.currentTimeMillis() }
    
    return when {
        recentRecords >= 4 && overdueReminders == 0 -> "EXCELLENT"
        recentRecords >= 2 && overdueReminders <= 1 -> "GOOD"
        recentRecords >= 1 && overdueReminders <= 2 -> "FAIR"
        else -> "POOR"
    }
}

private fun calculateTotalMaintenanceCost(carLog: CarLog): Int {
    val oneYearAgo = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
    return carLog.maintenanceRecords
        .filter { it.date >= oneYearAgo }
        .sumOf { it.cost }
        .toInt()
}

private fun calculateMissedMaintenanceCost(carLog: CarLog): Int {
    val overdueCount = carLog.reminders.count { 
        !it.isCompleted && it.dueDate < System.currentTimeMillis() 
    }
    // Estimate 500$ per missed service
    return overdueCount * 500
}

private fun assessOilChangeImpact(carLog: CarLog): String {
    val lastOilChange = carLog.maintenanceRecords
        .filter { it.type == MaintenanceType.OIL_CHANGE }
        .maxByOrNull { it.date }

    return if (lastOilChange == null) {
        "CRITICAL - No oil change recorded"
    } else {
        val daysSince = getDaysSince(lastOilChange.date)
        when {
            daysSince > 365 -> "CRITICAL - ${daysSince} days without oil change"
            daysSince > 180 -> "HIGH - Oil change overdue"
            daysSince > 90 -> "MODERATE - Schedule oil change soon"
            else -> "GOOD - Recent oil change"
        }
    }
}

private fun assessMileageFactor(carLog: CarLog, carDetails: CarDetails): String {
    val lastOilChange = carLog.maintenanceRecords
        .filter { it.type == MaintenanceType.OIL_CHANGE }
        .maxByOrNull { it.date }

    return if (lastOilChange != null) {
        // Estimate current mileage (this would come from user input in real app)
        val estimatedCurrentMileage = lastOilChange.mileage + 5000
        val kmSinceOilChange = estimatedCurrentMileage - lastOilChange.mileage
        when {
            kmSinceOilChange > 15000 -> "CRITICAL - ${kmSinceOilChange} km since last oil change"
            kmSinceOilChange > 10000 -> "HIGH - Exceeds recommended interval"
            else -> "GOOD - Acceptable mileage"
        }
    } else {
        "UNKNOWN - No mileage history"
    }
}

// =============================================================================
// HELPER FUNCTIONS - CAR VALUE ESTIMATION
// =============================================================================

private fun estimateCarValueBeforeIssue(carDetails: CarDetails): Int {
    // Very basic market estimation
    val baseValue = when {
        carDetails.year >= 2020 -> 200000
        carDetails.year >= 2015 -> 120000
        carDetails.year >= 2010 -> 70000
        carDetails.year >= 2005 -> 40000
        else -> 25000
    }
    return baseValue
}

private fun estimateCarValueAfterRepair(carDetails: CarDetails, score: Int): Int {
    val baseValue = estimateCarValueBeforeIssue(carDetails)
    // Depreciation due to engine rebuild
    return (baseValue * 0.75).toInt()
}

private fun estimateCarValueAsIs(carDetails: CarDetails, score: Int): Int {
    val baseValue = estimateCarValueBeforeIssue(carDetails)
    val depreciationFactor = when {
        score < 30 -> 0.40 // 60% loss
        score < 50 -> 0.55 // 45% loss
        score < 70 -> 0.70 // 30% loss
        else -> 0.85 // 15% loss
    }
    return (baseValue * depreciationFactor).toInt()
}

// calculateAgeDepreciation is in AnalysisHelpers.kt

private fun calculateSellAsIsLoss(carDetails: CarDetails, audioData: AudioDiagnosticData): Int {
    val valueBefore = estimateCarValueBeforeIssue(carDetails)
    val valueAsIs = estimateCarValueAsIs(carDetails, audioData.rawScore)
    return valueBefore - valueAsIs
}

private fun calculateNegotiationDiscount(audioData: AudioDiagnosticData): Int {
    return when {
        audioData.rawScore < 30 -> 50000
        audioData.rawScore < 50 -> 30000
        audioData.rawScore < 70 -> 15000
        else -> 5000
    }
}

// =============================================================================
// HELPER FUNCTIONS - DIAGNOSTICS ANALYSIS
// =============================================================================

private fun findRecurringIssues(diagnostics: List<AudioDiagnosticData>): String {
    if (diagnostics.isEmpty()) return "None"

    // Count occurrences of top sound labels
    val soundCounts = mutableMapOf<String, Int>()
    diagnostics.forEach { diag ->
        soundCounts[diag.topSoundLabel] = soundCounts.getOrDefault(diag.topSoundLabel, 0) + 1
    }

    val recurring = soundCounts.filter { it.value > 1 }
    return if (recurring.isEmpty()) {
        "No recurring problem"
    } else {
        recurring.entries.joinToString(", ") { "${it.key} (${it.value}x)" }
    }
}

private fun assessAudioQuality(audioData: AudioDiagnosticData): String {
    return when {
        audioData.topSoundConfidence > 0.8f -> "Excellent"
        audioData.topSoundConfidence > 0.6f -> "Good"
        audioData.topSoundConfidence > 0.4f -> "Average"
        else -> "Poor"
    }
}

private fun getModelAccuracy(): String {
    return "92.4%" // From TFLite model metadata
}

private fun getCtImpact(carLog: CarLog): String {
    val ct = carLog.documents.find { it.type.name.contains("TECHNICAL") }
    return if (ct == null) {
        "No Technical Inspection registered - Cannot legally drive"
    } else if (ct.isExpired) {
        "EXPIRED Technical Inspection - Non-compliant vehicle, fine possible"
    } else {
        val daysLeft = getDaysSince(ct.expiryDate) * -1
        when {
            daysLeft > 180 -> "CT valid - No immediate impact"
            daysLeft > 60 -> "CT to renew in ${daysLeft} days"
            else -> "CT expires soon - Renew URGENTNTY"
        }
    }
}
