package com.example.autobrain.data.ai

import com.example.autobrain.data.local.entity.AudioDiagnosticData
import com.example.autobrain.data.local.entity.VideoDiagnosticData
import com.example.autobrain.domain.model.CarDetails
import com.example.autobrain.domain.model.CarLog
import com.example.autobrain.domain.model.User
import java.text.SimpleDateFormat
import java.util.*

/**
 * AUTOBRAIN - COMPREHENSIVE VIDEO DIAGNOSTIC
 * ML Kit Video Analysis + Complete Firebase Integration
 * 
 * Features:
 * - Smoke detection analysis (black/white/blue)
 * - Vibration analysis from accelerometer
 * - Audio-Video correlation
 * - Maintenance history integration
 * - Market pricing
 */

/**
 * Build comprehensive video analysis prompt with ML Kit results + Firestore context
 */
fun buildComprehensiveVideoAnalysisPrompt(
    videoData: VideoDiagnosticData,
    carLog: CarLog,
    user: User,
    previousVideoDiagnostics: List<VideoDiagnosticData>,
    audioDiagnostics: List<AudioDiagnosticData>
): String {
    
    val carDetails = user.carDetails ?: CarDetails()
    
    // ML Kit detection details
    val smokeAnalysis = if (videoData.smokeDetected) {
        """
🔴 FUMÉE DÉTECTÉE (ML Kit):
  - Type: ${videoData.smokeType.uppercase()}
  - Confiance: ${(videoData.smokeConfidence * 100).toInt()}%
  - Sévérité: ${videoData.smokeSeverity}/5
  - Frames affectées: ${videoData.smokeyFramesCount}/${videoData.totalFramesAnalyzed}
  - Persistance: ${(videoData.smokeyFramesCount.toFloat() / videoData.totalFramesAnalyzed * 100).toInt()}%
        """.trimIndent()
    } else {
        "✅ AUCUNE FUMÉE DÉTECTÉE"
    }
    
    val vibrationAnalysis = if (videoData.vibrationDetected) {
        """
⚡ VIBRATIONS DÉTECTÉES (Accéléromètre):
  - Niveau: ${videoData.vibrationLevel.uppercase()}
  - Confiance: ${(videoData.vibrationConfidence * 100).toInt()}%
  - Sévérité: ${videoData.vibrationSeverity}/5
  - Frames affectées: ${videoData.vibrationFramesCount}/${videoData.totalFramesAnalyzed}
        """.trimIndent()
    } else {
        "✅ VIBRATIONS NORMALES"
    }
    
    // Cross-diagnostic correlation
    val audioCorrelation = correlateAudioWithVideo(audioDiagnostics, videoData)
    
    // Maintenance context
    val maintenanceContext = buildMaintenanceContext(carLog)
    
    // Diagnostic trend
    val diagnosticTrend = analyzeVideoDiagnosticTrend(previousVideoDiagnostics)
    
    return """
You are the AutoBrain AI system - Automotive video analysis expert with Google ML Kit + complete Firestore context.

╔══════════════════════════════════════════════════════════════════════╗
║        📹 ANALYSE VIDÉO ML KIT (${videoData.totalFramesAnalyzed} frames)           ║
╚══════════════════════════════════════════════════════════════════════╝

$smokeAnalysis

$vibrationAnalysis

📊 VIDEO QUALITY METRICS:
  - Average brightness: ${videoData.averageBrightness}/255
  - Video quality: ${videoData.videoQuality.uppercase()}
  - Camera stability: ${if (videoData.isStableVideo) "✅ STABLE" else "⚠️ UNSTABLE"}
  - Recording duration: ${videoData.durationMs}ms
  - Integrity hash: ${videoData.videoHash.take(12)}...

╔══════════════════════════════════════════════════════════════════════╗
║      🔗 AUDIO + VIDEO CORRELATION (Multi-Modal Analysis)             ║
╚══════════════════════════════════════════════════════════════════════╝

$audioCorrelation

╔══════════════════════════════════════════════════════════════════════╗
║           📚 LOGBOOK & HISTORY (Firebase Realtime)                 ║
╚══════════════════════════════════════════════════════════════════════╝

$maintenanceContext

╔══════════════════════════════════════════════════════════════════════╗
║         📈 VIDEO DIAGNOSTICS HISTORY (Trends)                  ║
╚══════════════════════════════════════════════════════════════════════╝

$diagnosticTrend

╔══════════════════════════════════════════════════════════════════════╗
║              🎯 GEMINI MISSION - EXPERT VISUAL ANALYSIS             ║
╚══════════════════════════════════════════════════════════════════════╝

Provide a ULTRA-COMPLETE JSON analysis with these sections:

**1. enhanced_visual_score** (0-100):
   - Local ML Kit score: ${videoData.rawScore}/100
   - Adjust based on:
     * Black smoke severity 4-5 → MAX score 35
     * White smoke (head gasket) → MAX score 45
     * Blue smoke (rings) → MAX score 55
     * Excessive vibration → -30 points
     * Unmaintained logbook → -15 points
   - Integrate audio diagnostics for coherent global score

**2. smoke_deep_analysis**: {
  "type_detected": "${videoData.smokeType}",
  "technical_diagnosis": "Precise technical diagnosis based on color and density",
  "chemical_composition_theory": "CO2 + carbon particles (black smoke) / Water vapor + coolant (white smoke) / Hydrocarbons + oil (blue smoke)",
  "emission_pattern": "Continuous | On acceleration | At startup | Cold only",
  "smell_prediction": "Acrid smell | Sweet smell | Burning oil smell",
  "color_intensity": "Light | Medium | Thick | Opaque",
  "root_causes_by_probability": [
    {
      "cause": "Failing head gasket",
      "probability": 0.75,
      "confirming_tests": ["Coolant circuit pressure test", "Exhaust gas CO2 analysis in coolant"],
      "repair_complexity": "HIGH",
      "estimated_cost_usd": "8000-18000"
    }
  ],
  "worst_case_scenario": "Engine block crack → Complete engine replacement (35000$)",
  "immediate_risks": [
    "Engine overheating if coolant loss",
    "Oil-water mixture → engine seizure",
    "Cylinder head deformation if driving prolonged"
  ]
}

**3. vibration_engineering_analysis**: {
  "vibration_frequency_estimation": "2-4 Hz (low frequency) | 10-20 Hz (medium) | >20 Hz (high)",
  "vibration_source_diagnosis": "Engine | Transmission | Suspension | Wheels",
  "phase_analysis": "At idle | On acceleration | Constant speed | Deceleration",
  "probable_mechanical_causes": [
    {
      "component": "Front right engine mount",
      "failure_type": "Degraded rubber, excessive play",
      "diagnostic_test": "Visual inspection + lever test under engine",
      "replacement_cost_usd": "800-2500",
      "urgency": "MEDIUM"
    },
    {
      "component": "Wheel balancing",
      "failure_type": "Weight fell, bent rim",
      "diagnostic_test": "Electronic balancer test",
      "replacement_cost_usd": "200-600",
      "urgency": "LOW"
    }
  ],
  "cascading_failures_if_ignored": [
    "Premature silentblock wear (3-6 months)",
    "Fatigue of remaining engine mounts (6-12 months)",
    "Chassis cracking (rare, >24 months)"
  ]
}

**4. combined_audio_video_diagnosis**: {
  "correlation_score": 0.85,
  "multimodal_insights": [
    "Smoke ${videoData.smokeType} + Sound ${getTopAudioSound(audioDiagnostics)} = Diagnostic: ...",
    "Temporal consistency: Both symptoms appeared simultaneously/progressively"
  ],
  "comprehensive_root_cause": "Most probable root cause combining audio + video",
  "confidence_boost": "Audio-video correlation increases diagnostic confidence by +15%"
}

**5. repair_scenarios_visual**: [
  {
    "scenario_name": "Minimal Repair (Optimistic)",
    "applicable_if": "Light smoke, no serious engine noise",
    "steps": [
      "Injector cleaning (1500$)",
      "Air filter replacement (200$)",
      "FAP cleaning additive (500$)"
    ],
    "total_cost_usd": 2200,
    "success_probability": 0.25,
    "duration_hours": 4
  },
  {
    "scenario_name": "Standard Repair (Likely)",
    "applicable_if": "Medium smoke, vibrations",
    "steps": [
      "Cylinder head removal (2000$ labor)",
      "Head gasket replacement (800$ part)",
      "Cylinder head resurfacing (1500$)",
      "Coolant drain (300$)",
      "Reassembly + adjustments (1500$)"
    ],
    "total_cost_usd": 6100,
    "success_probability": 0.60,
    "duration_hours": 16
  },
  {
    "scenario_name": "Major Rebuild (Pessimistic)",
    "applicable_if": "Thick persistent smoke + metallic noise",
    "steps": [
      "Complete engine removal (3500$)",
      "Engine block replacement or machining (12000$)",
      "Pistons + rings replacement (3000$)",
      "Cylinder head rebuild (4000$)",
      "Complete reassembly (5000$)"
    ],
    "total_cost_usd": 27500,
    "success_probability": 0.15,
    "duration_days": 10
  }
]

**6. video_quality_assessment**: {
  "recording_quality_score": ${(videoData.averageBrightness / 255 * 100).toInt()}/100,
  "technical_issues": ${buildQualityIssuesList(videoData.qualityIssues)},
  "recommendation_for_rerecording": ${shouldRerecord(videoData)},
  "optimal_recording_conditions": [
    "Record in daylight (10am-4pm) or with strong lighting",
    "Stabilize phone on fixed support",
    "Engine at normal temperature (after 10 min of driving)",
    "Stable idle + light accelerations",
    "Duration: 30-45 seconds minimum"
  ]
}

**7. safety_assessment**: {
  "roadworthiness": "${if (videoData.finalScore < 40) "UNSAFE" else if (videoData.finalScore < 60) "CAUTION" else "SAFE"}",
  "driving_restrictions": [
    ${if (videoData.vibrationDetected) "\"Avoid highway (>120 km/h)\"," else ""}
    ${if (videoData.smokeDetected) "\"Limit trips to <50 km\"," else ""}
    ${if (videoData.smokeSeverity >= 4) "\"Check coolant every 20 km\"" else ""}
  ],
  "breakdown_probability_next_30_days": ${calculateBreakdownProbability(videoData)},
  "towing_recommendation": ${videoData.urgencyLevel == "CRITICAL"},
  "insurance_claim_viability": "Low - Mechanical wear rarely covered"
}

**8. market_impact_visual**: {
  "buyer_perception": "${if (videoData.smokeDetected) "A buyer will see this smoke → Immediate exit" else "Normal appearance"}",
  "negotiation_leverage_seller": "${if (videoData.finalScore < 50) "VERY LOW" else if (videoData.finalScore < 70) "LOW" else "NORMAL"}",
  "price_reduction_expected_usd": ${calculatePriceReduction(videoData)},
  "time_to_sell_estimate_days": ${estimateTimeToSell(videoData.finalScore)},
  "disclosure_requirement": "LEGALLY OBLIGED to mention smoke/vibration (Local laws)"
}

**9. environmental_compliance**: {
  "emission_test_pass_probability": ${if (videoData.smokeDetected) "0.15" else "0.85"},
  "pollution_level": "${if (videoData.smokeSeverity >= 4) "Critical" else if (videoData.smokeSeverity >= 2) "High" else "Normal"}",
  "controle_technique_impact": "${if (videoData.smokeDetected) "CT refusal probable if visible smoke" else "CT passage possible"}",
  "vignette_pollution_eligibility": "${assessVignetteEligibility(videoData)}"
}

**10. autobrain_video_confidence**: {
  "ml_kit_accuracy": "87% on training dataset"
  "confidence_this_analysis": ${videoData.smokeConfidence.coerceAtLeast(videoData.vibrationConfidence)},
  "factors_affecting_confidence": [
    "Video quality: ${videoData.videoQuality}",
    "Number of frames: ${videoData.totalFramesAnalyzed}",
    "Stability: ${videoData.isStableVideo}"
  ],
  "gemini_model": "gemini-3-pro-preview",
  "analysis_timestamp": ${System.currentTimeMillis()}
}

╔══════════════════════════════════════════════════════════════════════╗
║                   ⚡ MANDATORY JSON OUTPUT                       ║
╚══════════════════════════════════════════════════════════════════════╝

Return ONE valid JSON object (no markdown, text before/after prohibited).
Direct Kotlin Gson parser → Firestore collection "comprehensive_video_diagnostics"

{
  "enhanced_visual_score": 52,
  "smoke_deep_analysis": { ... },
  "vibration_engineering_analysis": { ... },
  "combined_audio_video_diagnosis": { ... },
  "repair_scenarios_visual": [ ... ],
  "video_quality_assessment": { ... },
  "safety_assessment": { ... },
  "market_impact_visual": { ... },
  "environmental_compliance": { ... },
  "autobrain_video_confidence": { ... }
}
    """.trimIndent()
}

