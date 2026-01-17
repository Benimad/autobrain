package com.example.autobrain.data.repository

import android.net.Uri
import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.remote.FirebaseAuthDataSource
import com.example.autobrain.domain.model.User
import com.example.autobrain.domain.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AuthRepository {

    override suspend fun signUp(
        email: String,
        password: String,
        name: String,
        age: Int,
        carMake: String,
        carModel: String,
        carYear: Int
    ): Result<User> {
        return authDataSource.signUp(email, password, name, age, carMake, carModel, carYear)
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return authDataSource.signIn(email, password)
    }

    override suspend fun signOut(): Result<Unit> {
        return authDataSource.signOut()
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            // Firebase auth reset password logic
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val userId = authDataSource.getCurrentUserId()
            if (userId != null) {
                val document = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()

                val user = document.toObject(User::class.java)
                Result.Success(user)
            } else {
                Result.Success(null)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun observeAuthState(): Flow<User?> = callbackFlow {
        val userId = authDataSource.getCurrentUserId()

        if (userId != null) {
            val listener = firestore.collection("users")
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val user = snapshot?.toObject(User::class.java)
                    trySend(user)
                }

            awaitClose { listener.remove() }
        } else {
            trySend(null)
            awaitClose()
        }
    }

    override suspend fun updateProfile(user: User): Result<Unit> {
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

    override suspend fun uploadProfileImage(imageUri: String): Result<String> {
        return try {
            val userId = authDataSource.getCurrentUserId()
                ?: return Result.Error(Exception("Utilisateur non connecté"))

            val uri = Uri.parse(imageUri)
            val storageRef = storage.reference
                .child("users")
                .child(userId)
                .child("profile")
                .child("profile_image.jpg")

            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await()

            Result.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return authDataSource.isUserLoggedIn()
    }
}
