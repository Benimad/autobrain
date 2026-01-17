package com.example.autobrain.data.local.dao

import androidx.room.*
import com.example.autobrain.data.local.entity.AudioDiagnosticEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Audio Diagnostic operations
 * Supports offline-first architecture with sync tracking
 */
@Dao
interface AudioDiagnosticDao {
    
    /**
     * Insert new audio diagnostic
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioDiagnostic(diagnostic: AudioDiagnosticEntity)
    
    /**
     * Insert multiple diagnostics
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioDiagnostics(diagnostics: List<AudioDiagnosticEntity>)
    
    /**
     * Update existing diagnostic
     */
    @Update
    suspend fun updateAudioDiagnostic(diagnostic: AudioDiagnosticEntity)
    
    /**
     * Delete diagnostic
     */
    @Delete
    suspend fun deleteAudioDiagnostic(diagnostic: AudioDiagnosticEntity)
    
    /**
     * Delete diagnostic by ID
     */
    @Query("DELETE FROM audio_diagnostics WHERE id = :id")
    suspend fun deleteById(id: String)
    
    /**
     * Get diagnostic by ID
     */
    @Query("SELECT * FROM audio_diagnostics WHERE id = :id")
    suspend fun getById(id: String): AudioDiagnosticEntity?
    
    /**
     * Get all diagnostics for a user (Flow for reactive UI)
     */
    @Query("SELECT * FROM audio_diagnostics WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllByUserFlow(userId: String): Flow<List<AudioDiagnosticEntity>>
    
    /**
     * Get all diagnostics for a user (suspend)
     */
    @Query("SELECT * FROM audio_diagnostics WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getAllByUser(userId: String): List<AudioDiagnosticEntity>
    
    /**
     * Get diagnostics for a specific car
     */
    @Query("SELECT * FROM audio_diagnostics WHERE carId = :carId ORDER BY createdAt DESC")
    fun getByCarFlow(carId: String): Flow<List<AudioDiagnosticEntity>>
    
    /**
     * Get recent diagnostics (last N)
     */
    @Query("SELECT * FROM audio_diagnostics WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentDiagnostics(userId: String, limit: Int = 10): List<AudioDiagnosticEntity>
    
    /**
     * Get critical diagnostics (score < 50)
     */
    @Query("SELECT * FROM audio_diagnostics WHERE userId = :userId AND rawScore < 50 ORDER BY createdAt DESC")
    fun getCriticalDiagnostics(userId: String): Flow<List<AudioDiagnosticEntity>>
    
    /**
     * Get unsynced diagnostics for background sync
     */
    @Query("SELECT * FROM audio_diagnostics WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedDiagnostics(): List<AudioDiagnosticEntity>
    
    /**
     * Get diagnostics that need retry (failed sync)
     */
    @Query("SELECT * FROM audio_diagnostics WHERE isSynced = 0 AND syncAttempts < 5 ORDER BY lastSyncAttempt ASC")
    suspend fun getDiagnosticsForRetry(): List<AudioDiagnosticEntity>
    
    /**
     * Mark diagnostic as synced
     */
    @Query("UPDATE audio_diagnostics SET isSynced = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun markAsSynced(id: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Increment sync attempts
     */
    @Query("UPDATE audio_diagnostics SET syncAttempts = syncAttempts + 1, lastSyncAttempt = :timestamp WHERE id = :id")
    suspend fun incrementSyncAttempts(id: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Record sync error
     */
    @Query("UPDATE audio_diagnostics SET syncError = :error, syncAttempts = syncAttempts + 1, lastSyncAttempt = :timestamp WHERE id = :id")
    suspend fun recordSyncError(id: String, error: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Clear sync error
     */
    @Query("UPDATE audio_diagnostics SET syncError = NULL WHERE id = :id")
    suspend fun clearSyncError(id: String)
    
    /**
     * Update local modification timestamp
     */
    @Query("UPDATE audio_diagnostics SET localModifiedAt = :timestamp, isSynced = 0 WHERE id = :id")
    suspend fun markAsLocallyModified(id: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Update audio URL after Firebase Storage upload
     */
    @Query("UPDATE audio_diagnostics SET audioUrl = :url, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateAudioUrl(id: String, url: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Get diagnostics count for a user
     */
    @Query("SELECT COUNT(*) FROM audio_diagnostics WHERE userId = :userId")
    suspend fun getDiagnosticsCount(userId: String): Int
    
    /**
     * Get diagnostics count for a car
     */
    @Query("SELECT COUNT(*) FROM audio_diagnostics WHERE carId = :carId")
    suspend fun getCarDiagnosticsCount(carId: String): Int
    
    /**
     * Get average score for a car
     */
    @Query("SELECT AVG(rawScore) FROM audio_diagnostics WHERE carId = :carId")
    suspend fun getAverageScoreForCar(carId: String): Float?
    
    /**
     * Get diagnostics within date range
     */
    @Query("SELECT * FROM audio_diagnostics WHERE userId = :userId AND createdAt BETWEEN :startTime AND :endTime ORDER BY createdAt DESC")
    suspend fun getDiagnosticsByDateRange(
        userId: String,
        startTime: Long,
        endTime: Long
    ): List<AudioDiagnosticEntity>
    
    /**
     * Delete old diagnostics (data cleanup)
     */
    @Query("DELETE FROM audio_diagnostics WHERE createdAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
    
    /**
     * Get diagnostics by urgency level
     */
    @Query("SELECT * FROM audio_diagnostics WHERE userId = :userId AND urgencyLevel = :urgencyLevel ORDER BY createdAt DESC")
    fun getByUrgencyLevel(userId: String, urgencyLevel: String): Flow<List<AudioDiagnosticEntity>>
    
    /**
     * Search diagnostics by sound type
     */
    @Query("SELECT * FROM audio_diagnostics WHERE userId = :userId AND topSoundLabel LIKE '%' || :soundType || '%' ORDER BY createdAt DESC")
    suspend fun searchBySoundType(userId: String, soundType: String): List<AudioDiagnosticEntity>
    
    /**
     * Get latest diagnostic for a car
     */
    @Query("SELECT * FROM audio_diagnostics WHERE carId = :carId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestForCar(carId: String): AudioDiagnosticEntity?
    
    /**
     * Delete all diagnostics for a user (for testing/cleanup)
     */
    @Query("DELETE FROM audio_diagnostics WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