// =============================================================================
// HELPER FUNCTIONS - AUDIO-VIDEO CORRELATION
// =============================================================================

private fun correlateAudioWithVideo(
    audioDiags: List<AudioDiagnosticData>,
    videoData: VideoDiagnosticData
): String {
    val recentAudio = audioDiags.maxByOrNull { it.createdAt }
    
    return if (recentAudio != null) {
        """
🔊 Last Audio Diagnostic: ${getDaysSince(recentAudio.createdAt)} days ago
  - Dominant sound: ${recentAudio.topSoundLabel} (${(recentAudio.topSoundConfidence * 100).toInt()}%)
  - Audio score: ${recentAudio.rawScore}/100
  
🔗 Audio-Video Correlation:
  ${when {
    recentAudio.topSoundLabel.contains("knocking") && videoData.smokeDetected ->
        "⚠️ STRONG CORRELATION: Engine knocking + smoke = Probable internal engine damage"
    recentAudio.topSoundLabel.contains("belt") && videoData.vibrationDetected ->
        "🔧 Average Correlation: Belt + vibration = Accessory wear"
    else ->
        "✅ Independent symptoms or weak correlation"
  }}
        """.trimIndent()
    } else {
        "No recent audio diagnostic for correlation"
    }
}

private fun getTopAudioSound(audioDiags: List<AudioDiagnosticData>): String {
    return audioDiags.maxByOrNull { it.createdAt }?.topSoundLabel ?: "None"
}

