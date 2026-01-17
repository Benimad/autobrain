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
Tu es le système d'IA AutoBrain - Expert analyse vidéo automobile avec ML Kit Google + contexte Firestore complet.

╔══════════════════════════════════════════════════════════════════════╗
║        📹 ANALYSE VIDÉO ML KIT (${videoData.totalFramesAnalyzed} frames)           ║
╚══════════════════════════════════════════════════════════════════════╝

$smokeAnalysis

$vibrationAnalysis

📊 MÉTRIQUES QUALITÉ VIDÉO:
  - Luminosité moyenne: ${videoData.averageBrightness}/255
  - Qualité vidéo: ${videoData.videoQuality.uppercase()}
  - Stabilité caméra: ${if (videoData.isStableVideo) "✅ STABLE" else "⚠️ INSTABLE"}
  - Durée enregistrement: ${videoData.durationMs}ms
  - Hash intégrité: ${videoData.videoHash.take(12)}...

╔══════════════════════════════════════════════════════════════════════╗
║      🔗 CORRÉLATION AUDIO + VIDÉO (Multi-Modal Analysis)             ║
╚══════════════════════════════════════════════════════════════════════╝

$audioCorrelation

╔══════════════════════════════════════════════════════════════════════╗
║           📚 CARNET & HISTORIQUE (Firebase Realtime)                 ║
╚══════════════════════════════════════════════════════════════════════╝

$maintenanceContext

╔══════════════════════════════════════════════════════════════════════╗
║         📈 HISTORIQUE DIAGNOSTICS VIDÉO (Tendances)                  ║
╚══════════════════════════════════════════════════════════════════════╝

$diagnosticTrend

╔══════════════════════════════════════════════════════════════════════╗
║              🎯 MISSION GEMINI - ANALYSE VISUELLE EXPERT             ║
╚══════════════════════════════════════════════════════════════════════╝

Fournis une analyse JSON ULTRA-COMPLÈTE avec ces sections:

**1. enhanced_visual_score** (0-100):
   - Score local ML Kit: ${videoData.rawScore}/100
   - Ajuster selon:
     * Fumée noire sévérité 4-5 → MAX score 35
     * Fumée blanche (joint culasse) → MAX score 45
     * Fumée bleue (segments) → MAX score 55
     * Vibrations excessives → -30 points
     * Carnet non tenu → -15 points
   - Intégrer les diagnostics audio pour score global cohérent

**2. smoke_deep_analysis**: {
  "type_detected": "${videoData.smokeType}",
  "technical_diagnosis": "Diagnostic technique précis basé sur couleur et densité",
  "chemical_composition_theory": "CO2 + particules de carbone (fumée noire) / Vapeur d'eau + liquide refroidissement (fumée blanche) / Hydrocarbures + huile (fumée bleue)",
  "emission_pattern": "Continu | À l'accélération | Au démarrage | À froid uniquement",
  "smell_prediction": "Odeur âcre | Odeur sucrée | Odeur d'huile brûlée",
  "color_intensity": "Légère | Moyenne | Épaisse | Opaque",
  "root_causes_by_probability": [
    {
      "cause": "Joint de culasse défaillant",
      "probability": 0.75,
      "confirming_tests": ["Test pression circuit refroidissement", "Analyse gaz échappement CO2 dans liquide refroidissement"],
      "repair_complexity": "ÉLEVÉE",
      "estimated_cost_dh": "8000-18000"
    }
  ],
  "worst_case_scenario": "Fissure bloc moteur → Remplacement moteur complet (35 000$)",
  "immediate_risks": [
    "Surchauffe moteur si perte liquide refroidissement",
    "Mélange huile-eau → grippage moteur",
    "Déformation culasse si conduite prolongée"
  ]
}

**3. vibration_engineering_analysis**: {
  "vibration_frequency_estimation": "2-4 Hz (basse fréquence) | 10-20 Hz (moyenne) | >20 Hz (haute)",
  "vibration_source_diagnosis": "Moteur | Transmission | Suspension | Roues",
  "phase_analysis": "Au ralenti | À l'accélération | Vitesse constante | Décélération",
  "probable_mechanical_causes": [
    {
      "component": "Support moteur avant droit",
      "failure_type": "Caoutchouc dégradé, jeu excessif",
      "diagnostic_test": "Inspection visuelle + test levier sous moteur",
      "replacement_cost_dh": "800-2500",
      "urgency": "MEDIUM"
    },
    {
      "component": "Équilibrage roues",
      "failure_type": "Poids tombé, jante voilée",
      "diagnostic_test": "Test équilibreuse électronique",
      "replacement_cost_dh": "200-600",
      "urgency": "LOW"
    }
  ],
  "cascading_failures_if_ignored": [
    "Usure prématurée silentblocs (3-6 mois)",
    "Fatigue supports moteur restants (6-12 mois)",
    "Fissuration châssis (rare, >24 mois)"
  ]
}

