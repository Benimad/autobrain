package com.example.autobrain.presentation.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.autobrain.core.preferences.PreferencesManager
import com.example.autobrain.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.setOnboardingCompleted()
        }
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val pageNumber: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pages = listOf(
        OnboardingPage(
            title = "Know the real\ncondition of any car",
            description = "Instantly see beyond the surface.\nUncover hidden issues before you\ncommit.",
            pageNumber = 1
        ),
        OnboardingPage(
            title = "AI-powered\ndiagnostics using\nyour phone",
            description = "Our advanced AI analyzes photos and\nsounds to generate professional\nhealth reports.",
            pageNumber = 2
        ),
        OnboardingPage(
            title = "Buy and sell\nwith confidence",
            description = "Avoid scams and unfair deals with\nobjective, transparent vehicle grading.",
            pageNumber = 3
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

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
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Progress bar at top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (index <= pagerState.currentPage)
                                    Color(0xFF00D9D9)
                                else
                                    Color(0xFF1E3A4C)
                            )
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    isLastPage = page == pages.size - 1
                )
            }

            // Bottom section with button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (pagerState.currentPage == pages.size - 1) {
                    Button(
                        onClick = {
                            // Mark onboarding as completed
                            viewModel.completeOnboarding()

                            // Navigate to Sign In
                            navController.navigate(Screen.SignIn.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00D9D9),
                            contentColor = Color(0xFF0A1628)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = "Get Started",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    isLastPage: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Illustration area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (page.pageNumber) {
                1 -> CarScanningIllustration()
                2 -> AIPhoneDiagnosticsIllustration()
                3 -> TrustScoreIllustration()
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = page.title,
            fontSize = 28.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            fontSize = 15.sp,
            color = Color(0xFFB0B0B0),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// Page 1: Car with scanning line
@Composable
private fun CarScanningIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")

    // Scanning line animation
    val scanPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    // Glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .size(320.dp, 280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glow effect behind scan line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .offset(x = ((-160 + scanPosition * 320).dp))
                .blur(20.dp)
                .alpha(glowAlpha * 0.5f)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF00D9D9).copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Car side profile
        Canvas(modifier = Modifier.size(280.dp, 180.dp)) {
            val width = size.width
            val height = size.height
            val strokeWidth = 2.5.dp.toPx()
            val tint = Color(0xFF00D9D9)

            // Car body path - sleek sedan profile
            val carBodyPath = Path().apply {
                // Start from bottom left wheel
                moveTo(width * 0.1f, height * 0.75f)
                // Front wheel arch
                quadraticBezierTo(width * 0.1f, height * 0.6f, width * 0.15f, height * 0.6f)
                // Bottom front
                lineTo(width * 0.2f, height * 0.6f)
                // Front bumper up
                lineTo(width * 0.15f, height * 0.55f)
                // Hood line
                lineTo(width * 0.25f, height * 0.45f)
                // Windshield
                lineTo(width * 0.4f, height * 0.25f)
                // Roof
                lineTo(width * 0.65f, height * 0.25f)
                // Rear window
                lineTo(width * 0.8f, height * 0.4f)
                // Trunk
                lineTo(width * 0.88f, height * 0.45f)
                // Rear
                lineTo(width * 0.9f, height * 0.6f)
                // Rear wheel arch
                quadraticBezierTo(width * 0.9f, height * 0.75f, width * 0.85f, height * 0.75f)
                // Bottom
                lineTo(width * 0.2f, height * 0.75f)
                close()
            }

            drawPath(carBodyPath, tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

            // Windows
            val windowPath = Path().apply {
                moveTo(width * 0.42f, height * 0.28f)
                lineTo(width * 0.52f, height * 0.28f)
                lineTo(width * 0.52f, height * 0.42f)
                lineTo(width * 0.38f, height * 0.42f)
                close()
            }
            drawPath(windowPath, tint.copy(alpha = 0.5f), style = Stroke(width = strokeWidth * 0.6f))

            val windowPath2 = Path().apply {
                moveTo(width * 0.54f, height * 0.28f)
                lineTo(width * 0.63f, height * 0.28f)
                lineTo(width * 0.7f, height * 0.38f)
                lineTo(width * 0.54f, height * 0.42f)
                close()
            }
            drawPath(windowPath2, tint.copy(alpha = 0.5f), style = Stroke(width = strokeWidth * 0.6f))

            // Front wheel
            drawCircle(tint, radius = width * 0.08f, center = Offset(width * 0.22f, height * 0.72f), style = Stroke(width = strokeWidth))
            drawCircle(tint.copy(alpha = 0.5f), radius = width * 0.04f, center = Offset(width * 0.22f, height * 0.72f), style = Stroke(width = strokeWidth * 0.5f))

            // Rear wheel
            drawCircle(tint, radius = width * 0.08f, center = Offset(width * 0.78f, height * 0.72f), style = Stroke(width = strokeWidth))
            drawCircle(tint.copy(alpha = 0.5f), radius = width * 0.04f, center = Offset(width * 0.78f, height * 0.72f), style = Stroke(width = strokeWidth * 0.5f))

            // Headlight
            drawOval(tint.copy(alpha = 0.7f), topLeft = Offset(width * 0.13f, height * 0.48f), size = Size(width * 0.06f, width * 0.03f), style = Stroke(width = strokeWidth * 0.5f))

            // Door handle
            drawLine(tint.copy(alpha = 0.6f), Offset(width * 0.48f, height * 0.45f), Offset(width * 0.52f, height * 0.45f), strokeWidth * 0.8f)
        }

        // Vertical scanning line
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(3.dp)
                .offset(x = ((-140 + scanPosition * 280).dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00D9D9).copy(alpha = 0f),
                            Color(0xFF00D9D9),
                            Color(0xFF00D9D9).copy(alpha = 0f)
                        )
                    )
                )
        )

        // Scan line glow
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(20.dp)
                .offset(x = ((-140 + scanPosition * 280).dp))
                .blur(8.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF00D9D9).copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

