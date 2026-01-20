package com.example.autobrain.domain.model

import com.google.gson.annotations.SerializedName

/**
 * AutoBrain AI Score Models
 * Comprehensive data classes for AI-powered car evaluation
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

enum class EngineIssueType(val score: Int, val severity: IssueSeverity, val descriptionEn: String) {
    NORMAL(100, IssueSeverity.NONE, "Normal operation"),
    BELT_SQUEAL(70, IssueSeverity.MINOR, "Belt squeak"),
    RATTLING(60, IssueSeverity.MEDIUM, "Rattling/Metal noise"),
    KNOCKING(40, IssueSeverity.GRAVE, "Engine knocking - Serious risk"),
    MISFIRING(50, IssueSeverity.MEDIUM, "Engine misfire"),
    EXHAUST_LEAK(55, IssueSeverity.MEDIUM, "Exhaust leak"),
    VALVE_TAPPING(65, IssueSeverity.MINOR, "Valve tapping"),
    BEARING_NOISE(35, IssueSeverity.GRAVE, "Bearing noise - Serious risk"),
    UNKNOWN(75, IssueSeverity.MINOR, "Unidentified noise")
}

data class VideoAnalysisResult(
    val smokeType: SmokeType,
    val smokeSeverity: Int,              // 0-5 scale
    val vibrationLevel: Int,             // 0-5 scale
    val otherObservations: List<String>,
    val framesAnalyzed: Int,
    val analysisTimestamp: Long
)

enum class SmokeType(val scorePenalty: Int, val severity: IssueSeverity, val descriptionEn: String) {
    NONE(0, IssueSeverity.NONE, "No smoke detected"),
    WHITE_LIGHT(20, IssueSeverity.MINOR, "Light white smoke (normal condensation)"),
    WHITE_HEAVY(40, IssueSeverity.GRAVE, "Heavy white smoke - Suspected head gasket"),
    BLUE(35, IssueSeverity.MEDIUM, "Blue smoke - Oil consumption"),
    BLACK_LIGHT(30, IssueSeverity.MEDIUM, "Light black smoke - Rich mixture"),
    BLACK_HEAVY(50, IssueSeverity.GRAVE, "Heavy black smoke - Serious injection problem")
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
    val isPopular: Boolean,
    val points: Int
) {
    companion object {
        val POPULAR_BRANDS = listOf(
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
    
    @SerializedName("price_range_usd")
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
    val grave: List<Issue>,              // Repair > $2,000
    val medium: List<Issue>,             // Repair $500 - $2,000
    val minor: List<Issue>               // Aesthetic or routine maintenance
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
    NONE("None", "green"),
    MINOR("Minor", "blue"),
    MEDIUM("Medium", "orange"),
    GRAVE("Serious", "red")
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

enum class ScoreCategory(val range: IntRange, val displayName: String, val displayNameEn: String) {
    EXCELLENT(90..100, "Excellent", "Excellent"),
    GOOD(70..89, "Good", "Very good condition"),
    FAIR(50..69, "Fair", "Average condition"),
    POOR(30..49, "Poor", "Poor condition"),
    CRITICAL(0..29, "Critical", "Avoid")
}

enum class RiskLevel(val displayName: String, val displayNameEn: String, val color: String) {
    LOW("Low Risk", "Low Risk", "green"),
    MEDIUM("Medium Risk", "Medium Risk", "orange"),
    HIGH("High Risk", "High Risk", "red")
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
