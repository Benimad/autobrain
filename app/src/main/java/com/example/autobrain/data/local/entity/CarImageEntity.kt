package com.example.autobrain.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching car images locally.
 * Reduces network calls and provides offline support.
 */
@Entity(tableName = "car_images")
data class CarImageEntity(
    @PrimaryKey
    val carKey: String, // Format: "make_model_year" (e.g., "audi_rs6_2024")
    val make: String,
    val model: String,
    val year: Int,
    val imageUrl: String,
    val isTransparent: Boolean = false,
    val source: String, // "freeiconspng", "unsplash", "serper", etc.
    val cachedAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis()
)