**4. combined_audio_video_diagnosis**: {
  "correlation_score": 0.85,
  "multimodal_insights": [
    "Fumée ${videoData.smokeType} + Son ${getTopAudioSound(audioDiagnostics)} = Diagnostic: ...",
    "Cohérence temporelle: Les deux symptômes sont apparus simultanément/progressivement"
  ],
  "comprehensive_root_cause": "Cause racine la plus probable en combinant audio + vidéo",
  "confidence_boost": "La corrélation audio-vidéo augmente la confiance de diagnostic de +15%"
}

**5. repair_scenarios_visual**: [
  {
    "scenario_name": "Réparation Minimale (Optimiste)",
    "applicable_if": "Fumée légère, pas de bruit moteur grave",
    "steps": [
      "Nettoyage injecteurs (1500$)",
      "Remplacement filtre à air (200$)",
      "Additif nettoyant FAP (500$)"
    ],
    "total_cost_dh": 2200,
    "success_probability": 0.25,
    "duration_hours": 4
  },
  {
    "scenario_name": "Réparation Standard (Probable)",
    "applicable_if": "Fumée moyenne, vibrations",
    "steps": [
      "Dépose culasse (2000$ main-d'œuvre)",
      "Remplacement joint culasse (800$ pièce)",
      "Rectification plan culasse (1500$)",
      "Vidange circuit refroidissement (300$)",
      "Remontage + réglages (1500$)"
    ],
    "total_cost_dh": 6100,
    "success_probability": 0.60,
    "duration_hours": 16
  },
  {
    "scenario_name": "Reconstruction Majeure (Pessimiste)",
    "applicable_if": "Fumée épaisse persistante + bruit métallique",
    "steps": [
      "Dépose moteur complète (3500$)",
      "Remplacement bloc moteur ou rectification (12000$)",
      "Remplacement pistons + segments (3000$)",
      "Reconstruction culasse (4000$)",
      "Remontage complet (5000$)"
    ],
    "total_cost_dh": 27500,
    "success_probability": 0.15,
    "duration_days": 10
  }
]

**6. video_quality_assessment**: {
  "recording_quality_score": ${(videoData.averageBrightness / 255 * 100).toInt()}/100,
  "technical_issues": ${buildQualityIssuesList(videoData.qualityIssues)},
  "recommendation_for_rerecording": ${shouldRerecord(videoData)},
  "optimal_recording_conditions": [
    "Enregistrer en plein jour (10h-16h) ou avec éclairage fort",
    "Stabiliser téléphone sur support fixe",
    "Moteur à température normale (après 10 min de conduite)",
    "Ralenti stable + légères accélérations",
    "Durée: 30-45 secondes minimum"
  ]
}

**7. safety_assessment**: {
  "roadworthiness": "${if (videoData.finalScore < 40) "UNSAFE" else if (videoData.finalScore < 60) "CAUTION" else "SAFE"}",
  "driving_restrictions": [
    ${if (videoData.vibrationDetected) "\"Éviter autoroute (>120 km/h)\"," else ""}
    ${if (videoData.smokeDetected) "\"Limiter trajets à <50 km\"," else ""}
    ${if (videoData.smokeSeverity >= 4) "\"Vérifier liquide refroidissement tous les 20 km\"" else ""}
  ],
  "breakdown_probability_next_30_days": ${calculateBreakdownProbability(videoData)},
  "towing_recommendation": ${videoData.urgencyLevel == "CRITICAL"},
  "insurance_claim_viability": "Faible - Usure mécanique rarement couverte"
}

**8. market_impact_visual**: {
  "buyer_perception": "${if (videoData.smokeDetected) "Un acheteur verra cette fumée → Fuite immédiate" else "Apparence normale"}",
  "negotiation_leverage_seller": "${if (videoData.finalScore < 50) "TRÈS FAIBLE" else if (videoData.finalScore < 70) "FAIBLE" else "NORMAL"}",
  "price_reduction_expected_dh": ${calculatePriceReduction(videoData)},
  "time_to_sell_estimate_days": ${estimateTimeToSell(videoData.finalScore)},
  "disclosure_requirement": "LÉGALEMENT OBLIGÉ de mentionner fumée/vibration (Lois locales)"
}

**9. environmental_compliance**: {
  "emission_test_pass_probability": ${if (videoData.smokeDetected) "0.15" else "0.85"},
  "pollution_level": "${if (videoData.smokeSeverity >= 4) "Critique" else if (videoData.smokeSeverity >= 2) "Élevé" else "Normal"}",
  "controle_technique_impact": "${if (videoData.smokeDetected) "Refus CT probable si fumée visible" else "Passage CT possible"}",
  "vignette_pollution_eligibility": "${assessVignetteEligibility(videoData)}"
}

