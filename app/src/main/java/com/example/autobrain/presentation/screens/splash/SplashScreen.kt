package com.example.autobrain.presentation.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autobrain.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.autobrain.core.preferences.PreferencesManager
import com.example.autobrain.presentation.navigation.Screen
import com.example.autobrain.presentation.theme.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val auth: FirebaseAuth
) : ViewModel() {
    
    suspend fun getStartDestination(): String {
        // Check if user is logged in (Firebase + DataStore)
        val isLoggedIn = preferencesManager.isLoggedIn.first()
        val firebaseUser = auth.currentUser
        
        // If both Firebase and DataStore confirm logged in, go to Home
        if (isLoggedIn && firebaseUser != null) {
            return Screen.Home.route
        }
        
        // Check if onboarding completed
        val onboardingCompleted = preferencesManager.isOnboardingCompleted.first()
        
        return if (onboardingCompleted) {
            // Onboarding done, go to Sign In
            Screen.SignIn.route
        } else {
            // First time user, show onboarding
            Screen.Onboarding.route
        }
    }
}

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    // Loading bar animation
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    // Glow pulse animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // Logo scale animation
    var logoScale by remember { mutableFloatStateOf(0f) }
    val animatedScale by animateFloatAsState(
        targetValue = logoScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        logoScale = 1f
        delay(2000) // Show splash for 2 seconds
        
        // Determine where to navigate based on authentication and onboarding status
        val destination = viewModel.getStartDestination()
        
        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A1628),
                        Color(0xFF0D1B2A),
                        Color(0xFF102030)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Hexagon pattern background (very subtle)
        HexagonPatternBackground(
            modifier = Modifier.fillMaxSize(),
            alpha = 0.02f
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer(
                scaleX = animatedScale,
                scaleY = animatedScale
            )
        ) {
            // AutoBrain PNG Logo
            Image(
                painter = painterResource(id = R.drawable.logowitoutbg),
                contentDescription = "AutoBrain Logo",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // AutoBrain text
            Text(
                text = "AutoBrain",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.2.sp
            )
            
            // Cyan underline
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00D9D9).copy(alpha = 0.2f),
                                    Color(0xFF00D9D9),
                                    Color(0xFF00D9D9),
                                    Color(0xFF00D9D9).copy(alpha = 0.2f)
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(100.dp))

            // Animated loading bar
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(Color(0xFF1E3A4C).copy(alpha = 0.5f))
            ) {
                // Loading progress with gradient
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00D9D9).copy(alpha = 0.6f),
                                    Color(0xFF00D9D9),
                                    Color(0xFF00D9D9)
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun HexagonPatternBackground(
    modifier: Modifier = Modifier,
    alpha: Float = 0.1f
) {
    Canvas(modifier = modifier) {
        val hexSize = 60.dp.toPx()
        val rows = (size.height / (hexSize * 1.5f)).toInt() + 2
        val cols = (size.width / (hexSize * 1.73f)).toInt() + 2
        
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val xOffset = if (row % 2 == 0) 0f else hexSize * 0.866f
                val x = col * hexSize * 1.73f + xOffset
                val y = row * hexSize * 1.5f
                
                drawHexagon(
                    center = Offset(x, y),
                    radius = hexSize * 0.5f,
                    color = Color(0xFF00D9D9).copy(alpha = alpha)
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHexagon(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path()
    for (i in 0..5) {
        val angle = Math.toRadians((60 * i - 30).toDouble())
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Stroke(width = 1.dp.toPx()))
}