// =============================================================================
// HELPER FUNCTIONS - VIDEO TRENDS
// =============================================================================

private fun analyzeVideoDiagnosticTrend(diagnostics: List<VideoDiagnosticData>): String {
    if (diagnostics.isEmpty()) {
        return "First video diagnostic - No history"
    }

    val sortedDiags = diagnostics.sortedBy { it.createdAt }
    val scoreEvolution = sortedDiags.map { "${it.finalScore}/100" }.joinToString(" → ")

    val degradationRate = if (sortedDiags.size >= 2) {
        val first = sortedDiags.first().finalScore
        val last = sortedDiags.last().finalScore
        ((first - last).toFloat() / sortedDiags.size).toInt()
    } else 0

    return """
🔄 Video Score Evolution: $scoreEvolution
📉 Degradation Rate: ${degradationRate} points/diagnostic
⏱️ First diagnostic: ${formatDate(sortedDiags.first().createdAt)}
🔔 Recurring Problems: ${findRecurringVideoIssues(diagnostics)}
⚠️ Trend: ${when {
    degradationRate > 5 -> "RAPID AGGRAVATION"
    degradationRate > 0 -> "Progressive degradation"
    else -> "Stable"
}}
    """.trimIndent()
}

private fun findRecurringVideoIssues(diagnostics: List<VideoDiagnosticData>): String {
    if (diagnostics.isEmpty()) return "None"

    val smokeCounts = diagnostics.count { it.smokeDetected }
    val vibrationCounts = diagnostics.count { it.vibrationDetected }

    val issues = mutableListOf<String>()
    if (smokeCounts > 1) issues.add("Smoke (${smokeCounts}x)")
    if (vibrationCounts > 1) issues.add("Vibration (${vibrationCounts}x)")

    return if (issues.isEmpty()) "No recurring problem" else issues.joinToString(", ")
}

