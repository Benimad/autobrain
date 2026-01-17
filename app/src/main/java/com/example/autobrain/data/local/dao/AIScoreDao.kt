package com.example.autobrain.data.local.dao

import androidx.room.*
import com.example.autobrain.data.local.entity.AIScoreEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for AI Score operations in Room
 * Supports offline-first with sync status tracking
 */
@Dao
interface AIScoreDao {

    // ==================== QUERIES ====================

    /**
     * Get all AI scores for a user, ordered by date (newest first)
     */
    @Query("SELECT * FROM ai_scores WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAIScoresByUser(userId: String): Flow<List<AIScoreEntity>>

    /**
     * Get AI scores for a specific car
     */
    @Query("SELECT * FROM ai_scores WHERE userId = :userId AND carId = :carId ORDER BY createdAt DESC")
    fun getAIScoresByCar(userId: String, carId: String): Flow<List<AIScoreEntity>>

    /**
     * Get the latest AI score for a user
     */
    @Query("SELECT * FROM ai_scores WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    fun getLatestAIScore(userId: String): Flow<AIScoreEntity?>

    /**
     * Get the latest AI score for a specific car
     */
    @Query("SELECT * FROM ai_scores WHERE userId = :userId AND carId = :carId ORDER BY createdAt DESC LIMIT 1")
    fun getLatestAIScoreForCar(userId: String, carId: String): Flow<AIScoreEntity?>

    /**
     * Get AI score by ID
     */
    @Query("SELECT * FROM ai_scores WHERE id = :scoreId")
    suspend fun getAIScoreById(scoreId: String): AIScoreEntity?

    /**
     * Get AI score by ID as Flow
     */
    @Query("SELECT * FROM ai_scores WHERE id = :scoreId")
    fun observeAIScoreById(scoreId: String): Flow<AIScoreEntity?>

    /**
     * Get unsynced AI scores for cloud sync
     */
    @Query("SELECT * FROM ai_scores WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedScores(userId: String): List<AIScoreEntity>

    /**
     * Get scores count for a user
     */
    @Query("SELECT COUNT(*) FROM ai_scores WHERE userId = :userId")
    suspend fun getScoresCount(userId: String): Int

    /**
     * Get average score for a user
     */
    @Query("SELECT AVG(score) FROM ai_scores WHERE userId = :userId")
    suspend fun getAverageScore(userId: String): Float?

    // ==================== INSERTS ====================

    /**
     * Insert a single AI score
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAIScore(score: AIScoreEntity)

    /**
     * Insert multiple AI scores (for bulk sync)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAIScores(scores: List<AIScoreEntity>)

    // ==================== UPDATES ====================

    /**
     * Update an AI score
     */
    @Update
    suspend fun updateAIScore(score: AIScoreEntity)

    /**
     * Mark a score as synced
     */
    @Query("UPDATE ai_scores SET isSynced = 1, updatedAt = :timestamp WHERE id = :scoreId")
    suspend fun markAsSynced(scoreId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Mark multiple scores as synced
     */
    @Query("UPDATE ai_scores SET isSynced = 1, updatedAt = :timestamp WHERE id IN (:scoreIds)")
    suspend fun markMultipleAsSynced(
        scoreIds: List<String>,
        timestamp: Long = System.currentTimeMillis()
    )

    // ==================== DELETES ====================

    /**
     * Delete a specific AI score
     */
    @Query("DELETE FROM ai_scores WHERE id = :scoreId")
    suspend fun deleteAIScore(scoreId: String)

    /**
     * Delete all AI scores for a user
     */
    @Query("DELETE FROM ai_scores WHERE userId = :userId")
    suspend fun deleteUserScores(userId: String)

    /**
     * Delete all AI scores for a car
     */
    @Query("DELETE FROM ai_scores WHERE carId = :carId")
    suspend fun deleteCarScores(carId: String)

    /**
     * Clear all AI scores (for logout)
     */
    @Query("DELETE FROM ai_scores")
    suspend fun clearAll()
}
