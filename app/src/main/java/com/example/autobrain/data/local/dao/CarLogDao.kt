package com.example.autobrain.data.local.dao

import androidx.room.*
import com.example.autobrain.data.local.entity.CarLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for car log operations in Room
 */
@Dao
interface CarLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarLog(carLog: CarLogEntity)

    @Query("SELECT * FROM car_logs WHERE userId = :userId LIMIT 1")
    fun getCarLogByUserId(userId: String): Flow<CarLogEntity?>

    @Query("DELETE FROM car_logs WHERE userId = :userId")
    suspend fun deleteCarLog(userId: String)

    @Query("DELETE FROM car_logs")
    suspend fun clearAll()
}
