package com.example.autobrain.domain.usecase.auth

import com.example.autobrain.core.utils.Result
import com.example.autobrain.domain.model.User
import com.example.autobrain.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.Error(Exception("L'email et le mot de passe sont requis"))
        }

        return authRepository.signIn(email, password)
    }
}
