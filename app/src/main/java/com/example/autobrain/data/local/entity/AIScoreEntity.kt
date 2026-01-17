package com.example.autobrain.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.autobrain.data.local.converter.ListStringConverter

/**
 * Room Entity for AI Score history
 * Stores car condition assessments (0-100 score)
 */
@Entity(tableName = "ai_scores")
@TypeConverters(ListStringConverter::class)
data class AIScoreEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val carId: String,
    val score: Int, // 0-100
    val condition: String, // Excellent, Good, Fair, Poor, Critical
    val riskLevel: String, // Low, Medium, High

    // Score breakdown
    val engineScore: Int = 0,
    val transmissionScore: Int = 0,
    val chassisScore: Int = 0,
    val electricalScore: Int = 0,
    val bodyScore: Int = 0,

    // Analysis details
    val observations: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val redFlags: List<String> = emptyList(),

    // AI confidence
    val confidence: Float = 0f, // 0.0 to 1.0

    // Car info at time of scan
    val carMake: String = "",
    val carModel: String = "",
    val carYear: Int = 0,
    val mileage: Int = 0,

    // Metadata
    val analysisType: String = "FULL_SCAN", // FULL_SCAN, ENGINE_SOUND, VIDEO_ANALYSIS
    val audioUrl: String = "",
    val videoUrl: String = "",
    val imageUrls: List<String> = emptyList(),

    // Sync status
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Domain model for AI Score
 */
data class AIScore(
    val id: String = "",
    val userId: String = "",
    val carId: String = "",
    val score: Int = 0,
    val condition: String = "",
    val riskLevel: String = "",
    val engineScore: Int = 0,
    val transmissionScore: Int = 0,
    val chassisScore: Int = 0,
    val electricalScore: Int = 0,
    val bodyScore: Int = 0,
    val observations: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val redFlags: List<String> = emptyList(),
    val confidence: Float = 0f,
    val carMake: String = "",
    val carModel: String = "",
    val carYear: Int = 0,
    val mileage: Int = 0,
    val analysisType: String = "FULL_SCAN",
    val audioUrl: String = "",
    val videoUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

// Extension functions for conversion
fun AIScoreEntity.toDomain(): AIScore {
    return AIScore(
        id = id,
        userId = userId,
        carId = carId,
        score = score,
        condition = condition,
        riskLevel = riskLevel,
        engineScore = engineScore,
        transmissionScore = transmissionScore,
        chassisScore = chassisScore,
        electricalScore = electricalScore,
        bodyScore = bodyScore,
        observations = observations,
        recommendations = recommendations,
        redFlags = redFlags,
        confidence = confidence,
        carMake = carMake,
        carModel = carModel,
        carYear = carYear,
        mileage = mileage,
        analysisType = analysisType,
        audioUrl = audioUrl,
        videoUrl = videoUrl,
        imageUrls = imageUrls,
        createdAt = createdAt
    )
}

fun AIScore.toAIScoreEntity(isSynced: Boolean = false): AIScoreEntity {
    return AIScoreEntity(
        id = id,
        userId = userId,
        carId = carId,
        score = score,
        condition = condition,
        riskLevel = riskLevel,
        engineScore = engineScore,
        transmissionScore = transmissionScore,
        chassisScore = chassisScore,
        electricalScore = electricalScore,
        bodyScore = bodyScore,
        observations = observations,
        recommendations = recommendations,
        redFlags = redFlags,
        confidence = confidence,
        carMake = carMake,
        carModel = carModel,
        carYear = carYear,
        mileage = mileage,
        analysisType = analysisType,
        audioUrl = audioUrl,
        videoUrl = videoUrl,
        imageUrls = imageUrls,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
}

// Firestore document conversion
fun AIScore.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "userId" to userId,
        "carId" to carId,
        "score" to score,
        "condition" to condition,
        "riskLevel" to riskLevel,
        "engineScore" to engineScore,
        "transmissionScore" to transmissionScore,
        "chassisScore" to chassisScore,
        "electricalScore" to electricalScore,
        "bodyScore" to bodyScore,
        "observations" to observations,
        "recommendations" to recommendations,
        "redFlags" to redFlags,
        "confidence" to confidence,
        "carMake" to carMake,
        "carModel" to carModel,
        "carYear" to carYear,
        "mileage" to mileage,
        "analysisType" to analysisType,
        "audioUrl" to audioUrl,
        "videoUrl" to videoUrl,
        "imageUrls" to imageUrls,
        "createdAt" to createdAt
    )
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toAIScore(): AIScore {
    return AIScore(
        id = this["id"] as? String ?: "",
        userId = this["userId"] as? String ?: "",
        carId = this["carId"] as? String ?: "",
        score = (this["score"] as? Long)?.toInt() ?: 0,
        condition = this["condition"] as? String ?: "",
        riskLevel = this["riskLevel"] as? String ?: "",
        engineScore = (this["engineScore"] as? Long)?.toInt() ?: 0,
        transmissionScore = (this["transmissionScore"] as? Long)?.toInt() ?: 0,
        chassisScore = (this["chassisScore"] as? Long)?.toInt() ?: 0,
        electricalScore = (this["electricalScore"] as? Long)?.toInt() ?: 0,
        bodyScore = (this["bodyScore"] as? Long)?.toInt() ?: 0,
        observations = this["observations"] as? List<String> ?: emptyList(),
        recommendations = this["recommendations"] as? List<String> ?: emptyList(),
        redFlags = this["redFlags"] as? List<String> ?: emptyList(),
        confidence = (this["confidence"] as? Double)?.toFloat() ?: 0f,
        carMake = this["carMake"] as? String ?: "",
        carModel = this["carModel"] as? String ?: "",
        carYear = (this["carYear"] as? Long)?.toInt() ?: 0,
        mileage = (this["mileage"] as? Long)?.toInt() ?: 0,
        analysisType = this["analysisType"] as? String ?: "FULL_SCAN",
        audioUrl = this["audioUrl"] as? String ?: "",
        videoUrl = this["videoUrl"] as? String ?: "",
        imageUrls = this["imageUrls"] as? List<String> ?: emptyList(),
        createdAt = this["createdAt"] as? Long ?: System.currentTimeMillis()
    )
}
