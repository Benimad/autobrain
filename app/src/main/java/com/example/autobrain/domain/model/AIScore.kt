package com.example.autobrain.domain.model

import com.google.gson.annotations.SerializedName

/**
 * AutoBrain AI Score Models
 * Comprehensive data classes for AI-powered car evaluation
 * Adapted for Morocco market 2025
 */

// ============================================================================
// MAIN AI SCORE RESULT
// ============================================================================

data class AIScoreResult(
    val finalScore: Int,
    val scoreCategory: ScoreCategory,
    val breakdown: AIScoreBreakdown,
    val llmAnalysis: LlmAnalysisResult?,
    val priceEstimate: PriceEstimate?,
    val issues: IssuesList,
    val buyerAdvice: String,
    val riskLevel: RiskLevel,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class AIScoreBreakdown(
    val technicalScore: TechnicalScore,
    val maintenanceScore: MaintenanceScore,
    val marketScore: MarketScore,
    val rawWeightedScore: Float,
    val llmAdjustment: Int
) {
    companion object {
        const val TECHNICAL_WEIGHT = 0.70f
        const val MAINTENANCE_WEIGHT = 0.20f
        const val MARKET_WEIGHT = 0.10f
    }
}

// ============================================================================
// TECHNICAL SCORE (70% of total)
// ============================================================================

data class TechnicalScore(
    val engineSoundScore: Int,           // Based on TFLite audio analysis
    val videoAnalysisScore: Int,         // Based on ML Kit video analysis
    val overallScore: Int,               // Weighted average
    val engineSoundResult: EngineSoundResult?,
    val videoResult: VideoAnalysisResult?
) {
    companion object {
        const val ENGINE_SOUND_WEIGHT = 0.40f  // 40% of technical
        const val VIDEO_ANALYSIS_WEIGHT = 0.30f // 30% of technical
    }
}

data class EngineSoundResult(
    val mainIssue: EngineIssueType,
    val confidence: Float,
    val otherIssues: List<EngineIssueType>,
    val rawDescription: String,
    val recordingDuration: Int,          // in seconds
    val analysisTimestamp: Long
)

enum class EngineIssueType(val score: Int, val severity: IssueSeverity, val descriptionFr: String) {
    NORMAL(100, IssueSeverity.NONE, "Fonctionnement normal"),
    BELT_SQUEAL(70, IssueSeverity.MINOR, "Grincement de courroie"),
    RATTLING(60, IssueSeverity.MEDIUM, "Cliquetis/Bruit de ferraille"),
    KNOCKING(40, IssueSeverity.GRAVE, "Cognement moteur - Risque grave"),
    MISFIRING(50, IssueSeverity.MEDIUM, "Ratés d'allumage"),
    EXHAUST_LEAK(55, IssueSeverity.MEDIUM, "Fuite d'échappement"),
    VALVE_TAPPING(65, IssueSeverity.MINOR, "Claquement de soupapes"),
    BEARING_NOISE(35, IssueSeverity.GRAVE, "Bruit de roulement - Risque grave"),
    UNKNOWN(75, IssueSeverity.MINOR, "Bruit non identifié")
}

data class VideoAnalysisResult(
    val smokeType: SmokeType,
    val smokeSeverity: Int,              // 0-5 scale
    val vibrationLevel: Int,             // 0-5 scale
    val otherObservations: List<String>,
    val framesAnalyzed: Int,
    val analysisTimestamp: Long
)

enum class SmokeType(val scorePenalty: Int, val severity: IssueSeverity, val descriptionFr: String) {
    NONE(0, IssueSeverity.NONE, "Pas de fumée détectée"),
    WHITE_LIGHT(20, IssueSeverity.MINOR, "Fumée blanche légère (condensation normale)"),
    WHITE_HEAVY(40, IssueSeverity.GRAVE, "Fumée blanche épaisse - Joint de culasse suspect"),
    BLUE(35, IssueSeverity.MEDIUM, "Fumée bleue - Consommation d'huile"),
    BLACK_LIGHT(30, IssueSeverity.MEDIUM, "Fumée noire légère - Mélange riche"),
    BLACK_HEAVY(50, IssueSeverity.GRAVE, "Fumée noire épaisse - Problème injection grave")
}

// ============================================================================
// MAINTENANCE SCORE (20% of total) - Carnet Intelligent
// ============================================================================

data class MaintenanceScore(
    val overallScore: Int,               // 0-100
    val rawPoints: Int,                  // Out of 70 max
    val oilChangeStatus: MaintenanceStatus,
    val technicalInspectionStatus: MaintenanceStatus,
    val insuranceStatus: MaintenanceStatus,
    val mileageConsistency: MileageConsistency
)

data class MaintenanceStatus(
    val status: StatusLevel,
    val points: Int,
    val description: String,
    val dueDate: String? = null,
    val lastDate: String? = null
)

enum class StatusLevel(val color: String) {
    GOOD("green"),
    WARNING("orange"),
    EXPIRED("red"),
    UNKNOWN("gray")
}

data class MileageConsistency(
    val isConsistent: Boolean,
    val points: Int,
    val currentKm: Int,
    val expectedKmRange: IntRange?,
    val anomalyDetected: Boolean,
    val anomalyDescription: String?
)

// ============================================================================
// MARKET SCORE (10% of total)
// ============================================================================

data class MarketScore(
    val overallScore: Int,
    val rawPoints: Int,                  // Out of 15 max
    val priceComparison: PriceComparison,
    val modelPopularity: ModelPopularity
)

data class PriceComparison(
    val askedPrice: Int,                 // in USD
    val estimatedPrice: IntRange,        // in USD
    val comparisonResult: PriceComparisonResult,
    val points: Int
)

enum class PriceComparisonResult(val points: Int) {
    BELOW_OR_EQUAL(10),
    SLIGHTLY_ABOVE(5),           // 10-20% above
    WAY_ABOVE(0)                 // >20% above
}

data class ModelPopularity(
    val brand: String,
    val isPopularInMorocco: Boolean,
    val points: Int
) {
    companion object {
        val POPULAR_BRANDS_MOROCCO = listOf(
            "Dacia", "Renault", "Peugeot", "Hyundai", "Volkswagen",
            "Toyota", "Fiat", "Citroën", "Kia", "Ford"
        )
    }
}

// ============================================================================
// LLM ANALYSIS RESULT
// ============================================================================

data class LlmAnalysisResult(
    @SerializedName("ai_score")
    val aiScore: Int,
    
    @SerializedName("score_category")
    val scoreCategory: String,
    
    @SerializedName("grave_issues")
    val graveIssues: List<String>,
    
    @SerializedName("medium_issues")
    val mediumIssues: List<String>,
    
    @SerializedName("minor_issues")
    val minorIssues: List<String>,
    
    @SerializedName("price_range_dh")
    val priceRangeDh: String,
    
    @SerializedName("buyer_advice")
    val buyerAdvice: String,
    
    @SerializedName("detailed_explanation")
    val detailedExplanation: String,
    
    val adjustment: Int = 0,             // -10 to +10
    val llmProvider: String = "gemini"   // Only Gemini AI is supported
)

// ============================================================================
// PRICE ESTIMATE
// ============================================================================

data class PriceEstimate(
    val lowPrice: Int,                   // in USD
    val highPrice: Int,                  // in USD
    val confidence: ConfidenceLevel,
    val basedOn: List<String>,           // Sources: market data
    val lastUpdated: Long
) {
    fun getFormattedRange(): String = "$$lowPrice - $$highPrice"
}

enum class ConfidenceLevel(val displayName: String) {
    HIGH("High Confidence"),
    MEDIUM("Medium Confidence"),
    LOW("Low Confidence")
}

// ============================================================================
// ISSUES CLASSIFICATION
// ============================================================================

data class IssuesList(
    val grave: List<Issue>,              // Réparation > $2,000
    val medium: List<Issue>,             // Réparation $500 - $2,000
    val minor: List<Issue>               // Esthétique ou entretien courant
) {
    fun hasGraveIssues(): Boolean = grave.isNotEmpty()
    fun totalIssues(): Int = grave.size + medium.size + minor.size
}

data class Issue(
    val title: String,
    val description: String,
    val severity: IssueSeverity,
    val estimatedRepairCost: IntRange?,  // in USD
    val source: IssueSource
)

enum class IssueSeverity(val displayName: String, val color: String) {
    NONE("Aucun", "green"),
    MINOR("Mineur", "blue"),
    MEDIUM("Moyen", "orange"),
    GRAVE("Grave", "red")
}

enum class IssueSource {
    ENGINE_SOUND_ANALYSIS,
    VIDEO_ANALYSIS,
    MAINTENANCE_LOG,
    LLM_ANALYSIS
}

// ============================================================================
// SCORE CATEGORY & RISK LEVEL
// ============================================================================

enum class ScoreCategory(val range: IntRange, val displayName: String, val displayNameFr: String) {
    EXCELLENT(90..100, "Excellent", "Excellent"),
    GOOD(70..89, "Good", "Très bon état"),
    FAIR(50..69, "Fair", "État moyen"),
    POOR(30..49, "Poor", "État médiocre"),
    CRITICAL(0..29, "Critical", "À éviter")
}

enum class RiskLevel(val displayName: String, val displayNameFr: String, val color: String) {
    LOW("Low Risk", "Risque faible", "green"),
    MEDIUM("Medium Risk", "Risque moyen", "orange"),
    HIGH("High Risk", "Risque élevé", "red")
}

// ============================================================================
// CAR DATA FOR ANALYSIS
// ============================================================================

data class CarDataForAnalysis(
    val brand: String,
    val model: String,
    val year: Int,
    val currentKm: Int,
    val lastOilChangeDate: String,
    val kmSinceOilChange: Int,
    val lastTechnicalInspection: String,
    val insuranceStatus: String,
    val askedPrice: Int? = null
)

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================

fun getScoreCategoryFromScore(score: Int): ScoreCategory {
    return when {
        score >= 90 -> ScoreCategory.EXCELLENT
        score >= 70 -> ScoreCategory.GOOD
        score >= 50 -> ScoreCategory.FAIR
        score >= 30 -> ScoreCategory.POOR
        else -> ScoreCategory.CRITICAL
    }
}

fun getRiskLevelFromScore(score: Int): RiskLevel {
    return when {
        score >= 70 -> RiskLevel.LOW
        score >= 50 -> RiskLevel.MEDIUM
        else -> RiskLevel.HIGH
    }
}
