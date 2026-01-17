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
        NORMAL_ENGINE to "Moteur fonctionnant normalement",
        KNOCKING to "Cognement du moteur - Possible problème de piston ou de bielle",
        RATTLING to "Bruit de cliquetis - Vérifier les fixations et les composants desserrés",
        BELT_SQUEAL to "Grincement de courroie - Courroie usée ou mal tendue",
        GRINDING to "Bruit de grincement - Possible usure des freins ou de la transmission",
        HISSING to "Sifflement - Possible fuite de liquide de refroidissement ou de vide",
        CLICKING to "Cliquetis - Vérifier les joints homocinétiques ou le démarreur",
        TAPPING to "Tapotement - Possible jeu de soupapes excessif",
        RUMBLING to "Grondement - Vérifier l'échappement ou les roulements de roue",
        WHINING to "Gémissement - Possible problème de direction assistée ou de transmission",
        MISFIRE to "Raté d'allumage - Problème de combustion ou système d'allumage"
    )

    val recommendations = mapOf(
        NORMAL_ENGINE to listOf("Continuer l'entretien régulier"),
        KNOCKING to listOf(
            "Consulter un mécanicien immédiatement",
            "Ne pas conduire sur de longues distances",
            "Vérifier le niveau et la qualité de l'huile"
        ),
        RATTLING to listOf(
            "Inspecter les écrans thermiques",
            "Vérifier les fixations du moteur",
            "Contrôler le système d'échappement"
        ),
        BELT_SQUEAL to listOf(
            "Remplacer la courroie d'accessoires",
            "Vérifier la tension de la courroie",
            "Inspecter les poulies"
        ),
        GRINDING to listOf(
            "Faire vérifier les plaquettes de frein",
            "Contrôler la transmission",
            "Ne pas ignorer ce bruit"
        ),
        HISSING to listOf(
            "Vérifier le circuit de refroidissement",
            "Contrôler les durites",
            "Inspecter le système de climatisation"
        ),
        CLICKING to listOf(
            "Faire vérifier les joints homocinétiques",
            "Contrôler la batterie",
            "Inspecter le démarreur"
        ),
        TAPPING to listOf(
            "Faire régler le jeu aux soupapes",
            "Vérifier le niveau d'huile",
            "Consulter un mécanicien"
        ),
        RUMBLING to listOf(
            "Inspecter le système d'échappement",
            "Vérifier les roulements de roue",
            "Contrôler les supports moteur"
        ),
        WHINING to listOf(
            "Vérifier le niveau de liquide de direction",
            "Contrôler la pompe de direction assistée",
            "Inspecter la transmission"
        ),
        MISFIRE to listOf(
            "Vérifier les bougies d'allumage",
            "Contrôler le système d'injection",
            "Inspecter les bobines d'allumage",
            "Diagnostiquer immédiatement"
        )
    )
}
