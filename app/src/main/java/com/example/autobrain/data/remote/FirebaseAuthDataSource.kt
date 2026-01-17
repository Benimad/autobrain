package com.example.autobrain.data.remote

import android.util.Log
import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.repository.CarImageRepository
import com.example.autobrain.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val carImageRepository: CarImageRepository
) {
    private val TAG = "FirebaseAuthDataSource"
    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        age: Int,
        carMake: String = "",
        carModel: String = "",
        carYear: Int = 0
    ): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Create car details if provided
                val carDetails = if (carMake.isNotBlank() || carModel.isNotBlank()) {
                    val carImageUrl = try {
                        Log.d(TAG, "Fetching car image for: $carMake $carModel $carYear")
                        val result = carImageRepository.fetchCarImageUrl(carMake, carModel, carYear)
                        result.getOrNull() ?: ""
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching car image: ${e.message}", e)
                        ""
                    }
                    
                    com.example.autobrain.domain.model.CarDetails(
                        make = carMake,
                        model = carModel,
                        year = carYear,
                        carImageUrl = carImageUrl
                    )
                } else null

                val user = User(
                    uid = firebaseUser.uid,
                    email = email,
                    name = name,
                    age = age,
                    carDetails = carDetails,
                    isOnline = true,
                    createdAt = System.currentTimeMillis()
                )

                // Save user to Firestore
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(user)
                    .await()

                Result.Success(user)
            } else {
                Result.Error(Exception("Échec de la création du compte"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Get user data from Firestore
                val document = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()

                val user = document.toObject(User::class.java)
                if (user != null) {
                    // Update online status
                    firestore.collection("users")
                        .document(firebaseUser.uid)
                        .update(
                            mapOf(
                                "isOnline" to true,
                                "lastSeen" to System.currentTimeMillis()
                            )
                        )
                        .await()

                    Result.Success(user)
                } else {
                    Result.Error(Exception("Utilisateur non trouvé"))
                }
            } else {
                Result.Error(Exception("Échec de la connexion"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                // Update online status before signing out
                firestore.collection("users")
                    .document(userId)
                    .update(
                        mapOf(
                            "isOnline" to false,
                            "lastSeen" to System.currentTimeMillis()
                        )
                    )
                    .await()
            }

            firebaseAuth.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
