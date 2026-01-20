package com.example.autobrain.domain.usecase

import com.example.autobrain.data.ai.AudioClassification
import com.example.autobrain.data.ai.EngineSoundTypes
import com.example.autobrain.data.ai.UrgencyLevel
import com.example.autobrain.domain.model.CarLog
import javax.inject.Inject
import kotlin.math.min

/**
 * Smart Audio Scoring Use Case - 100% Offline Logic
 * 
 * Calculates audio diagnostic score without LLM/cloud dependency
 * Integrates car maintenance history for enhanced accuracy
 * 
 * Score Formula:
 * - Base: 100 points
 * - Deductions based on detected sounds and confidence
 * - Bonus for normal engine sound
 * - Penalties from maintenance history (overdue services)
 * - Final score: 0-100 range
 */
class AudioScoringUseCase @Inject constructor() {

    /**
     * Calculate comprehensive audio score with smart logic
     */
    fun calculateScore(
        classifications: List<AudioClassification>,
        carLog: CarLog? = null
    ): AudioScoreResult {
        // Start with perfect score
        var score = 100f
        
        // Lists to track issues
        val detectedIssues = mutableListOf<DetectedIssue>()
        val recommendations = mutableListOf<String>()
        
        // === AUDIO CLASSIFICATION ANALYSIS ===
        val topClassifications = classifications
            .sortedByDescending { it.confidence }
            .take(3)
        
        topClassifications.forEach { classification ->
            val penalty = calculatePenalty(classification)
            score -= penalty
            
            if (penalty > 0) {
                val issue = DetectedIssue(
                    soundType = classification.label,
                    confidence = classification.confidence,
                    severity = determineSeverity(classification.label, classification.confidence),
                    description = classification.description,
                    estimatedCost = estimateRepairCost(classification.label)
                )
                detectedIssues.add(issue)
                
                // Add specific recommendations
                EngineSoundTypes.recommendations[classification.label]?.let { recs ->
                    recommendations.addAll(recs)
                }
            }
        }
        
        // === BONUS FOR NORMAL ENGINE ===
        val normalEngineClass = classifications.find { it.label == EngineSoundTypes.NORMAL_ENGINE }
        if (normalEngineClass != null && normalEngineClass.confidence > 0.8f) {
            score += 10f
            score = min(score, 100f)
        }
        
        // === MAINTENANCE HISTORY INTEGRATION ===
        carLog?.let { log ->
            val maintenancePenalty = evaluateMaintenanceHistory(log)
            score -= maintenancePenalty.penalty
            
            maintenancePenalty.reasons.forEach { reason ->
                recommendations.add(reason)
            }
        }
        
        // === ENSURE SCORE BOUNDS ===
        score = score.coerceIn(0f, 100f)
        
        // === DETERMINE URGENCY ===
        val urgency = determineUrgencyLevel(score, detectedIssues)
        
        // === BUILD RESULT ===
        return AudioScoreResult(
            rawScore = score.toInt(),
            normalizedScore = score,
            detectedIssues = detectedIssues,
            recommendations = recommendations.distinct(),
            urgencyLevel = urgency,
            criticalWarning = if (score < 50) "⚠️ IMMEDIATE ATTENTION REQUIRED - Serious problem detected!" else null,
            healthStatus = determineHealthStatus(score),
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Calculate penalty points based on sound classification
     */
    private fun calculatePenalty(classification: AudioClassification): Float {
        // Base penalties for each sound type
        val basePenalty = when (classification.label) {
            EngineSoundTypes.KNOCKING -> 60f      // Critical - piston/bearing issues
            EngineSoundTypes.MISFIRE -> 50f       // Critical - combustion issues
            EngineSoundTypes.GRINDING -> 45f      // Severe - mechanical damage
            EngineSoundTypes.RATTLING -> 40f      // Moderate-severe - loose components
            EngineSoundTypes.HISSING -> 35f       // Moderate - possible leak
            EngineSoundTypes.BELT_SQUEAL -> 30f   // Moderate - belt issues
            EngineSoundTypes.RUMBLING -> 25f      // Moderate - exhaust/bearing
            EngineSoundTypes.WHINING -> 25f       // Moderate - fluid/transmission
            EngineSoundTypes.TAPPING -> 20f       // Minor-moderate - valve issues
            EngineSoundTypes.CLICKING -> 15f      // Minor - CV joint or starter
            EngineSoundTypes.NORMAL_ENGINE -> 0f  // No penalty
            else -> 10f                            // Unknown sound
        }
        
        // Weight penalty by confidence (only apply if confidence > 70%)
        return if (classification.confidence >= 0.7f) {
            basePenalty * classification.confidence
        } else {
            // Lower confidence = reduced penalty
            basePenalty * 0.3f
        }
    }
    
    /**
     * Evaluate maintenance history and return penalty
     */
    private fun evaluateMaintenanceHistory(carLog: CarLog): MaintenancePenalty {
        var penalty = 0f
        val reasons = mutableListOf<String>()
        
        val now = System.currentTimeMillis()
        val oneMonth = 30L * 24 * 60 * 60 * 1000
        
        // Check for overdue maintenance
        carLog.reminders.forEach { reminder ->
            if (!reminder.isCompleted && reminder.dueDate < now) {
                val overdueDays = ((now - reminder.dueDate) / (24 * 60 * 60 * 1000)).toInt()
                
                when {
                    overdueDays > 180 -> { // 6 months overdue
                        penalty += 20f
                        reasons.add("⚠️ Maintenance '${reminder.title}' is $overdueDays days overdue - Critical!")
                    }
                    overdueDays > 90 -> { // 3 months overdue
                        penalty += 15f
                        reasons.add("⚠️ Maintenance '${reminder.title}' is $overdueDays days overdue")
                    }
                    overdueDays > 30 -> { // 1 month overdue
                        penalty += 10f
                        reasons.add("Maintenance '${reminder.title}' is $overdueDays days overdue")
                    }
                }
            }
        }
        
        // Check last maintenance records
        val recentRecords = carLog.maintenanceRecords
            .filter { (now - it.date) < oneMonth * 6 } // Last 6 months
        
        if (recentRecords.isEmpty() && carLog.maintenanceRecords.isNotEmpty()) {
            penalty += 15f
            reasons.add("No maintenance recorded in the last 6 months")
        }
        
        // Check mileage-based maintenance (if available)
        val lastOilChange = carLog.maintenanceRecords
            .filter { it.type.name.contains("OIL") }
            .maxByOrNull { it.date }
        
        if (lastOilChange != null) {
            // Get most recent maintenance record to estimate current mileage
            val latestRecord = carLog.maintenanceRecords.maxByOrNull { it.date }
            if (latestRecord != null && latestRecord.mileage > 0) {
                val kmSinceOil = latestRecord.mileage - lastOilChange.mileage
                if (kmSinceOil > 15000) {
                    penalty += 20f
                    reasons.add("⚠️ Oil change overdue by ${kmSinceOil - 10000} km - Urgent!")
                }
            }
        }
        
        // Check CT (Technical Control) expiry
        carLog.documents.find { it.type.name.contains("TECHNICAL") || it.type.name.contains("INSPECTION") }
            ?.let { ct ->
                if (ct.isExpired) {
                    penalty += 20f
                    reasons.add("⚠️ Technical inspection expired - Legally non-compliant")
                } else {
                    val timeUntilExpiry = ct.expiryDate - now
                    if (timeUntilExpiry < oneMonth && timeUntilExpiry > 0) {
                        penalty += 5f
                        reasons.add("Technical inspection expiring soon")
                    }
                }
            }
        
        return MaintenancePenalty(
            penalty = penalty.coerceAtMost(40f), // Cap at 40 points
            reasons = reasons
        )
    }
    
    /**
     * Determine severity level for a detected issue
     */
    private fun determineSeverity(soundType: String, confidence: Float): IssueSeverity {
        val baseSeverity = when (soundType) {
            EngineSoundTypes.KNOCKING,
            EngineSoundTypes.MISFIRE -> IssueSeverity.CRITICAL
            
            EngineSoundTypes.GRINDING,
            EngineSoundTypes.RATTLING -> IssueSeverity.HIGH
            
            EngineSoundTypes.HISSING,
            EngineSoundTypes.BELT_SQUEAL,
            EngineSoundTypes.RUMBLING,
            EngineSoundTypes.WHINING -> IssueSeverity.MEDIUM
            
            EngineSoundTypes.TAPPING,
            EngineSoundTypes.CLICKING -> IssueSeverity.LOW
            
            else -> IssueSeverity.LOW
        }
        
        // Reduce severity if confidence is low
        return if (confidence < 0.7f && baseSeverity != IssueSeverity.LOW) {
            when (baseSeverity) {
                IssueSeverity.CRITICAL -> IssueSeverity.HIGH
                IssueSeverity.HIGH -> IssueSeverity.MEDIUM
                IssueSeverity.MEDIUM -> IssueSeverity.LOW
                else -> IssueSeverity.LOW
            }
        } else {
            baseSeverity
        }
    }
    
    /**
     * Estimate repair cost range in USD
     */
    private fun estimateRepairCost(soundType: String): CostRange {
        return when (soundType) {
            EngineSoundTypes.KNOCKING -> CostRange(15000.0, 35000.0, "Probable engine rebuild")
            EngineSoundTypes.MISFIRE -> CostRange(2000.0, 8000.0, "Ignition/injection system repair")
            EngineSoundTypes.GRINDING -> CostRange(3000.0, 12000.0, "Brake or transmission repair")
            EngineSoundTypes.RATTLING -> CostRange(500.0, 3000.0, "Tightening or replacement of mountings")
            EngineSoundTypes.HISSING -> CostRange(800.0, 4000.0, "Fluid leak repair")
            EngineSoundTypes.BELT_SQUEAL -> CostRange(300.0, 1500.0, "Belt replacement")
            EngineSoundTypes.RUMBLING -> CostRange(1000.0, 5000.0, "Exhaust or bearing repair")
            EngineSoundTypes.WHINING -> CostRange(1500.0, 6000.0, "Steering or transmission repair")
            EngineSoundTypes.TAPPING -> CostRange(800.0, 3000.0, "Valve adjustment")
            EngineSoundTypes.CLICKING -> CostRange(500.0, 2500.0, "CV joint or starter repair")
            else -> CostRange(500.0, 5000.0, "Diagnostic required")
        }
    }
    
    /**
     * Determine overall urgency level
     */
    private fun determineUrgencyLevel(score: Float, issues: List<DetectedIssue>): UrgencyLevel {
        val hasCritical = issues.any { it.severity == IssueSeverity.CRITICAL }
        val hasHigh = issues.any { it.severity == IssueSeverity.HIGH }
        
        return when {
            score < 40 || hasCritical -> UrgencyLevel.CRITICAL
            score < 60 || hasHigh -> UrgencyLevel.HIGH
            score < 75 -> UrgencyLevel.MEDIUM
            score < 85 -> UrgencyLevel.LOW
            else -> UrgencyLevel.NONE
        }
    }
    
    /**
     * Determine health status string
     */
    private fun determineHealthStatus(score: Float): String {
        return when {
            score >= 90 -> "Excellent"
            score >= 75 -> "Good"
            score >= 60 -> "Acceptable"
            score >= 40 -> "Poor"
            else -> "Critical"
        }
    }
}

// =============================================================================
// DATA CLASSES
// =============================================================================

data class AudioScoreResult(
    val rawScore: Int,
    val normalizedScore: Float,
    val detectedIssues: List<DetectedIssue>,
    val recommendations: List<String>,
    val urgencyLevel: UrgencyLevel,
    val criticalWarning: String?,
    val healthStatus: String,
    val timestamp: Long
)

data class DetectedIssue(
    val soundType: String,
    val confidence: Float,
    val severity: IssueSeverity,
    val description: String,
    val estimatedCost: CostRange
)

enum class IssueSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class CostRange(
    val minCost: Double,
    val maxCost: Double,
    val description: String,
    val currency: String = "USD"
) {
    fun getFormattedRange(): String {
        return "$${minCost.toInt()} - $${maxCost.toInt()}"
    }
}

private data class MaintenancePenalty(
    val penalty: Float,
    val reasons: List<String>
)
