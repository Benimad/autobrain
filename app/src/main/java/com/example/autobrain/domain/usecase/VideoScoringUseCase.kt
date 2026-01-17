package com.example.autobrain.domain.usecase

import com.example.autobrain.data.ai.VideoAnalysisResults
import com.example.autobrain.data.local.entity.MaintenanceRecordEntity
import com.example.autobrain.data.local.entity.VideoIssue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Video Scoring Use Case - Smart Scoring Without LLM
 * 
 * Scoring Formula:
 * - Base Score: 100
 * - Smoke Black: -50 (Critical engine issue)
 * - Smoke White: -40 (Coolant/oil issue)
 * - Smoke Blue: -35 (Oil burning)
 * - Vibration Excessive: -30
 * - Vibration High: -20
 * - Vibration Medium: -15
 * - Carnet Integration: -10 if oil change overdue, -15 if CT expired
 * - Bonus: +10 if no detection in >90% frames
 * 
 * Final Score: Coerced to 0-100
 */
@Singleton
class VideoScoringUseCase @Inject constructor() {
    
    companion object {
        // Deduction weights
        private const val SMOKE_BLACK_DEDUCTION = 50
        private const val SMOKE_WHITE_DEDUCTION = 40
        private const val SMOKE_BLUE_DEDUCTION = 35
        private const val VIBRATION_EXCESSIVE_DEDUCTION = 30
        private const val VIBRATION_HIGH_DEDUCTION = 20
        private const val VIBRATION_MEDIUM_DEDUCTION = 15
        private const val VIBRATION_LOW_DEDUCTION = 10
        
        // Carnet penalties
        private const val OIL_CHANGE_OVERDUE_PENALTY = 10
        private const val CT_EXPIRED_PENALTY = 15
        private const val SEVERE_OVERDUE_PENALTY = 20 // >15k km overdue
        
        // Bonus
        private const val CLEAN_VIDEO_BONUS = 10
        
        // Cost estimates (in Dirhams)
        private val SMOKE_COSTS = mapOf(
            "black" to Pair(15000.0, 25000.0), // Major engine repair
            "white" to Pair(8000.0, 18000.0), // Head gasket/coolant system
            "blue" to Pair(5000.0, 15000.0) // Piston rings/valve seals
        )
        
        private val VIBRATION_COSTS = mapOf(
            "excessive" to Pair(3000.0, 10000.0), // Engine mounts + suspension
            "high" to Pair(2000.0, 7000.0), // Engine mounts
            "medium" to Pair(1000.0, 4000.0), // Minor adjustments
            "low" to Pair(500.0, 2000.0) // Inspection + minor fixes
        )
    }
    
