package com.example.autobrain.data.local.dao

import androidx.room.*
import com.example.autobrain.data.local.entity.CarImageEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for car image caching operations.
 */
@Dao
interface CarImageDao {
    
    @Query("SELECT * FROM car_images WHERE carKey = :carKey LIMIT 1")
    suspend fun getCarImage(carKey: String): CarImageEntity?
    
    @Query("SELECT * FROM car_images WHERE carKey = :carKey LIMIT 1")
    fun getCarImageFlow(carKey: String): Flow<CarImageEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarImage(carImage: CarImageEntity)
    
    @Query("UPDATE car_images SET lastAccessedAt = :timestamp WHERE carKey = :carKey")
    suspend fun updateLastAccessed(carKey: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM car_images WHERE cachedAt < :expiryTime")
    suspend fun deleteExpiredImages(expiryTime: Long)
    
    @Query("DELETE FROM car_images WHERE carKey = :carKey")
    suspend fun deleteCarImage(carKey: String)
    
    @Query("DELETE FROM car_images")
    suspend fun clearAll()
    
    companion object {
        fun generateCarKey(make: String, model: String, year: Int): String {
            return "${make.lowercase()}_${model.lowercase()}_$year"
                .replace(" ", "_")
                .replace("-", "_")
        }
    }
}
