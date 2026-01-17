package com.example.autobrain.domain.repository

import com.example.autobrain.core.utils.Result
import com.example.autobrain.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        age: Int,
        carMake: String = "",
        carModel: String = "",
        carYear: Int = 0
    ): Result<User>
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    fun observeAuthState(): Flow<User?>
    suspend fun updateProfile(user: User): Result<Unit>
    suspend fun uploadProfileImage(imageUri: String): Result<String>
    suspend fun isUserLoggedIn(): Boolean
}
