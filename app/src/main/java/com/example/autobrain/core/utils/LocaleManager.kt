package com.example.autobrain.core.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * LocaleManager - Centralized language and locale management
 * 
 * Supports:
 * - Arabic (ar) with RTL support
 * - English (en)
 * - Darija (Arabic dialect)
 */
object LocaleManager {
    
    const val ARABIC = "ar"
    const val ENGLISH = "en"
    const val DARIJA = "ar" // Darija (Arabic variant)
    
    /**
     * Available languages in AutoBrain
     */
    data class Language(
        val code: String,
        val displayName: String,
        val nativeName: String,
        val isRTL: Boolean = false,
        val flag: String = ""
    )
    
    val supportedLanguages = listOf(
        Language(
            code = ENGLISH,
            displayName = "English",
            nativeName = "English",
            isRTL = false,
            flag = "🇬🇧"
        ),
        Language(
            code = ARABIC,
            displayName = "Arabic",
            nativeName = "العربية",
            isRTL = true,
            flag = "🌍"
        )
    )
    
    /**
     * Check if language is Right-to-Left
     */
    fun isRTL(languageCode: String): Boolean {
        return languageCode.startsWith("ar")
    }
    
    /**
     * Get language object by code
     */
    fun getLanguage(code: String): Language? {
        return supportedLanguages.find { it.code == code }
    }
    
    /**
     * Set app locale programmatically
     */
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = if (languageCode == DARIJA) {
            Locale("ar") // Darija
        } else {
            Locale(languageCode)
        }
        
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        
        // Set layout direction for RTL
        if (isRTL(languageCode)) {
            configuration.setLayoutDirection(locale)
        }
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }
    
    /**
     * Get current locale from context
     */
    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }
    
    /**
     * Get current language code
     */
    fun getCurrentLanguageCode(context: Context): String {
        val locale = getCurrentLocale(context)
        return locale.language
            locale.language
        }
    }
}

/**
 * Composable to check if current layout is RTL
 */
@Composable
fun isRTL(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.layoutDirection == android.view.View.LAYOUT_DIRECTION_RTL
}

/**
 * Composable to get current language code
 */
@Composable
fun currentLanguageCode(): String {
    val context = LocalContext.current
    return remember(context) {
        LocaleManager.getCurrentLanguageCode(context)
    }
}
