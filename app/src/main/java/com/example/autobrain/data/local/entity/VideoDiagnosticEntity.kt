package com.example.autobrain.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.autobrain.data.local.converter.ListStringConverter
import com.example.autobrain.data.local.converter.MapStringConverter

/**
 * Video Diagnostic Entity for Room Database
 * Stores complete video diagnostic results locally (smoke/vibration detection)
 * 
 * Security Features:
 * - Video paths encrypted via EncryptedSharedPreferences
 * - Auto-delete after 7 days
 * - Consent tracking
 */
@Entity(tableName = "video_diagnostics")
@TypeConverters(ListStringConverter::class, MapStringConverter::class)
data class VideoDiagnosticEntity(
    @PrimaryKey
    val id: String,
    
    // User & Car Info
    val userId: String,
    val carId: String,
    
    // Video Data
    val videoFilePath: String,
    val videoUrl: String = "", // Firestore URL after upload (if consent given)
    val durationMs: Int,
    val videoHash: String = "", // For integrity verification
    
    // ML Kit Detection Results
    val smokeDetected: Boolean = false,
    val smokeType: String = "", // "black", "white", "blue", "none"
    val smokeConfidence: Float = 0f,
    val smokeSeverity: Int = 0, // 0-5 scale
    
    val vibrationDetected: Boolean = false,
    val vibrationLevel: String = "", // "low", "medium", "high", "excessive"
    val vibrationConfidence: Float = 0f,
    val vibrationSeverity: Int = 0, // 0-5 scale
    
    // Frame Analysis Stats
    val totalFramesAnalyzed: Int = 0,
    val smokeyFramesCount: Int = 0,
    val vibrationFramesCount: Int = 0,
    val averageBrightness: Float = 0f, // For quality check
    val isStableVideo: Boolean = true, // Based on accelerometer
    
    // Scoring
    val rawScore: Int = 100, // Base 100, deductions applied
    val finalScore: Int = 100,
    val healthStatus: String = "", // "Excellent", "Bon", "Moyen", "Problème Grave"
    val urgencyLevel: String = "", // "NONE", "LOW", "MEDIUM", "HIGH", "CRITICAL"
    
    // Smart Suggestions (Local, No LLM)
    val detectedIssues: List<String>, // JSON array ["issue1", "issue2"]
    val recommendations: List<String>, // JSON array of smart recommendations
    val criticalWarning: String = "",
    
    // Cost Estimates (from local map)
    val estimatedMinCost: Double = 0.0,
    val estimatedMaxCost: Double = 0.0,
    
    // Maintenance Integration
    val carnetImpactScore: Int = 0, // Penalty from carnet (oil change, CT)
    val overdueMaintenanceItems: List<String> = emptyList(),
    
    // Quality Metadata
    val videoQuality: String = "", // "good", "acceptable", "poor"
    val qualityIssues: List<String> = emptyList(), // ["too_dark", "shaky", "blurry"]
    
    // Security & Consent
    val hasStorageConsent: Boolean = false,
    val anonymized: Boolean = false, // License plates blurred?
    val autoDeleteAt: Long = 0, // Timestamp for auto-deletion (7 days)
    
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
 * Domain model for Video Diagnostic
 */
data class VideoDiagnosticData(
    val id: String = "",
    val userId: String = "",
    val carId: String = "",
    
    // Video
    val videoFilePath: String = "",
    val videoUrl: String = "",
    val durationMs: Int = 0,
    val videoHash: String = "",
    
    // Detection Results
    val smokeDetected: Boolean = false,
    val smokeType: String = "",
    val smokeConfidence: Float = 0f,
    val smokeSeverity: Int = 0,
    
    val vibrationDetected: Boolean = false,
    val vibrationLevel: String = "",
    val vibrationConfidence: Float = 0f,
    val vibrationSeverity: Int = 0,
    
    // Frame Stats
    val totalFramesAnalyzed: Int = 0,
    val smokeyFramesCount: Int = 0,
    val vibrationFramesCount: Int = 0,
    val averageBrightness: Float = 0f,
    val isStableVideo: Boolean = true,
    
    // Scoring
    val rawScore: Int = 100,
    val finalScore: Int = 100,
    val healthStatus: String = "",
    val urgencyLevel: String = "",
    
    // Issues & Recommendations
    val detectedIssues: List<VideoIssue> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val criticalWarning: String = "",
    
    // Costs
    val estimatedMinCost: Double = 0.0,
    val estimatedMaxCost: Double = 0.0,
    
    // Maintenance
    val carnetImpactScore: Int = 0,
    val overdueMaintenanceItems: List<String> = emptyList(),
    
    // Quality
    val videoQuality: String = "",
    val qualityIssues: List<String> = emptyList(),
    
    // Security
    val hasStorageConsent: Boolean = false,
    val anonymized: Boolean = false,
    val autoDeleteAt: Long = 0,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class VideoIssue(
    val issueType: String, // "smoke_black", "smoke_white", "vibration_high"
    val severity: String, // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val confidence: Float,
    val description: String,
    val estimatedMinCost: Double,
    val estimatedMaxCost: Double
)

// =============================================================================
// CONVERTERS
// =============================================================================

fun VideoDiagnosticEntity.toDomain(): VideoDiagnosticData {
    return VideoDiagnosticData(
        id = id,
        userId = userId,
        carId = carId,
        videoFilePath = videoFilePath,
        videoUrl = videoUrl,
        durationMs = durationMs,
        videoHash = videoHash,
        smokeDetected = smokeDetected,
        smokeType = smokeType,
        smokeConfidence = smokeConfidence,
        smokeSeverity = smokeSeverity,
        vibrationDetected = vibrationDetected,
        vibrationLevel = vibrationLevel,
        vibrationConfidence = vibrationConfidence,
        vibrationSeverity = vibrationSeverity,
        totalFramesAnalyzed = totalFramesAnalyzed,
        smokeyFramesCount = smokeyFramesCount,
        vibrationFramesCount = vibrationFramesCount,
        averageBrightness = averageBrightness,
        isStableVideo = isStableVideo,
        rawScore = rawScore,
        finalScore = finalScore,
        healthStatus = healthStatus,
        urgencyLevel = urgencyLevel,
        detectedIssues = parseVideoIssues(detectedIssues),
        recommendations = recommendations,
        criticalWarning = criticalWarning,
        estimatedMinCost = estimatedMinCost,
        estimatedMaxCost = estimatedMaxCost,
        carnetImpactScore = carnetImpactScore,
        overdueMaintenanceItems = overdueMaintenanceItems,
        videoQuality = videoQuality,
        qualityIssues = qualityIssues,
        hasStorageConsent = hasStorageConsent,
        anonymized = anonymized,
        autoDeleteAt = autoDeleteAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun VideoDiagnosticData.toEntity(isSynced: Boolean = false): VideoDiagnosticEntity {
    return VideoDiagnosticEntity(
        id = id,
        userId = userId,
        carId = carId,
        videoFilePath = videoFilePath,
        videoUrl = videoUrl,
        durationMs = durationMs,
        videoHash = videoHash,
        smokeDetected = smokeDetected,
        smokeType = smokeType,
        smokeConfidence = smokeConfidence,
        smokeSeverity = smokeSeverity,
        vibrationDetected = vibrationDetected,
        vibrationLevel = vibrationLevel,
        vibrationConfidence = vibrationConfidence,
        vibrationSeverity = vibrationSeverity,
        totalFramesAnalyzed = totalFramesAnalyzed,
        smokeyFramesCount = smokeyFramesCount,
        vibrationFramesCount = vibrationFramesCount,
        averageBrightness = averageBrightness,
        isStableVideo = isStableVideo,
        rawScore = rawScore,
        finalScore = finalScore,
        healthStatus = healthStatus,
        urgencyLevel = urgencyLevel,
        detectedIssues = formatVideoIssues(detectedIssues),
        recommendations = recommendations,
        criticalWarning = criticalWarning,
        estimatedMinCost = estimatedMinCost,
        estimatedMaxCost = estimatedMaxCost,
        carnetImpactScore = carnetImpactScore,
        overdueMaintenanceItems = overdueMaintenanceItems,
        videoQuality = videoQuality,
        qualityIssues = qualityIssues,
        hasStorageConsent = hasStorageConsent,
        anonymized = anonymized,
        autoDeleteAt = autoDeleteAt,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
}

fun VideoDiagnosticData.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "userId" to userId,
        "carId" to carId,
        "videoUrl" to videoUrl,
        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),  // Use server timestamp
        "durationMs" to durationMs,
        "videoHash" to videoHash,
        "smokeDetected" to smokeDetected,
        "smokeType" to smokeType,
        "smokeConfidence" to smokeConfidence,
        "smokeSeverity" to smokeSeverity,
        "vibrationDetected" to vibrationDetected,
        "vibrationLevel" to vibrationLevel,
        "vibrationConfidence" to vibrationConfidence,
        "vibrationSeverity" to vibrationSeverity,
        "totalFramesAnalyzed" to totalFramesAnalyzed,
        "smokeyFramesCount" to smokeyFramesCount,
        "vibrationFramesCount" to vibrationFramesCount,
        "averageBrightness" to averageBrightness,
        "isStableVideo" to isStableVideo,
        "rawScore" to rawScore,
        "finalScore" to finalScore,
        "healthStatus" to healthStatus,
        "urgencyLevel" to urgencyLevel,
        "detectedIssues" to detectedIssues.map { issue ->
            mapOf(
                "issueType" to issue.issueType,
                "severity" to issue.severity,
                "confidence" to issue.confidence,
                "description" to issue.description,
                "estimatedMinCost" to issue.estimatedMinCost,
                "estimatedMaxCost" to issue.estimatedMaxCost
            )
        },
        "recommendations" to recommendations,
        "criticalWarning" to criticalWarning,
        "estimatedMinCost" to estimatedMinCost,
        "estimatedMaxCost" to estimatedMaxCost,
        "carnetImpactScore" to carnetImpactScore,
        "overdueMaintenanceItems" to overdueMaintenanceItems,
        "videoQuality" to videoQuality,
        "qualityIssues" to qualityIssues,
        "hasStorageConsent" to hasStorageConsent,
        "anonymized" to anonymized,
        "createdAt" to createdAt
    )
}

// =============================================================================
// HELPER FUNCTIONS
// =============================================================================

private fun parseVideoIssues(issues: List<String>): List<VideoIssue> {
    return issues.mapNotNull { json ->
        try {
            // Format: "issueType|severity|confidence|description|minCost|maxCost"
            val parts = json.split("|")
            if (parts.size == 6) {
                VideoIssue(
                    issueType = parts[0],
                    severity = parts[1],
                    confidence = parts[2].toFloat(),
                    description = parts[3],
                    estimatedMinCost = parts[4].toDouble(),
                    estimatedMaxCost = parts[5].toDouble()
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}

private fun formatVideoIssues(issues: List<VideoIssue>): List<String> {
    return issues.map { issue ->
        "${issue.issueType}|${issue.severity}|${issue.confidence}|${issue.description}|${issue.estimatedMinCost}|${issue.estimatedMaxCost}"
    }
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toVideoDiagnosticData(): VideoDiagnosticData {
    val issuesList = this["detectedIssues"] as? List<Map<String, Any?>>
    
    return VideoDiagnosticData(
        id = this["id"] as? String ?: "",
        userId = this["userId"] as? String ?: "",
        carId = this["carId"] as? String ?: "",
        videoUrl = this["videoUrl"] as? String ?: "",
        durationMs = (this["durationMs"] as? Long)?.toInt() ?: 0,
        videoHash = this["videoHash"] as? String ?: "",
        smokeDetected = this["smokeDetected"] as? Boolean ?: false,
        smokeType = this["smokeType"] as? String ?: "",
        smokeConfidence = (this["smokeConfidence"] as? Double)?.toFloat() ?: 0f,
        smokeSeverity = (this["smokeSeverity"] as? Long)?.toInt() ?: 0,
        vibrationDetected = this["vibrationDetected"] as? Boolean ?: false,
        vibrationLevel = this["vibrationLevel"] as? String ?: "",
        vibrationConfidence = (this["vibrationConfidence"] as? Double)?.toFloat() ?: 0f,
        vibrationSeverity = (this["vibrationSeverity"] as? Long)?.toInt() ?: 0,
        totalFramesAnalyzed = (this["totalFramesAnalyzed"] as? Long)?.toInt() ?: 0,
        smokeyFramesCount = (this["smokeyFramesCount"] as? Long)?.toInt() ?: 0,
        vibrationFramesCount = (this["vibrationFramesCount"] as? Long)?.toInt() ?: 0,
        averageBrightness = (this["averageBrightness"] as? Double)?.toFloat() ?: 0f,
        isStableVideo = this["isStableVideo"] as? Boolean ?: true,
        rawScore = (this["rawScore"] as? Long)?.toInt() ?: 100,
        finalScore = (this["finalScore"] as? Long)?.toInt() ?: 100,
        healthStatus = this["healthStatus"] as? String ?: "",
        urgencyLevel = this["urgencyLevel"] as? String ?: "",
        detectedIssues = issuesList?.mapNotNull { issueMap ->
            try {
                VideoIssue(
                    issueType = issueMap["issueType"] as? String ?: "",
                    severity = issueMap["severity"] as? String ?: "",
                    confidence = (issueMap["confidence"] as? Double)?.toFloat() ?: 0f,
                    description = issueMap["description"] as? String ?: "",
                    estimatedMinCost = issueMap["estimatedMinCost"] as? Double ?: 0.0,
                    estimatedMaxCost = issueMap["estimatedMaxCost"] as? Double ?: 0.0
                )
            } catch (e: Exception) {
                null
            }
        } ?: emptyList(),
        recommendations = this["recommendations"] as? List<String> ?: emptyList(),
        criticalWarning = this["criticalWarning"] as? String ?: "",
        estimatedMinCost = this["estimatedMinCost"] as? Double ?: 0.0,
        estimatedMaxCost = this["estimatedMaxCost"] as? Double ?: 0.0,
        carnetImpactScore = (this["carnetImpactScore"] as? Long)?.toInt() ?: 0,
        overdueMaintenanceItems = this["overdueMaintenanceItems"] as? List<String> ?: emptyList(),
        videoQuality = this["videoQuality"] as? String ?: "",
        qualityIssues = this["qualityIssues"] as? List<String> ?: emptyList(),
        hasStorageConsent = this["hasStorageConsent"] as? Boolean ?: false,
        anonymized = this["anonymized"] as? Boolean ?: false,
        createdAt = this["createdAt"] as? Long ?: System.currentTimeMillis(),
        updatedAt = this["updatedAt"] as? Long ?: System.currentTimeMillis()
    )
}
