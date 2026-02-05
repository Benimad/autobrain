package com.example.autobrain.presentation.screens.carlog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autobrain.domain.logic.ReminderCalculation
import com.example.autobrain.domain.model.MaintenanceType
import com.example.autobrain.presentation.components.GeminiIconWithGlow
import com.example.autobrain.presentation.theme.BorderDark
import com.example.autobrain.presentation.theme.DeepNavy
import com.example.autobrain.presentation.theme.ElectricTeal
import com.example.autobrain.presentation.theme.ErrorRed
import com.example.autobrain.presentation.theme.MidnightBlack
import com.example.autobrain.presentation.theme.SlateGray
import com.example.autobrain.presentation.theme.SuccessGreen
import com.example.autobrain.presentation.theme.TextOnAccent
import com.example.autobrain.presentation.theme.TextPrimary
import com.example.autobrain.presentation.theme.TextSecondary
import com.example.autobrain.presentation.theme.TextMuted
import com.example.autobrain.presentation.theme.WarningAmber

/**
 * 🎯 ADD REMINDER SCREEN
 * 
 * Professional screen with intelligent reminder creation
 * Focus: Strong logic > UI (but UI still looks good)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AddReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val calculationState by viewModel.calculationState.collectAsState()
    val geminiSuggestions by viewModel.geminiSuggestions.collectAsState()
    
    var selectedType by remember { mutableStateOf<MaintenanceType?>(null) }
    var customTitle by remember { mutableStateOf("") }
    var customDescription by remember { mutableStateOf("") }
    var enableNotifications by remember { mutableStateOf(true) }
    var notifyDaysBefore by remember { mutableStateOf(7) }
    
    // Handle success
    LaunchedEffect(uiState) {
        if (uiState is AddReminderUiState.Success) {
            navController.popBackStack()
        }
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Add reminder", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ElectricTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = MidnightBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step 1: Select Maintenance Type
            MaintenanceTypeSelector(
                selectedType = selectedType,
                onTypeSelected = {
                    selectedType = it
                    viewModel.calculateIntelligentReminder(it)
                }
            )
            
            // Show calculation results
            when (val calcState = calculationState) {
                is CalculationState.Calculating -> {
                    LoadingCard()
                }
                is CalculationState.Success -> {
                    IntelligentCalculationCard(calculation = calcState.calculation)
                }
                is CalculationState.Error -> {
                    ErrorCard(message = calcState.message)
                }
                else -> {}
            }
            
            // Gemini AI Suggestions
            when (val geminiState = geminiSuggestions) {
                is GeminiSuggestionsState.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GeminiIconWithGlow(size = 14.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini analysis in progress...", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                is GeminiSuggestionsState.Success -> {
                    GeminiSuggestionsCard(suggestions = geminiState.suggestions)
                }
                is GeminiSuggestionsState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DeepNavy.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = geminiState.message,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                else -> {}
            }
            
            // Custom fields (optional)
            if (selectedType != null) {
                CustomFieldsSection(
                    customTitle = customTitle,
                    onTitleChange = { customTitle = it },
                    customDescription = customDescription,
                    onDescriptionChange = { customDescription = it }
                )
                
                // Notification settings
                NotificationSettingsSection(
                    enabled = enableNotifications,
                    onEnabledChange = { enableNotifications = it },
                    daysBefore = notifyDaysBefore,
                    onDaysBeforeChange = { notifyDaysBefore = it }
                )
            }
            
            // Warning message
            if (uiState is AddReminderUiState.Warning) {
                WarningCard(message = (uiState as AddReminderUiState.Warning).message)
            }
            
            // Error message
            if (uiState is AddReminderUiState.Error) {
                ErrorCard(message = (uiState as AddReminderUiState.Error).message)
            }
            
            // Save button
            if (selectedType != null && calculationState is CalculationState.Success) {
                Button(
                    onClick = {
                        viewModel.saveReminder(
                            maintenanceType = selectedType!!,
                            customTitle = customTitle.ifBlank { null },
                            customDescription = customDescription.ifBlank { null },
                            enableNotification = enableNotifications,
                            notifyDaysBefore = notifyDaysBefore
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState !is AddReminderUiState.Saving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricTeal,
                        contentColor = TextOnAccent
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (uiState is AddReminderUiState.Saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = TextOnAccent
                        )
                    } else {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Create reminder", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MaintenanceTypeSelector(
    selectedType: MaintenanceType?,
    onTypeSelected: (MaintenanceType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepNavy.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Build, null, tint = ElectricTeal, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Maintenance type",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Grid of maintenance types
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MaintenanceType.entries.filter { it != MaintenanceType.REPAIR && it != MaintenanceType.OTHER }
                    .chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { type ->
                                MaintenanceTypeChip(
                                    type = type,
                                    isSelected = type == selectedType,
                                    onClick = { onTypeSelected(type) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill empty space if odd number
                            if (row.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun MaintenanceTypeChip(
    type: MaintenanceType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = when (type) {
        MaintenanceType.OIL_CHANGE -> "Oil change"
        MaintenanceType.TIRE_ROTATION -> "Tires"
        MaintenanceType.BRAKE_SERVICE -> "Brakes"
        MaintenanceType.ENGINE_TUNE_UP -> "Engine"
        MaintenanceType.BATTERY_REPLACEMENT -> "Battery"
        MaintenanceType.AIR_FILTER -> "Air filter"
        MaintenanceType.TRANSMISSION_SERVICE -> "Transmission"
        MaintenanceType.COOLANT_FLUSH -> "Cooling"
        MaintenanceType.GENERAL_INSPECTION -> "Inspection"
        MaintenanceType.REPAIR -> "Repair"
        MaintenanceType.OTHER -> "Other"
    }
    
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) ElectricTeal.copy(alpha = 0.15f) else DeepNavy.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) ElectricTeal else BorderDark.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getMaintenanceTypeIcon(type),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) ElectricTeal else TextSecondary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = if (isSelected) ElectricTeal else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun IntelligentCalculationCard(
    calculation: ReminderCalculation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepNavy.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ElectricTeal.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Smart calculation", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                if (calculation.usedLearnedPattern) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Based on your history",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = SuccessGreen
                        )
                    }
                }
            }
            
            // Due mileage
            InfoRow("Due mileage", "${String.format("%,d", calculation.dueMileage)} km")
            InfoRow("Km remaining", "${String.format("%,d", calculation.kmRemaining)} km")
            InfoRow("Days remaining", "${calculation.daysRemaining} days")
            
            // Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Priority", fontSize = 14.sp, color = TextSecondary)
                PriorityBadge(calculation.priority)
            }
            
            // Urgency score
            UrgencyBar(calculation.urgencyScore)
            
            // Cost estimation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estimated cost", fontSize = 14.sp, color = TextSecondary)
                Text(
                    "${calculation.estimatedCost.first.toInt()}-$${calculation.estimatedCost.second.toInt()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarningAmber
                )
            }
            
            // Description
            Text(
                calculation.description,
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateGray, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun GeminiSuggestionsCard(
    suggestions: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Gemini AI advice", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            
            suggestions.forEach { suggestion ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("•", color = WarningAmber, modifier = Modifier.padding(end = 8.dp))
                    Text(suggestion, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun CustomFieldsSection(
    customTitle: String,
    onTitleChange: (String) -> Unit,
    customDescription: String,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepNavy),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Customize (optional)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            
            AttractiveTextField(
                value = customTitle,
                onValueChange = onTitleChange,
                label = "Custom title",
                leadingIcon = Icons.AutoMirrored.Filled.StickyNote2,
                modifier = Modifier.fillMaxWidth()
            )
            
            AttractiveTextField(
                value = customDescription,
                onValueChange = onDescriptionChange,
                label = "Description",
                leadingIcon = Icons.Default.Description,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }
}

@Composable
private fun NotificationSettingsSection(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    daysBefore: Int,
    onDaysBeforeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepNavy),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = ElectricTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Notifications",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ElectricTeal,
                        checkedTrackColor = ElectricTeal.copy(alpha = 0.5f),
                        uncheckedThumbColor = SlateGray,
                        uncheckedTrackColor = BorderDark
                    )
                )
            }
            
            if (enabled) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Remind $daysBefore days before", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
private fun PriorityBadge(
    priority: com.example.autobrain.domain.model.ReminderPriority,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (priority) {
        com.example.autobrain.domain.model.ReminderPriority.CRITICAL -> ErrorRed to "CRITICAL"
        com.example.autobrain.domain.model.ReminderPriority.HIGH -> WarningAmber to "HIGH"
        com.example.autobrain.domain.model.ReminderPriority.MEDIUM -> ElectricTeal to "MEDIUM"
        com.example.autobrain.domain.model.ReminderPriority.LOW -> SuccessGreen to "LOW"
    }
    
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun UrgencyBar(
    score: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Urgency", fontSize = 14.sp, color = TextSecondary)
            Text("$score/100", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when {
                score >= 75 -> ErrorRed
                score >= 50 -> WarningAmber
                else -> SuccessGreen
            },
            trackColor = SlateGray
        )
    }
}

@Composable
private fun LoadingCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepNavy)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ElectricTeal)
            Spacer(Modifier.width(12.dp))
            Text("Calculating...", color = TextSecondary)
        }
    }
}

@Composable
private fun WarningCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Warning, null, tint = WarningAmber)
            Spacer(Modifier.width(8.dp))
            Text(message, fontSize = 13.sp, color = TextPrimary)
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Error, null, tint = ErrorRed)
            Spacer(Modifier.width(8.dp))
            Text(message, fontSize = 13.sp, color = TextPrimary)
        }
    }
}


