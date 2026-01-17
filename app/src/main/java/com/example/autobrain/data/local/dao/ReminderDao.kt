package com.example.autobrain.data.local.dao

import androidx.room.*
import com.example.autobrain.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Reminder operations in Room
 * Supports maintenance reminders, insurance, technical inspections
 */
@Dao
interface ReminderDao {

    // ==================== QUERIES ====================

    /**
     * Get all reminders for a user
     */
    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY dueDate ASC")
    fun getRemindersByUser(userId: String): Flow<List<ReminderEntity>>

    /**
     * Get active (not completed) reminders for a user
     */
    @Query("SELECT * FROM reminders WHERE userId = :userId AND isCompleted = 0 ORDER BY dueDate ASC")
    fun getActiveReminders(userId: String): Flow<List<ReminderEntity>>

    /**
     * Get completed reminders for a user
     */
    @Query("SELECT * FROM reminders WHERE userId = :userId AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedReminders(userId: String): Flow<List<ReminderEntity>>

    /**
     * Get reminders for a specific car
     */
    @Query("SELECT * FROM reminders WHERE userId = :userId AND carId = :carId ORDER BY dueDate ASC")
    fun getRemindersByCar(userId: String, carId: String): Flow<List<ReminderEntity>>

    /**
     * Get reminders by type
     */
    @Query("SELECT * FROM reminders WHERE userId = :userId AND type = :type ORDER BY dueDate ASC")
    fun getRemindersByType(userId: String, type: String): Flow<List<ReminderEntity>>

    /**
     * Get upcoming reminders (due within X days)
     */
    @Query(
        """
        SELECT * FROM reminders 
        WHERE userId = :userId 
        AND isCompleted = 0 
        AND dueDate <= :endDate 
        AND dueDate >= :startDate
        ORDER BY dueDate ASC
    """
    )
    fun getUpcomingReminders(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<ReminderEntity>>

    /**
     * Get overdue reminders
     */
    @Query(
        """
        SELECT * FROM reminders 
        WHERE userId = :userId 
        AND isCompleted = 0 
        AND dueDate < :currentTime
        ORDER BY dueDate ASC
    """
    )
    fun getOverdueReminders(
        userId: String,
        currentTime: Long = System.currentTimeMillis()
    ): Flow<List<ReminderEntity>>

    /**
     * Get reminder by ID
     */
    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminderById(reminderId: String): ReminderEntity?

    /**
     * Observe reminder by ID
     */
    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    fun observeReminderById(reminderId: String): Flow<ReminderEntity?>

    /**
     * Get reminders that need notification (not sent yet, due soon)
     */
    @Query(
        """
        SELECT * FROM reminders 
        WHERE userId = :userId 
        AND isCompleted = 0 
        AND isNotificationEnabled = 1 
        AND notificationSent = 0
        AND (dueDate - (reminderDaysBefore * 86400000)) <= :currentTime
    """
    )
    suspend fun getRemindersNeedingNotification(
        userId: String,
        currentTime: Long = System.currentTimeMillis()
    ): List<ReminderEntity>

    /**
     * Get unsynced reminders
     */
    @Query("SELECT * FROM reminders WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedReminders(userId: String): List<ReminderEntity>

    /**
     * Get count of active reminders
     */
    @Query("SELECT COUNT(*) FROM reminders WHERE userId = :userId AND isCompleted = 0")
    suspend fun getActiveRemindersCount(userId: String): Int

    /**
     * Get count of overdue reminders
     */
    @Query("SELECT COUNT(*) FROM reminders WHERE userId = :userId AND isCompleted = 0 AND dueDate < :currentTime")
    suspend fun getOverdueRemindersCount(
        userId: String,
        currentTime: Long = System.currentTimeMillis()
    ): Int

    // ==================== INSERTS ====================

    /**
     * Insert a single reminder
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    /**
     * Insert multiple reminders
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    // ==================== UPDATES ====================

    /**
     * Update a reminder
     */
    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    /**
     * Mark reminder as completed
     */
    @Query("UPDATE reminders SET isCompleted = 1, completedAt = :completedAt, updatedAt = :completedAt WHERE id = :reminderId")
    suspend fun markAsCompleted(reminderId: String, completedAt: Long = System.currentTimeMillis())

    /**
     * Mark notification as sent
     */
    @Query("UPDATE reminders SET notificationSent = 1, updatedAt = :timestamp WHERE id = :reminderId")
    suspend fun markNotificationSent(
        reminderId: String,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Mark as synced
     */
    @Query("UPDATE reminders SET isSynced = 1, updatedAt = :timestamp WHERE id = :reminderId")
    suspend fun markAsSynced(reminderId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Reset notification sent status (for recurring reminders)
     */
    @Query("UPDATE reminders SET notificationSent = 0, updatedAt = :timestamp WHERE id = :reminderId")
    suspend fun resetNotificationSent(
        reminderId: String,
        timestamp: Long = System.currentTimeMillis()
    )

    // ==================== DELETES ====================

    /**
     * Delete a reminder
     */
    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteReminder(reminderId: String)

    /**
     * Delete all reminders for a user
     */
    @Query("DELETE FROM reminders WHERE userId = :userId")
    suspend fun deleteUserReminders(userId: String)

    /**
     * Delete all reminders for a car
     */
    @Query("DELETE FROM reminders WHERE carId = :carId")
    suspend fun deleteCarReminders(carId: String)

    /**
     * Delete completed reminders older than X days
     */
    @Query("DELETE FROM reminders WHERE userId = :userId AND isCompleted = 1 AND completedAt < :cutoffTime")
    suspend fun deleteOldCompletedReminders(userId: String, cutoffTime: Long)

    /**
     * Clear all reminders
     */
    @Query("DELETE FROM reminders")
    suspend fun clearAll()
}