    /**
     * Calculate comprehensive video diagnostic score
     */
    fun calculateScore(
        analysisResults: VideoAnalysisResults,
        maintenanceRecords: List<MaintenanceRecordEntity> = emptyList(),
        currentMileage: Int? = null
    ): VideoScoreResult {
        
        var score = 100
        val deductions = mutableListOf<ScoreDeduction>()
        val detectedIssues = mutableListOf<VideoIssue>()
        val recommendations = mutableListOf<String>()
        
        // =============================================================================
        // 1. SMOKE DEDUCTIONS
        // =============================================================================
        
        if (analysisResults.smokeDetected) {
            val smokeDeduction = when (analysisResults.smokeType) {
                "black" -> {
                    deductions.add(ScoreDeduction("Fumée noire détectée", SMOKE_BLACK_DEDUCTION))
                    detectedIssues.add(VideoIssue(
                        issueType = "smoke_black",
                        severity = "CRITICAL",
                        confidence = analysisResults.smokeConfidence,
                        description = "Fumée noire – Problème moteur grave. Combustion incomplète ou excès de carburant. Risque de dommages sévères au moteur.",
                        estimatedMinCost = SMOKE_COSTS["black"]!!.first,
                        estimatedMaxCost = SMOKE_COSTS["black"]!!.second
                    ))
                    recommendations.add("⚠️ URGENT: Arrêter le véhicule immédiatement et consulter un mécanicien expert.")
                    recommendations.add("Ne pas conduire le véhicule – risque de dommages irréversibles au moteur (>20 000$).")
                    recommendations.add("Vérifier: injecteurs, filtre à air, turbo (si applicable).")
                    SMOKE_BLACK_DEDUCTION
                }
                "white" -> {
                    deductions.add(ScoreDeduction("Fumée blanche détectée", SMOKE_WHITE_DEDUCTION))
                    detectedIssues.add(VideoIssue(
                        issueType = "smoke_white",
                        severity = "HIGH",
                        confidence = analysisResults.smokeConfidence,
                        description = "Fumée blanche – Fuite de liquide de refroidissement dans la chambre de combustion. Joint de culasse probable.",
                        estimatedMinCost = SMOKE_COSTS["white"]!!.first,
                        estimatedMaxCost = SMOKE_COSTS["white"]!!.second
                    ))
                    recommendations.add("🔧 Consulter un mécanicien rapidement – risque de surchauffe moteur.")
                    recommendations.add("Vérifier le niveau de liquide de refroidissement régulièrement.")
                    recommendations.add("Réparation nécessaire: joint de culasse ou bloc moteur (8 000-18 000$).")
                    SMOKE_WHITE_DEDUCTION
                }
                "blue" -> {
                    deductions.add(ScoreDeduction("Fumée bleue détectée", SMOKE_BLUE_DEDUCTION))
                    detectedIssues.add(VideoIssue(
                        issueType = "smoke_blue",
                        severity = "HIGH",
                        confidence = analysisResults.smokeConfidence,
                        description = "Fumée bleue – Huile brûlée dans la combustion. Segments de piston ou joints de soupape usés.",
                        estimatedMinCost = SMOKE_COSTS["blue"]!!.first,
                        estimatedMaxCost = SMOKE_COSTS["blue"]!!.second
                    ))
                    recommendations.add("🛠️ Réparation recommandée sous 1 mois.")
                    recommendations.add("Vérifier le niveau d'huile fréquemment – risque de consommation excessive.")
                    recommendations.add("Coût réparation: segments de piston ou joints (5 000-15 000$).")
                    SMOKE_BLUE_DEDUCTION
                }
                else -> 0
            }
            score -= smokeDeduction
        }
        
        // =============================================================================
        // 2. VIBRATION DEDUCTIONS
        // =============================================================================
        
        if (analysisResults.vibrationDetected) {
            val vibrationDeduction = when (analysisResults.vibrationLevel) {
                "excessive" -> {
                    deductions.add(ScoreDeduction("Vibrations excessives", VIBRATION_EXCESSIVE_DEDUCTION))
                    detectedIssues.add(VideoIssue(
                        issueType = "vibration_excessive",
                        severity = "CRITICAL",
                        confidence = analysisResults.vibrationConfidence,
                        description = "Vibrations excessives – Supports moteur défaillants ou déséquilibrage sévère.",
                        estimatedMinCost = VIBRATION_COSTS["excessive"]!!.first,
                        estimatedMaxCost = VIBRATION_COSTS["excessive"]!!.second
                    ))
                    recommendations.add("⚠️ Vibrations dangereuses – Inspecter supports moteur et suspension immédiatement.")
                    recommendations.add("Risque: usure prématurée de composants + inconfort de conduite.")
                    VIBRATION_EXCESSIVE_DEDUCTION
                }
                "high" -> {
                    deductions.add(ScoreDeduction("Vibrations élevées", VIBRATION_HIGH_DEDUCTION))
                    detectedIssues.add(VideoIssue(
                        issueType = "vibration_high",
                        severity = "HIGH",
                        confidence = analysisResults.vibrationConfidence,
                        description = "Vibrations élevées – Supports moteur usés ou problème d'équilibrage.",
                        estimatedMinCost = VIBRATION_COSTS["high"]!!.first,
                        estimatedMaxCost = VIBRATION_COSTS["high"]!!.second
                    ))
                    recommendations.add("🔧 Remplacer les supports moteur usés (2 000-7 000$).")
                    VIBRATION_HIGH_DEDUCTION
                }
                "medium" -> {
                    deductions.add(ScoreDeduction("Vibrations moyennes", VIBRATION_MEDIUM_DEDUCTION))
                    detectedIssues.add(VideoIssue(
                        issueType = "vibration_medium",
                        severity = "MEDIUM",
                        confidence = analysisResults.vibrationConfidence,
                        description = "Vibrations moyennes – Usure normale ou équilibrage des roues nécessaire.",
                        estimatedMinCost = VIBRATION_COSTS["medium"]!!.first,
                        estimatedMaxCost = VIBRATION_COSTS["medium"]!!.second
                    ))
                    recommendations.add("✅ Vérifier équilibrage des roues et parallélisme.")
                    VIBRATION_MEDIUM_DEDUCTION
                }
                "low" -> {
                    deductions.add(ScoreDeduction("Vibrations légères", VIBRATION_LOW_DEDUCTION))
                    recommendations.add("ℹ️ Vibrations légères détectées – Inspection recommandée lors du prochain entretien.")
                    VIBRATION_LOW_DEDUCTION
                }
                else -> 0
            }
            score -= vibrationDeduction
        }
        
        // =============================================================================
        // 3. CARNET INTEGRATION (Maintenance Impact)
        // =============================================================================
        
        val carnetImpact = calculateCarnetImpact(maintenanceRecords, currentMileage)
        score -= carnetImpact.totalPenalty
        deductions.addAll(carnetImpact.deductions)
        recommendations.addAll(carnetImpact.recommendations)
        
        // =============================================================================
        // 4. BONUS FOR CLEAN VIDEO
        // =============================================================================
        
        if (!analysisResults.smokeDetected && !analysisResults.vibrationDetected) {
            val cleanFramePercentage = if (analysisResults.totalFrames > 0) {
                (analysisResults.totalFrames - analysisResults.smokeyFramesCount - analysisResults.vibrationFramesCount).toFloat() / analysisResults.totalFrames
            } else 0f
            
            if (cleanFramePercentage > 0.9f) {
                score += CLEAN_VIDEO_BONUS
                deductions.add(ScoreDeduction("Bonus: Aucun problème détecté", -CLEAN_VIDEO_BONUS))
                recommendations.add("✅ Excellent état – Aucun problème visuel détecté dans la vidéo.")
            }
        }
        
        // =============================================================================
        // 5. VIDEO QUALITY WARNINGS
        // =============================================================================
        
        if (analysisResults.videoQuality == "poor") {
            recommendations.add(0, "⚠️ Qualité vidéo faible – Reprendre avec meilleure luminosité et stabilité pour un diagnostic précis.")
        } else if (analysisResults.videoQuality == "acceptable") {
            recommendations.add(0, "ℹ️ Qualité vidéo acceptable – Pour un diagnostic optimal, améliorer l'éclairage.")
        }
        
        // =============================================================================
        // 6. FINAL SCORE & STATUS
        // =============================================================================
        
        val finalScore = score.coerceIn(0, 100)
        val healthStatus = determineHealthStatus(finalScore)
        val urgencyLevel = determineUrgencyLevel(detectedIssues, finalScore)
        
        return VideoScoreResult(
            rawScore = score,
            finalScore = finalScore,
            healthStatus = healthStatus,
            urgencyLevel = urgencyLevel,
            detectedIssues = detectedIssues,
            recommendations = recommendations.distinct(),
            deductions = deductions,
            carnetImpactScore = carnetImpact.totalPenalty,
            overdueMaintenanceItems = carnetImpact.overdueItems,
            criticalWarning = if (urgencyLevel == UrgencyLevel.CRITICAL) {
                "ATTENTION CRITIQUE: Problème grave détecté – Ne pas conduire sans inspection professionnelle."
            } else null
        )
    }
    
