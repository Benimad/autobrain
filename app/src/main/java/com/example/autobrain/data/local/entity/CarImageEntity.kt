package com.example.autobrain.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching car images locally.
 * User-specific caching to ensure each user gets their personalized car image.
 */
@Entity(tableName = "car_images")
data class CarImageEntity(
    @PrimaryKey
    val carKey: String, // Format: "userId_make_model_year" (e.g., "user123_audi_rs6_2024")
    val userId: String, // User ID to make cache user-specific
    val make: String,
    val model: String,
    val year: Int,
    val imageUrl: String,
    val isTransparent: Boolean = false,
    val source: String, // "gemini+firebase", "gemini+stock", etc.
    val cacheVersion: Int = 1, // Cache schema version for invalidation
    val cachedAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis()
)
