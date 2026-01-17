package com.example.autobrain.presentation.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.autobrain.core.preferences.PreferencesManager
import com.example.autobrain.core.utils.*
import com.example.autobrain.presentation.components.AdaptiveScaffold
import com.example.autobrain.presentation.navigation.Screen
import com.example.autobrain.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LanguageOption(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String = ""
)

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    val languages = remember {
        LocaleManager.supportedLanguages.map { lang ->
            LanguageOption(lang.code, lang.displayName, lang.nativeName, lang.flag)
        }
    }

    AdaptiveScaffold(
        title = "Settings",
        showBackButton = true,
        onBackClick = { navController.popBackStack() },
        containerColor = MidnightBlack,
        scrollable = true
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AdaptiveSpacing.medium())
        ) {
            Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))

            // Preferences Section
            Text(
                text = "Preferences",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))

            // Dark Mode Toggle
            SettingsToggleItem(
                title = "Dark Mode",
                checked = uiState.darkModeEnabled,
                onCheckedChange = { viewModel.setDarkMode(it) }
            )

            Spacer(modifier = Modifier.height(AdaptiveSpacing.small()))

            // Language Selection (shows current language)
            SettingsLanguageItem(
                currentLanguage = LocaleManager.getLanguage(uiState.languageCode)?.nativeName ?: "English",
                onClick = { showLanguageDialog = true }
            )

            Spacer(modifier = Modifier.height(AdaptiveSpacing.small()))

            // Export Report
            SettingsMenuItem(
                icon = Icons.Outlined.Description,
                title = "Export Report (PDF)",
                showIcon = true,
                onClick = { /* Export PDF */ }
            )

            Spacer(modifier = Modifier.height(AdaptiveSpacing.extraLarge()))

            // Privacy & Legal Section
            Text(
                text = "Privacy & Legal",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))

            // Data Controls
            SettingsMenuItem(
                icon = Icons.Outlined.Security,
                title = "Data Controls",
                onClick = { navController.navigate(Screen.DataControls.route) }
            )

            Spacer(modifier = Modifier.height(AdaptiveSpacing.small()))

            // Terms of Service
            SettingsMenuItem(
                icon = Icons.Outlined.Description,
                title = "Terms of Service",
                onClick = { /* Open Terms */ }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            languages = languages,
            selectedLanguage = uiState.languageCode,
            onLanguageSelected = { 
                viewModel.setLanguage(it)
            },
            onDismiss = { showLanguageDialog = false },
            onSave = { 
                showLanguageDialog = false
            }
        )
    }
}

/**
 * ViewModel for Settings Screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadPreferences()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesManager.languageCode.collect { languageCode ->
                _uiState.value = _uiState.value.copy(languageCode = languageCode)
            }
        }
        
        viewModelScope.launch {
            preferencesManager.isDarkModeEnabled.collect { darkMode ->
                _uiState.value = _uiState.value.copy(darkModeEnabled = darkMode)
            }
        }
    }
    
    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            preferencesManager.setLanguageCode(languageCode)
            _uiState.value = _uiState.value.copy(languageCode = languageCode)
        }
    }
    
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDarkMode(enabled)
            _uiState.value = _uiState.value.copy(darkModeEnabled = enabled)
        }
    }
}

data class SettingsUiState(
    val languageCode: String = "en",
    val darkModeEnabled: Boolean = true
)

@Composable
private fun SettingsToggleItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ElectricTeal,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = SlateGray,
                    uncheckedBorderColor = Color.Transparent,
                    checkedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun SettingsLanguageItem(
    currentLanguage: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Language",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = currentLanguage,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    showIcon: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (showIcon) {
                Icon(
                    imageVector = Icons.Outlined.InsertDriveFile,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    languages: List<LanguageOption>,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = SlateGray
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Select Language",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                languages.forEach { language ->
                    LanguageOptionItem(
                        language = language,
                        isSelected = selectedLanguage == language.code,
                        onClick = { onLanguageSelected(language.code) }
                    )
                    if (language != languages.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricTeal,
                        contentColor = MidnightBlack
                    )
                ) {
                    Text(
                        text = "Save",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageOptionItem(
    language: LanguageOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) ElectricTeal.copy(alpha = 0.1f) else Color.Transparent
            )
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flag emoji
        if (language.flag.isNotEmpty()) {
            Text(
                text = language.flag,
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = language.name,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = TextPrimary
            )
            Text(
                text = language.nativeName,
                fontSize = 14.sp,
                color = if (isSelected) ElectricTeal else TextSecondary
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = ElectricTeal,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ============================================================================
// DATA CONTROLS SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataControlsScreen(
    navController: NavController
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MidnightBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Data Controls",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightBlack
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Section Header
            Text(
                text = "Data Controls",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Download My Data
            DataControlItem(
                icon = Icons.Outlined.CloudDownload,
                title = "Download My Data",
                trailingIcon = Icons.Outlined.Download,
                onClick = { /* Download data */ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Delete My Account
            DataControlItem(
                icon = Icons.Outlined.Delete,
                title = "Delete My Account",
                trailingIcon = Icons.Outlined.Delete,
                onClick = { showDeleteConfirmation = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Another Delete option (as shown in design)
            DataControlItem(
                icon = Icons.Outlined.Schedule,
                title = "Delete My Account",
                trailingIcon = Icons.Outlined.Delete,
                onClick = { showDeleteConfirmation = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Permissions
            DataControlItem(
                icon = Icons.Outlined.Security,
                title = "Permissions",
                trailingIcon = null,
                onClick = { /* Open permissions */ }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = "Delete Account?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "This action cannot be undone. All your data will be permanently deleted.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        // Delete account
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text(
                        text = "Cancel",
                        color = TextSecondary
                    )
                }
            },
            containerColor = DeepNavy,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun DataControlItem(
    icon: ImageVector,
    title: String,
    trailingIcon: ImageVector?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        ),
        border = BorderStroke(1.dp, Color(0xFF30363D))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
