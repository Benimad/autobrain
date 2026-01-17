package com.example.autobrain.data.ai

import com.google.gson.annotations.SerializedName

/**
 * AUTOBRAIN - COMPREHENSIVE GEMINI AI RESPONSE MODELS
 * Data classes for parsing complete Gemini 2.5 Pro JSON responses
 * 
 * This matches the exact JSON structure expected from the comprehensive prompt
 */

/**
 * Complete Gemini AI Audio Diagnostic Response
 */
data class ComprehensiveAudioDiagnostic(
    @SerializedName("enhanced_health_score")
    val enhancedHealthScore: Int,
    
    @SerializedName("primary_diagnosis")
    val primaryDiagnosis: PrimaryDiagnosis,
    
    @SerializedName("secondary_issues")
    val secondaryIssues: List<SecondaryIssue>,
    
    @SerializedName("root_cause_analysis")
    val rootCauseAnalysis: RootCauseAnalysis,
    
    @SerializedName("progressive_damage_prediction")
    val progressiveDamagePrediction: ProgressiveDamagePrediction,
    
    @SerializedName("detailed_repair_plan")
    val detailedRepairPlan: DetailedRepairPlan,
    
    @SerializedName("market_value_impact")
    val marketValueImpact: MarketValueImpact,
    
    @SerializedName("maintenance_correlation")
    val maintenanceCorrelation: MaintenanceCorrelation,
    
    @SerializedName("intelligent_recommendations")
    val intelligentRecommendations: IntelligentRecommendations,
    
    @SerializedName("autobrain_ai_confidence")
    val autobrainAiConfidence: AutobrainAiConfidence,
    
    @SerializedName("legal_compliance_morocco")
    val legalComplianceGeneral: LegalComplianceGeneral
)

// =============================================================================
// PRIMARY DIAGNOSIS
// =============================================================================

data class PrimaryDiagnosis(
    @SerializedName("issue")
    val issue: String,
    
    @SerializedName("technical_name")
    val technicalName: String,
    
    @SerializedName("confidence")
    val confidence: Float,
    
    @SerializedName("severity")
    val severity: String, // "CRITICAL", "HIGH", "MEDIUM", "LOW"
    
    @SerializedName("affected_components")
    val affectedComponents: List<String>
)

// =============================================================================
// SECONDARY ISSUES
// =============================================================================

data class SecondaryIssue(
    @SerializedName("issue")
    val issue: String,
    
    @SerializedName("confidence")
    val confidence: Float,
    
    @SerializedName("severity")
    val severity: String,
    
    @SerializedName("components")
    val components: List<String>
)

// =============================================================================
// ROOT CAUSE ANALYSIS
// =============================================================================

data class RootCauseAnalysis(
    @SerializedName("most_likely_cause")
    val mostLikelyCause: String,
    
    @SerializedName("probability")
    val probability: Float,
    
    @SerializedName("alternative_causes")
    val alternativeCauses: List<String>,
    
    @SerializedName("evidence")
    val evidence: List<String>
)

// =============================================================================
// PROGRESSIVE DAMAGE PREDICTION
// =============================================================================

data class ProgressiveDamagePrediction(
    @SerializedName("current_stage")
    val currentStage: String,
    
    @SerializedName("next_failure_timeline")
    val nextFailureTimeline: String,
    
    @SerializedName("final_failure_description")
    val finalFailureDescription: String,
    
    @SerializedName("cascading_failures")
    val cascadingFailures: List<String>
)

// =============================================================================
// DETAILED REPAIR PLAN
// =============================================================================

data class DetailedRepairPlan(
    @SerializedName("immediate_actions")
    val immediateActions: List<String>,
    
    @SerializedName("repair_scenarios")
    val repairScenarios: List<RepairScenario>,
    
    @SerializedName("recommended_garage_type")
    val recommendedGarageType: String,
    
    @SerializedName("negotiation_tip")
    val negotiationTip: String
)

data class RepairScenario(
    @SerializedName("scenario")
    val scenario: String,
    
    @SerializedName("steps")
    val steps: List<String>,
    
    @SerializedName("parts_cost_dh")
    val partsCostDh: Double,
    
    @SerializedName("labor_cost_dh")
    val laborCostDh: Double,
    
    @SerializedName("total_cost_dh")
    val totalCostDh: Double,
    
    @SerializedName("duration_days")
    val durationDays: Int,
    
    @SerializedName("probability")
    val probability: Float
)

