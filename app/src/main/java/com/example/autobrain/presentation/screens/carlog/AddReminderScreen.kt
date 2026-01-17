package com.example.autobrain.presentation.screens.carlog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.autobrain.presentation.theme.*

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
        topBar = {
            TopAppBar(
                title = { Text("Ajouter un rappel", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = ElectricTeal)
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
                    IntelligentCalculationCard(calcState.calculation)
                }
                is CalculationState.Error -> {
                    ErrorCard(calcState.message)
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
                        Text("Analyse Gemini en cours...", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                is GeminiSuggestionsState.Success -> {
                    GeminiSuggestionsCard(geminiState.suggestions)
                }
                is GeminiSuggestionsState.Error -> {
                    Text("❌ ${geminiState.message}", color = ErrorRed, fontSize = 12.sp)
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
                WarningCard((uiState as AddReminderUiState.Warning).message)
            }
            
            // Error message
            if (uiState is AddReminderUiState.Error) {
                ErrorCard((uiState as AddReminderUiState.Error).message)
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
                        Text("Créer le rappel", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MaintenanceTypeSelector(
    selectedType: MaintenanceType?,
    onTypeSelected: (MaintenanceType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepNavy),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Build, null, tint = ElectricTeal, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Type d'entretien",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Grid of maintenance types
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MaintenanceType.values().filter { it != MaintenanceType.REPAIR && it != MaintenanceType.OTHER }
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
        MaintenanceType.OIL_CHANGE -> "Vidange"
        MaintenanceType.TIRE_ROTATION -> "Pneus"
        MaintenanceType.BRAKE_SERVICE -> "Freins"
        MaintenanceType.ENGINE_TUNE_UP -> "Moteur"
        MaintenanceType.BATTERY_REPLACEMENT -> "Batterie"
        MaintenanceType.AIR_FILTER -> "Filtre à air"
        MaintenanceType.TRANSMISSION_SERVICE -> "Transmission"
        MaintenanceType.COOLANT_FLUSH -> "Refroidissement"
        MaintenanceType.GENERAL_INSPECTION -> "Inspection"
        MaintenanceType.REPAIR -> "Réparation"
        MaintenanceType.OTHER -> "Autre"
    }
    
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) ElectricTeal.copy(alpha = 0.2f) else SlateGray,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) ElectricTeal else BorderDark
        )
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = if (isSelected) ElectricTeal else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun IntelligentCalculationCard(calculation: ReminderCalculation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepNavy),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Calcul intelligent", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                if (calculation.usedLearnedPattern) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Basé sur votre historique",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = SuccessGreen
                        )
                    }
                }
            }
            
            // Due mileage
            InfoRow("Kilométrage dû", "${String.format("%,d", calculation.dueMileage)} km")
            InfoRow("Km restants", "${String.format("%,d", calculation.kmRemaining)} km")
            InfoRow("Jours restants", "${calculation.daysRemaining} jours")
            
            // Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Priorité", fontSize = 14.sp, color = TextSecondary)
                PriorityBadge(calculation.priority)
            }
            
            // Urgency score
            UrgencyBar(calculation.urgencyScore)
            
            // Cost estimation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Coût estimé", fontSize = 14.sp, color = TextSecondary)
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
private fun GeminiSuggestionsCard(suggestions: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Conseils IA Gemini", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
    onDescriptionChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepNavy),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Personnaliser (optionnel)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            
            OutlinedTextField(
                value = customTitle,
                onValueChange = onTitleChange,
                label = { Text("Titre personnalisé") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricTeal,
                    unfocusedBorderColor = BorderDark,
                    focusedLabelColor = ElectricTeal,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            OutlinedTextField(
                value = customDescription,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricTeal,
                    unfocusedBorderColor = BorderDark,
                    focusedLabelColor = ElectricTeal,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }
    }
}

@Composable
private fun NotificationSettingsSection(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    daysBefore: Int,
    onDaysBeforeChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepNavy),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notifications", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ElectricTeal,
                        checkedTrackColor = ElectricTeal.copy(alpha = 0.5f)
                    )
                )
            }
            
            if (enabled) {
                Spacer(Modifier.height(8.dp))
                Text("Rappeler $daysBefore jours avant", fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
private fun PriorityBadge(priority: com.example.autobrain.domain.model.ReminderPriority) {
    val (color, label) = when (priority) {
        com.example.autobrain.domain.model.ReminderPriority.CRITICAL -> ErrorRed to "CRITIQUE"
        com.example.autobrain.domain.model.ReminderPriority.HIGH -> WarningAmber to "HAUTE"
        com.example.autobrain.domain.model.ReminderPriority.MEDIUM -> ElectricTeal to "MOYENNE"
        com.example.autobrain.domain.model.ReminderPriority.LOW -> SuccessGreen to "BASSE"
    }
    
    Surface(
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
private fun UrgencyBar(score: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Urgence", fontSize = 14.sp, color = TextSecondary)
            Text("$score/100", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = score / 100f,
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
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepNavy)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ElectricTeal)
            Spacer(Modifier.width(12.dp))
            Text("Calcul en cours...", color = TextSecondary)
        }
    }
}

@Composable
private fun WarningCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Error, null, tint = ErrorRed)
            Spacer(Modifier.width(8.dp))
            Text(message, fontSize = 13.sp, color = TextPrimary)
        }
    }
}


