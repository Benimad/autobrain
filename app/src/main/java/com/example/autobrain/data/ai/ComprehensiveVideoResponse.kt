package com.example.autobrain.data.ai

import com.google.gson.annotations.SerializedName

/**
 * AUTOBRAIN - COMPREHENSIVE VIDEO DIAGNOSTIC RESPONSE MODELS
 * Data classes for parsing Gemini 2.5 Pro video analysis JSON
 * 
 * Matches the exact JSON structure from buildComprehensiveVideoAnalysisPrompt
 */

/**
 * Complete Gemini AI Video Diagnostic Response
 */
data class ComprehensiveVideoDiagnostic(
    @SerializedName("enhanced_visual_score")
    val enhancedVisualScore: Int,
    
    @SerializedName("smoke_deep_analysis")
    val smokeDeepAnalysis: SmokeDeepAnalysis,
    
    @SerializedName("vibration_engineering_analysis")
    val vibrationEngineeringAnalysis: VibrationEngineeringAnalysis,
    
    @SerializedName("combined_audio_video_diagnosis")
    val combinedAudioVideoDiagnosis: CombinedAudioVideoDiagnosis,
    
    @SerializedName("repair_scenarios_visual")
    val repairScenariosVisual: List<VisualRepairScenario>,
    
    @SerializedName("video_quality_assessment")
    val videoQualityAssessment: VideoQualityAssessment,
    
    @SerializedName("safety_assessment")
    val safetyAssessment: SafetyAssessment,
    
    @SerializedName("market_impact_visual")
    val marketImpactVisual: MarketImpactVisual,
    
    @SerializedName("environmental_compliance")
    val environmentalCompliance: EnvironmentalCompliance,
    
    @SerializedName("autobrain_video_confidence")
    val autobrainVideoConfidence: AutobrainVideoConfidence
)

// =============================================================================
// SMOKE DEEP ANALYSIS
// =============================================================================

data class SmokeDeepAnalysis(
    @SerializedName("type_detected")
    val typeDetected: String,
    
    @SerializedName("technical_diagnosis")
    val technicalDiagnosis: String,
    
    @SerializedName("chemical_composition_theory")
    val chemicalCompositionTheory: String,
    
    @SerializedName("emission_pattern")
    val emissionPattern: String,
    
    @SerializedName("smell_prediction")
    val smellPrediction: String,
    
    @SerializedName("color_intensity")
    val colorIntensity: String,
    
    @SerializedName("root_causes_by_probability")
    val rootCausesByProbability: List<SmokeRootCause>,
    
    @SerializedName("worst_case_scenario")
    val worstCaseScenario: String,
    
    @SerializedName("immediate_risks")
    val immediateRisks: List<String>
)

data class SmokeRootCause(
    @SerializedName("cause")
    val cause: String,
    
    @SerializedName("probability")
    val probability: Float,
    
    @SerializedName("confirming_tests")
    val confirmingTests: List<String>,
    
    @SerializedName("repair_complexity")
    val repairComplexity: String,
    
    @SerializedName("estimated_cost_usd")
    val estimatedCostUsd: String
)

// =============================================================================
// VIBRATION ENGINEERING ANALYSIS
// =============================================================================

data class VibrationEngineeringAnalysis(
    @SerializedName("vibration_frequency_estimation")
    val vibrationFrequencyEstimation: String,
    
    @SerializedName("vibration_source_diagnosis")
    val vibrationSourceDiagnosis: String,
    
    @SerializedName("phase_analysis")
    val phaseAnalysis: String,
    
    @SerializedName("probable_mechanical_causes")
    val probableMechanicalCauses: List<VibrationMechanicalCause>,
    
    @SerializedName("cascading_failures_if_ignored")
    val cascadingFailuresIfIgnored: List<String>
)

data class VibrationMechanicalCause(
    @SerializedName("component")
    val component: String,

    @SerializedName("failure_type")
    val failureType: String,

    @SerializedName("diagnostic_test")
    val diagnosticTest: String,

    @SerializedName("replacement_cost_usd")
    val replacementCostUsd: String,

    @SerializedName("urgency")
    val urgency: String
)

// =============================================================================
// COMBINED AUDIO-VIDEO DIAGNOSIS
// =============================================================================

data class CombinedAudioVideoDiagnosis(
    @SerializedName("correlation_score")
    val correlationScore: Float,
    
    @SerializedName("multimodal_insights")
    val multimodalInsights: List<String>,
    
    @SerializedName("comprehensive_root_cause")
    val comprehensiveRootCause: String,
    
    @SerializedName("confidence_boost")
    val confidenceBoost: String
)

// =============================================================================
// REPAIR SCENARIOS
// =============================================================================