// Page 2: Phone with AI diagnostics
@Composable
private fun AIPhoneDiagnosticsIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "diagnostic")

    // Floating animation for icons
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // Pulse for center glow
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .size(320.dp, 300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Concentric rings
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
        ) {
            val tint = Color(0xFF00D9D9)
            drawCircle(tint.copy(alpha = 0.1f), radius = size.minDimension * 0.5f, style = Stroke(width = 1.dp.toPx()))
            drawCircle(tint.copy(alpha = 0.2f), radius = size.minDimension * 0.35f, style = Stroke(width = 1.dp.toPx()))
            drawCircle(tint.copy(alpha = 0.3f), radius = size.minDimension * 0.2f, style = Stroke(width = 1.dp.toPx()))
        }

        // Phone with glow from center
        Box(
            modifier = Modifier
                .size(100.dp, 180.dp)
                .offset(y = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glow effect
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .blur(25.dp)
                    .background(Color(0xFF00D9D9).copy(alpha = 0.4f), CircleShape)
            )

            // Phone outline
            PhoneWithGlowIcon(modifier = Modifier.fillMaxSize())
        }

        // Floating diagnostic icons
        // Top: Brake/disc icon
        Box(
            modifier = Modifier
                .offset(y = (-100 + floatOffset).dp)
        ) {
            BrakeDiscIcon(modifier = Modifier.size(50.dp))
        }

        // Top-left: Engine icon
        Box(
            modifier = Modifier
                .offset(x = (-90).dp, y = (-60 + floatOffset * 0.8f).dp)
        ) {
            EngineBlockIcon(modifier = Modifier.size(45.dp))
        }

        // Top-right: Battery icon
        Box(
            modifier = Modifier
                .offset(x = 90.dp, y = (-60 + floatOffset * 0.8f).dp)
        ) {
            BatteryIcon(modifier = Modifier.size(45.dp))
        }

        // Connection lines (subtle)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tint = Color(0xFF00D9D9).copy(alpha = 0.3f)
            val strokeWidth = 1.dp.toPx()
            val center = Offset(size.width / 2, size.height / 2 + 20.dp.toPx())

            // Line to top
            drawLine(tint, center.copy(y = center.y - 60.dp.toPx()), Offset(center.x, center.y - 100.dp.toPx() + floatOffset.dp.toPx()), strokeWidth)
            // Line to top-left
            drawLine(tint, center.copy(x = center.x - 30.dp.toPx(), y = center.y - 50.dp.toPx()), Offset(center.x - 70.dp.toPx(), center.y - 80.dp.toPx()), strokeWidth)
            // Line to top-right
            drawLine(tint, center.copy(x = center.x + 30.dp.toPx(), y = center.y - 50.dp.toPx()), Offset(center.x + 70.dp.toPx(), center.y - 80.dp.toPx()), strokeWidth)
        }
    }
}

