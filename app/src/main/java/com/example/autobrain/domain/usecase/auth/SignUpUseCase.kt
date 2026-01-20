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
            return Result.Error(Exception("Name is required"))
        }

        if (!email.isValidEmail()) {
            return Result.Error(Exception("Invalid email"))
        }

        if (!password.isValidPassword()) {
            return Result.Error(Exception("Password must contain at least 8 characters, one letter and one digit"))
        }

        if (age < Constants.MINIMUM_USER_AGE) {
            return Result.Error(Exception("You must be at least ${Constants.MINIMUM_USER_AGE} years old"))
        }

        return authRepository.signUp(email, password, name, age, carMake, carModel, carYear)
    }
}
