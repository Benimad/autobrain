package com.example.autobrain.data.local.dao

import androidx.room.*
import com.example.autobrain.data.local.entity.CarImageEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for car image caching operations.
 * User-specific caching ensures each user gets their personalized car image.
 */
@Dao
interface CarImageDao {
    
    @Query("SELECT * FROM car_images WHERE carKey = :carKey LIMIT 1")
    suspend fun getCarImage(carKey: String): CarImageEntity?
    
    @Query("SELECT * FROM car_images WHERE carKey = :carKey LIMIT 1")
    fun getCarImageFlow(carKey: String): Flow<CarImageEntity?>
    
    @Query("SELECT * FROM car_images WHERE userId = :userId")
    suspend fun getUserCarImages(userId: String): List<CarImageEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarImage(carImage: CarImageEntity)
    
    @Query("UPDATE car_images SET lastAccessedAt = :timestamp WHERE carKey = :carKey")
    suspend fun updateLastAccessed(carKey: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM car_images WHERE cachedAt < :expiryTime")
    suspend fun deleteExpiredImages(expiryTime: Long)
    
    @Query("DELETE FROM car_images WHERE carKey = :carKey")
    suspend fun deleteCarImage(carKey: String)
    
    @Query("DELETE FROM car_images WHERE userId = :userId")
    suspend fun deleteUserCarImages(userId: String)
    
    @Query("DELETE FROM car_images")
    suspend fun clearAll()
    
    companion object {
        /**
         * Generates a user-specific cache key for car images.
         * Format: "userId_make_model_year"
         * This ensures each user gets their own cached image, even for the same car model.
         */
        fun generateCarKey(userId: String, make: String, model: String, year: Int): String {
            return "${userId.take(10)}_${make.lowercase()}_${model.lowercase()}_$year"
                .replace(" ", "_")
                .replace("-", "_")
        }
        
        /**
         * Legacy method for backward compatibility (without userId).
         * @deprecated Use generateCarKey(userId, make, model, year) instead
         */
        @Deprecated("Use generateCarKey with userId parameter")
        fun generateCarKey(make: String, model: String, year: Int): String {
            return "${make.lowercase()}_${model.lowercase()}_$year"
                .replace(" ", "_")
                .replace("-", "_")
        }
    }
}
