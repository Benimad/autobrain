package com.example.autobrain.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.autobrain.data.local.converter.ListStringConverter

/**
 * Audio Diagnostic Entity for Room Database
 * Stores complete audio diagnostic results locally
 */
@Entity(tableName = "audio_diagnostics")
@TypeConverters(ListStringConverter::class)
data class AudioDiagnosticEntity(
    @PrimaryKey
    val id: String,
    
    // User & Car Info
    val userId: String,
    val carId: String,
    
    // Audio Data
    val audioFilePath: String,
    val audioUrl: String = "", // Firestore URL after upload
    val durationMs: Int,
    
    // Classification Results
    val topSoundLabel: String,
    val topSoundConfidence: Float,
    val allDetectedSounds: List<String>, // JSON: ["knocking:0.8", "rattling:0.6"]
    
    // Scoring
    val rawScore: Int,
    val normalizedScore: Float,
    val healthStatus: String, // "Excellent", "Good", "Acceptable", "Poor", "Critical"
    val urgencyLevel: String, // "NONE", "LOW", "MEDIUM", "HIGH", "CRITICAL"
    
    // Issues & Recommendations
    val detectedIssues: List<String>, // JSON array of issues
    val recommendations: List<String>, // JSON array of recommendations
    val criticalWarning: String = "",
    
    // Cost Estimates
    val minRepairCost: Double,
    val maxRepairCost: Double,
    
    // Maintenance Integration
    val maintenancePenalty: Float,
    val overdueServices: List<String>,
    
    // Sync Status
    val isSynced: Boolean = false,
    val syncAttempts: Int = 0,
    val lastSyncAttempt: Long = 0,
    val syncError: String? = null,
    val localModifiedAt: Long = System.currentTimeMillis(),
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Domain model for Audio Diagnostic
 */
data class AudioDiagnosticData(
    val id: String = "",
    val userId: String = "",
    val carId: String = "",
    val audioFilePath: String = "",
    val audioUrl: String = "",
    val durationMs: Int = 0,
    val topSoundLabel: String = "",
    val topSoundConfidence: Float = 0f,
    val allDetectedSounds: Map<String, Float> = emptyMap(),
    val rawScore: Int = 0,
    val normalizedScore: Float = 0f,
    val healthStatus: String = "",
    val urgencyLevel: String = "",
    val detectedIssues: List<IssueData> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val criticalWarning: String = "",
    val minRepairCost: Double = 0.0,
    val maxRepairCost: Double = 0.0,
    val maintenancePenalty: Float = 0f,
    val overdueServices: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class IssueData(
    val soundType: String,
    val confidence: Float,
    val severity: String,
    val description: String,
    val minCost: Double,
    val maxCost: Double
)

// =============================================================================
// CONVERTERS
// =============================================================================

fun AudioDiagnosticEntity.toDomain(): AudioDiagnosticData {
    return AudioDiagnosticData(
        id = id,
        userId = userId,
        carId = carId,
        audioFilePath = audioFilePath,
        audioUrl = audioUrl,
        durationMs = durationMs,
        topSoundLabel = topSoundLabel,
        topSoundConfidence = topSoundConfidence,
        allDetectedSounds = parseDetectedSounds(allDetectedSounds),
        rawScore = rawScore,
        normalizedScore = normalizedScore,
        healthStatus = healthStatus,
        urgencyLevel = urgencyLevel,
        detectedIssues = parseIssues(detectedIssues),
        recommendations = recommendations,
        criticalWarning = criticalWarning,
        minRepairCost = minRepairCost,
        maxRepairCost = maxRepairCost,
        maintenancePenalty = maintenancePenalty,
        overdueServices = overdueServices,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AudioDiagnosticData.toEntity(isSynced: Boolean = false): AudioDiagnosticEntity {
    return AudioDiagnosticEntity(
        id = id,
        userId = userId,
        carId = carId,
        audioFilePath = audioFilePath,
        audioUrl = audioUrl,
        durationMs = durationMs,
        topSoundLabel = topSoundLabel,
        topSoundConfidence = topSoundConfidence,
        allDetectedSounds = formatDetectedSounds(allDetectedSounds),
        rawScore = rawScore,
        normalizedScore = normalizedScore,
        healthStatus = healthStatus,
        urgencyLevel = urgencyLevel,
        detectedIssues = formatIssues(detectedIssues),
        recommendations = recommendations,
        criticalWarning = criticalWarning,
        minRepairCost = minRepairCost,
        maxRepairCost = maxRepairCost,
        maintenancePenalty = maintenancePenalty,
        overdueServices = overdueServices,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
}

fun AudioDiagnosticData.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "userId" to userId,
        "carId" to carId,
        "audioUrl" to audioUrl,
        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),  // Use server timestamp
        "durationMs" to durationMs,
        "topSoundLabel" to topSoundLabel,
        "topSoundConfidence" to topSoundConfidence,
        "allDetectedSounds" to allDetectedSounds,
        "rawScore" to rawScore,
        "normalizedScore" to normalizedScore,
        "healthStatus" to healthStatus,
        "urgencyLevel" to urgencyLevel,
        "detectedIssues" to detectedIssues.map { issue ->
            mapOf(
                "soundType" to issue.soundType,
                "confidence" to issue.confidence,
                "severity" to issue.severity,
                "description" to issue.description,
                "minCost" to issue.minCost,
                "maxCost" to issue.maxCost
            )
        },
        "recommendations" to recommendations,
        "criticalWarning" to criticalWarning,
        "minRepairCost" to minRepairCost,
        "maxRepairCost" to maxRepairCost,
        "maintenancePenalty" to maintenancePenalty,
        "overdueServices" to overdueServices,
        "createdAt" to createdAt
    )
}

// =============================================================================
// HELPER FUNCTIONS
// =============================================================================

private fun parseDetectedSounds(sounds: List<String>): Map<String, Float> {
    return sounds.mapNotNull { entry ->
        val parts = entry.split(":")
        if (parts.size == 2) {
            val confidence = parts[1].toFloatOrNull() ?: 0f
            parts[0] to confidence
        } else {
            null
        }
    }.toMap()
}

private fun formatDetectedSounds(sounds: Map<String, Float>): List<String> {
    return sounds.map { (label, confidence) ->
        "$label:$confidence"
    }
}

private fun parseIssues(issues: List<String>): List<IssueData> {
    return issues.mapNotNull { json ->
        try {
            // Simple parsing: "soundType|confidence|severity|description|minCost|maxCost"
            val parts = json.split("|")
            if (parts.size == 6) {
                IssueData(
                    soundType = parts[0],
                    confidence = parts[1].toFloat(),
                    severity = parts[2],
                    description = parts[3],
                    minCost = parts[4].toDouble(),
                    maxCost = parts[5].toDouble()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

private fun formatIssues(issues: List<IssueData>): List<String> {
    return issues.map { issue ->
        "${issue.soundType}|${issue.confidence}|${issue.severity}|${issue.description}|${issue.minCost}|${issue.maxCost}"
    }
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toAudioDiagnosticData(): AudioDiagnosticData {
    val detectedSoundsMap = this["allDetectedSounds"] as? Map<String, Double>
    val issuesList = this["detectedIssues"] as? List<Map<String, Any?>>
    
    return AudioDiagnosticData(
        id = this["id"] as? String ?: "",
        userId = this["userId"] as? String ?: "",
        carId = this["carId"] as? String ?: "",
        audioUrl = this["audioUrl"] as? String ?: "",
        durationMs = (this["durationMs"] as? Long)?.toInt() ?: 0,
        topSoundLabel = this["topSoundLabel"] as? String ?: "",
        topSoundConfidence = (this["topSoundConfidence"] as? Double)?.toFloat() ?: 0f,
        allDetectedSounds = detectedSoundsMap?.mapValues { it.value.toFloat() } ?: emptyMap(),
        rawScore = (this["rawScore"] as? Long)?.toInt() ?: 0,
        normalizedScore = (this["normalizedScore"] as? Double)?.toFloat() ?: 0f,
        healthStatus = this["healthStatus"] as? String ?: "",
        urgencyLevel = this["urgencyLevel"] as? String ?: "",
        detectedIssues = issuesList?.mapNotNull { issueMap ->
            try {
                IssueData(
                    soundType = issueMap["soundType"] as? String ?: "",
                    confidence = (issueMap["confidence"] as? Double)?.toFloat() ?: 0f,
                    severity = issueMap["severity"] as? String ?: "",
                    description = issueMap["description"] as? String ?: "",
                    minCost = issueMap["minCost"] as? Double ?: 0.0,
                    maxCost = issueMap["maxCost"] as? Double ?: 0.0
                )
            } catch (e: Exception) {
                null
            }
        } ?: emptyList(),
        recommendations = this["recommendations"] as? List<String> ?: emptyList(),
        criticalWarning = this["criticalWarning"] as? String ?: "",
        minRepairCost = this["minRepairCost"] as? Double ?: 0.0,
        maxRepairCost = this["maxRepairCost"] as? Double ?: 0.0,
        maintenancePenalty = (this["maintenancePenalty"] as? Double)?.toFloat() ?: 0f,
        overdueServices = this["overdueServices"] as? List<String> ?: emptyList(),
        createdAt = this["createdAt"] as? Long ?: System.currentTimeMillis(),
        updatedAt = this["updatedAt"] as? Long ?: System.currentTimeMillis()
    )
}
