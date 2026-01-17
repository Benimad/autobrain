package com.example.autobrain.presentation.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            // AI Car Icon with glow effect
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                // Outer glow effect (wider)
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .blur(40.dp)
                        .alpha(glowAlpha * 0.8f)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00D9D9).copy(alpha = 0.4f),
                                    Color(0xFF00D9D9).copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                
                // Inner glow effect
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .blur(20.dp)
                        .alpha(glowAlpha)
                        .background(
                            Color(0xFF00D9D9).copy(alpha = 0.3f),
                            CircleShape
                        )
                )
                
                // Main car icon with circuit pattern
                AICarIcon(
                    modifier = Modifier.size(150.dp),
                    tint = Color(0xFF00D9D9)
                )
                
                // Plus icon in circle (top right) with glow
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 32.dp)
                ) {
                    // Plus icon glow
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .blur(10.dp)
                            .alpha(glowAlpha * 0.6f)
                            .background(
                                Color(0xFF00D9D9).copy(alpha = 0.4f),
                                CircleShape
                            )
                    )
                    // Plus icon background
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                Color(0xFF0D1B2A),
                                CircleShape
                            )
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        DiagnosticPlusIcon(
                            modifier = Modifier.size(30.dp),
                            tint = Color(0xFF00D9D9)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AutoBrain text with shadow
            Box(contentAlignment = Alignment.Center) {
                // Text shadow/glow
                Text(
                    text = "AutoBrain",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00D9D9).copy(alpha = 0.2f),
                    letterSpacing = 1.2.sp,
                    modifier = Modifier
                        .offset(y = 3.dp)
                        .blur(6.dp)
                )
                // Main text
                Text(
                    text = "AutoBrain",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.2.sp
                )
            }
            
            // Cyan underline with glow
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
            ) {
                // Underline glow
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(4.dp)
                        .blur(6.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF00D9D9).copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            ),
                            RoundedCornerShape(2.dp)
                        )
                )
                // Main underline
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(4.dp)
                        .align(Alignment.Center)
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
                
                // Glow effect on loading bar
                if (progress > 0.1f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(30.dp)
                            .offset(x = ((progress * 220 - 15).coerceAtLeast(0f)).dp)
                            .blur(8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF00D9D9).copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
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

@Composable
private fun AICarIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF00D9D9)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidth = 3.dp.toPx()
        
        // Car body outline - front view with hood open look
        val carPath = Path().apply {
            // Left mirror
            moveTo(width * 0.1f, height * 0.45f)
            lineTo(width * 0.18f, height * 0.45f)
            lineTo(width * 0.18f, height * 0.55f)
            lineTo(width * 0.1f, height * 0.55f)
            close()
            
            // Right mirror
            moveTo(width * 0.9f, height * 0.45f)
            lineTo(width * 0.82f, height * 0.45f)
            lineTo(width * 0.82f, height * 0.55f)
            lineTo(width * 0.9f, height * 0.55f)
            close()
            
            // Main car body
            moveTo(width * 0.18f, height * 0.35f)
            // Top left corner
            lineTo(width * 0.25f, height * 0.2f)
            // Roof line to right
            lineTo(width * 0.75f, height * 0.2f)
            // Top right corner
            lineTo(width * 0.82f, height * 0.35f)
            // Right side down
            lineTo(width * 0.82f, height * 0.75f)
            // Bottom right
            lineTo(width * 0.75f, height * 0.85f)
            // Bottom line
            lineTo(width * 0.25f, height * 0.85f)
            // Bottom left
            lineTo(width * 0.18f, height * 0.75f)
            close()
        }
        
        drawPath(carPath, tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        
        // Windshield / hood area with circuit pattern
        val windshieldPath = Path().apply {
            moveTo(width * 0.28f, height * 0.25f)
            lineTo(width * 0.72f, height * 0.25f)
            lineTo(width * 0.78f, height * 0.4f)
            lineTo(width * 0.22f, height * 0.4f)
            close()
        }
        drawPath(windshieldPath, tint, style = Stroke(width = strokeWidth * 0.7f))
        
        // Circuit patterns inside the car (AI brain representation)
        // Horizontal lines
        drawLine(tint.copy(alpha = 0.7f), Offset(width * 0.3f, height * 0.5f), Offset(width * 0.7f, height * 0.5f), strokeWidth * 0.5f)
        drawLine(tint.copy(alpha = 0.7f), Offset(width * 0.3f, height * 0.6f), Offset(width * 0.7f, height * 0.6f), strokeWidth * 0.5f)
        drawLine(tint.copy(alpha = 0.7f), Offset(width * 0.3f, height * 0.7f), Offset(width * 0.7f, height * 0.7f), strokeWidth * 0.5f)
        
        // Vertical connector lines
        drawLine(tint.copy(alpha = 0.7f), Offset(width * 0.4f, height * 0.45f), Offset(width * 0.4f, height * 0.75f), strokeWidth * 0.5f)
        drawLine(tint.copy(alpha = 0.7f), Offset(width * 0.5f, height * 0.45f), Offset(width * 0.5f, height * 0.75f), strokeWidth * 0.5f)
        drawLine(tint.copy(alpha = 0.7f), Offset(width * 0.6f, height * 0.45f), Offset(width * 0.6f, height * 0.75f), strokeWidth * 0.5f)
        
        // Circuit nodes (small circles)
        val nodeRadius = 4.dp.toPx()
        val nodePositions = listOf(
            Offset(width * 0.4f, height * 0.5f),
            Offset(width * 0.5f, height * 0.5f),
            Offset(width * 0.6f, height * 0.5f),
            Offset(width * 0.4f, height * 0.6f),
            Offset(width * 0.6f, height * 0.6f),
            Offset(width * 0.4f, height * 0.7f),
            Offset(width * 0.5f, height * 0.7f),
            Offset(width * 0.6f, height * 0.7f),
        )
        
        nodePositions.forEach { pos ->
            drawCircle(tint, nodeRadius, pos)
        }
        
        // Front grille / bottom detail
        drawLine(tint, Offset(width * 0.3f, height * 0.8f), Offset(width * 0.7f, height * 0.8f), strokeWidth * 0.7f)
    }
}

@Composable
private fun DiagnosticPlusIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF00D9D9)
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val padding = 6.dp.toPx()
        
        // Circle outline
        drawCircle(
            color = tint,
            radius = size.minDimension / 2 - strokeWidth,
            style = Stroke(width = strokeWidth)
        )
        
        val center = Offset(size.width / 2, size.height / 2)
        val crossSize = size.minDimension / 3.5f
        
        drawLine(
            tint,
            Offset(center.x - crossSize, center.y),
            Offset(center.x + crossSize, center.y),
            strokeWidth * 1.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            tint,
            Offset(center.x, center.y - crossSize),
            Offset(center.x, center.y + crossSize),
            strokeWidth * 1.5f,
            cap = StrokeCap.Round
        )
    }
}
