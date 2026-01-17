package com.example.autobrain.data.repository

import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.local.dao.ReminderDao
import com.example.autobrain.data.local.entity.Reminder
import com.example.autobrain.data.local.entity.toReminderEntity
import com.example.autobrain.data.local.entity.toFirestoreMap
import com.example.autobrain.data.local.entity.toReminder
import com.example.autobrain.data.local.entity.toDomain
import com.example.autobrain.domain.repository.ReminderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ReminderRepository {

    override fun getRemindersByUser(userId: String): Flow<List<Reminder>> {
        return reminderDao.getRemindersByUser(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getActiveReminders(userId: String): Flow<List<Reminder>> {
        return reminderDao.getActiveReminders(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getUpcomingReminders(userId: String, daysAhead: Int): Flow<List<Reminder>> {
        val startDate = System.currentTimeMillis()
        val endDate = startDate + TimeUnit.DAYS.toMillis(daysAhead.toLong())
        
        return reminderDao.getUpcomingReminders(userId, startDate, endDate)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getOverdueReminders(userId: String): Flow<List<Reminder>> {
        return reminderDao.getOverdueReminders(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getReminderById(reminderId: String): Reminder? {
        return reminderDao.getReminderById(reminderId)?.toDomain()
    }

    override suspend fun saveReminder(reminder: Reminder): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.Error(Exception("User not authenticated"))

            val reminderId = if (reminder.id.isEmpty()) UUID.randomUUID().toString() else reminder.id
            val reminderWithId = reminder.copy(id = reminderId, userId = userId)

            reminderDao.insertReminder(reminderWithId.toReminderEntity(isSynced = false))

            firestore.collection("car_logs")
                .document(userId)
                .collection("reminders")
                .document(reminderId)
                .set(reminderWithId.toFirestoreMap())
                .await()

            reminderDao.markAsSynced(reminderId)

            Result.Success(reminderId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateReminder(reminder: Reminder): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.Error(Exception("User not authenticated"))

            reminderDao.updateReminder(reminder.toReminderEntity(isSynced = false))

            firestore.collection("car_logs")
                .document(userId)
                .collection("reminders")
                .document(reminder.id)
                .set(reminder.toFirestoreMap())
                .await()

            reminderDao.markAsSynced(reminder.id)

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.Error(Exception("User not authenticated"))

            reminderDao.deleteReminder(reminderId)

            firestore.collection("car_logs")
                .document(userId)
                .collection("reminders")
                .document(reminderId)
                .delete()
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun markReminderAsCompleted(reminderId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.Error(Exception("User not authenticated"))

            val completedAt = System.currentTimeMillis()
            reminderDao.markAsCompleted(reminderId, completedAt)

            firestore.collection("car_logs")
                .document(userId)
                .collection("reminders")
                .document(reminderId)
                .update(
                    mapOf(
                        "isCompleted" to true,
                        "completedAt" to completedAt
                    )
                )
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getRemindersNeedingNotification(userId: String): List<Reminder> {
        return reminderDao.getRemindersNeedingNotification(userId)
            .map { it.toDomain() }
    }

    override suspend fun markNotificationSent(reminderId: String): Result<Unit> {
        return try {
            reminderDao.markNotificationSent(reminderId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun syncReminders(userId: String): Result<Unit> {
        return try {
            val unsyncedReminders = reminderDao.getUnsyncedReminders(userId)

            unsyncedReminders.forEach { entity ->
                firestore.collection("car_logs")
                    .document(userId)
                    .collection("reminders")
                    .document(entity.id)
                    .set(entity.toDomain().toFirestoreMap())
                    .await()

                reminderDao.markAsSynced(entity.id)
            }

            val cloudReminders = firestore.collection("car_logs")
                .document(userId)
                .collection("reminders")
                .get()
                .await()

            val reminders = cloudReminders.documents.mapNotNull { doc ->
                doc.data?.toReminder()
            }

            reminderDao.insertReminders(reminders.map { it.toReminderEntity(isSynced = true) })

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
