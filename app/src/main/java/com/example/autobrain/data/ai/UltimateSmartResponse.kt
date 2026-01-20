package com.example.autobrain.data.ai

import com.google.gson.annotations.SerializedName

/**
 * AUTOBRAIN ULTIMATE SMART ANALYSIS RESPONSE MODELS
 * Combined Audio + Video + Price + Firestore History
 * 
 * The MOST comprehensive automotive evaluation system
 * 15 sections for complete decision making
 */

/**
 * Ultimate Smart Analysis Report (15 sections)
 */
data class UltimateSmartAnalysis(
    @SerializedName("overall_autobrain_score")
    val overallAutobrainScore: Int,
    
    @SerializedName("score_breakdown")
    val scoreBreakdown: ScoreBreakdown,
    
    @SerializedName("comprehensive_diagnosis")
    val comprehensiveDiagnosis: ComprehensiveDiagnosis,
    
    @SerializedName("total_repair_cost_estimate")
    val totalRepairCostEstimate: TotalRepairCostEstimate,
    
    @SerializedName("realistic_market_value")
    val realisticMarketValue: RealisticMarketValue,
    
    @SerializedName("buyer_decision_matrix")
    val buyerDecisionMatrix: BuyerDecisionMatrix,
    
    @SerializedName("maintenance_quality_report")
    val maintenanceQualityReport: MaintenanceQualityReport,
    
    @SerializedName("risk_assessment")
    val riskAssessment: RiskAssessment,
    
    @SerializedName("timeline_projections")
    val timelineProjections: TimelineProjections,
    
    @SerializedName("legal_compliance_general")
    val legalComplianceGeneral: LegalComplianceStatus,
    
    @SerializedName("environmental_impact")
    val environmentalImpact: EnvironmentalImpact,
    
    @SerializedName("comparable_market_analysis")
    val comparableMarketAnalysis: ComparableMarketAnalysis,
    
    @SerializedName("autobrain_confidence_metrics")
    val autobrainConfidenceMetrics: AutobrainConfidenceMetrics,
    
    @SerializedName("actionable_next_steps")
    val actionableNextSteps: ActionableNextSteps,
    
    @SerializedName("autobrain_premium_insights")
    val autobrainPremiumInsights: AutobrainPremiumInsights
)

// =============================================================================
// SECTION DATA MODELS
// =============================================================================

data class ScoreBreakdown(
    @SerializedName("audio_contribution")
    val audioContribution: Float,
    
    @SerializedName("video_contribution")
    val videoContribution: Float,
    
    @SerializedName("maintenance_contribution")
    val maintenanceContribution: Float,
    
    @SerializedName("market_contribution")
    val marketContribution: Float,
    
    @SerializedName("penalties")
    val penalties: List<String>,
    
    @SerializedName("bonuses")
    val bonuses: List<String>
)

data class ComprehensiveDiagnosis(
    @SerializedName("primary_issue")
    val primaryIssue: String,
    
    @SerializedName("secondary_issues")
    val secondaryIssues: List<String>,
    
    @SerializedName("underlying_root_cause")
    val underlyingRootCause: String,
    
    @SerializedName("issue_timeline")
    val issueTimeline: IssueTimeline
)

data class IssueTimeline(
    @SerializedName("first_symptoms_estimated")
    val firstSymptomsEstimated: String,
    
    @SerializedName("current_stage")
    val currentStage: String,
    
    @SerializedName("without_repair_projection")
    val withoutRepairProjection: String
)

data class TotalRepairCostEstimate(
    @SerializedName("immediate_repairs_usd")
    val immediateRepairsUsd: Double,

    @SerializedName("short_term_repairs_usd")
    val shortTermRepairsUsd: Double,

    @SerializedName("preventive_maintenance_usd")
    val preventiveMaintenanceUsd: Double,

    @SerializedName("total_investment_needed_usd")
    val totalInvestmentNeededUsd: Double,

    @SerializedName("repair_priority_list")
    val repairPriorityList: List<RepairPriority>
)

data class RepairPriority(
    @SerializedName("item")
    val item: String,
    
    @SerializedName("cost")
    val cost: Double,
    
    @SerializedName("urgency")
    val urgency: String
)

