package com.example.autobrain.presentation.screens.auth

import androidx.compose.animation.core.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                Spacer(modifier = Modifier.height(80.dp))

                // Logo section
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow effect
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .blur(15.dp)
                            .alpha(0.4f)
                            .background(Color(0xFF00D9D9), CircleShape)
                    )
                    
                    // Car icon with plus
                    AutoBrainLogo(
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFF00D9D9)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "AutoBrain",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(60.dp))

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text(
                            "Email Address",
                            color = Color(0xFF6B7280),
                            fontSize = 15.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF374151),
                        unfocusedBorderColor = Color(0xFF374151),
                        focusedContainerColor = Color(0xFF1F2937),
                        unfocusedContainerColor = Color(0xFF1F2937),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF00D9D9)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = {
                        Text(
                            "Password",
                            color = Color(0xFF6B7280),
                            fontSize = 15.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF374151),
                        unfocusedBorderColor = Color(0xFF374151),
                        focusedContainerColor = Color(0xFF1F2937),
                        unfocusedContainerColor = Color(0xFF1F2937),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF00D9D9)
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (email.isNotBlank() && password.isNotBlank()) {
                                viewModel.signIn(email, password)
                            }
                        }
                    ),
                    trailingIcon = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF374151))
                                .clickable { passwordVisible = !passwordVisible },
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
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sign In button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.signIn(email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00D9D9),
                        contentColor = Color(0xFF0A1117),
                        disabledContainerColor = Color(0xFF00D9D9).copy(alpha = 0.5f),
                        disabledContentColor = Color(0xFF0A1117).copy(alpha = 0.5f)
                    ),
                    enabled = authState !is AuthState.Loading && email.isNotBlank() && password.isNotBlank()
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF0A1117),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Sign In",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Forgot Password
                TextButton(
                    onClick = { /* TODO: Navigate to forgot password */ }
                ) {
                    Text(
                        text = "Forgot Password?",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Social login section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Google
                    SocialLoginButton(
                        onClick = { /* TODO: Google sign in */ },
                        content = {
                            GoogleIcon(modifier = Modifier.size(22.dp))
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Twitter/X bird
                    SocialLoginButton(
                        onClick = { /* TODO: Twitter sign in */ },
                        content = {
                            TwitterIcon(modifier = Modifier.size(20.dp))
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // X (formerly Twitter)
                    SocialLoginButton(
                        onClick = { /* TODO: X sign in */ },
                        content = {
                            XIcon(modifier = Modifier.size(18.dp))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sign Up link
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp
                    )
                    TextButton(
                        onClick = {
                            navController.navigate(Screen.SignUp.route)
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Sign Up",
                            color = Color(0xFF00D9D9),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SocialLoginButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFF1F2937))
            .border(1.dp, Color(0xFF374151), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun AutoBrainLogo(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF00D9D9)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidth = 2.5.dp.toPx()
        
        // Car body - front view simplified
        val carPath = Path().apply {
            // Main body
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
        
        // Circuit pattern inside
        drawLine(tint.copy(alpha = 0.5f), Offset(width * 0.3f, height * 0.52f), Offset(width * 0.7f, height * 0.52f), strokeWidth * 0.4f)
        drawLine(tint.copy(alpha = 0.5f), Offset(width * 0.3f, height * 0.6f), Offset(width * 0.7f, height * 0.6f), strokeWidth * 0.4f)
        drawLine(tint.copy(alpha = 0.5f), Offset(width * 0.4f, height * 0.48f), Offset(width * 0.4f, height * 0.68f), strokeWidth * 0.4f)
        drawLine(tint.copy(alpha = 0.5f), Offset(width * 0.5f, height * 0.48f), Offset(width * 0.5f, height * 0.68f), strokeWidth * 0.4f)
        drawLine(tint.copy(alpha = 0.5f), Offset(width * 0.6f, height * 0.48f), Offset(width * 0.6f, height * 0.68f), strokeWidth * 0.4f)
        
        // Nodes
        drawCircle(tint, 2.5.dp.toPx(), Offset(width * 0.4f, height * 0.52f))
        drawCircle(tint, 2.5.dp.toPx(), Offset(width * 0.5f, height * 0.52f))
        drawCircle(tint, 2.5.dp.toPx(), Offset(width * 0.6f, height * 0.52f))
        drawCircle(tint, 2.5.dp.toPx(), Offset(width * 0.4f, height * 0.6f))
        drawCircle(tint, 2.5.dp.toPx(), Offset(width * 0.6f, height * 0.6f))
        
        // Plus icon in top right
        val plusCenterX = width * 0.85f
        val plusCenterY = height * 0.18f
        val plusSize = width * 0.08f
        
        // Circle around plus
        drawCircle(tint, plusSize * 1.2f, Offset(plusCenterX, plusCenterY), style = Stroke(width = strokeWidth * 0.8f))
        
        // Plus sign
        drawLine(tint, Offset(plusCenterX - plusSize * 0.5f, plusCenterY), Offset(plusCenterX + plusSize * 0.5f, plusCenterY), strokeWidth * 0.8f, cap = StrokeCap.Round)
        drawLine(tint, Offset(plusCenterX, plusCenterY - plusSize * 0.5f), Offset(plusCenterX, plusCenterY + plusSize * 0.5f), strokeWidth * 0.8f, cap = StrokeCap.Round)
    }
}

@Composable
private fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val size = size.minDimension
        val strokeWidth = size * 0.15f
        
        // Google G - simplified colored version
        val colors = listOf(
            Color(0xFFEA4335), // Red
            Color(0xFFFBBC05), // Yellow
            Color(0xFF34A853), // Green
            Color(0xFF4285F4)  // Blue
        )
        
        // Draw the G shape with 4 colors
        val radius = size * 0.4f
        val center = Offset(size / 2, size / 2)
        
        // Red arc (top right)
        drawArc(
            color = colors[0],
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        
        // Yellow arc (bottom right)
        drawArc(
            color = colors[1],
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        
        // Green arc (bottom left)
        drawArc(
            color = colors[2],
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        
        // Blue arc (top left) - partial
        drawArc(
            color = colors[3],
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        
        // Horizontal line for G
        drawLine(
            color = colors[3],
            start = Offset(center.x, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
private fun TwitterIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val color = Color(0xFF9CA3AF)
        
        // Simplified Twitter bird
        val path = Path().apply {
            // Bird body
            moveTo(size.width * 0.1f, size.height * 0.7f)
            cubicTo(
                size.width * 0.25f, size.height * 0.8f,
                size.width * 0.45f, size.height * 0.75f,
                size.width * 0.5f, size.height * 0.55f
            )
            cubicTo(
                size.width * 0.52f, size.height * 0.45f,
                size.width * 0.6f, size.height * 0.35f,
                size.width * 0.9f, size.height * 0.25f
            )
            cubicTo(
                size.width * 0.75f, size.height * 0.32f,
                size.width * 0.65f, size.height * 0.28f,
                size.width * 0.6f, size.height * 0.2f
            )
            cubicTo(
                size.width * 0.7f, size.height * 0.15f,
                size.width * 0.8f, size.height * 0.18f,
                size.width * 0.85f, size.height * 0.12f
            )
            cubicTo(
                size.width * 0.75f, size.height * 0.18f,
                size.width * 0.65f, size.height * 0.15f,
                size.width * 0.55f, size.height * 0.2f
            )
            cubicTo(
                size.width * 0.35f, size.height * 0.2f,
                size.width * 0.2f, size.height * 0.35f,
                size.width * 0.1f, size.height * 0.7f
            )
            close()
        }
        
        drawPath(path, color)
    }
}

@Composable
private fun XIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val color = Color(0xFF9CA3AF)
        val strokeWidth = 2.5.dp.toPx()
        
        // X shape
        drawLine(
            color = color,
            start = Offset(size.width * 0.15f, size.height * 0.15f),
            end = Offset(size.width * 0.85f, size.height * 0.85f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.85f, size.height * 0.15f),
            end = Offset(size.width * 0.15f, size.height * 0.85f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
