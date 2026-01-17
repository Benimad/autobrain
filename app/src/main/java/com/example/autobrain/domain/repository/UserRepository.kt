package com.example.autobrain.domain.repository

import com.example.autobrain.core.utils.Result
import com.example.autobrain.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserById(userId: String): Result<User?>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean): Result<Unit>
    suspend fun updateFcmToken(userId: String, token: String): Result<Unit>
}
