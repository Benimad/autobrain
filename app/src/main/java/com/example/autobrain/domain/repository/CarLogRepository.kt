package com.example.autobrain.domain.repository

import com.example.autobrain.core.utils.Result
import com.example.autobrain.domain.model.CarLog
import com.example.autobrain.domain.model.MaintenanceRecord
import com.example.autobrain.domain.model.MaintenanceReminder
import com.example.autobrain.domain.model.CarDocument
import kotlinx.coroutines.flow.Flow

interface CarLogRepository {
    suspend fun getCarLog(userId: String): Flow<Result<CarLog?>>
    suspend fun createOrUpdateCarLog(carLog: CarLog): Result<Unit>

    // Maintenance Records
    suspend fun addMaintenanceRecord(userId: String, record: MaintenanceRecord): Result<Unit>
    suspend fun updateMaintenanceRecord(userId: String, record: MaintenanceRecord): Result<Unit>
    suspend fun deleteMaintenanceRecord(userId: String, recordId: String): Result<Unit>
    suspend fun getMaintenanceRecords(userId: String): Result<List<MaintenanceRecord>>

    // Reminders
    suspend fun addReminder(userId: String, reminder: MaintenanceReminder): Result<Unit>
    suspend fun updateReminder(userId: String, reminder: MaintenanceReminder): Result<Unit>
    suspend fun deleteReminder(userId: String, reminderId: String): Result<Unit>
    suspend fun getUpcomingReminders(userId: String): Result<List<MaintenanceReminder>>
    suspend fun markReminderAsCompleted(userId: String, reminderId: String): Result<Unit>

    // Documents
    suspend fun addDocument(userId: String, document: CarDocument): Result<Unit>
    suspend fun updateDocument(userId: String, document: CarDocument): Result<Unit>
    suspend fun deleteDocument(userId: String, documentId: String): Result<Unit>
    suspend fun getDocuments(userId: String): Result<List<CarDocument>>
    suspend fun uploadDocument(documentUri: String): Result<String>

    // Offline support
    suspend fun syncCarLogData(): Result<Unit>
}