    /**
     * Calculate carnet (maintenance log) impact on score
     */
    private fun calculateCarnetImpact(
        maintenanceRecords: List<MaintenanceRecordEntity>,
        currentMileage: Int?
    ): CarnetImpactResult {
        var totalPenalty = 0
        val deductions = mutableListOf<ScoreDeduction>()
        val recommendations = mutableListOf<String>()
        val overdueItems = mutableListOf<String>()
        
        if (currentMileage == null || maintenanceRecords.isEmpty()) {
            return CarnetImpactResult(0, deductions, recommendations, overdueItems)
        }
        
        // Find last oil change
        val lastOilChange = maintenanceRecords
            .filter { it.type == "OIL_CHANGE" }
            .maxByOrNull { it.date }
        
        if (lastOilChange != null) {
            val kmSinceOilChange = currentMileage - lastOilChange.mileage
            
            when {
                kmSinceOilChange > 15000 -> {
                    totalPenalty += SEVERE_OVERDUE_PENALTY
                    deductions.add(ScoreDeduction("Vidange très en retard (${kmSinceOilChange} km)", SEVERE_OVERDUE_PENALTY))
                    recommendations.add("🚨 URGENT: Vidange en retard de ${kmSinceOilChange - 10000} km – Risque de dommages moteur.")
                    overdueItems.add("Vidange moteur (retard: ${kmSinceOilChange - 10000} km)")
                }
                kmSinceOilChange > 10000 -> {
                    totalPenalty += OIL_CHANGE_OVERDUE_PENALTY
                    deductions.add(ScoreDeduction("Vidange en retard (${kmSinceOilChange} km)", OIL_CHANGE_OVERDUE_PENALTY))
                    recommendations.add("⚠️ Vidange recommandée sous 2 semaines (${kmSinceOilChange} km depuis dernière vidange).")
                    overdueItems.add("Vidange moteur")
                }
            }
        } else {
            // No oil change record
            totalPenalty += OIL_CHANGE_OVERDUE_PENALTY
            recommendations.add("⚠️ Aucun historique de vidange – Vérifier l'entretien du véhicule.")
        }
        
        // Check for expired CT (Contrôle Technique)
        val lastCT = maintenanceRecords
            .filter { it.type == "INSPECTION" || it.description.contains("CT", ignoreCase = true) }
            .maxByOrNull { it.date }
        
        if (lastCT != null) {
            val daysSinceCT = (System.currentTimeMillis() - lastCT.date) / (1000 * 60 * 60 * 24)
            val ctValidityDays = 365 * 2 // CT valid for 2 years in Morocco
            
            if (daysSinceCT > ctValidityDays) {
                totalPenalty += CT_EXPIRED_PENALTY
                deductions.add(ScoreDeduction("Contrôle Technique expiré", CT_EXPIRED_PENALTY))
                recommendations.add("🚨 Contrôle Technique expiré – Passer le CT avant de vendre/acheter.")
                overdueItems.add("Contrôle Technique")
            }
        }
        
        return CarnetImpactResult(totalPenalty, deductions, recommendations, overdueItems)
    }
    