// Page 3: Trust Score Shield
@Composable
private fun TrustScoreIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "trust")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val checkScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "check"
    )

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(240.dp)
                .blur(30.dp)
                .alpha(glowAlpha * 0.5f)
                .background(Color(0xFF00D9D9).copy(alpha = 0.2f), RoundedCornerShape(40.dp))
        )

        // Shield shape
        Canvas(modifier = Modifier.size(220.dp)) {
            val width = size.width
            val height = size.height
            val tint = Color(0xFF00D9D9)

            // Shield path
            val shieldPath = Path().apply {
                moveTo(width * 0.5f, height * 0.02f)
                // Top right curve
                cubicTo(
                    width * 0.75f, height * 0.02f,
                    width * 0.95f, height * 0.1f,
                    width * 0.95f, height * 0.25f
                )
                // Right side down
                lineTo(width * 0.95f, height * 0.5f)
                // Bottom right curve to point
                cubicTo(
                    width * 0.9f, height * 0.7f,
                    width * 0.7f, height * 0.85f,
                    width * 0.5f, height * 0.98f
                )
                // Bottom left curve
                cubicTo(
                    width * 0.3f, height * 0.85f,
                    width * 0.1f, height * 0.7f,
                    width * 0.05f, height * 0.5f
                )
                // Left side up
                lineTo(width * 0.05f, height * 0.25f)
                // Top left curve
                cubicTo(
                    width * 0.05f, height * 0.1f,
                    width * 0.25f, height * 0.02f,
                    width * 0.5f, height * 0.02f
                )
                close()
            }

            // Fill with gradient
            drawPath(
                shieldPath,
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D2030),
                        Color(0xFF0A1628)
                    )
                )
            )

            // Border
            drawPath(
                shieldPath,
                Brush.verticalGradient(
                    colors = listOf(
                        tint.copy(alpha = 0.8f),
                        tint.copy(alpha = 0.4f)
                    )
                ),
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Content inside shield
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-10).dp)
        ) {
            // Checkmark circle at top
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .graphicsLayer(scaleX = checkScale, scaleY = checkScale),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val tint = Color(0xFF00D9D9)
                    drawCircle(tint, style = Stroke(width = 2.dp.toPx()))

                    // Checkmark
                    val checkPath = Path().apply {
                        moveTo(size.width * 0.25f, size.height * 0.5f)
                        lineTo(size.width * 0.42f, size.height * 0.68f)
                        lineTo(size.width * 0.75f, size.height * 0.32f)
                    }
                    drawPath(checkPath, tint, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score number
            Text(
                text = "94",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-2).sp
            )

            // SCORE label
            Text(
                text = "SCORE",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D9D9),
                letterSpacing = 4.sp
            )
        }
    }
}

