package com.example.autobrain.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.autobrain.presentation.components.AnimatedBackground
import com.example.autobrain.presentation.components.AnimatedEntrance

@Preview(showBackground = true)
@Composable
fun CarRegistrationScreenPreview() {
    CarRegistrationScreenContent(
        navController = rememberNavController()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarRegistrationScreenContent(
    navController: NavController,
    onSave: (String, String, Int) -> Unit = { _, _, _ -> },
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onResetError: () -> Unit = {}
) {
    var carMake by remember { mutableStateOf("") }
    var carModel by remember { mutableStateOf("") }
    var carYear by remember { mutableStateOf("") }
    
    val carBrands = listOf(
        "Audi", "BMW", "Mercedes", "Toyota", "Honda", "Ford", "Volkswagen",
        "Nissan", "Hyundai", "Kia", "Mazda", "Subaru", "Lexus", "Porsche",
        "Tesla", "Volvo", "Jaguar", "Land Rover", "Peugeot", "Renault",
        "Dacia", "Fiat", "Alfa Romeo", "Chevrolet", "Dodge", "Jeep"
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val visibleState = remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visibleState.value = true
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onResetError()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Your Car", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF2D1B1B),
                    contentColor = Color(0xFFFF6B6B),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A1117))
                .padding(padding)
        ) {
            AnimatedBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                AnimatedEntrance(visible = visibleState.value, delay = 100) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFF00D9D9).copy(alpha = 0.1f))
                            .border(1.dp, Color(0xFF00D9D9).copy(alpha = 0.2f), RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color(0xFF00D9D9),
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedEntrance(visible = visibleState.value, delay = 200) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Vehicle Info",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Customize your AutoBrain experience",
                            fontSize = 16.sp,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Form card
                AnimatedEntrance(visible = visibleState.value, delay = 300) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF1F2937).copy(alpha = 0.4f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                            .padding(24.dp)
                    ) {
                        Column {
                            // Car Make Dropdown (Styled to match CustomTextField)
                            var expandedMake by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expandedMake,
                                onExpandedChange = { expandedMake = it }
                            ) {
                                OutlinedTextField(
                                    value = carMake,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("Select Brand", color = Color(0xFF6B7280), fontSize = 15.sp) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMake) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(58.dp)
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00D9D9).copy(alpha = 0.5f),
                                        unfocusedBorderColor = Color(0xFF374151),
                                        focusedContainerColor = Color(0xFF161E29),
                                        unfocusedContainerColor = Color(0xFF1F2937).copy(alpha = 0.6f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = Color(0xFF00D9D9)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedMake,
                                    onDismissRequest = { expandedMake = false },
                                    modifier = Modifier.background(Color(0xFF1F2937))
                                ) {
                                    carBrands.forEach { brand ->
                                        DropdownMenuItem(
                                            text = { Text(brand, color = Color.White) },
                                            onClick = {
                                                carMake = brand
                                                expandedMake = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            CustomTextField(
                                value = carModel,
                                onValueChange = { carModel = it },
                                placeholder = "Model (e.g., RS3, M3)",
                                imeAction = ImeAction.Next
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            CustomTextField(
                                value = carYear,
                                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) carYear = it },
                                placeholder = "Year (e.g., 2024)",
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            MainButton(
                                text = "Save & Continue",
                                onClick = { 
                                    onSave(carMake, carModel, carYear.toIntOrNull() ?: 0)
                                },
                                loading = isLoading,
                                enabled = carMake.isNotBlank() && carModel.isNotBlank() && carYear.length == 4
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            TextButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Skip for now", color = Color(0xFF9CA3AF))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CarRegistrationScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }

    CarRegistrationScreenContent(
        navController = navController,
        onSave = { make, model, year ->
            isLoading = true
            viewModel.saveCarDetails(
                make = make,
                model = model,
                year = year,
                onSuccess = {
                    isLoading = false
                    navController.popBackStack()
                },
                onError = {
                    isLoading = false
                }
            )
        },
        isLoading = isLoading,
        errorMessage = (authState as? AuthState.Error)?.message,
        onResetError = { viewModel.resetState() }
    )
}