    /**
     * Determine health status based on score
     */
    private fun determineHealthStatus(score: Int): String {
        return when {
            score >= 85 -> "Excellent"
            score >= 70 -> "Bon"
            score >= 50 -> "Moyen"
            score >= 30 -> "Problème Sérieux"
            else -> "Problème Grave"
        }
    }
    
    /**
     * Determine urgency level
     */
    private fun determineUrgencyLevel(issues: List<VideoIssue>, score: Int): UrgencyLevel {
        return when {
            issues.any { it.severity == "CRITICAL" } -> UrgencyLevel.CRITICAL
            score < 40 -> UrgencyLevel.CRITICAL
            score < 60 -> UrgencyLevel.HIGH
            issues.any { it.severity == "HIGH" } -> UrgencyLevel.MEDIUM
            issues.any { it.severity == "MEDIUM" } -> UrgencyLevel.LOW
            else -> UrgencyLevel.NONE
        }
    }
}

// =============================================================================
// DATA CLASSES
// =============================================================================

data class VideoScoreResult(
    val rawScore: Int,
    val finalScore: Int,
    val healthStatus: String,
    val urgencyLevel: UrgencyLevel,
    val detectedIssues: List<VideoIssue>,
    val recommendations: List<String>,
    val deductions: List<ScoreDeduction>,
    val carnetImpactScore: Int,
    val overdueMaintenanceItems: List<String>,
    val criticalWarning: String?
)

data class ScoreDeduction(
    val reason: String,
    val amount: Int
)

data class CarnetImpactResult(
    val totalPenalty: Int,
    val deductions: List<ScoreDeduction>,
    val recommendations: List<String>,
    val overdueItems: List<String>
)

enum class UrgencyLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