// =============================================================================
// MARKET VALUE IMPACT
// =============================================================================

data class MarketValueImpact(
    @SerializedName("value_before_issue")
    val valueBeforeIssue: Double,
    
    @SerializedName("value_after_repair")
    val valueAfterRepair: Double,
    
    @SerializedName("value_as_is")
    val valueAsIs: Double,
    
    @SerializedName("depreciation_factors")
    val depreciationFactors: List<String>,
    
    @SerializedName("resale_timeline")
    val resaleTimeline: String,
    
    @SerializedName("buyer_negotiation_power")
    val buyerNegotiationPower: String
)

// =============================================================================
// MAINTENANCE CORRELATION
// =============================================================================

data class MaintenanceCorrelation(
    @SerializedName("oil_change_impact")
    val oilChangeImpact: String,
    
    @SerializedName("mileage_factor")
    val mileageFactor: String,
    
    @SerializedName("service_history_quality")
    val serviceHistoryQuality: String,
    
    @SerializedName("preventable_percentage")
    val preventablePercentage: Int,
    
    @SerializedName("lessons_learned")
    val lessonsLearned: List<String>
)

// =============================================================================
// INTELLIGENT RECOMMENDATIONS
// =============================================================================

data class IntelligentRecommendations(
    @SerializedName("for_current_owner")
    val forCurrentOwner: List<String>,
    
    @SerializedName("for_potential_buyer")
    val forPotentialBuyer: List<String>,
    
    @SerializedName("for_mechanic")
    val forMechanic: List<String>
)

// =============================================================================
// AUTOBRAIN AI CONFIDENCE
// =============================================================================

data class AutobrainAiConfidence(
    @SerializedName("analysis_confidence")
    val analysisConfidence: Float,
    
    @SerializedName("data_quality_score")
    val dataQualityScore: Float,
    
    @SerializedName("tflite_model_accuracy")
    val tfliteModelAccuracy: String,
    
    @SerializedName("factors_boosting_confidence")
    val factorsBoostingConfidence: List<String>,
    
    @SerializedName("uncertainty_factors")
    val uncertaintyFactors: List<String>,
    
    @SerializedName("recommend_second_opinion")
    val recommendSecondOpinion: Boolean,
    
    @SerializedName("gemini_model_version")
    val geminiModelVersion: String,
    
    @SerializedName("analysis_timestamp_utc")
    val analysisTimestampUtc: String
)

// =============================================================================
// LEGAL COMPLIANCE GENERAL
// =============================================================================

data class LegalComplianceGeneral(
    @SerializedName("inspection_requirements")
    val inspectionRequirements: String,
    
    @SerializedName("insurance_notification_required")
    val insuranceNotificationRequired: Boolean,
    
    @SerializedName("roadworthiness")
    val roadworthiness: String,
    
    @SerializedName("legal_resale_obligations")
    val legalResaleObligations: List<String>
)

// =============================================================================
// FIRESTORE CONVERSION EXTENSIONS
// =============================================================================

/**
 * Convert ComprehensiveAudioDiagnostic to Firestore Map
 */