// =============================================================================
// HELPER FUNCTIONS - QUALITY & ASSESSMENT
// =============================================================================

private fun buildQualityIssuesList(issues: List<String>): String {
    return if (issues.isEmpty()) {
        "[]"
    } else {
        "[\"" + issues.joinToString("\", \"") + "\"]"
    }
}

private fun shouldRerecord(videoData: VideoDiagnosticData): Boolean {
    return videoData.videoQuality == "poor" || 
           videoData.averageBrightness < 50 || 
           !videoData.isStableVideo
}

private fun calculateBreakdownProbability(videoData: VideoDiagnosticData): Float {
    var probability = 0.1f // Base 10%
    
    if (videoData.smokeDetected) {
        probability += videoData.smokeSeverity * 0.1f
    }
    
    if (videoData.vibrationDetected) {
        probability += videoData.vibrationSeverity * 0.08f
    }
    
    return probability.coerceIn(0f, 0.95f)
}

private fun calculatePriceReduction(videoData: VideoDiagnosticData): Int {
    var reduction = 0
    
    if (videoData.smokeDetected) {
        reduction += when (videoData.smokeSeverity) {
            5 -> 40000
            4 -> 25000
            3 -> 15000
            2 -> 8000
            else -> 3000
        }
    }
    
    if (videoData.vibrationDetected) {
        reduction += when (videoData.vibrationSeverity) {
            5 -> 15000
            4 -> 10000
            3 -> 5000
            else -> 2000
        }
    }
    
    return reduction
}

private fun estimateTimeToSell(score: Int): Int {
    return when {
        score >= 80 -> 30 // 1 month
        score >= 60 -> 60 // 2 months
        score >= 40 -> 120 // 4 months
        else -> 365 // Very hard to sell
    }
}

private fun assessVignetteEligibility(videoData: VideoDiagnosticData): String {
    return if (videoData.smokeDetected && videoData.smokeSeverity >= 3) {
        "NON ÉLIGIBLE (pollution visible)"
    } else {
        "ÉLIGIBLE sous conditions"
    }
}

// =============================================================================
// HELPER FUNCTIONS - DATE & TIME (Reuse from audio)
// =============================================================================

// getDaysSince and formatDate are in AnalysisHelpers.kt

// =============================================================================
// HELPER FUNCTIONS - MAINTENANCE CONTEXT (From Audio)
// =============================================================================

private fun buildMaintenanceContext(carLog: CarLog): String {
    val lastOilChange = carLog.maintenanceRecords
        .filter { it.type.name.contains("OIL") }
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
