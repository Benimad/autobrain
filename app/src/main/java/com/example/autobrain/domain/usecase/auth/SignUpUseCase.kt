package com.example.autobrain.domain.usecase.auth

import com.example.autobrain.core.utils.Constants
import com.example.autobrain.core.utils.Result
import com.example.autobrain.core.utils.isValidEmail
import com.example.autobrain.core.utils.isValidPassword
import com.example.autobrain.domain.model.User
import com.example.autobrain.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String,
        age: Int,
        carMake: String = "",
        carModel: String = "",
        carYear: Int = 0
    ): Result<User> {
        // Validation
        if (name.isBlank()) {
            return Result.Error(Exception("Le nom est requis"))
        }

        if (!email.isValidEmail()) {
            return Result.Error(Exception("Email invalide"))
        }

        if (!password.isValidPassword()) {
            return Result.Error(Exception("Le mot de passe doit contenir au moins 8 caractères, une lettre et un chiffre"))
        }

        if (age < Constants.MINIMUM_USER_AGE) {
            return Result.Error(Exception("Vous devez avoir au moins ${Constants.MINIMUM_USER_AGE} ans"))
        }

        return authRepository.signUp(email, password, name, age, carMake, carModel, carYear)
    }
}
