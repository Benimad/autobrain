package com.example.autobrain.data.repository

import com.example.autobrain.core.utils.Result
import com.example.autobrain.domain.model.User
import com.example.autobrain.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val user = document.toObject(User::class.java)
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.uid)
                .set(user)
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateOnlineStatus(userId: String, isOnline: Boolean): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "isOnline" to isOnline,
                        "lastSeen" to System.currentTimeMillis()
                    )
                )
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateFcmToken(userId: String, token: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