data class RealisticMarketValue(
    @SerializedName("perfect_condition_value_usd")
    val perfectConditionValueUsd: Double,

    @SerializedName("current_condition_value_usd")
    val currentConditionValueUsd: Double,

    @SerializedName("after_repairs_value_usd")
    val afterRepairsValueUsd: Double,

    @SerializedName("depreciation_breakdown")
    val depreciationBreakdown: DepreciationBreakdown,

    @SerializedName("market_comparables")
    val marketComparables: List<MarketComparable>,

    @SerializedName("fair_market_range_usd")
    val fairMarketRangeUsd: String,

    @SerializedName("quick_sale_price_usd")
    val quickSalePriceUsd: Double,

    @SerializedName("dealer_trade_in_price_usd")
    val dealerTradeInPriceUsd: Double
)

data class DepreciationBreakdown(
    @SerializedName("age_depreciation")
    val ageDepreciation: String,
    
    @SerializedName("mileage_depreciation")
    val mileageDepreciation: String,
    
    @SerializedName("mechanical_issues")
    val mechanicalIssues: String,
    
    @SerializedName("poor_maintenance")
    val poorMaintenance: String,
    
    @SerializedName("total_depreciation")
    val totalDepreciation: String
)

data class MarketComparable(
    @SerializedName("listing")
    val listing: String,

    @SerializedName("price_usd")
    val priceUsd: Double,

    @SerializedName("location")
    val location: String
)

data class BuyerDecisionMatrix(
    @SerializedName("for_current_owner")
    val forCurrentOwner: OwnerDecision,
    
    @SerializedName("for_potential_buyer")
    val forPotentialBuyer: BuyerDecision
)

data class OwnerDecision(
    @SerializedName("should_repair")
    val shouldRepair: Boolean,
    
    @SerializedName("reasoning")
    val reasoning: String,
    
    @SerializedName("recommended_action")
    val recommendedAction: String,
    
    @SerializedName("negotiation_strategy_if_selling")
    val negotiationStrategyIfSelling: List<String>
)

data class BuyerDecision(
    @SerializedName("buy_recommendation")
    val buyRecommendation: String,

    @SerializedName("max_acceptable_price_usd")
    val maxAcceptablePriceUsd: Double,

    @SerializedName("negotiation_script")
    val negotiationScript: List<String>,

    @SerializedName("investment_viability")
    val investmentViability: InvestmentViability
)

data class InvestmentViability(
    @SerializedName("purchase_price")
    val purchasePrice: Double,
    
    @SerializedName("repair_costs")
    val repairCosts: Double,
    
    @SerializedName("total_investment")
    val totalInvestment: Double,
    
    @SerializedName("market_value_after_repair")
    val marketValueAfterRepair: Double,
    
    @SerializedName("potential_profit_loss")
    val potentialProfitLoss: Double,
    
    @SerializedName("roi_percentage")
    val roiPercentage: Double,
    
    @SerializedName("verdict")
    val verdict: String
)

data class MaintenanceQualityReport(
    @SerializedName("overall_grade")
    val overallGrade: String,
    
    @SerializedName("maintenance_score")
    val maintenanceScore: Int,
    
    @SerializedName("positives")
    val positives: List<String>,
    
    @SerializedName("negatives")
    val negatives: List<String>,
    
    @SerializedName("impact_on_resale")
    val impactOnResale: String,
    
    @SerializedName("missing_critical_services")
    val missingCriticalServices: List<String>,
    
    @SerializedName("recommended_immediate_actions")
    val recommendedImmediateActions: List<String>
)

data class RiskAssessment(
    @SerializedName("financial_risk")
    val financialRisk: String,
    
    @SerializedName("reliability_risk")
    val reliabilityRisk: String,
    
    @SerializedName("safety_risk")
    val safetyRisk: String,
    
    @SerializedName("legal_risk")
    val legalRisk: String,
    
    @SerializedName("total_risk_score")
    val totalRiskScore: Float
)

data class TimelineProjections(
    @SerializedName("if_repaired_now")
    val ifRepairedNow: TimelineScenario,
    
    @SerializedName("if_sold_as_is")
    val ifSoldAsIs: TimelineScenario,
    
    @SerializedName("if_no_action")
    val ifNoAction: NoActionScenario
)

data class TimelineScenario(
    @SerializedName("time_to_sell")
    val timeToSell: String,
    
    @SerializedName("expected_price")
    val expectedPrice: Double,
    
    @SerializedName("total_cost_ownership")
    val totalCostOwnership: Double? = null,
    
    @SerializedName("buyer_profile")
    val buyerProfile: String? = null
)

data class NoActionScenario(
    @SerializedName("breakdown_probability_6_months")
    val breakdownProbability6Months: Float,
    
    @SerializedName("final_scrap_value")
    val finalScrapValue: Double
)

