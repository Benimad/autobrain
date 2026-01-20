package com.example.autobrain.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autobrain.core.preferences.PreferencesManager
import com.example.autobrain.core.utils.Result
import com.example.autobrain.domain.model.User
import com.example.autobrain.domain.usecase.auth.SignInUseCase
import com.example.autobrain.domain.usecase.auth.SignUpUseCase
import com.example.autobrain.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val preferencesManager: PreferencesManager,
    private val authRepository: com.example.autobrain.domain.repository.AuthRepository,
    private val carImageRepository: com.example.autobrain.data.repository.CarImageRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = signInUseCase(email, password)) {
                is Result.Success -> {
                    // Save user session to DataStore for persistent login
                    preferencesManager.saveUserSession(
                        userId = result.data.uid,
                        email = result.data.email
                    )
                    _authState.value = AuthState.Success(result.data)
                }

                is Result.Error -> {
                    _authState.value =
                        AuthState.Error(result.exception.message ?: "Connection error")
                }

                is Result.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    fun signUp(
        email: String,
        password: String,
        name: String,
        age: Int,
        carMake: String = "",
        carModel: String = "",
        carYear: Int = 0
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = signUpUseCase(email, password, name, age, carMake, carModel, carYear)) {
                is Result.Success -> {
                    // Save user session to DataStore for persistent login
                    preferencesManager.saveUserSession(
                        userId = result.data.uid,
                        email = result.data.email
                    )
                    _authState.value = AuthState.Success(result.data)
                }

                is Result.Error -> {
                    _authState.value =
                        AuthState.Error(result.exception.message ?: "Sign up error")
                }

                is Result.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            // Clear user session from DataStore
            preferencesManager.clearUserSession()
            
            // Sign out from Firebase
            signOutUseCase()
            
            _authState.value = AuthState.Idle
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
    
    fun saveCarDetails(
        make: String,
        model: String,
        year: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentUserResult = authRepository.getCurrentUser()
                if (currentUserResult is Result.Success && currentUserResult.data != null) {
                    val user = currentUserResult.data
                    
                    // Fetch car image
                    val imageResult = carImageRepository.fetchCarImageUrl(make, model, year)
                    val imageUrl = imageResult.getOrNull() ?: ""
                    
                    // Update user with car details
                    val carDetails = com.example.autobrain.domain.model.CarDetails(
                        make = make,
                        model = model,
                        year = year,
                        carImageUrl = imageUrl
                    )
                    
                    val updatedUser = user.copy(carDetails = carDetails)
                    
                    when (authRepository.updateProfile(updatedUser)) {
                        is Result.Success -> onSuccess()
                        is Result.Error -> onError("Failed to save car details")
                        else -> {}
                    }
                } else {
                    onError("User not found")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}
