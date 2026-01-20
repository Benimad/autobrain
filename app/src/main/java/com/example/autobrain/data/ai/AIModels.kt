package com.example.autobrain.data.ai

/**
 * AI Models and Data Classes for Car Diagnostics
 * Uses Google Gemini AI for all AI-powered features
 */

// =============================================================================
// AI PROVIDER ENUM
// =============================================================================

enum class AIProvider {
    GEMINI,       // Google Gemini API (primary)
    LOCAL_TFLITE  // On-device TensorFlow Lite (fallback)
}

// =============================================================================
// AI API CONFIGURATION
// =============================================================================

data class AIApiConfig(
    val geminiKey: String = "",
    val preferredProvider: AIProvider = AIProvider.GEMINI,
    val fallbackToLocal: Boolean = true
) {
    fun isConfigured(): Boolean = geminiKey.isNotEmpty()
}

// =============================================================================
// GEMINI-SPECIFIC MODELS (using Gemini SDK directly)
// =============================================================================

// Gemini uses its own SDK (com.google.ai.client.generativeai) 
// No need for custom request/response models here

// =============================================================================
// CAR DIAGNOSTICS MODELS
// =============================================================================

data class AudioClassification(
    val label: String,          // e.g., "knocking", "normal_engine", "belt_squeal"
    val confidence: Float,      // 0.0 - 1.0
    val description: String = ""
)

data class VideoAnalysisResult(
    val detectedObjects: List<DetectedObject>,
    val anomalies: List<CarAnomaly>,
    val frameCount: Int,
    val analysisTime: Long,
    val summary: String = "",
    val recommendations: List<String> = emptyList(),
    val visualHealthScore: Int = 0 // 0-100
)

data class DetectedObject(
    val label: String,
    val confidence: Float,
    val boundingBox: BoundingBox?
)

data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

data class CarAnomaly(
    val type: AnomalyType,
    val severity: SeverityLevel,
    val confidence: Float,
    val description: String,
    val recommendation: String
)

enum class AnomalyType {
    SMOKE_EXHAUST,
    SMOKE_ENGINE,
    VIBRATION_EXCESSIVE,
    LEAK_OIL,
    LEAK_COOLANT,
    RUST,
    DAMAGE_BODY,
    TIRE_WEAR,
    NONE
}

enum class SeverityLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

// =============================================================================
// AI ANALYSIS RESULT
// =============================================================================

data class AICarDiagnosticResult(
    val provider: AIProvider,
    val audioAnalysis: AudioAnalysisResult?,
    val videoAnalysis: VideoAnalysisResult?,
    val combinedDiagnosis: CombinedDiagnosis,
    val timestamp: Long = System.currentTimeMillis()
)

data class AudioAnalysisResult(
    val classifications: List<AudioClassification>,
    val mainIssue: String?,
    val possibleCauses: List<String>,
    val recommendations: List<String>,
    val healthScore: Int  // 0-100
)

data class CombinedDiagnosis(
    val overallHealthScore: Int,    // 0-100
    val mainIssues: List<String>,
    val urgencyLevel: UrgencyLevel,
    val estimatedRepairCost: CostRange?,
    val detailedAnalysis: String,
    val recommendations: List<String>,
    val preSaleReport: PreSaleReport?
)

enum class UrgencyLevel {
    NONE,           // No issues detected
    LOW,            // Can wait, minor issues
    MEDIUM,         // Schedule maintenance soon
    HIGH,           // Needs attention within days
    CRITICAL        // Immediate attention required
}

data class CostRange(
    val minCost: Double,
    val maxCost: Double,
    val currency: String = "USD"
)

data class PreSaleReport(
    val qualityScore: Int,          // 0-100
    val estimatedValue: Double,
    val priceRange: CostRange,
    val positivePoints: List<String>,
    val negativePoints: List<String>,
    val recommendation: String      // "Good buy", "Negotiate", "Avoid"
)

// =============================================================================
// ENGINE SOUND TYPES
// =============================================================================

object EngineSoundTypes {
    const val NORMAL_ENGINE = "normal_engine"
    const val KNOCKING = "knocking"
    const val RATTLING = "rattling"
    const val BELT_SQUEAL = "belt_squeal"
    const val GRINDING = "grinding"
    const val HISSING = "hissing"
    const val CLICKING = "clicking"
    const val TAPPING = "tapping"
    const val RUMBLING = "rumbling"
    const val WHINING = "whining"
    const val MISFIRE = "misfire"

    val descriptions = mapOf(
        NORMAL_ENGINE to "Engine running normally",
        KNOCKING to "Engine knocking - Possible piston or connecting rod issue",
        RATTLING to "Ratting noise - Check fasteners and loose components",
        BELT_SQUEAL to "Belt squeal - Worn or improperly tensioned belt",
        GRINDING to "Grinding noise - Possible brake or transmission wear",
        HISSING to "Hissing - Possible coolant or vacuum leak",
        CLICKING to "Clicking - Check CV joints or starter",
        TAPPING to "Tapping - Possible excessive valve clearance",
        RUMBLING to "Rumbling - Check exhaust or wheel bearings",
        WHINING to "Whining - Possible power steering or transmission issue",
        MISFIRE to "Engine misfire - Combustion or ignition system issue"
    )

    val recommendations = mapOf(
        NORMAL_ENGINE to listOf("Continue regular maintenance"),
        KNOCKING to listOf(
            "Consult a mechanic immediately",
            "Do not drive long distances",
            "Check oil level and quality"
        ),
        RATTLING to listOf(
            "Inspect heat shields",
            "Check engine mount fasteners",
            "Control exhaust system"
        ),
        BELT_SQUEAL to listOf(
            "Replace accessory belt",
            "Check belt tension",
            "Inspect pulleys"
        ),
        GRINDING to listOf(
            "Have brake pads checked",
            "Inspect transmission",
            "Do not ignore this noise"
        ),
        HISSING to listOf(
            "Check coolant circuit",
            "Inspect hoses",
            "Check air conditioning system"
        ),
        CLICKING to listOf(
            "Have CV joints checked",
            "Check battery",
            "Inspect starter"
        ),
        TAPPING to listOf(
            "Adjust valve clearance",
            "Check oil level",
            "Consult a mechanic"
        ),
        RUMBLING to listOf(
            "Inspect exhaust system",
            "Check wheel bearings",
            "Control engine mounts"
        ),
        WHINING to listOf(
            "Check power steering fluid level",
            "Inspect power steering pump",
            "Check transmission"
        ),
        MISFIRE to listOf(
            "Check spark plugs",
            "Inspect fuel injection system",
            "Check ignition coils",
            "Diagnose immediately"
        )
    )
}
