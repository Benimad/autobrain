package com.example.autobrain.domain.repository

import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.local.entity.AIScore
import kotlinx.coroutines.flow.Flow

interface AIScoreRepository {
    
    fun getAIScoresByUser(userId: String): Flow<List<AIScore>>
    
    fun getAIScoresByCar(userId: String, carId: String): Flow<List<AIScore>>
    
    fun getLatestAIScore(userId: String): Flow<AIScore?>
    
    fun getLatestAIScoreForCar(userId: String, carId: String): Flow<AIScore?>
    
    suspend fun getAIScoreById(scoreId: String): AIScore?
    
    suspend fun saveAIScore(score: AIScore): Result<String>
    
    suspend fun deleteAIScore(scoreId: String): Result<Unit>
    
    suspend fun syncAIScores(userId: String): Result<Unit>
    
    suspend fun getAverageScore(userId: String): Float?
}
