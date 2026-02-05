package com.example.autobrain.presentation.screens.carlog

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autobrain.domain.model.MaintenanceType
import com.example.autobrain.presentation.theme.DeepNavy
import com.example.autobrain.presentation.theme.ElectricTeal
import com.example.autobrain.presentation.theme.ErrorRed
import com.example.autobrain.presentation.theme.MidnightBlack
import com.example.autobrain.presentation.theme.TextMuted
import com.example.autobrain.presentation.theme.TextOnAccent
import com.example.autobrain.presentation.theme.TextPrimary
import com.example.autobrain.presentation.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
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
        modifier = modifier,
        containerColor = MidnightBlack,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Log Service",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ElectricTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightBlack,
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Type of maintenance
            ExposedDropdownMenuBox(
                expanded = showTypeMenu,
                onExpandedChange = { showTypeMenu = it }
            ) {
                AttractiveTextField(
                    value = getMaintenanceTypeLabel(selectedType),
                    onValueChange = {},
                    readOnly = true,
                    label = "Maintenance type",
                    leadingIcon = getMaintenanceTypeIcon(selectedType),
                    trailingIcon = { 
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = showTypeMenu
                        ) 
                    },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                )

                ExposedDropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false },
                    containerColor = DeepNavy
                ) {
                    MaintenanceType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        getMaintenanceTypeIcon(type),
                                        contentDescription = null,
                                        tint = ElectricTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(getMaintenanceTypeLabel(type), color = TextPrimary)
                                }
                            },
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
            AttractiveTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                leadingIcon = Icons.Default.Description,
                placeholder = "Ex: Motor oil change 5W-30",
                minLines = 2
            )

            // Date
            AttractiveTextField(
                value = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date(date)),
                onValueChange = {},
                readOnly = true,
                label = "Date",
                leadingIcon = Icons.Default.CalendarToday,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Select date",
                            tint = ElectricTeal
                        )
                    }
                }
            )

            // Mileage
            AttractiveTextField(
                value = mileage,
                onValueChange = { mileage = it.filter { char -> char.isDigit() } },
                label = "Kilometers",
                leadingIcon = Icons.Default.Speed,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("km", color = TextSecondary, fontWeight = FontWeight.Bold) }
            )

            // Cost
            AttractiveTextField(
                value = cost,
                onValueChange = { cost = it.filter { char -> char.isDigit() || char == '.' } },
                label = "Cost",
                leadingIcon = Icons.Default.AttachMoney,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("$", color = TextSecondary, fontWeight = FontWeight.Bold) }
            )

            // Service provider
            AttractiveTextField(
                value = serviceProvider,
                onValueChange = { serviceProvider = it },
                label = "Service Provider",
                leadingIcon = Icons.Default.Business,
                placeholder = "Garage or mechanic name"
            )

            // Notes
            AttractiveTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes (optional)",
                leadingIcon = Icons.AutoMirrored.Filled.StickyNote2,
                placeholder = "Additional notes...",
                minLines = 3
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
                    .height(60.dp),
                enabled = description.isNotBlank() && saveState !is SaveState.Saving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricTeal,
                    contentColor = MidnightBlack,
                    disabledContainerColor = Color(0xFF30363D),
                    disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp
                )
            ) {
                if (saveState is SaveState.Saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MidnightBlack
                    )
                } else {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        tint = MidnightBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Save Record",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightBlack
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
