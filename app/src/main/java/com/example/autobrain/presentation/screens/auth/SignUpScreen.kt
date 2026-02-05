package com.example.autobrain.presentation.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.autobrain.presentation.navigation.Screen
import com.example.autobrain.presentation.theme.AutoBrainTheme
import com.example.autobrain.presentation.components.AnimatedBackground
import com.example.autobrain.presentation.components.AnimatedEntrance
import com.example.autobrain.presentation.components.LogoSection
import java.util.Calendar

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    AutoBrainTheme {
        SignUpScreenContent(
            navController = rememberNavController()
        )
    }
}

@Composable
fun SignUpScreenContent(
    navController: NavController,
    onSignUp: (String, String, String, Int, String, String, Int) -> Unit = { _, _, _, _, _, _, _ -> },
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onResetError: () -> Unit = {}
) {
    var currentStep by remember { mutableIntStateOf(1) }
    
    // Step 1: Basic Info
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }
    
    // Step 2: Car Info
    var carMake by remember { mutableStateOf("") }
    var carModel by remember { mutableStateOf("") }
    var carYear by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    val age = remember(birthYear) {
        val year = birthYear.toIntOrNull() ?: 0
        if (year > 1900) {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            currentYear - year
        } else 0
    }
    
    val isAgeValid = age >= 18
    val passwordsMatch = password == confirmPassword && password.isNotBlank()
    val isStep1Valid = fullName.isNotBlank() && email.isNotBlank() && 
                       passwordsMatch && agreedToTerms && isAgeValid

    // Entrance animation state
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
                Spacer(modifier = Modifier.height(48.dp))

                AnimatedEntrance(visible = visibleState.value, delay = 100) {
                    LogoSection()
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedEntrance(visible = visibleState.value, delay = 200) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (currentStep == 1) "Create Account" else "Car Details",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = if (currentStep == 1) "Join the AutoBrain community" else "Tell us what you're driving",
                            fontSize = 16.sp,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                AnimatedEntrance(visible = visibleState.value, delay = 250) {
                    StepIndicator(currentStep = currentStep, totalSteps = 2)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Main form card
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
                            if (currentStep == 1) {
                                // Step 1 Fields
                                CustomTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    placeholder = "Full Name",
                                    imeAction = ImeAction.Next,
                                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                CustomTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    placeholder = "Email Address",
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next,
                                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                
                                CustomTextField(
                                    value = birthYear,
                                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) birthYear = it },
                                    placeholder = "Birth Year (YYYY)",
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                                    isError = birthYear.length == 4 && !isAgeValid,
                                    errorMessage = if (birthYear.length == 4 && !isAgeValid) "Must be 18 or older" else null
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                CustomTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    placeholder = "Password",
                                    isPassword = true,
                                    passwordVisible = passwordVisible,
                                    onPasswordToggle = { passwordVisible = !passwordVisible },
                                    imeAction = ImeAction.Next,
                                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                CustomTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    placeholder = "Confirm Password",
                                    isPassword = true,
                                    passwordVisible = confirmPasswordVisible,
                                    onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                                    imeAction = ImeAction.Done,
                                    onImeAction = { focusManager.clearFocus() },
                                    isError = confirmPassword.isNotBlank() && !passwordsMatch,
                                    errorMessage = if (confirmPassword.isNotBlank() && !passwordsMatch) "Passwords don't match" else null
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Terms Checkbox
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { agreedToTerms = !agreedToTerms },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .border(
                                                width = 1.5.dp,
                                                color = if (agreedToTerms) Color(0xFF00D9D9) else Color(0xFF374151),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .background(
                                                color = if (agreedToTerms) Color(0xFF00D9D9).copy(alpha = 0.1f) else Color.Transparent,
                                                shape = RoundedCornerShape(6.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (agreedToTerms) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF00D9D9),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "I agree to Terms of Service",
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                MainButton(
                                    text = "Continue",
                                    onClick = { currentStep = 2 },
                                    loading = false,
                                    enabled = isStep1Valid
                                )
                            } else {
                                // Step 2 Fields
                                CustomTextField(
                                    value = carMake,
                                    onValueChange = { carMake = it },
                                    placeholder = "Car Make (e.g., Audi)",
                                    imeAction = ImeAction.Next,
                                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                CustomTextField(
                                    value = carModel,
                                    onValueChange = { carModel = it },
                                    placeholder = "Car Model (e.g., RS3)",
                                    imeAction = ImeAction.Next,
                                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                CustomTextField(
                                    value = carYear,
                                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) carYear = it },
                                    placeholder = "Car Year (e.g., 2024)",
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                    onImeAction = { focusManager.clearFocus() }
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                MainButton(
                                    text = "Create Account",
                                    onClick = { 
                                        onSignUp(email, password, fullName, age, carMake, carModel, carYear.toIntOrNull() ?: 0)
                                    },
                                    loading = isLoading
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                TextButton(
                                    onClick = { 
                                        onSignUp(email, password, fullName, age, "", "", 0)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Skip for now", color = Color(0xFF9CA3AF))
                                }
                                
                                TextButton(
                                    onClick = { currentStep = 1 },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Back to info", color = Color(0xFF00D9D9))
                                }
                            }
                        }
                    }
                }

                if (currentStep == 1) {
                    Spacer(modifier = Modifier.height(32.dp))

                    AnimatedEntrance(visible = visibleState.value, delay = 600) {
                        OrDivider()
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedEntrance(visible = visibleState.value, delay = 700) {
                        SocialSection()
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    AnimatedEntrance(visible = visibleState.value, delay = 800) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Already have an account? ", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                            TextButton(onClick = { navController.popBackStack() }, contentPadding = PaddingValues(0.dp)) {
                                Text("Sign In", color = Color(0xFF00D9D9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    SignUpScreenContent(
        navController = navController,
        onSignUp = { email, password, name, age, make, model, year ->
            viewModel.signUp(email, password, name, age, make, model, year)
        },
        isLoading = authState is AuthState.Loading,
        errorMessage = (authState as? AuthState.Error)?.message,
        onResetError = { viewModel.resetState() }
    )
}
