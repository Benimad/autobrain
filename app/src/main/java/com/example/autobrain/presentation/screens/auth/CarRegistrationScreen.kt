package com.example.autobrain.presentation.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autobrain.core.utils.AdaptiveSpacing
import com.example.autobrain.core.utils.adaptiveButtonHeight
import com.example.autobrain.core.utils.adaptiveCornerRadius
import com.example.autobrain.core.utils.adaptiveTextScale
import com.example.autobrain.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarRegistrationScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var carMake by remember { mutableStateOf("") }
    var carModel by remember { mutableStateOf("") }
    var carYear by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val carBrands = listOf(
        "Audi", "BMW", "Mercedes", "Toyota", "Honda", "Ford", "Volkswagen",
        "Nissan", "Hyundai", "Kia", "Mazda", "Subaru", "Lexus", "Porsche",
        "Tesla", "Volvo", "Jaguar", "Land Rover", "Peugeot", "Renault",
        "Dacia", "Fiat", "Alfa Romeo", "Chevrolet", "Dodge", "Jeep"
    )
    
    Scaffold(
        containerColor = MidnightBlack,
        topBar = {
            TopAppBar(
                title = { Text("Add Your Car", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlack)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = AdaptiveSpacing.large())
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(AdaptiveSpacing.large()))
            
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = ElectricTeal,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))
            
            Text(
                text = "Tell us about your car",
                fontSize = (24.sp.value * adaptiveTextScale()).sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "We'll fetch a professional image for your profile",
                fontSize = (14.sp.value * adaptiveTextScale()).sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(AdaptiveSpacing.extraLarge()))
            
            // Car Make Dropdown
            var expandedMake by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedMake,
                onExpandedChange = { expandedMake = it }
            ) {
                OutlinedTextField(
                    value = carMake,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Car Brand", color = TextSecondary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMake) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = SlateGray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(adaptiveCornerRadius())
                )
                ExposedDropdownMenu(
                    expanded = expandedMake,
                    onDismissRequest = { expandedMake = false }
                ) {
                    carBrands.forEach { brand ->
                        DropdownMenuItem(
                            text = { Text(brand) },
                            onClick = {
                                carMake = brand
                                expandedMake = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))
            
            // Car Model
            OutlinedTextField(
                value = carModel,
                onValueChange = { carModel = it },
                label = { Text("Model", color = TextSecondary) },
                placeholder = { Text("e.g., RS3, M3, Corolla", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricTeal,
                    unfocusedBorderColor = SlateGray,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(adaptiveCornerRadius())
            )
            
            Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))
            
            // Car Year
            OutlinedTextField(
                value = carYear,
                onValueChange = { if (it.length <= 4) carYear = it },
                label = { Text("Year", color = TextSecondary) },
                placeholder = { Text("e.g., 2024", color = TextMuted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricTeal,
                    unfocusedBorderColor = SlateGray,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(adaptiveCornerRadius())
            )
            
            Spacer(modifier = Modifier.height(AdaptiveSpacing.extraLarge()))
            
            // Save Button
            Button(
                onClick = {
                    if (carMake.isNotBlank() && carModel.isNotBlank() && carYear.isNotBlank()) {
                        isLoading = true
                        viewModel.saveCarDetails(
                            make = carMake,
                            model = carModel,
                            year = carYear.toIntOrNull() ?: 0,
                            onSuccess = {
                                isLoading = false
                                navController.popBackStack()
                            },
                            onError = {
                                isLoading = false
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(adaptiveButtonHeight()),
                enabled = carMake.isNotBlank() && carModel.isNotBlank() && carYear.length == 4 && !isLoading,
                shape = RoundedCornerShape(adaptiveCornerRadius()),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricTeal,
                    contentColor = MidnightBlack,
                    disabledContainerColor = SlateGray
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MidnightBlack,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save & Continue",
                        fontSize = (16.sp.value * adaptiveTextScale()).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))
            
            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    text = "Skip for now",
                    color = TextSecondary,
                    fontSize = (14.sp.value * adaptiveTextScale()).sp
                )
            }
            
            Spacer(modifier = Modifier.height(AdaptiveSpacing.large()))
        }
    }
}
