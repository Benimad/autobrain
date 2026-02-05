package com.example.autobrain.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    AutoBrainTheme {
        SignInScreenContent(
            navController = rememberNavController()
        )
    }
}

@Composable
fun SignInScreenContent(
    navController: NavController,
    onSignIn: (String, String) -> Unit = { _, _ -> },
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onResetError: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    
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
            // Animated Background
            AnimatedBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(64.dp))

                // Logo section with animation
                AnimatedEntrance(visible = visibleState.value, delay = 100) {
                    LogoSection()
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedEntrance(visible = visibleState.value, delay = 200) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Welcome Back",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Sign in to continue your journey",
                            fontSize = 16.sp,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

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
                            // Email field
                            CustomTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = "Email Address",
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Password field
                            CustomTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = "Password",
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onPasswordToggle = { passwordVisible = !passwordVisible },
                                imeAction = ImeAction.Done,
                                onImeAction = { 
                                    focusManager.clearFocus()
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        onSignIn(email, password)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Forgot Password
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                Text(
                                    text = "Forgot Password?",
                                    color = Color(0xFF00D9D9),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable { /* Handle forgot password */ }
                                )
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // Sign In button
                            MainButton(
                                text = "Sign In",
                                onClick = { 
                                    focusManager.clearFocus()
                                    onSignIn(email, password)
                                },
                                loading = isLoading,
                                enabled = email.isNotBlank() && password.isNotBlank()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Divider Section
                AnimatedEntrance(visible = visibleState.value, delay = 650) {
                    OrDivider()
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Social login section
                AnimatedEntrance(visible = visibleState.value, delay = 700) {
                    SocialSection()
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Sign Up link
                AnimatedEntrance(visible = visibleState.value, delay = 800) {
                    SignUpLink(onSignUpClick = { navController.navigate(Screen.SignUp.route) })
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SignUpLink(onSignUpClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Don't have an account? ",
            color = Color(0xFF9CA3AF),
            fontSize = 14.sp
        )
        TextButton(
            onClick = onSignUpClick,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Sign Up",
                color = Color(0xFF00D9D9),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
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

    SignInScreenContent(
        navController = navController,
        onSignIn = { email, password -> viewModel.signIn(email, password) },
        isLoading = authState is AuthState.Loading,
        errorMessage = (authState as? AuthState.Error)?.message,
        onResetError = { viewModel.resetState() }
    )
}