data class VisualRepairScenario(
    @SerializedName("scenario_name")
    val scenarioName: String,

    @SerializedName("applicable_if")
    val applicableIf: String,

    @SerializedName("steps")
    val steps: List<String>,

    @SerializedName("total_cost_usd")
    val totalCostUsd: Double,

    @SerializedName("success_probability")
    val successProbability: Float,

    @SerializedName("duration_hours")
    val durationHours: Int? = null,

    @SerializedName("duration_days")
    val durationDays: Int? = null
)

// =============================================================================
// VIDEO QUALITY ASSESSMENT
// =============================================================================

data class VideoQualityAssessment(
    @SerializedName("recording_quality_score")
    val recordingQualityScore: String,
    
    @SerializedName("technical_issues")
    val technicalIssues: List<String>,
    
    @SerializedName("recommendation_for_rerecording")
    val recommendationForRerecording: Boolean,
    
    @SerializedName("optimal_recording_conditions")
    val optimalRecordingConditions: List<String>
)

// =============================================================================
// SAFETY ASSESSMENT
// =============================================================================

data class SafetyAssessment(
    @SerializedName("roadworthiness")
    val roadworthiness: String, // "SAFE", "CAUTION", "UNSAFE"
    
    @SerializedName("driving_restrictions")
    val drivingRestrictions: List<String>,
    
    @SerializedName("breakdown_probability_next_30_days")
    val breakdownProbabilityNext30Days: Float,
    
    @SerializedName("towing_recommendation")
    val towingRecommendation: Boolean,
    
    @SerializedName("insurance_claim_viability")
    val insuranceClaimViability: String
)

// =============================================================================
// MARKET IMPACT
// =============================================================================

data class MarketImpactVisual(
    @SerializedName("buyer_perception")
    val buyerPerception: String,

    @SerializedName("negotiation_leverage_seller")
    val negotiationLeverageSeller: String,

    @SerializedName("price_reduction_expected_usd")
    val priceReductionExpectedUsd: Double,

    @SerializedName("time_to_sell_estimate_days")
    val timeToSellEstimateDays: Int,

    @SerializedName("disclosure_requirement")
    val disclosureRequirement: String
)

// =============================================================================
// ENVIRONMENTAL COMPLIANCE
// =============================================================================

data class EnvironmentalCompliance(
    @SerializedName("emission_test_pass_probability")
    val emissionTestPassProbability: String,
    
    @SerializedName("pollution_level")
    val pollutionLevel: String,
    
    @SerializedName("controle_technique_impact")
    val controleTechniqueImpact: String,
    
    @SerializedName("vignette_pollution_eligibility")
    val vignettePollutionEligibility: String
)

// =============================================================================
// AI CONFIDENCE
// =============================================================================

data class AutobrainVideoConfidence(
    @SerializedName("ml_kit_accuracy")
    val mlKitAccuracy: String,
    
    @SerializedName("confidence_this_analysis")
    val confidenceThisAnalysis: Float,
    
    @SerializedName("factors_affecting_confidence")
    val factorsAffectingConfidence: List<String>,
    
    @SerializedName("gemini_model")
    val geminiModel: String,
    
    @SerializedName("analysis_timestamp")
    val analysisTimestamp: Long
)

// =============================================================================
// FIRESTORE CONVERSION EXTENSIONS
// =============================================================================

/**
 * Convert ComprehensiveVideoDiagnostic to Firestore Map
 */
