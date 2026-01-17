package com.example.autobrain.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferences Manager using DataStore
 * 
 * Manages:
 * - Onboarding completion status
 * - User authentication state
 * - User session data
 */

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "autobrain_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_LANGUAGE_CODE = stringPreferencesKey("language_code")
        val KEY_DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val KEY_SELECTED_CAR_ID = stringPreferencesKey("selected_car_id")
    }
    
    // =============================================================================
    // ONBOARDING
    // =============================================================================
    
    /**
     * Check if onboarding has been completed
     */
    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }
    
    /**
     * Mark onboarding as completed
     */
    suspend fun setOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = true
        }
    }
    
    // =============================================================================
    // AUTHENTICATION
    // =============================================================================
    
    /**
     * Check if user is logged in
     */
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_LOGGED_IN] ?: false
    }
    
    /**
     * Get current user ID
     */
    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_USER_ID]
    }
    
    /**
     * Get current user email
     */
    val userEmail: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_USER_EMAIL]
    }
    
    /**
     * Save user session (called on successful login)
     */
    suspend fun saveUserSession(userId: String, email: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
            preferences[KEY_USER_EMAIL] = email
            preferences[KEY_IS_LOGGED_IN] = true
        }
    }
    
    /**
     * Clear user session (called on logout)
     */
    suspend fun clearUserSession() {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = ""
            preferences[KEY_USER_EMAIL] = ""
            preferences[KEY_IS_LOGGED_IN] = false
        }
    }
    
    /**
     * Clear all preferences (for testing or reset)
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    // =============================================================================
    // LANGUAGE & LOCALIZATION
    // =============================================================================
    
    /**
     * Get selected language code
     * Default: "en" (English)
     */
    val languageCode: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE_CODE] ?: "en"
    }
    
    /**
     * Set language code
     * Supported: "ar", "en"
     */
    suspend fun setLanguageCode(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE_CODE] = languageCode
        }
    }
    
    // =============================================================================
    // THEME PREFERENCES
    // =============================================================================
    
    /**
     * Check if dark mode is enabled
     * Default: true (app is designed for dark mode)
     */
    val isDarkModeEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_DARK_MODE_ENABLED] ?: true
    }
    
    /**
     * Set dark mode preference
     */
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DARK_MODE_ENABLED] = enabled
        }
    }
    
    // =============================================================================
    // CAR SELECTION
    // =============================================================================
    
    /**
     * Get currently selected car ID
     */
    val selectedCarId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_CAR_ID]
    }
    
    /**
     * Set selected car ID
     */
    suspend fun setSelectedCarId(carId: String) {
        dataStore.edit { preferences ->
            preferences[KEY_SELECTED_CAR_ID] = carId
        }
    }
    
    /**
     * Clear selected car ID
     */
    suspend fun clearSelectedCarId() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_SELECTED_CAR_ID)
        }
    }
}
