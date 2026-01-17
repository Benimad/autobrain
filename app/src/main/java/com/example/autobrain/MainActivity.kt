package com.example.autobrain

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.autobrain.core.preferences.PreferencesManager
import com.example.autobrain.core.utils.LocaleManager
import com.example.autobrain.presentation.navigation.NavGraph
import com.example.autobrain.presentation.theme.AutoBrainTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@OptIn(ExperimentalPermissionsApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Observe language preference
            val languageCode by preferencesManager.languageCode.collectAsState(initial = "en")
            
            // Apply locale to activity
            applyLocale(languageCode)
            
            // Determine layout direction based on language
            val layoutDirection = if (LocaleManager.isRTL(languageCode)) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }
            
            // Request POST_NOTIFICATIONS permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notificationPermission = rememberPermissionState(
                    Manifest.permission.POST_NOTIFICATIONS
                )
                
                LaunchedEffect(Unit) {
                    if (!notificationPermission.status.isGranted) {
                        notificationPermission.launchPermissionRequest()
                    }
                }
            }
            
            AutoBrainTheme {
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalLayoutDirection provides layoutDirection
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavGraph()
                    }
                }
            }
        }
    }
    
    /**
     * Apply locale to activity context
     */
    private fun applyLocale(languageCode: String) {
        LocaleManager.setLocale(this, languageCode)
    }
    
    /**
     * Override attachBaseContext to apply locale before onCreate
     */
    override fun attachBaseContext(newBase: Context) {
        // Get stored language preference (default to English)
        val sharedPrefs = newBase.getSharedPreferences("autobrain_preferences", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString("language_code", "en") ?: "en"

        // Apply locale to context
        val localeContext = LocaleManager.setLocale(newBase, languageCode)
        super.attachBaseContext(localeContext)
    }
}