**10. autobrain_video_confidence**: {
  "ml_kit_accuracy": "87% sur dataset d'entraînement"
  "confidence_this_analysis": ${videoData.smokeConfidence.coerceAtLeast(videoData.vibrationConfidence)},
  "factors_affecting_confidence": [
    "Qualité vidéo: ${videoData.videoQuality}",
    "Nombre de frames: ${videoData.totalFramesAnalyzed}",
    "Stabilité: ${videoData.isStableVideo}"
  ],
  "gemini_model": "gemini-2.5-pro",
  "analysis_timestamp": ${System.currentTimeMillis()}
}

╔══════════════════════════════════════════════════════════════════════╗
║                   ⚡ JSON OUTPUT OBLIGATOIRE                         ║
╚══════════════════════════════════════════════════════════════════════╝

Retourne UN SEUL objet JSON valide (pas de markdown, texte avant/après interdit).
Parser direct Kotlin Gson → Stockage Firestore collection "comprehensive_video_diagnostics"

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
🔊 Dernier Diagnostic Audio: ${getDaysSince(recentAudio.createdAt)} jours
  - Son dominant: ${recentAudio.topSoundLabel} (${(recentAudio.topSoundConfidence * 100).toInt()}%)
  - Score audio: ${recentAudio.rawScore}/100
  
🔗 Corrélation Audio-Vidéo:
  ${when {
    recentAudio.topSoundLabel.contains("knocking") && videoData.smokeDetected -> 
        "⚠️ FORTE CORRÉLATION: Cognement moteur + fumée = Dommage interne moteur probable"
    recentAudio.topSoundLabel.contains("belt") && videoData.vibrationDetected -> 
        "🔧 Corrélation Moyenne: Courroie + vibration = Usure accessoires"
    else -> 
        "✅ Symptômes indépendants ou faible corrélation"
  }}
        """.trimIndent()
    } else {
        "Aucun diagnostic audio récent pour corrélation"
    }
}

private fun getTopAudioSound(audioDiags: List<AudioDiagnosticData>): String {
    return audioDiags.maxByOrNull { it.createdAt }?.topSoundLabel ?: "Aucun"
}

// =============================================================================
// HELPER FUNCTIONS - VIDEO TRENDS
// =============================================================================

private fun analyzeVideoDiagnosticTrend(diagnostics: List<VideoDiagnosticData>): String {
    if (diagnostics.isEmpty()) {
        return "Premier diagnostic vidéo - Pas d'historique"
    }
    
    val sortedDiags = diagnostics.sortedBy { it.createdAt }
    val scoreEvolution = sortedDiags.map { "${it.finalScore}/100" }.joinToString(" → ")
    
    val degradationRate = if (sortedDiags.size >= 2) {
        val first = sortedDiags.first().finalScore
        val last = sortedDiags.last().finalScore
        ((first - last).toFloat() / sortedDiags.size).toInt()
    } else 0
    
    return """
🔄 Évolution Scores Vidéo: $scoreEvolution
📉 Taux Dégradation: ${degradationRate} points/diagnostic
⏱️ Premier diagnostic: ${formatDate(sortedDiags.first().createdAt)}
🔔 Problèmes récurrents: ${findRecurringVideoIssues(diagnostics)}
⚠️ Tendance: ${when {
    degradationRate > 5 -> "AGGRAVATION RAPIDE"
    degradationRate > 0 -> "Dégradation progressive"
    else -> "Stable"
}}
    """.trimIndent()
}

private fun findRecurringVideoIssues(diagnostics: List<VideoDiagnosticData>): String {
    if (diagnostics.isEmpty()) return "Aucun"
    
    val smokeCounts = diagnostics.count { it.smokeDetected }
    val vibrationCounts = diagnostics.count { it.vibrationDetected }
    
    val issues = mutableListOf<String>()
    if (smokeCounts > 1) issues.add("Fumée (${smokeCounts}x)")
    if (vibrationCounts > 1) issues.add("Vibration (${vibrationCounts}x)")
    
    return if (issues.isEmpty()) "Aucun problème récurrent" else issues.joinToString(", ")
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
🛢️ Dernière Vidange: ${lastOilChange?.let { 
    "Il y a ${getDaysSince(it.date)} jours (${it.mileage} km)" 
} ?: "AUCUNE ENREGISTRÉE ⚠️"}

🔍 Dernier CT: ${lastCT?.let {
    if (it.isExpired) "EXPIRÉ depuis ${getDaysSince(it.expiryDate)} jours ❌"
    else "Valide jusqu'au ${formatDate(it.expiryDate)} ✅"
} ?: "NON RENSEIGNÉ"}

📋 Rappels en Retard (${overdueReminders.size}):
${if (overdueReminders.isEmpty()) "   Aucun" else overdueReminders.joinToString("\n") { 
    "   - ${it.title}: ${getDaysSince(it.dueDate)} jours de retard"
}}

📊 Qualité Entretien Global: ${assessMaintenanceQuality(carLog)}
💸 Coût Total Entretien (12 mois): $${calculateTotalMaintenanceCost(carLog)}
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
