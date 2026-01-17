package com.example.autobrain.data.local.dao

import androidx.room.*
import com.example.autobrain.data.local.entity.MaintenanceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceRecordDao {
    @Query("SELECT * FROM maintenance_records WHERE userId = :userId ORDER BY date DESC")
    fun getMaintenanceRecords(userId: String): Flow<List<MaintenanceRecordEntity>>

    @Query("SELECT * FROM maintenance_records WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedRecords(userId: String): List<MaintenanceRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MaintenanceRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<MaintenanceRecordEntity>)

    @Update
    suspend fun updateRecord(record: MaintenanceRecordEntity)

    @Query("UPDATE maintenance_records SET isSynced = 1 WHERE id = :recordId")
    suspend fun markAsSynced(recordId: String)

    @Query("DELETE FROM maintenance_records WHERE id = :recordId")
    suspend fun deleteRecord(recordId: String)

    @Query("DELETE FROM maintenance_records WHERE userId = :userId")
    suspend fun deleteUserRecords(userId: String)

    @Query("DELETE FROM maintenance_records")
    suspend fun clearAll()
}