// Helper composables for diagnostic icons
@Composable
private fun PhoneWithGlowIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val tint = Color(0xFF00D9D9)
        val strokeWidth = 2.5.dp.toPx()

        // Phone frame
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.15f, 0f),
            size = Size(size.width * 0.7f, size.height),
            cornerRadius = CornerRadius(12.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )

        // Screen area
        drawRoundRect(
            color = tint.copy(alpha = 0.3f),
            topLeft = Offset(size.width * 0.2f, size.height * 0.08f),
            size = Size(size.width * 0.6f, size.height * 0.75f),
            cornerRadius = CornerRadius(6.dp.toPx()),
            style = Stroke(width = strokeWidth * 0.5f)
        )

        // Glow from screen (light rays)
        val centerX = size.width * 0.5f
        val screenTop = size.height * 0.3f

        for (i in 0..2) {
            val angle = -30f + i * 30f
            val radians = Math.toRadians(angle.toDouble())
            drawLine(
                color = tint.copy(alpha = 0.6f),
                start = Offset(centerX, screenTop),
                end = Offset(
                    centerX + (50.dp.toPx() * cos(radians)).toFloat(),
                    screenTop - (50.dp.toPx() * sin(radians)).toFloat()
                ),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Home button / bar at bottom
        drawRoundRect(
            color = tint.copy(alpha = 0.5f),
            topLeft = Offset(size.width * 0.35f, size.height * 0.92f),
            size = Size(size.width * 0.3f, 3.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
    }
}

@Composable
private fun BrakeDiscIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val tint = Color(0xFF00D9D9)
        val strokeWidth = 2.dp.toPx()
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension * 0.4f

        // Outer disc
        drawCircle(tint, radius, center, style = Stroke(width = strokeWidth))

        // Inner circle
        drawCircle(tint, radius * 0.5f, center, style = Stroke(width = strokeWidth))

        // Center hub
        drawCircle(tint, radius * 0.2f, center, style = Stroke(width = strokeWidth))

        // Ventilation slots
        for (i in 0..5) {
            val angle = Math.toRadians((i * 60).toDouble())
            val startRadius = radius * 0.55f
            val endRadius = radius * 0.9f
            drawLine(
                tint.copy(alpha = 0.7f),
                Offset(center.x + (startRadius * cos(angle)).toFloat(), center.y + (startRadius * sin(angle)).toFloat()),
                Offset(center.x + (endRadius * cos(angle)).toFloat(), center.y + (endRadius * sin(angle)).toFloat()),
                strokeWidth * 0.8f
            )
        }
    }
}

@Composable
private fun EngineBlockIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val tint = Color(0xFF00D9D9)
        val strokeWidth = 2.dp.toPx()

        // Main block
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.15f, size.height * 0.3f),
            size = Size(size.width * 0.7f, size.height * 0.5f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )

        // Top cylinders/valves
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.2f, size.height * 0.15f),
            size = Size(size.width * 0.15f, size.height * 0.2f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = strokeWidth * 0.8f)
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.42f, size.height * 0.15f),
            size = Size(size.width * 0.15f, size.height * 0.2f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = strokeWidth * 0.8f)
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.65f, size.height * 0.15f),
            size = Size(size.width * 0.15f, size.height * 0.2f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = strokeWidth * 0.8f)
        )

        // Exhaust/side pipe
        drawLine(tint, Offset(size.width * 0.85f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), strokeWidth)
        drawCircle(tint, 3.dp.toPx(), Offset(size.width, size.height * 0.5f))
    }
}

@Composable
private fun BatteryIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val tint = Color(0xFF00D9D9)
        val strokeWidth = 2.dp.toPx()

        // Main battery body
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.1f, size.height * 0.25f),
            size = Size(size.width * 0.8f, size.height * 0.6f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )

        // Battery terminals on top
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.2f, size.height * 0.12f),
            size = Size(size.width * 0.15f, size.height * 0.15f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = strokeWidth * 0.8f)
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.65f, size.height * 0.12f),
            size = Size(size.width * 0.15f, size.height * 0.15f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = strokeWidth * 0.8f)
        )

        // + and - signs
        // Plus
        drawLine(tint.copy(alpha = 0.8f), Offset(size.width * 0.23f, size.height * 0.05f), Offset(size.width * 0.31f, size.height * 0.05f), strokeWidth * 0.8f)
        drawLine(tint.copy(alpha = 0.8f), Offset(size.width * 0.27f, size.height * 0.01f), Offset(size.width * 0.27f, size.height * 0.09f), strokeWidth * 0.8f)
        // Minus
        drawLine(tint.copy(alpha = 0.8f), Offset(size.width * 0.69f, size.height * 0.05f), Offset(size.width * 0.77f, size.height * 0.05f), strokeWidth * 0.8f)

        // Battery level indicator lines inside
        drawLine(tint.copy(alpha = 0.6f), Offset(size.width * 0.2f, size.height * 0.45f), Offset(size.width * 0.8f, size.height * 0.45f), strokeWidth * 0.5f)
        drawLine(tint.copy(alpha = 0.6f), Offset(size.width * 0.2f, size.height * 0.55f), Offset(size.width * 0.6f, size.height * 0.55f), strokeWidth * 0.5f)
        drawLine(tint.copy(alpha = 0.6f), Offset(size.width * 0.2f, size.height * 0.65f), Offset(size.width * 0.4f, size.height * 0.65f), strokeWidth * 0.5f)
    }
}