fun ComprehensiveAudioDiagnostic.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "enhanced_health_score" to enhancedHealthScore,
        "primary_diagnosis" to mapOf(
            "issue" to primaryDiagnosis.issue,
            "technical_name" to primaryDiagnosis.technicalName,
            "confidence" to primaryDiagnosis.confidence,
            "severity" to primaryDiagnosis.severity,
            "affected_components" to primaryDiagnosis.affectedComponents
        ),
        "secondary_issues" to secondaryIssues.map { issue ->
            mapOf(
                "issue" to issue.issue,
                "confidence" to issue.confidence,
                "severity" to issue.severity,
                "components" to issue.components
            )
        },
        "root_cause_analysis" to mapOf(
            "most_likely_cause" to rootCauseAnalysis.mostLikelyCause,
            "probability" to rootCauseAnalysis.probability,
            "alternative_causes" to rootCauseAnalysis.alternativeCauses,
            "evidence" to rootCauseAnalysis.evidence
        ),
        "progressive_damage_prediction" to mapOf(
            "current_stage" to progressiveDamagePrediction.currentStage,
            "next_failure_timeline" to progressiveDamagePrediction.nextFailureTimeline,
            "final_failure_description" to progressiveDamagePrediction.finalFailureDescription,
            "cascading_failures" to progressiveDamagePrediction.cascadingFailures
        ),
        "detailed_repair_plan" to mapOf(
            "immediate_actions" to detailedRepairPlan.immediateActions,
            "repair_scenarios" to detailedRepairPlan.repairScenarios.map { scenario ->
                mapOf(
                    "scenario" to scenario.scenario,
                    "steps" to scenario.steps,
                    "parts_cost_dh" to scenario.partsCostDh,
                    "labor_cost_dh" to scenario.laborCostDh,
                    "total_cost_dh" to scenario.totalCostDh,
                    "duration_days" to scenario.durationDays,
                    "probability" to scenario.probability
                )
            },
            "recommended_garage_type" to detailedRepairPlan.recommendedGarageType,
            "negotiation_tip" to detailedRepairPlan.negotiationTip
        ),
        "market_value_impact" to mapOf(
            "value_before_issue" to marketValueImpact.valueBeforeIssue,
            "value_after_repair" to marketValueImpact.valueAfterRepair,
            "value_as_is" to marketValueImpact.valueAsIs,
            "depreciation_factors" to marketValueImpact.depreciationFactors,
            "resale_timeline" to marketValueImpact.resaleTimeline,
            "buyer_negotiation_power" to marketValueImpact.buyerNegotiationPower
        ),
        "maintenance_correlation" to mapOf(
            "oil_change_impact" to maintenanceCorrelation.oilChangeImpact,
            "mileage_factor" to maintenanceCorrelation.mileageFactor,
            "service_history_quality" to maintenanceCorrelation.serviceHistoryQuality,
            "preventable_percentage" to maintenanceCorrelation.preventablePercentage,
            "lessons_learned" to maintenanceCorrelation.lessonsLearned
        ),
        "intelligent_recommendations" to mapOf(
            "for_current_owner" to intelligentRecommendations.forCurrentOwner,
            "for_potential_buyer" to intelligentRecommendations.forPotentialBuyer,
            "for_mechanic" to intelligentRecommendations.forMechanic
        ),
        "autobrain_ai_confidence" to mapOf(
            "analysis_confidence" to autobrainAiConfidence.analysisConfidence,
            "data_quality_score" to autobrainAiConfidence.dataQualityScore,
            "tflite_model_accuracy" to autobrainAiConfidence.tfliteModelAccuracy,
            "factors_boosting_confidence" to autobrainAiConfidence.factorsBoostingConfidence,
            "uncertainty_factors" to autobrainAiConfidence.uncertaintyFactors,
            "recommend_second_opinion" to autobrainAiConfidence.recommendSecondOpinion,
            "gemini_model_version" to autobrainAiConfidence.geminiModelVersion,
            "analysis_timestamp_utc" to autobrainAiConfidence.analysisTimestampUtc
        ),
        "legal_compliance_general" to mapOf(
            "inspection_requirements" to legalComplianceGeneral.inspectionRequirements,
            "insurance_notification_required" to legalComplianceGeneral.insuranceNotificationRequired,
            "roadworthiness" to legalComplianceGeneral.roadworthiness,
            "legal_resale_obligations" to legalComplianceGeneral.legalResaleObligations
        )
    )
}

/**
 * Summary for UI display
 */
fun ComprehensiveAudioDiagnostic.getSummary(): String {
    return """
        Health Score: ${enhancedHealthScore}/100
        Primary Diagnosis: ${primaryDiagnosis.issue}
        Severity: ${primaryDiagnosis.severity}
        AI Confidence: ${(autobrainAiConfidence.analysisConfidence * 100).toInt()}%
    """.trimIndent()
}

/**
 * Get most likely repair cost
 */
fun ComprehensiveAudioDiagnostic.getMostLikelyRepairCost(): RepairScenario? {
    return detailedRepairPlan.repairScenarios.maxByOrNull { it.probability }
}

/**
 * Check if driving is safe
 */
fun ComprehensiveAudioDiagnostic.isSafeToDrive(): Boolean {
    return primaryDiagnosis.severity != "CRITICAL" && enhancedHealthScore >= 50
}
