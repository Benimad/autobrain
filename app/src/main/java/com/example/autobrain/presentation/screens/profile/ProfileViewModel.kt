package com.example.autobrain.presentation.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.repository.CarImageRepository
import com.example.autobrain.domain.model.CarDetails
import com.example.autobrain.domain.model.User
import com.example.autobrain.domain.repository.AuthRepository
import com.example.autobrain.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val signOutUseCase: SignOutUseCase,
    private val carImageRepository: CarImageRepository
) : ViewModel() {
    
    private val TAG = "ProfileViewModel"

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    if (result.data != null) {
                        val user = result.data
                        
                        val needsImageUpdate = user.carDetails != null && 
                            user.carDetails.make.isNotBlank() && 
                            user.carDetails.model.isNotBlank() &&
                            (user.carDetails.carImageUrl.isBlank() || 
                             !user.carDetails.carImageUrl.contains("firebasestorage.googleapis.com"))
                        
                        if (needsImageUpdate) {
                            Log.d(TAG, "Car image needs update, fetching new image...")
                            fetchAndUpdateCarImage(user)
                        } else {
                            _profileState.value = ProfileState.Success(user)
                        }
                    } else {
                        _profileState.value = ProfileState.Error("User not found")
                    }
                }

                is Result.Error -> {
                    _profileState.value = ProfileState.Error(
                        result.exception.message ?: "Loading error"
                    )
                }

                else -> {}
            }
        }
    }
    
    private fun fetchAndUpdateCarImage(user: User) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Success(user)
            
            user.carDetails?.let { carDetails ->
                try {
                    val CURRENT_IMAGE_VERSION = 3 // Match cache version for mobile-optimized images
                    val needsRefresh = carDetails.imageVersion < CURRENT_IMAGE_VERSION
                    
                    if (needsRefresh) {
                        Log.d(TAG, "🔄 MIGRATION: Image version ${carDetails.imageVersion} is outdated (current: $CURRENT_IMAGE_VERSION)")
                        Log.d(TAG, "🗑️ Clearing old cache and forcing fresh fetch...")
                        
                        // Clear the outdated cache for this specific car
                        carImageRepository.clearCarImageCache(user.uid, carDetails.make, carDetails.model, carDetails.year)
                    }
                    
                    Log.d(TAG, "🚀 Starting USER-SPECIFIC car image fetch...")
                    Log.d(TAG, "👤 User ID: ${user.uid}")
                    Log.d(TAG, "📝 Car details: ${carDetails.make} ${carDetails.model} ${carDetails.year}")
                    Log.d(TAG, "🔗 Old URL: ${carDetails.carImageUrl}")
                    Log.d(TAG, "📌 Image version: ${carDetails.imageVersion} (needs refresh: $needsRefresh)")
                    
                    val imageResult = carImageRepository.fetchCarImageUrl(
                        userId = user.uid,
                        make = carDetails.make,
                        model = carDetails.model,
                        year = carDetails.year
                    )
                    
                    Log.d(TAG, "📊 Image fetch result: ${imageResult.isSuccess}")
                    
                    if (imageResult.isSuccess) {
                        val imageUrl = imageResult.getOrNull()
                        Log.d(TAG, "🔗 New URL: $imageUrl")
                        
                        // CRITICAL: Only update Firestore if we got a REAL verified image (not placeholder)
                        val isRealImage = !imageUrl.isNullOrBlank() && 
                                         !imageUrl.contains("placeholder.com", ignoreCase = true) &&
                                         (imageUrl.contains("firebasestorage.googleapis.com") ||
                                          imageUrl.contains("wikimedia.org") ||
                                          imageUrl.contains("pexels.com") ||
                                          imageUrl.contains("audi-mediacenter.com") ||
                                          imageUrl.contains("press.bmwgroup.com") ||
                                          imageUrl.contains("media.mercedes-benz.com"))
                        
                        if (isRealImage) {
                            // Update with new verified image and current version
                            val updatedCarDetails = carDetails.copy(
                                carImageUrl = imageUrl,
                                imageVersion = CURRENT_IMAGE_VERSION
                            )
                            val updatedUser = user.copy(carDetails = updatedCarDetails)
                            
                            when (authRepository.updateProfile(updatedUser)) {
                                is Result.Success -> {
                                    Log.d(TAG, "✅ Car image updated successfully in Firestore (version: $CURRENT_IMAGE_VERSION)")
                                    _profileState.value = ProfileState.Success(updatedUser)
                                }
                                is Result.Error -> {
                                    Log.e(TAG, "❌ Failed to update car image in Firestore")
                                    _profileState.value = ProfileState.Success(updatedUser)
                                }
                                else -> {}
                            }
                        } else {
                            Log.w(TAG, "⚠️ Got placeholder/unverified URL - NOT updating Firestore")
                            Log.w(TAG, "🔄 Will retry on next app launch (imageVersion remains ${carDetails.imageVersion})")
                        }
                    } else {
                        Log.e(TAG, "❌ Image fetch failed: ${imageResult.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error fetching car image: ${e.message}", e)
                }
            }
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (signOutUseCase()) {
                is Result.Success -> {
                    onSuccess()
                }

                else -> {}
            }
        }
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            when (authRepository.updateProfile(user)) {
                is Result.Success -> {
                    loadProfile()
                }

                else -> {}
            }
        }
    }
    
    fun getFallbackImageUrl(make: String, model: String, year: Int, attemptIndex: Int): String {
        return carImageRepository.generateFallbackImageUrl(make, model, year, attemptIndex)
    }
    
    fun forceRefreshCarImage() {
        viewModelScope.launch {
            val currentState = _profileState.value
            if (currentState is ProfileState.Success) {
                currentState.user.carDetails?.let { carDetails ->
                    Log.d(TAG, "🔄 Force refreshing USER-SPECIFIC car image with background removal...")
                    
                    // Clear USER-SPECIFIC cache to force new fetch
                    carImageRepository.clearCarImageCache(
                        userId = currentState.user.uid,
                        make = carDetails.make,
                        model = carDetails.model,
                        year = carDetails.year
                    )
                    
                    // Fetch new image with background removal
                    fetchAndUpdateCarImage(currentState.user)
                }
            }
        }
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: String) : ProfileState()
}
