package com.example.autobrain.presentation.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autobrain.presentation.navigation.Screen
import com.example.autobrain.presentation.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableIntStateOf(1) } // 1 = Basic info, 2 = Car info
    
    // Step 1 fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }
    var birthYear by remember { mutableStateOf("") }
    
    // Step 2 fields (Car info)
    var carMake by remember { mutableStateOf("") }
    var carModel by remember { mutableStateOf("") }
    var carYear by remember { mutableStateOf("") }
    
    val focusManager = LocalFocusManager.current
    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Calculate age
    val age = remember(birthYear) {
        try {
            val year = birthYear.toIntOrNull() ?: return@remember 0
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            currentYear - year
        } catch (e: Exception) {
            0
        }
    }
    
    val isAgeValid = age >= 18
    val passwordsMatch = password == confirmPassword && password.isNotBlank()
    val isStep1Valid = fullName.isNotBlank() && email.isNotBlank() && 
                       passwordsMatch && agreedToTerms && isAgeValid

    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (authState as AuthState.Error).message,
                    duration = SnackbarDuration.Short
                )
                viewModel.resetState()
            }
            else -> {}
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
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A1117),
                            Color(0xFF0D1419),
                            Color(0xFF0A1117)
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                // Logo section
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .blur(15.dp)
                            .alpha(0.4f)
                            .background(Color(0xFF00D9D9), CircleShape)
                    )
                    
                    AutoBrainLogoSmall(
                        modifier = Modifier.size(70.dp),
                        tint = Color(0xFF00D9D9)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "AutoBrain",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(40.dp))

                AnimatedVisibility(
                    visible = currentStep == 1,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Full Name field with cyan border
                        CyanBorderedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = "Full Name",
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email field
                        CyanBorderedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "Email Address",
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Birth Year field (for age verification)
                        CyanBorderedTextField(
                            value = birthYear,
                            onValueChange = { 
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    birthYear = it
                                }
                            },
                            placeholder = "Birth Year (e.g., 1990)",
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            isError = birthYear.length == 4 && !isAgeValid,
                            errorMessage = if (birthYear.length == 4 && !isAgeValid) "You must be 18 or older" else null
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password field with cyan border
                        CyanBorderedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Password",
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onPasswordVisibilityToggle = { passwordVisible = !passwordVisible }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confirm Password field
                        CyanBorderedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = "Confirm Password",
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                            onNext = { focusManager.clearFocus() },
                            isPassword = true,
                            passwordVisible = confirmPasswordVisible,
                            onPasswordVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                            isError = confirmPassword.isNotBlank() && !passwordsMatch,
                            errorMessage = if (confirmPassword.isNotBlank() && !passwordsMatch) "Passwords don't match" else null
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Create Account / Continue button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                currentStep = 2
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00D9D9),
                                contentColor = Color(0xFF0A1117),
                                disabledContainerColor = Color(0xFF00D9D9).copy(alpha = 0.4f),
                                disabledContentColor = Color(0xFF0A1117).copy(alpha = 0.5f)
                            ),
                            enabled = isStep1Valid
                        ) {
                            Text(
                                text = "Continue",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Terms checkbox
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
                                    .size(22.dp)
                                    .border(
                                        width = 2.dp,
                                        color = if (agreedToTerms) Color(0xFF00D9D9) else Color(0xFF374151),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .background(
                                        color = if (agreedToTerms) Color(0xFF00D9D9) else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (agreedToTerms) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF0A1117),
                                        modifier = Modifier.size(16.dp)
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
                    }
                }

                AnimatedVisibility(
                    visible = currentStep == 2,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tell us about your car",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "(Optional - you can add later)",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Car Make
                        CyanBorderedTextField(
                            value = carMake,
                            onValueChange = { carMake = it },
                            placeholder = "Car Make (e.g., Toyota)",
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Car Model
                        CyanBorderedTextField(
                            value = carModel,
                            onValueChange = { carModel = it },
                            placeholder = "Car Model (e.g., Corolla)",
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Car Year
                        CyanBorderedTextField(
                            value = carYear,
                            onValueChange = { 
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    carYear = it
                                }
                            },
                            placeholder = "Car Year (e.g., 2020)",
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                            onNext = { focusManager.clearFocus() }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Create Account button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.signUp(
                                    email = email,
                                    password = password,
                                    name = fullName,
                                    age = age,
                                    carMake = carMake,
                                    carModel = carModel,
                                    carYear = carYear.toIntOrNull() ?: 0
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00D9D9),
                                contentColor = Color(0xFF0A1117),
                                disabledContainerColor = Color(0xFF00D9D9).copy(alpha = 0.4f),
                                disabledContentColor = Color(0xFF0A1117).copy(alpha = 0.5f)
                            ),
                            enabled = authState !is AuthState.Loading
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF0A1117),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Create Account",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Skip button
                        TextButton(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.signUp(
                                    email = email,
                                    password = password,
                                    name = fullName,
                                    age = age,
                                    carMake = "",
                                    carModel = "",
                                    carYear = 0
                                )
                            }
                        ) {
                            Text(
                                text = "Skip for now",
                                color = Color(0xFF9CA3AF),
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Back button
                        TextButton(
                            onClick = { currentStep = 1 }
                        ) {
                            Text(
                                text = "Back",
                                color = Color(0xFF00D9D9),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (currentStep == 1) {
                    // Sign In link
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have account? ",
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp
                        )
                        TextButton(
                            onClick = { navController.popBackStack() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Sign In",
                                color = Color(0xFF00D9D9),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CyanBorderedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onNext: () -> Unit,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    color = Color(0xFF6B7280),
                    fontSize = 15.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) Color(0xFFFF6B6B) else Color(0xFF00D9D9),
                unfocusedBorderColor = if (isError) Color(0xFFFF6B6B).copy(alpha = 0.5f) else Color(0xFF00D9D9).copy(alpha = 0.6f),
                focusedContainerColor = Color(0xFF0A1117),
                unfocusedContainerColor = Color(0xFF0A1117),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF00D9D9)
            ),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext() },
                onDone = { onNext() }
            ),
            trailingIcon = if (isPassword && onPasswordVisibilityToggle != null) {
                {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1F2937))
                            .clickable { onPasswordVisibilityToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null
        )
        
        // Error message
        AnimatedVisibility(visible = isError && errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color(0xFFFF6B6B),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun AutoBrainLogoSmall(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF00D9D9)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidth = 2.dp.toPx()
        
        // Car body - front view
        val carPath = Path().apply {
            moveTo(width * 0.15f, height * 0.4f)
            lineTo(width * 0.25f, height * 0.22f)
            lineTo(width * 0.75f, height * 0.22f)
            lineTo(width * 0.85f, height * 0.4f)
            lineTo(width * 0.85f, height * 0.75f)
            lineTo(width * 0.15f, height * 0.75f)
            close()
        }
        
        drawPath(carPath, tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        
        // Left mirror
        drawLine(tint, Offset(width * 0.08f, height * 0.45f), Offset(width * 0.15f, height * 0.45f), strokeWidth)
        drawLine(tint, Offset(width * 0.08f, height * 0.45f), Offset(width * 0.08f, height * 0.55f), strokeWidth)
        drawLine(tint, Offset(width * 0.08f, height * 0.55f), Offset(width * 0.15f, height * 0.55f), strokeWidth)
        
        // Right mirror
        drawLine(tint, Offset(width * 0.85f, height * 0.45f), Offset(width * 0.92f, height * 0.45f), strokeWidth)
        drawLine(tint, Offset(width * 0.92f, height * 0.45f), Offset(width * 0.92f, height * 0.55f), strokeWidth)
        drawLine(tint, Offset(width * 0.85f, height * 0.55f), Offset(width * 0.92f, height * 0.55f), strokeWidth)
        
        // Windshield
        val windshieldPath = Path().apply {
            moveTo(width * 0.28f, height * 0.26f)
            lineTo(width * 0.72f, height * 0.26f)
            lineTo(width * 0.78f, height * 0.42f)
            lineTo(width * 0.22f, height * 0.42f)
            close()
        }
        drawPath(windshieldPath, tint.copy(alpha = 0.6f), style = Stroke(width = strokeWidth * 0.7f))
        
        // Circuit pattern
        drawLine(tint.copy(alpha = 0.5f), Offset(width * 0.3f, height * 0.52f), Offset(width * 0.7f, height * 0.52f), strokeWidth * 0.4f)
        drawLine(tint.copy(alpha = 0.5f), Offset(width * 0.3f, height * 0.6f), Offset(width * 0.7f, height * 0.6f), strokeWidth * 0.4f)
        drawLine(tint.copy(alpha = 0.5f), Offset(width * 0.4f, height * 0.48f), Offset(width * 0.4f, height * 0.68f), strokeWidth * 0.4f)
        drawLine(tint.copy(alpha = 0.5f), Offset(width * 0.5f, height * 0.48f), Offset(width * 0.5f, height * 0.68f), strokeWidth * 0.4f)
        drawLine(tint.copy(alpha = 0.5f), Offset(width * 0.6f, height * 0.48f), Offset(width * 0.6f, height * 0.68f), strokeWidth * 0.4f)
        
        // Nodes
        drawCircle(tint, 2.dp.toPx(), Offset(width * 0.4f, height * 0.52f))
        drawCircle(tint, 2.dp.toPx(), Offset(width * 0.5f, height * 0.52f))
        drawCircle(tint, 2.dp.toPx(), Offset(width * 0.6f, height * 0.52f))
        drawCircle(tint, 2.dp.toPx(), Offset(width * 0.4f, height * 0.6f))
        drawCircle(tint, 2.dp.toPx(), Offset(width * 0.6f, height * 0.6f))
        
        // Plus icon in top right
        val plusCenterX = width * 0.85f
        val plusCenterY = height * 0.18f
        val plusSize = width * 0.06f
        
        drawCircle(tint, plusSize * 1.2f, Offset(plusCenterX, plusCenterY), style = Stroke(width = strokeWidth * 0.7f))
        drawLine(tint, Offset(plusCenterX - plusSize * 0.5f, plusCenterY), Offset(plusCenterX + plusSize * 0.5f, plusCenterY), strokeWidth * 0.7f, cap = StrokeCap.Round)
        drawLine(tint, Offset(plusCenterX, plusCenterY - plusSize * 0.5f), Offset(plusCenterX, plusCenterY + plusSize * 0.5f), strokeWidth * 0.7f, cap = StrokeCap.Round)
    }
}
