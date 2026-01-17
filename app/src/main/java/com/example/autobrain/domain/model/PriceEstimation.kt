package com.example.autobrain.domain.model

/**
 * Price Estimation Data Models
 * Used by Price Estimation feature with Gemini AI
 */

data class GeminiPriceEstimation(
    val minPrice: Int,
    val maxPrice: Int,
    val avgPrice: Int,
    val confidence: Float,
    val factors: List<PriceFactor>,
    val marketAnalysis: String,
    val geminiInsights: List<String>,
    val comparableVehicles: List<ComparableVehicle> = emptyList(),
    val depreciationFactors: List<String> = emptyList(),
    val negotiationTips: List<String> = emptyList()
)

data class PriceFactor(
    val name: String,
    val value: String,
    val isPositive: Boolean,
    val impact: String = "" // "High", "Medium", "Low"
)

data class ComparableVehicle(
    val name: String,
    val year: Int,
    val mileage: Int,
    val price: Int,
    val location: String
)