data class LegalComplianceStatus(
    @SerializedName("inspection_status")
    val inspectionStatus: String,
    
    @SerializedName("insurance_validity")
    val insuranceValidity: String,
    
    @SerializedName("registration_status")
    val registrationStatus: String,
    
    @SerializedName("resale_legal_requirements")
    val resaleLegalRequirements: List<String>,
    
    @SerializedName("penalties_if_non_compliant")
    val penaltiesIfNonCompliant: String
)

data class EnvironmentalImpact(
    @SerializedName("emission_level_estimated")
    val emissionLevelEstimated: String,
    
    @SerializedName("pollution_test_result_prediction")
    val pollutionTestResultPrediction: String,
    
    @SerializedName("eligibility_eco_incentives")
    val eligibilityEcoIncentives: Boolean,
    
    @SerializedName("future_resale_impact_pollution")
    val futureResaleImpactPollution: String
)

data class ComparableMarketAnalysis(
    @SerializedName("better_alternatives_same_budget")
    val betterAlternativesSameBudget: List<BetterAlternative>,
    
    @SerializedName("this_vehicle_competitiveness")
    val thisVehicleCompetitiveness: String
)

data class BetterAlternative(
    @SerializedName("vehicle")
    val vehicle: String,

    @SerializedName("price_usd")
    val priceUsd: Double,

    @SerializedName("autobrain_estimated_score")
    val autobrainEstimatedScore: String,

    @SerializedName("why_better")
    val whyBetter: String
)

data class AutobrainConfidenceMetrics(
    @SerializedName("overall_confidence")
    val overallConfidence: Float,
    
    @SerializedName("data_completeness")
    val dataCompleteness: DataCompleteness,
    
    @SerializedName("uncertainty_factors")
    val uncertaintyFactors: List<String>,
    
    @SerializedName("recommend_professional_inspection")
    val recommendProfessionalInspection: Boolean,
    
    @SerializedName("gemini_model")
    val geminiModel: String,
    
    @SerializedName("analysis_date")
    val analysisDate: String
)

data class DataCompleteness(
    @SerializedName("audio_diagnostic")
    val audioDiagnostic: Boolean,
    
    @SerializedName("video_diagnostic")
    val videoDiagnostic: Boolean,
    
    @SerializedName("maintenance_records")
    val maintenanceRecords: Boolean,
    
    @SerializedName("market_data")
    val marketData: Boolean
)

data class ActionableNextSteps(
    @SerializedName("for_owner")
    val forOwner: List<String>,
    
    @SerializedName("for_buyer")
    val forBuyer: List<String>
)

data class AutobrainPremiumInsights(
    @SerializedName("hidden_value_opportunities")
    val hiddenValueOpportunities: List<String>,
    
    @SerializedName("negotiation_leverage_points")
    val negotiationLeveragePoints: List<String>,
    
    @SerializedName("optimal_sale_timing")
    val optimalSaleTiming: String,
    
    @SerializedName("market_trend_prediction_2025")
    val marketTrendPrediction2025: String
)

// =============================================================================
// FIRESTORE CONVERSION
// =============================================================================

