package com.example.autobrain.domain.usecase.auth

import com.example.autobrain.core.utils.Result
import com.example.autobrain.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.signOut()
    }
}
