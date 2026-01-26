package com.example.autobrain.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks which image fetch strategies work best for specific car makes/models
 * Enables intelligent strategy selection and early exit on known failures
 */
@Entity(tableName = "image_fetch_strategies")
data class ImageFetchStrategyEntity(
    @PrimaryKey
    val makeModel: String, // "bmw_3series"
    val successfulStrategy: String, // "fallback_unsplash", "gemini_url", etc.
    val successRate: Float, // 0.0 to 1.0
    val totalAttempts: Int,
    val successfulAttempts: Int,
    val avgFetchTimeMs: Long,
    val lastSuccessTimestamp: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)
