package com.example.autobrain.data.local.dao

import androidx.room.*
import com.example.autobrain.data.local.entity.ImageFetchStrategyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageFetchStrategyDao {
    
    @Query("SELECT * FROM image_fetch_strategies WHERE makeModel = :makeModel")
    suspend fun getStrategy(makeModel: String): ImageFetchStrategyEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrategy(strategy: ImageFetchStrategyEntity)
    
    @Query("SELECT * FROM image_fetch_strategies ORDER BY successRate DESC, avgFetchTimeMs ASC LIMIT 20")
    fun getTopStrategies(): Flow<List<ImageFetchStrategyEntity>>
    
    @Query("DELETE FROM image_fetch_strategies WHERE lastUpdated < :expiryTime")
    suspend fun deleteExpiredStrategies(expiryTime: Long)
    
    companion object {
        fun generateKey(make: String, model: String): String {
            return "${make.lowercase().trim()}_${model.lowercase().trim().replace(" ", "")}"
        }
    }
}