fun UltimateSmartAnalysis.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "overall_autobrain_score" to overallAutobrainScore,
        "score_breakdown" to mapOf(
            "audio_contribution" to scoreBreakdown.audioContribution,
            "video_contribution" to scoreBreakdown.videoContribution,
            "maintenance_contribution" to scoreBreakdown.maintenanceContribution,
            "market_contribution" to scoreBreakdown.marketContribution,
            "penalties" to scoreBreakdown.penalties,
            "bonuses" to scoreBreakdown.bonuses
        ),
        "comprehensive_diagnosis" to mapOf(
            "primary_issue" to comprehensiveDiagnosis.primaryIssue,
            "secondary_issues" to comprehensiveDiagnosis.secondaryIssues,
            "underlying_root_cause" to comprehensiveDiagnosis.underlyingRootCause,
            "issue_timeline" to mapOf(
                "first_symptoms_estimated" to comprehensiveDiagnosis.issueTimeline.firstSymptomsEstimated,
                "current_stage" to comprehensiveDiagnosis.issueTimeline.currentStage,
                "without_repair_projection" to comprehensiveDiagnosis.issueTimeline.withoutRepairProjection
            )
        ),
        "total_repair_cost_estimate" to mapOf(
            "immediate_repairs_usd" to totalRepairCostEstimate.immediateRepairsUsd,
            "short_term_repairs_usd" to totalRepairCostEstimate.shortTermRepairsUsd,
            "preventive_maintenance_usd" to totalRepairCostEstimate.preventiveMaintenanceUsd,
            "total_investment_needed_usd" to totalRepairCostEstimate.totalInvestmentNeededUsd,
            "repair_priority_list" to totalRepairCostEstimate.repairPriorityList.map { priority ->
                mapOf(
                    "item" to priority.item,
                    "cost" to priority.cost,
                    "urgency" to priority.urgency
                )
            }
        ),
        "realistic_market_value" to mapOf(
            "perfect_condition_value_usd" to realisticMarketValue.perfectConditionValueUsd,
            "current_condition_value_usd" to realisticMarketValue.currentConditionValueUsd,
            "after_repairs_value_usd" to realisticMarketValue.afterRepairsValueUsd,
            "depreciation_breakdown" to mapOf(
                "age_depreciation" to realisticMarketValue.depreciationBreakdown.ageDepreciation,
                "mileage_depreciation" to realisticMarketValue.depreciationBreakdown.mileageDepreciation,
                "mechanical_issues" to realisticMarketValue.depreciationBreakdown.mechanicalIssues,
                "poor_maintenance" to realisticMarketValue.depreciationBreakdown.poorMaintenance,
                "total_depreciation" to realisticMarketValue.depreciationBreakdown.totalDepreciation
            ),
            "market_comparables" to realisticMarketValue.marketComparables.map { comp ->
                mapOf(
                    "listing" to comp.listing,
                    "price_usd" to comp.priceUsd,
                    "location" to comp.location
                )
            },
            "fair_market_range_usd" to realisticMarketValue.fairMarketRangeUsd,
            "quick_sale_price_usd" to realisticMarketValue.quickSalePriceUsd,
            "dealer_trade_in_price_usd" to realisticMarketValue.dealerTradeInPriceUsd
        ),
        "buyer_decision_matrix" to mapOf(
            "for_current_owner" to mapOf(
                "should_repair" to buyerDecisionMatrix.forCurrentOwner.shouldRepair,
                "reasoning" to buyerDecisionMatrix.forCurrentOwner.reasoning,
                "recommended_action" to buyerDecisionMatrix.forCurrentOwner.recommendedAction,
                "negotiation_strategy_if_selling" to buyerDecisionMatrix.forCurrentOwner.negotiationStrategyIfSelling
            ),
            "for_potential_buyer" to mapOf(
                "buy_recommendation" to buyerDecisionMatrix.forPotentialBuyer.buyRecommendation,
                "max_acceptable_price_usd" to buyerDecisionMatrix.forPotentialBuyer.maxAcceptablePriceUsd,
                "negotiation_script" to buyerDecisionMatrix.forPotentialBuyer.negotiationScript,
                "investment_viability" to mapOf(
                    "purchase_price" to buyerDecisionMatrix.forPotentialBuyer.investmentViability.purchasePrice,
                    "repair_costs" to buyerDecisionMatrix.forPotentialBuyer.investmentViability.repairCosts,
                    "total_investment" to buyerDecisionMatrix.forPotentialBuyer.investmentViability.totalInvestment,
                    "market_value_after_repair" to buyerDecisionMatrix.forPotentialBuyer.investmentViability.marketValueAfterRepair,
                    "potential_profit_loss" to buyerDecisionMatrix.forPotentialBuyer.investmentViability.potentialProfitLoss,
                    "roi_percentage" to buyerDecisionMatrix.forPotentialBuyer.investmentViability.roiPercentage,
                    "verdict" to buyerDecisionMatrix.forPotentialBuyer.investmentViability.verdict
                )
            )
        ),
        // Add remaining sections...
        "maintenance_quality_report" to mapOf(
            "overall_grade" to maintenanceQualityReport.overallGrade,
            "maintenance_score" to maintenanceQualityReport.maintenanceScore,
            "positives" to maintenanceQualityReport.positives,
            "negatives" to maintenanceQualityReport.negatives,
            "impact_on_resale" to maintenanceQualityReport.impactOnResale,
            "missing_critical_services" to maintenanceQualityReport.missingCriticalServices,
            "recommended_immediate_actions" to maintenanceQualityReport.recommendedImmediateActions
        ),
        "risk_assessment" to mapOf<String, Any>(
            "financial_risk" to riskAssessment.financialRisk,
            "reliability_risk" to riskAssessment.reliabilityRisk,
            "safety_risk" to riskAssessment.safetyRisk,
            "legal_risk" to riskAssessment.legalRisk,
            "total_risk_score" to riskAssessment.totalRiskScore
        ),
        "timeline_projections" to mapOf(
            "if_repaired_now" to mapOf(
                "time_to_sell" to timelineProjections.ifRepairedNow.timeToSell,
                "expected_price" to timelineProjections.ifRepairedNow.expectedPrice,
                "total_cost_ownership" to timelineProjections.ifRepairedNow.totalCostOwnership
            ),
            "if_sold_as_is" to mapOf(
                "time_to_sell" to timelineProjections.ifSoldAsIs.timeToSell,
                "expected_price" to timelineProjections.ifSoldAsIs.expectedPrice,
                "buyer_profile" to timelineProjections.ifSoldAsIs.buyerProfile
            ),
            "if_no_action" to mapOf(
                "breakdown_probability_6_months" to timelineProjections.ifNoAction.breakdownProbability6Months,
                "final_scrap_value" to timelineProjections.ifNoAction.finalScrapValue
            )
        ),
        "legal_compliance_general" to mapOf(
            "inspection_status" to legalComplianceGeneral.inspectionStatus,
            "insurance_validity" to legalComplianceGeneral.insuranceValidity,
            "registration_status" to legalComplianceGeneral.registrationStatus,
            "resale_legal_requirements" to legalComplianceGeneral.resaleLegalRequirements,
            "penalties_if_non_compliant" to legalComplianceGeneral.penaltiesIfNonCompliant
        ),
        "environmental_impact" to mapOf(
            "emission_level_estimated" to environmentalImpact.emissionLevelEstimated,
            "pollution_test_result_prediction" to environmentalImpact.pollutionTestResultPrediction,
            "eligibility_eco_incentives" to environmentalImpact.eligibilityEcoIncentives,
            "future_resale_impact_pollution" to environmentalImpact.futureResaleImpactPollution
        ),
        "comparable_market_analysis" to mapOf(
            "better_alternatives_same_budget" to comparableMarketAnalysis.betterAlternativesSameBudget.map { alt ->
                mapOf(
                    "vehicle" to alt.vehicle,
                    "price_usd" to alt.priceUsd,
                    "autobrain_estimated_score" to alt.autobrainEstimatedScore,
                    "why_better" to alt.whyBetter
                )
            },
            "this_vehicle_competitiveness" to comparableMarketAnalysis.thisVehicleCompetitiveness
        ),
        "autobrain_confidence_metrics" to mapOf(
            "overall_confidence" to autobrainConfidenceMetrics.overallConfidence,
            "data_completeness" to mapOf(
                "audio_diagnostic" to autobrainConfidenceMetrics.dataCompleteness.audioDiagnostic,
                "video_diagnostic" to autobrainConfidenceMetrics.dataCompleteness.videoDiagnostic,
                "maintenance_records" to autobrainConfidenceMetrics.dataCompleteness.maintenanceRecords,
                "market_data" to autobrainConfidenceMetrics.dataCompleteness.marketData
            ),
            "uncertainty_factors" to autobrainConfidenceMetrics.uncertaintyFactors,
            "recommend_professional_inspection" to autobrainConfidenceMetrics.recommendProfessionalInspection,
            "gemini_model" to autobrainConfidenceMetrics.geminiModel,
            "analysis_date" to autobrainConfidenceMetrics.analysisDate
        ),
        "actionable_next_steps" to mapOf(
            "for_owner" to actionableNextSteps.forOwner,
            "for_buyer" to actionableNextSteps.forBuyer
        ),
        "autobrain_premium_insights" to mapOf(
            "hidden_value_opportunities" to autobrainPremiumInsights.hiddenValueOpportunities,
            "negotiation_leverage_points" to autobrainPremiumInsights.negotiationLeveragePoints,
            "optimal_sale_timing" to autobrainPremiumInsights.optimalSaleTiming,
            "market_trend_prediction_2025" to autobrainPremiumInsights.marketTrendPrediction2025
        )
    )
}

// =============================================================================
// HELPER EXTENSIONS
// =============================================================================

/**
 * Get overall recommendation (buy/sell/repair)
 */
fun UltimateSmartAnalysis.getOverallRecommendation(): String {
    return when {
        overallAutobrainScore >= 80 -> "Excellent vehicle - Buy or keep"
        overallAutobrainScore >= 60 -> "Good vehicle with maintenance"
        overallAutobrainScore >= 40 -> "Requires significant repairs"
        else -> "Critical condition - Avoid or negotiate heavily"
    }
}

/**
 * Is this a good investment?
 */
fun UltimateSmartAnalysis.isGoodInvestment(): Boolean {
    return buyerDecisionMatrix.forPotentialBuyer.investmentViability.roiPercentage > 10.0
}

/**
 * Should current owner repair or sell as-is?
 */
fun UltimateSmartAnalysis.shouldOwnerRepair(): Boolean {
    return buyerDecisionMatrix.forCurrentOwner.shouldRepair
}
