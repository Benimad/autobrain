package com.example.autobrain.data.local.dao

import androidx.room.*
import com.example.autobrain.data.local.entity.VideoDiagnosticEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Video Diagnostics
 * Provides offline-first data access with reactive Flow queries
 */
@Dao
interface VideoDiagnosticDao {
    
    // =============================================================================
    // QUERIES
    // =============================================================================
    
    @Query("SELECT * FROM video_diagnostics WHERE id = :id")
    suspend fun getById(id: String): VideoDiagnosticEntity?
    
    @Query("SELECT * FROM video_diagnostics WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllByUserFlow(userId: String): Flow<List<VideoDiagnosticEntity>>
    
    @Query("SELECT * FROM video_diagnostics WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getAllByUser(userId: String): List<VideoDiagnosticEntity>
    
    @Query("SELECT * FROM video_diagnostics WHERE carId = :carId ORDER BY createdAt DESC")
    fun getByCarFlow(carId: String): Flow<List<VideoDiagnosticEntity>>
    
    @Query("SELECT * FROM video_diagnostics WHERE carId = :carId ORDER BY createdAt DESC")
    suspend fun getByCar(carId: String): List<VideoDiagnosticEntity>
    
    @Query("SELECT * FROM video_diagnostics WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentDiagnostics(userId: String, limit: Int): List<VideoDiagnosticEntity>
    
    @Query("""
        SELECT * FROM video_diagnostics 
        WHERE userId = :userId 
        AND (urgencyLevel = 'CRITICAL' OR urgencyLevel = 'HIGH')
        ORDER BY createdAt DESC
    """)
    fun getCriticalDiagnostics(userId: String): Flow<List<VideoDiagnosticEntity>>
    
    @Query("""
        SELECT * FROM video_diagnostics 
        WHERE userId = :userId 
        AND (smokeDetected = 1 OR vibrationDetected = 1)
        ORDER BY createdAt DESC
    """)
    fun getProblematicDiagnostics(userId: String): Flow<List<VideoDiagnosticEntity>>
    
    @Query("SELECT * FROM video_diagnostics WHERE carId = :carId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestForCar(carId: String): VideoDiagnosticEntity?
    
    // =============================================================================
    // STATISTICS
    // =============================================================================
    
    @Query("SELECT COUNT(*) FROM video_diagnostics WHERE carId = :carId")
    suspend fun getCarDiagnosticsCount(carId: String): Int
    
    @Query("SELECT AVG(finalScore) FROM video_diagnostics WHERE carId = :carId")
    suspend fun getAverageScoreForCar(carId: String): Float?
    
    @Query("""
        SELECT COUNT(*) FROM video_diagnostics 
        WHERE userId = :userId 
        AND createdAt >= :startTime
    """)
    suspend fun getDiagnosticsCountSince(userId: String, startTime: Long): Int
    
    @Query("""
        SELECT AVG(finalScore) FROM video_diagnostics 
        WHERE carId = :carId 
        AND createdAt >= :startTime
    """)
    suspend fun getAverageScoreSince(carId: String, startTime: Long): Float?
    
    @Query("""
        SELECT COUNT(*) FROM video_diagnostics 
        WHERE carId = :carId 
        AND smokeDetected = 1
    """)
    suspend fun getSmokeDetectionCount(carId: String): Int
    
    @Query("""
        SELECT COUNT(*) FROM video_diagnostics 
        WHERE carId = :carId 
        AND vibrationDetected = 1
    """)
    suspend fun getVibrationDetectionCount(carId: String): Int
    
    // =============================================================================
    // SYNC OPERATIONS
    // =============================================================================
    
    @Query("SELECT * FROM video_diagnostics WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedDiagnostics(): List<VideoDiagnosticEntity>
    
    @Query("UPDATE video_diagnostics SET isSynced = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun markAsSynced(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE video_diagnostics SET syncAttempts = syncAttempts + 1, lastSyncAttempt = :timestamp WHERE id = :id")
    suspend fun incrementSyncAttempts(id: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Record sync error
     */
    @Query("UPDATE video_diagnostics SET syncError = :error, syncAttempts = syncAttempts + 1, lastSyncAttempt = :timestamp WHERE id = :id")
    suspend fun recordSyncError(id: String, error: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Clear sync error
     */
    @Query("UPDATE video_diagnostics SET syncError = NULL WHERE id = :id")
    suspend fun clearSyncError(id: String)
    
    /**
     * Update local modification timestamp
     */
    @Query("UPDATE video_diagnostics SET localModifiedAt = :timestamp, isSynced = 0 WHERE id = :id")
    suspend fun markAsLocallyModified(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE video_diagnostics SET videoUrl = :url, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateVideoUrl(id: String, url: String, timestamp: Long = System.currentTimeMillis())
    
    // =============================================================================
    // SECURITY & CLEANUP
    // =============================================================================
    
    @Query("SELECT * FROM video_diagnostics WHERE autoDeleteAt > 0 AND autoDeleteAt < :currentTime")
    suspend fun getExpiredDiagnostics(currentTime: Long = System.currentTimeMillis()): List<VideoDiagnosticEntity>
    
    @Query("DELETE FROM video_diagnostics WHERE autoDeleteAt > 0 AND autoDeleteAt < :currentTime")
    suspend fun deleteExpiredDiagnostics(currentTime: Long = System.currentTimeMillis()): Int
    
    @Query("DELETE FROM video_diagnostics WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM video_diagnostics WHERE userId = :userId")
    suspend fun deleteUserDiagnostics(userId: String)
    
    @Query("DELETE FROM video_diagnostics")
    suspend fun clearAll()
    
    // =============================================================================
    // INSERT / UPDATE
    // =============================================================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoDiagnostic(diagnostic: VideoDiagnosticEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(diagnostics: List<VideoDiagnosticEntity>)
    
    @Update
    suspend fun updateVideoDiagnostic(diagnostic: VideoDiagnosticEntity)
    
    // =============================================================================
    // SEARCH & FILTER
    // =============================================================================
    
    @Query("""
        SELECT * FROM video_diagnostics 
        WHERE userId = :userId 
        AND finalScore <= :maxScore
        ORDER BY createdAt DESC
    """)
    fun getDiagnosticsWithLowScore(userId: String, maxScore: Int = 60): Flow<List<VideoDiagnosticEntity>>
    
    @Query("""
        SELECT * FROM video_diagnostics 
        WHERE userId = :userId 
        AND smokeType = :smokeType
        ORDER BY createdAt DESC
    """)
    fun getDiagnosticsBySmokeType(userId: String, smokeType: String): Flow<List<VideoDiagnosticEntity>>
    
    @Query("""
        SELECT * FROM video_diagnostics 
        WHERE userId = :userId 
        AND createdAt BETWEEN :startTime AND :endTime
        ORDER BY createdAt DESC
    """)
    suspend fun getDiagnosticsInTimeRange(userId: String, startTime: Long, endTime: Long): List<VideoDiagnosticEntity>
}
