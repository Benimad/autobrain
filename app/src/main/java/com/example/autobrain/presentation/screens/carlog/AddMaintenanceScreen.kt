package com.example.autobrain.presentation.screens.carlog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autobrain.domain.model.MaintenanceType
import com.example.autobrain.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceScreen(
    navController: NavController,
    viewModel: CarLogViewModel = hiltViewModel()
) {
    var selectedType by remember { mutableStateOf(MaintenanceType.OIL_CHANGE) }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var mileage by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var serviceProvider by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showTypeMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = MidnightBlack,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Add Maintenance",
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ElectricTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = ElectricTeal
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type of maintenance
            ExposedDropdownMenuBox(
                expanded = showTypeMenu,
                onExpandedChange = { showTypeMenu = it }
            ) {
                OutlinedTextField(
                    value = getMaintenanceTypeLabel(selectedType),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Maintenance type", color = TextSecondary) },
                    trailingIcon = { 
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = showTypeMenu
                        ) 
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = TextMuted,
                        focusedLabelColor = ElectricTeal,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = ElectricTeal,
                        focusedContainerColor = DeepNavy,
                        unfocusedContainerColor = DeepNavy
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false },
                    containerColor = DeepNavy
                ) {
                    MaintenanceType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(getMaintenanceTypeLabel(type), color = TextPrimary) },
                            onClick = {
                                selectedType = type
                                showTypeMenu = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = TextPrimary
                            )
                        )
                    }
                }
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                placeholder = { Text("Ex: Motor oil change 5W-30", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = ElectricTeal,
                    unfocusedBorderColor = TextMuted,
                    focusedLabelColor = ElectricTeal,
                    unfocusedLabelColor = TextSecondary,
                    cursorColor = ElectricTeal,
                    focusedContainerColor = DeepNavy,
                    unfocusedContainerColor = DeepNavy,
                    focusedPlaceholderColor = TextMuted,
                    unfocusedPlaceholderColor = TextMuted
                )
            )

            // Date
            OutlinedTextField(
                value = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date(date)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date", color = TextSecondary) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Select date",
                            tint = ElectricTeal
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = ElectricTeal,
                    unfocusedBorderColor = TextMuted,
                    focusedLabelColor = ElectricTeal,
                    unfocusedLabelColor = TextSecondary,
                    cursorColor = ElectricTeal,
                    focusedContainerColor = DeepNavy,
                    unfocusedContainerColor = DeepNavy
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Mileage
            OutlinedTextField(
                value = mileage,
                onValueChange = { mileage = it.filter { char -> char.isDigit() } },
                label = { Text("Kilometers", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("km", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = ElectricTeal,
                    unfocusedBorderColor = TextMuted,
                    focusedLabelColor = ElectricTeal,
                    unfocusedLabelColor = TextSecondary,
                    cursorColor = ElectricTeal,
                    focusedContainerColor = DeepNavy,
                    unfocusedContainerColor = DeepNavy
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Cost
            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it.filter { char -> char.isDigit() || char == '.' } },
                label = { Text("Cost", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("$", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = ElectricTeal,
                    unfocusedBorderColor = TextMuted,
                    focusedLabelColor = ElectricTeal,
                    unfocusedLabelColor = TextSecondary,
                    cursorColor = ElectricTeal,
                    focusedContainerColor = DeepNavy,
                    unfocusedContainerColor = DeepNavy
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Service provider
            OutlinedTextField(
                value = serviceProvider,
                onValueChange = { serviceProvider = it },
                label = { Text("Service Provider", color = TextSecondary) },
                placeholder = { Text("Garage or mechanic name", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = ElectricTeal,
                    unfocusedBorderColor = TextMuted,
                    focusedLabelColor = ElectricTeal,
                    unfocusedLabelColor = TextSecondary,
                    cursorColor = ElectricTeal,
                    focusedContainerColor = DeepNavy,
                    unfocusedContainerColor = DeepNavy,
                    focusedPlaceholderColor = TextMuted,
                    unfocusedPlaceholderColor = TextMuted
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("Additional notes...", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = ElectricTeal,
                    unfocusedBorderColor = TextMuted,
                    focusedLabelColor = ElectricTeal,
                    unfocusedLabelColor = TextSecondary,
                    cursorColor = ElectricTeal,
                    focusedContainerColor = DeepNavy,
                    unfocusedContainerColor = DeepNavy,
                    focusedPlaceholderColor = TextMuted,
                    unfocusedPlaceholderColor = TextMuted
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Error message
            if (saveState is SaveState.Error) {
                Text(
                    text = (saveState as SaveState.Error).message,
                    color = ErrorRed
                )
            }

            // Save button
            Button(
                onClick = {
                    viewModel.addMaintenanceRecord(
                        type = selectedType,
                        description = description,
                        date = date,
                        mileage = mileage.toIntOrNull() ?: 0,
                        cost = cost.toDoubleOrNull() ?: 0.0,
                        serviceProvider = serviceProvider,
                        notes = notes
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = description.isNotBlank() && saveState !is SaveState.Saving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricTeal,
                    contentColor = TextOnAccent,
                    disabledContainerColor = TextMuted,
                    disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (saveState is SaveState.Saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TextOnAccent
                    )
                } else {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        tint = TextOnAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Save",
                        color = TextOnAccent
                    )
                }
            }
        }
    }
    
    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            date = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = ElectricTeal)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = DeepNavy
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = DeepNavy,
                    titleContentColor = TextPrimary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = TextPrimary,
                    yearContentColor = TextPrimary,
                    currentYearContentColor = ElectricTeal,
                    selectedYearContentColor = TextOnAccent,
                    selectedYearContainerColor = ElectricTeal,
                    dayContentColor = TextPrimary,
                    selectedDayContentColor = TextOnAccent,
                    selectedDayContainerColor = ElectricTeal,
                    todayContentColor = ElectricTeal,
                    todayDateBorderColor = ElectricTeal
                )
            )
        }
    }
}

fun getMaintenanceTypeLabel(type: MaintenanceType): String {
    return when (type) {
        MaintenanceType.OIL_CHANGE -> "Oil change"
        MaintenanceType.TIRE_ROTATION -> "Tire rotation"
        MaintenanceType.BRAKE_SERVICE -> "Brake service"
        MaintenanceType.ENGINE_TUNE_UP -> "Engine tune-up"
        MaintenanceType.BATTERY_REPLACEMENT -> "Battery replacement"
        MaintenanceType.AIR_FILTER -> "Air filter"
        MaintenanceType.TRANSMISSION_SERVICE -> "Transmission service"
        MaintenanceType.COOLANT_FLUSH -> "Coolant flush"
        MaintenanceType.GENERAL_INSPECTION -> "General inspection"
        MaintenanceType.REPAIR -> "Repair"
        MaintenanceType.OTHER -> "Other"
    }
}