fun ComprehensiveVideoDiagnostic.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "enhanced_visual_score" to enhancedVisualScore,
        "smoke_deep_analysis" to mapOf(
            "type_detected" to smokeDeepAnalysis.typeDetected,
            "technical_diagnosis" to smokeDeepAnalysis.technicalDiagnosis,
            "chemical_composition_theory" to smokeDeepAnalysis.chemicalCompositionTheory,
            "emission_pattern" to smokeDeepAnalysis.emissionPattern,
            "smell_prediction" to smokeDeepAnalysis.smellPrediction,
            "color_intensity" to smokeDeepAnalysis.colorIntensity,
            "root_causes_by_probability" to smokeDeepAnalysis.rootCausesByProbability.map { cause ->
                mapOf(
                    "cause" to cause.cause,
                    "probability" to cause.probability,
                    "confirming_tests" to cause.confirmingTests,
                    "repair_complexity" to cause.repairComplexity,
                    "estimated_cost_usd" to cause.estimatedCostUsd
                )
            },
            "worst_case_scenario" to smokeDeepAnalysis.worstCaseScenario,
            "immediate_risks" to smokeDeepAnalysis.immediateRisks
        ),
        "vibration_engineering_analysis" to mapOf(
            "vibration_frequency_estimation" to vibrationEngineeringAnalysis.vibrationFrequencyEstimation,
            "vibration_source_diagnosis" to vibrationEngineeringAnalysis.vibrationSourceDiagnosis,
            "phase_analysis" to vibrationEngineeringAnalysis.phaseAnalysis,
            "probable_mechanical_causes" to vibrationEngineeringAnalysis.probableMechanicalCauses.map { cause ->
                mapOf(
                    "component" to cause.component,
                    "failure_type" to cause.failureType,
                    "diagnostic_test" to cause.diagnosticTest,
                    "replacement_cost_usd" to cause.replacementCostUsd,
                    "urgency" to cause.urgency
                )
            },
            "cascading_failures_if_ignored" to vibrationEngineeringAnalysis.cascadingFailuresIfIgnored
        ),
        "combined_audio_video_diagnosis" to mapOf(
            "correlation_score" to combinedAudioVideoDiagnosis.correlationScore,
            "multimodal_insights" to combinedAudioVideoDiagnosis.multimodalInsights,
            "comprehensive_root_cause" to combinedAudioVideoDiagnosis.comprehensiveRootCause,
            "confidence_boost" to combinedAudioVideoDiagnosis.confidenceBoost
        ),
        "repair_scenarios_visual" to repairScenariosVisual.map { scenario ->
            mapOf(
                "scenario_name" to scenario.scenarioName,
                "applicable_if" to scenario.applicableIf,
                "steps" to scenario.steps,
                "total_cost_usd" to scenario.totalCostUsd,
                "success_probability" to scenario.successProbability,
                "duration_hours" to scenario.durationHours,
                "duration_days" to scenario.durationDays
            )
        },
        "video_quality_assessment" to mapOf(
            "recording_quality_score" to videoQualityAssessment.recordingQualityScore,
            "technical_issues" to videoQualityAssessment.technicalIssues,
            "recommendation_for_rerecording" to videoQualityAssessment.recommendationForRerecording,
            "optimal_recording_conditions" to videoQualityAssessment.optimalRecordingConditions
        ),
        "safety_assessment" to mapOf(
            "roadworthiness" to safetyAssessment.roadworthiness,
            "driving_restrictions" to safetyAssessment.drivingRestrictions,
            "breakdown_probability_next_30_days" to safetyAssessment.breakdownProbabilityNext30Days,
            "towing_recommendation" to safetyAssessment.towingRecommendation,
            "insurance_claim_viability" to safetyAssessment.insuranceClaimViability
        ),
        "market_impact_visual" to mapOf(
            "buyer_perception" to marketImpactVisual.buyerPerception,
            "negotiation_leverage_seller" to marketImpactVisual.negotiationLeverageSeller,
            "price_reduction_expected_usd" to marketImpactVisual.priceReductionExpectedUsd,
            "time_to_sell_estimate_days" to marketImpactVisual.timeToSellEstimateDays,
            "disclosure_requirement" to marketImpactVisual.disclosureRequirement
        ),
        "environmental_compliance" to mapOf(
            "emission_test_pass_probability" to environmentalCompliance.emissionTestPassProbability,
            "pollution_level" to environmentalCompliance.pollutionLevel,
            "controle_technique_impact" to environmentalCompliance.controleTechniqueImpact,
            "vignette_pollution_eligibility" to environmentalCompliance.vignettePollutionEligibility
        ),
        "autobrain_video_confidence" to mapOf(
            "ml_kit_accuracy" to autobrainVideoConfidence.mlKitAccuracy,
            "confidence_this_analysis" to autobrainVideoConfidence.confidenceThisAnalysis,
            "factors_affecting_confidence" to autobrainVideoConfidence.factorsAffectingConfidence,
            "gemini_model" to autobrainVideoConfidence.geminiModel,
            "analysis_timestamp" to autobrainVideoConfidence.analysisTimestamp
        )
    )
}

/**
 * Summary for UI display
 */
fun ComprehensiveVideoDiagnostic.getSummary(): String {
    return """
        Score Visuel: ${enhancedVisualScore}/100
        Fumée: ${smokeDeepAnalysis.typeDetected}
        Sévérité: ${safetyAssessment.roadworthiness}
        Confiance AI: ${(autobrainVideoConfidence.confidenceThisAnalysis * 100).toInt()}%
    """.trimIndent()
}

/**
 * Get most likely repair scenario
 */
fun ComprehensiveVideoDiagnostic.getMostLikelyRepairScenario(): VisualRepairScenario? {
    return repairScenariosVisual.maxByOrNull { it.successProbability }
}

/**
 * Check if safe to drive
 */
fun ComprehensiveVideoDiagnostic.isSafeToDrive(): Boolean {
    return safetyAssessment.roadworthiness != "UNSAFE" && enhancedVisualScore >= 40
}

/**
 * Check if recording requires improvement
 */
fun ComprehensiveVideoDiagnostic.shouldRerecord(): Boolean {
    return videoQualityAssessment.recommendationForRerecording
}
