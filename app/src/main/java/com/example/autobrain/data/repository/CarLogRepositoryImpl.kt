package com.example.autobrain.data.repository

import android.net.Uri
import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.local.dao.MaintenanceRecordDao
import com.example.autobrain.data.local.entity.toDomain
import com.example.autobrain.data.local.entity.toEntity
import com.example.autobrain.domain.model.CarDocument
import com.example.autobrain.domain.model.CarLog
import com.example.autobrain.domain.model.MaintenanceRecord
import com.example.autobrain.domain.model.MaintenanceReminder
import com.example.autobrain.domain.repository.CarLogRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CarLogRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val maintenanceRecordDao: MaintenanceRecordDao
) : CarLogRepository {

    override suspend fun getCarLog(userId: String): Flow<Result<CarLog?>> = callbackFlow {
        // Direct listener on maintenance records subcollection
        val recordsListener = firestore.collection("car_logs")
            .document(userId)
            .collection("maintenance_records")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { recordsSnapshot, recordsError ->
                if (recordsError != null) {
                    trySend(Result.Error(recordsError))
                    return@addSnapshotListener
                }
                
                // Parse maintenance records
                val records = recordsSnapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(MaintenanceRecord::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        android.util.Log.e("CarLogRepo", "Error parsing record ${doc.id}: ${e.message}")
                        null
                    }
                } ?: emptyList()
                
                android.util.Log.d("CarLogRepo", "Loaded ${records.size} maintenance records for user $userId")
                
                val totalExpenses = records.sumOf { it.cost }
                
                // Try to get the main carLog document (optional)
                firestore.collection("car_logs")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            // Main document exists, merge with records
                            val carLog = snapshot.toObject(CarLog::class.java)?.copy(
                                id = snapshot.id,
                                maintenanceRecords = records,
                                totalExpenses = totalExpenses
                            ) ?: CarLog(
                                id = userId,
                                userId = userId,
                                maintenanceRecords = records,
                                totalExpenses = totalExpenses
                            )
                            trySend(Result.Success(carLog))
                        } else {
                            // Main document doesn't exist, create CarLog from records only
                            android.util.Log.d("CarLogRepo", "Main CarLog doc doesn't exist, creating from records")
                            val carLog = CarLog(
                                id = userId,
                                userId = userId,
                                maintenanceRecords = records,
                                totalExpenses = totalExpenses
                            )
                            trySend(Result.Success(carLog))
                        }
                    }
                    .addOnFailureListener { error ->
                        android.util.Log.e("CarLogRepo", "Error fetching main CarLog: ${error.message}")
                        // Even if main doc fails, still send records
                        val carLog = CarLog(
                            id = userId,
                            userId = userId,
                            maintenanceRecords = records,
                            totalExpenses = totalExpenses
                        )
                        trySend(Result.Success(carLog))
                    }
            }

        awaitClose { 
            recordsListener?.remove()
        }
    }

    override suspend fun createOrUpdateCarLog(carLog: CarLog): Result<Unit> {
        return try {
            firestore.collection("car_logs")
                .document(carLog.userId)
                .set(carLog.copy(updatedAt = System.currentTimeMillis()))
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun addMaintenanceRecord(
        userId: String,
        record: MaintenanceRecord
    ): Result<Unit> {
        return try {
            // Ensure main CarLog document exists
            val carLogDocRef = firestore.collection("car_logs").document(userId)
            val carLogSnapshot = carLogDocRef.get().await()
            
            if (!carLogSnapshot.exists()) {
                // Create initial CarLog document
                android.util.Log.d("CarLogRepo", "Creating initial CarLog document for user $userId")
                val initialCarLog = CarLog(
                    id = userId,
                    userId = userId,
                    maintenanceRecords = emptyList(),
                    reminders = emptyList(),
                    documents = emptyList(),
                    totalExpenses = 0.0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                carLogDocRef.set(initialCarLog).await()
            } else {
                // Update timestamp
                carLogDocRef.update("updatedAt", System.currentTimeMillis()).await()
            }
            
            // Add maintenance record to subcollection
            val docRef = firestore.collection("car_logs")
                .document(userId)
                .collection("maintenance_records")
                .add(record)
                .await()
            
            android.util.Log.d("CarLogRepo", "Added maintenance record ${docRef.id} for user $userId")

            // Save locally to Room
            maintenanceRecordDao.insertRecord(record.toEntity(userId))

            Result.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("CarLogRepo", "Error adding maintenance record: ${e.message}", e)
            Result.Error(e)
        }
    }

    override suspend fun updateMaintenanceRecord(
        userId: String,
        record: MaintenanceRecord
    ): Result<Unit> {
        return try {
            firestore.collection("car_logs")
                .document(userId)
                .collection("maintenance_records")
                .document(record.id)
                .set(record)
                .await()

            maintenanceRecordDao.updateRecord(record.toEntity(userId))

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteMaintenanceRecord(userId: String, recordId: String): Result<Unit> {
        return try {
            firestore.collection("car_logs")
                .document(userId)
                .collection("maintenance_records")
                .document(recordId)
                .delete()
                .await()

            maintenanceRecordDao.deleteRecord(recordId)

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getMaintenanceRecords(userId: String): Result<List<MaintenanceRecord>> {
        return try {
            val snapshot = firestore.collection("car_logs")
                .document(userId)
                .collection("maintenance_records")
                .get()
                .await()

            val records = snapshot.documents.mapNotNull { doc ->
                doc.toObject(MaintenanceRecord::class.java)?.copy(id = doc.id)
            }

            Result.Success(records)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun addReminder(userId: String, reminder: MaintenanceReminder): Result<Unit> {
        return try {
            firestore.collection("car_logs")
                .document(userId)
                .collection("reminders")
                .add(reminder)
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateReminder(
        userId: String,
        reminder: MaintenanceReminder
    ): Result<Unit> {
        return try {
            firestore.collection("car_logs")
                .document(userId)
                .collection("reminders")
                .document(reminder.id)
                .set(reminder)
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteReminder(userId: String, reminderId: String): Result<Unit> {
        return try {
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

    override suspend fun getUpcomingReminders(userId: String): Result<List<MaintenanceReminder>> {
        return try {
            android.util.Log.d("CarLogRepo", "Fetching reminders for user: $userId")
            val snapshot = firestore.collection("car_logs")
                .document(userId)
                .collection("reminders")
                .whereEqualTo("completed", false)  // Fixed: was "isCompleted", should be "completed"
                .get()
                .await()

            android.util.Log.d("CarLogRepo", "Found ${snapshot.documents.size} reminder documents")
            
            val reminders = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(MaintenanceReminder::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    android.util.Log.e("CarLogRepo", "Error parsing reminder ${doc.id}: ${e.message}")
                    null
                }
            }.sortedBy { it.dueDate }

            android.util.Log.d("CarLogRepo", "Successfully parsed ${reminders.size} reminders")
            Result.Success(reminders)
        } catch (e: Exception) {
            android.util.Log.e("CarLogRepo", "Error fetching reminders: ${e.message}")
            Result.Error(e)
        }
    }

    override suspend fun markReminderAsCompleted(userId: String, reminderId: String): Result<Unit> {
        return try {
            firestore.collection("car_logs")
                .document(userId)
                .collection("reminders")
                .document(reminderId)
                .update("completed", true)  // Fixed: was "isCompleted", should be "completed"
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun addDocument(userId: String, document: CarDocument): Result<Unit> {
        return try {
            firestore.collection("car_logs")
                .document(userId)
                .collection("documents")
                .add(document)
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateDocument(userId: String, document: CarDocument): Result<Unit> {
        return try {
            firestore.collection("car_logs")
                .document(userId)
                .collection("documents")
                .document(document.id)
                .set(document)
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteDocument(userId: String, documentId: String): Result<Unit> {
        return try {
            firestore.collection("car_logs")
                .document(userId)
                .collection("documents")
                .document(documentId)
                .delete()
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getDocuments(userId: String): Result<List<CarDocument>> {
        return try {
            val snapshot = firestore.collection("car_logs")
                .document(userId)
                .collection("documents")
                .get()
                .await()

            val documents = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CarDocument::class.java)?.copy(id = doc.id)
            }

            Result.Success(documents)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun uploadDocument(documentUri: String): Result<String> {
        return try {
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.Error(Exception("User not authenticated"))

            val uri = Uri.parse(documentUri)
            val fileName = "document_${System.currentTimeMillis()}.pdf"
            val storageRef = storage.reference
                .child("users")
                .child(userId)
                .child("documents")
                .child(fileName)

            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await()

            Result.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun syncCarLogData(): Result<Unit> {
        return try {
            // Sync unsynced maintenance records
            // This would be implemented with WorkManager in production
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
