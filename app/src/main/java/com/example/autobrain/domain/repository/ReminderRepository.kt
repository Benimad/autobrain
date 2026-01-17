package com.example.autobrain.domain.repository

import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.local.entity.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    
    fun getRemindersByUser(userId: String): Flow<List<Reminder>>
    
    fun getActiveReminders(userId: String): Flow<List<Reminder>>
    
    fun getUpcomingReminders(userId: String, daysAhead: Int): Flow<List<Reminder>>
    
    fun getOverdueReminders(userId: String): Flow<List<Reminder>>
    
    suspend fun getReminderById(reminderId: String): Reminder?
    
    suspend fun saveReminder(reminder: Reminder): Result<String>
    
    suspend fun updateReminder(reminder: Reminder): Result<Unit>
    
    suspend fun deleteReminder(reminderId: String): Result<Unit>
    
    suspend fun markReminderAsCompleted(reminderId: String): Result<Unit>
    
    suspend fun getRemindersNeedingNotification(userId: String): List<Reminder>
    
    suspend fun markNotificationSent(reminderId: String): Result<Unit>
    
    suspend fun syncReminders(userId: String): Result<Unit>
}
