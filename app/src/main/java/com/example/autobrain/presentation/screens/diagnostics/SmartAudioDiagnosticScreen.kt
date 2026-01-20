package com.example.autobrain.presentation.screens.diagnostics

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autobrain.data.ai.getMostLikelyRepairCost
import com.example.autobrain.data.ai.isSafeToDrive
import com.example.autobrain.data.local.entity.AudioDiagnosticData
import com.example.autobrain.presentation.components.AudioWaveform
import com.example.autobrain.presentation.components.GeminiIcon
import com.example.autobrain.presentation.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.math.cos
import kotlin.math.sin

// Using theme colors for consistency with HomeScreen
private val PremiumDarkBackground = MidnightBlack
private val PremiumDarkSurface = DeepNavy
private val PremiumDarkCard = DarkNavy
private val PremiumCyan = ElectricTeal
private val PremiumCyanDark = TealDark
private val PremiumCyanGlow = TealGlow
private val PremiumBorder = BorderDark
private val PremiumBorderLight = SlateGray
private val PremiumGradientTop = MidnightBlack
private val PremiumGradientBottom = SurfaceDimDark

/**
 * Smart Audio Diagnostic Screen
 * 
 * Complete professional UI with:
 * - Dark neon blue theme (#001F3F background, #00FFFF accents)
 * - Real-time waveform visualization
 * - Step-by-step instructions
 * - Live audio quality feedback
 * - Results with score circle, issues, and recommendations
 * - Offline-first with sync indicator
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SmartAudioDiagnosticScreen(
    navController: NavController,
    carId: String,
    viewModel: AudioDiagnosticViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val waveformData by viewModel.waveformData.collectAsState()
    val audioQuality by viewModel.audioQuality.collectAsState()
    val comprehensiveDiagnostic by viewModel.comprehensiveDiagnostic.collectAsState()
    val isComprehensiveAnalyzing by viewModel.isComprehensiveAnalyzing.collectAsState()
    
    val audioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    // Set car profile on first composition
    LaunchedEffect(carId) {
        viewModel.setCarProfile(carId)
    }
    
    // Handle permission result
    LaunchedEffect(audioPermission.status) {
        if (audioPermission.status.isGranted) {
            viewModel.onPermissionResult(true)
        }
    }
    
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(PremiumCyan, CircleShape)
                                .shadow(3.dp, CircleShape, spotColor = PremiumCyan)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Diagnostic Audio",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(4.dp)
                            .background(
                                PremiumDarkCard.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = PremiumCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        // Premium animated background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PremiumGradientTop,
                            PremiumDarkBackground,
                            PremiumGradientBottom
                        )
                    )
                )
        ) {
            // Animated background particles/grid effect
            PremiumBackgroundEffect()
            
            // Main content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
            when (uiState) {
                is AudioDiagnosticState.Idle,
                is AudioDiagnosticState.Ready -> {
                    IdleReadyState(
                        hasPermission = audioPermission.status.isGranted,
                        onRequestPermission = { audioPermission.launchPermissionRequest() },
                        onStartDiagnostic = { viewModel.startDiagnostic() }
                    )
                }
                
                is AudioDiagnosticState.Recording -> {
                    RecordingState(
                        progress = progress,
                        statusMessage = statusMessage,
                        waveformData = waveformData,
                        audioQuality = audioQuality,
                        onStop = { viewModel.stopRecording() }
                    )
                }
                
                is AudioDiagnosticState.Analyzing -> {
                    AnalyzingState(statusMessage = statusMessage)
                }
                
                is AudioDiagnosticState.Success -> {
                    val diagnostic = (uiState as AudioDiagnosticState.Success).diagnostic
                    SuccessState(
                        diagnostic = diagnostic,
                        comprehensiveDiagnostic = comprehensiveDiagnostic,
                        isComprehensiveAnalyzing = isComprehensiveAnalyzing,
                        onPerformComprehensive = { viewModel.performComprehensiveAnalysis(diagnostic) },
                        onSaveAndContinue = {
                            viewModel.resetToReady()
                            navController.popBackStack()
                        },
                        onNewDiagnostic = { viewModel.resetToReady() }
                    )
                }
                
                is AudioDiagnosticState.Error -> {
                    val message = (uiState as AudioDiagnosticState.Error).message
                    ErrorState(
                        message = message,
                        onRetry = { viewModel.resetToReady() },
                        onBack = { navController.popBackStack() }
                    )
                }
                
                else -> {
                    // Should not reach here, but handle gracefully
                    IdleReadyState(
                        hasPermission = audioPermission.status.isGranted,
                        onRequestPermission = { audioPermission.launchPermissionRequest() },
                        onStartDiagnostic = { viewModel.startDiagnostic() }
                    )
                }
            }
            }
        }
    }
}

// =============================================================================
// PREMIUM BACKGROUND EFFECT
// =============================================================================

@Composable
private fun PremiumBackgroundEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "bgEffect")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation2"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.02f,
        targetValue = 0.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = size.width.coerceAtLeast(size.height) * 0.8f
        
        // Draw multiple layered radial gradient circles with pulse
        for (i in 0..5) {
            val radius = maxRadius * (0.25f + i * 0.15f)
            val alpha = (pulseAlpha - (i * 0.008f)).coerceAtLeast(0.003f)
            drawCircle(
                color = PremiumCyan.copy(alpha = alpha),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
        
        // Draw rotating accent arcs with gradient
        rotate(rotation, Offset(centerX, centerY)) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        PremiumCyan.copy(alpha = 0.08f),
                        PremiumCyan.copy(alpha = 0.02f),
                        Color.Transparent
                    )
                ),
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(centerX - maxRadius * 0.6f, centerY - maxRadius * 0.6f),
                size = Size(maxRadius * 1.2f, maxRadius * 1.2f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Draw counter-rotating accent arcs
        rotate(rotation2, Offset(centerX, centerY)) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        PremiumCyanDark.copy(alpha = 0.06f),
                        PremiumCyanDark.copy(alpha = 0.02f)
                    )
                ),
                startAngle = 180f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(centerX - maxRadius * 0.4f, centerY - maxRadius * 0.4f),
                size = Size(maxRadius * 0.8f, maxRadius * 0.8f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Add subtle particles effect
        for (i in 0..8) {
            val angle = (rotation + i * 40f) * (Math.PI / 180).toFloat()
            val particleRadius = maxRadius * 0.4f
            val x = centerX + cos(angle) * particleRadius
            val y = centerY + sin(angle) * particleRadius
            
            drawCircle(
                color = PremiumCyan.copy(alpha = (0.04f - i * 0.003f).coerceAtLeast(0.01f)),
                radius = (4f - i * 0.3f).coerceAtLeast(1f).dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

// =============================================================================
// IDLE / READY STATE - PREMIUM DESIGN
// =============================================================================

@Composable
private fun IdleReadyState(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartDiagnostic: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isCompactScreen = screenHeight < 700.dp
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(if (isCompactScreen) 12.dp else 24.dp))
        
        // Premium Animated Microphone with Glow
        PremiumMicrophoneIcon(
            size = if (isCompactScreen) 140.dp else 180.dp
        )
        
        Spacer(modifier = Modifier.height(if (isCompactScreen) 20.dp else 32.dp))
        
        // Title with gradient effect
        Text(
            text = "Record Sound",
            style = if (isCompactScreen) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = "Engine",
            style = if (isCompactScreen) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
            color = PremiumCyan,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(if (isCompactScreen) 14.dp else 20.dp))
        
        // Subtitle
        Text(
            text = "100% Local Analysis • No cloud",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            letterSpacing = 0.8.sp
        )
        
        Spacer(modifier = Modifier.height(if (isCompactScreen) 18.dp else 28.dp))
        
        // Premium Instructions Card
        PremiumInstructionsCard(isCompact = isCompactScreen)
        
        Spacer(modifier = Modifier.height(if (isCompactScreen) 20.dp else 32.dp))
        
        // Premium Start Button
        PremiumActionButton(
            text = if (hasPermission) "Start Recording" else "Enable Microphone",
            icon = if (hasPermission) Icons.Rounded.PlayArrow else Icons.Rounded.Lock,
            onClick = if (hasPermission) onStartDiagnostic else onRequestPermission
        )
        
        Spacer(modifier = Modifier.height(if (isCompactScreen) 16.dp else 32.dp))
    }
}

@Composable
private fun PremiumMicrophoneIcon(size: Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "micAnim")
    
    // Multi-layer pulsating glow
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    val innerGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "innerGlowAlpha"
    )
    
    // Rotating rings
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )
    
    val counterRingRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counterRingRotation"
    )
    
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow layers
        Box(
            modifier = Modifier
                .size(size * glowScale * 1.1f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PremiumCyan.copy(alpha = glowAlpha * 0.25f),
                            PremiumCyan.copy(alpha = glowAlpha * 0.12f),
                            PremiumCyan.copy(alpha = glowAlpha * 0.05f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        Box(
            modifier = Modifier
                .size(size * glowScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PremiumCyan.copy(alpha = glowAlpha * 0.4f),
                            PremiumCyan.copy(alpha = glowAlpha * 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Rotating outer ring with enhanced gradient
        Canvas(
            modifier = Modifier
                .size(size)
                .rotate(ringRotation)
        ) {
            val strokeWidth = 3.5.dp.toPx()
            
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        PremiumCyan.copy(alpha = 0.2f),
                        PremiumCyan.copy(alpha = 0.6f),
                        PremiumCyan,
                        PremiumCyan.copy(alpha = 0.6f),
                        PremiumCyan.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                startAngle = 0f,
                sweepAngle = 280f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(this.size.width - strokeWidth, this.size.height - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        // Counter-rotating middle ring
        Canvas(
            modifier = Modifier
                .size(size * 0.92f)
                .rotate(counterRingRotation)
        ) {
            val strokeWidth = 2.dp.toPx()
            
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        PremiumCyanDark.copy(alpha = 0.4f),
                        PremiumCyanDark.copy(alpha = 0.7f),
                        PremiumCyanDark.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                ),
                startAngle = 60f,
                sweepAngle = 200f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(this.size.width - strokeWidth, this.size.height - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        // Static inner ring with gradient border
        Box(
            modifier = Modifier
                .size(size * 0.85f)
                .border(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            PremiumCyan.copy(alpha = 0.6f),
                            PremiumCyan.copy(alpha = 0.3f),
                            PremiumCyan.copy(alpha = 0.6f)
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Center dark circle with enhanced depth
        Box(
            modifier = Modifier
                .size(size * 0.75f)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = PremiumCyan.copy(alpha = innerGlowAlpha * 0.3f)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PremiumDarkCard.copy(alpha = 0.95f),
                            PremiumDarkSurface,
                            PremiumDarkBackground
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PremiumBorderLight.copy(alpha = 0.5f),
                            PremiumBorder.copy(alpha = 0.3f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inner glow behind icon
            Box(
                modifier = Modifier
                    .size(size * 0.5f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PremiumCyan.copy(alpha = innerGlowAlpha * 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            // Microphone icon
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = "Microphone",
                tint = PremiumCyan,
                modifier = Modifier
                    .size(size * 0.35f)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        spotColor = PremiumCyan.copy(alpha = 0.5f)
                    )
            )
        }
    }
}

@Composable
private fun PremiumInstructionsCard(isCompact: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "cardGlow")
    val borderGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = PremiumCyan.copy(alpha = 0.12f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    PremiumCyan.copy(alpha = borderGlowAlpha * 0.6f),
                    PremiumBorderLight.copy(alpha = borderGlowAlpha * 0.4f),
                    PremiumBorder.copy(alpha = 0.25f),
                    PremiumCyan.copy(alpha = borderGlowAlpha * 0.5f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PremiumDarkCard.copy(alpha = 0.8f),
                            PremiumDarkCard.copy(alpha = 0.7f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(if (isCompact) 14.dp else 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        PremiumCyan,
                                        PremiumCyanDark
                                    )
                                ),
                                shape = CircleShape
                            )
                            .shadow(
                                elevation = 3.dp,
                                shape = CircleShape,
                                spotColor = PremiumCyan
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Instructions",
                        fontSize = if (isCompact) 16.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 0.4.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 16.dp))
                
                PremiumInstructionItem(
                    number = "1",
                    text = "Garez sur une surface plane",
                    isCompact = isCompact
                )
                PremiumInstructionItem(
                    number = "2",
                    text = "Pointez le micro vers le moteur",
                    isCompact = isCompact
                )
                PremiumInstructionItem(
                    number = "3",
                    text = "Gardez une distance de 1-2 pieds",
                    isCompact = isCompact
                )
                PremiumInstructionItem(
                    number = "4",
                    text = "Assurez un environnement calme",
                    isCompact = isCompact,
                    isLast = true
                )
            }
        }
    }
}

@Composable
private fun PremiumInstructionItem(
    number: String,
    text: String,
    isCompact: Boolean,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isCompact) 6.dp else 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        // Number badge with enhanced gradient and glow
        Box(
            modifier = Modifier
                .size(if (isCompact) 30.dp else 34.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(9.dp),
                    spotColor = PremiumCyan.copy(alpha = 0.3f)
                )
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PremiumCyan,
                            PremiumCyanDark
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    ),
                    shape = RoundedCornerShape(9.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(9.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLarge,
                color = TextOnAccent,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = if (isCompact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = TextPrimary.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PremiumActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "buttonGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = PremiumCyan.copy(alpha = glowAlpha * 0.6f)
            ),
        shape = RoundedCornerShape(18.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = PremiumDarkBackground,
            disabledContainerColor = PremiumDarkCard.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            PremiumCyan,
                            PremiumCyanDark,
                            PremiumCyan
                        ),
                        startX = shimmerOffset * 1000,
                        endX = (shimmerOffset + 1f) * 1000
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = CircleShape,
                            spotColor = PremiumDarkBackground.copy(alpha = 0.5f)
                        ),
                    tint = PremiumDarkBackground
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PremiumDarkBackground,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// =============================================================================
// RECORDING STATE - PREMIUM DESIGN
// =============================================================================

@Composable
private fun RecordingState(
    progress: Float,
    statusMessage: String,
    waveformData: List<Float>,
    audioQuality: com.example.autobrain.data.ai.AudioQuality,
    onStop: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isCompactScreen = screenHeight < 700.dp
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(if (isCompactScreen) 16.dp else 32.dp))
        
        // Premium Recording Header
        PremiumRecordingHeader()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Live Quality Indicator
        PremiumQualityIndicator(audioQuality = audioQuality)
        
        Spacer(modifier = Modifier.height(if (isCompactScreen) 32.dp else 48.dp))
        
        // Premium Waveform Card
        PremiumWaveformCard(
            waveformData = waveformData,
            isCompact = isCompactScreen
        )
        
        Spacer(modifier = Modifier.height(if (isCompactScreen) 24.dp else 32.dp))
        
        // Premium Progress Section
        PremiumProgressSection(
            progress = progress,
            isCompact = isCompactScreen
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Premium Stop Button
        PremiumStopButton(onClick = onStop)
        
        Spacer(modifier = Modifier.height(if (isCompactScreen) 16.dp else 32.dp))
    }
}

@Composable
private fun PremiumRecordingHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "recordingPulse")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Animated recording dot with glow
        Box(contentAlignment = Alignment.Center) {
            // Outer glow
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .scale(pulseScale)
                    .background(
                        ErrorRed.copy(alpha = pulseAlpha * 0.3f),
                        CircleShape
                    )
            )
            // Inner dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(ErrorRed.copy(alpha = pulseAlpha), CircleShape)
                    .shadow(4.dp, CircleShape, spotColor = ErrorRed)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = "Enregistrement...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun PremiumQualityIndicator(audioQuality: com.example.autobrain.data.ai.AudioQuality) {
    val qualityColor = when (audioQuality) {
        is com.example.autobrain.data.ai.AudioQuality.Good -> Color(0xFF00E676)
        is com.example.autobrain.data.ai.AudioQuality.TooQuiet -> Color(0xFFFFAB00)
        is com.example.autobrain.data.ai.AudioQuality.TooNoisy -> Color(0xFFFF5252)
        else -> Color.Gray
    }
    
    val qualityIcon = when (audioQuality) {
        is com.example.autobrain.data.ai.AudioQuality.Good -> Icons.Rounded.CheckCircle
        is com.example.autobrain.data.ai.AudioQuality.TooQuiet -> Icons.Rounded.VolumeDown
        is com.example.autobrain.data.ai.AudioQuality.TooNoisy -> Icons.Rounded.VolumeUp
        else -> Icons.Rounded.Info
    }
    
    Row(
        modifier = Modifier
            .background(
                color = qualityColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = qualityColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = qualityIcon,
            contentDescription = null,
            tint = qualityColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = audioQuality.message,
            style = MaterialTheme.typography.bodyMedium,
            color = qualityColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PremiumWaveformCard(
    waveformData: List<Float>,
    isCompact: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveformGlow")
    val borderGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )
    
    val backgroundPulse by infiniteTransition.animateFloat(
        initialValue = 0.02f,
        targetValue = 0.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgPulse"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isCompact) 150.dp else 190.dp)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = PremiumCyan.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    PremiumCyan.copy(alpha = borderGlowAlpha * 0.6f),
                    PremiumBorder.copy(alpha = 0.3f),
                    PremiumCyan.copy(alpha = borderGlowAlpha * 0.7f),
                    PremiumBorder.copy(alpha = 0.3f),
                    PremiumCyan.copy(alpha = borderGlowAlpha * 0.6f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PremiumDarkCard.copy(alpha = 0.9f),
                            PremiumDarkCard.copy(alpha = 0.8f),
                            PremiumDarkCard.copy(alpha = 0.9f)
                        )
                    )
                )
        ) {
            // Animated background glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                PremiumCyan.copy(alpha = backgroundPulse),
                                PremiumCyan.copy(alpha = backgroundPulse * 1.5f),
                                PremiumCyan.copy(alpha = backgroundPulse),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            // Waveform with enhanced styling
            AudioWaveform(
                amplitudes = waveformData,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                color = PremiumCyan
            )
            
            // Subtle grid overlay for tech feel
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSpacing = 40.dp.toPx()
                val alpha = 0.02f
                
                // Vertical lines
                var x = gridSpacing
                while (x < size.width) {
                    drawLine(
                        color = PremiumCyan.copy(alpha = alpha),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                    x += gridSpacing
                }
                
                // Horizontal lines
                var y = gridSpacing
                while (y < size.height) {
                    drawLine(
                        color = PremiumCyan.copy(alpha = alpha),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += gridSpacing
                }
            }
        }
    }
}

@Composable
private fun PremiumProgressSection(
    progress: Float,
    isCompact: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "progressAnim")
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Enhanced progress bar with multi-layer glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            // Track with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                PremiumDarkCard,
                                PremiumDarkSurface,
                                PremiumDarkCard
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = PremiumBorder.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(5.dp)
                    )
            )
            
            // Progress with enhanced gradient and glow
            if (progress > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(10.dp)
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(5.dp))
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(5.dp),
                            spotColor = PremiumCyan.copy(alpha = 0.6f)
                        )
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    PremiumCyanDark,
                                    PremiumCyan,
                                    PremiumCyan.copy(alpha = 0.9f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(5.dp)
                        )
                )
                
                // Animated glow at progress tip with pulse
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glowAlpha"
                )
                
                val glowScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glowScale"
                )
                
                Box(
                    modifier = Modifier
                        .size(20.dp * glowScale)
                        .align(Alignment.CenterStart)
                        .offset(x = (progress * 100).dp - 10.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    PremiumCyan.copy(alpha = glowAlpha * 0.8f),
                                    PremiumCyan.copy(alpha = glowAlpha * 0.4f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                // Progress tip indicator
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = (progress * 100).dp - 3.dp)
                        .background(Color.White, CircleShape)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            spotColor = Color.White
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(18.dp))
        
        // Time display with enhanced styling
        val seconds = (progress * 12).toInt()
        val remainingSeconds = 12 - seconds
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "00:%02d".format(seconds),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = PremiumCyan,
                letterSpacing = 1.sp,
                modifier = Modifier.shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    spotColor = PremiumCyan.copy(alpha = 0.5f)
                )
            )
            Text(
                text = "-00:%02d".format(remainingSeconds),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun PremiumStopButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "stopPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .size(76.dp)
                .scale(pulseScale)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = ErrorRed.copy(alpha = 0.4f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ErrorRedDark
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Stop,
                contentDescription = "Arrêter",
                tint = TextPrimary,
                modifier = Modifier.size(34.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Arrêter",
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

// =============================================================================
// ANALYZING STATE - PREMIUM DESIGN
// =============================================================================

@Composable
private fun AnalyzingState(statusMessage: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(28.dp)
        ) {
            // Premium animated loader
            PremiumAnalyzingLoader()
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Animated text
            val infiniteTransition = rememberInfiniteTransition(label = "textAnim")
            val textAlpha by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "textAlpha"
            )
            
            Text(
                text = "Analyse en cours...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary.copy(alpha = textAlpha)
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Status message with typing effect
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = PremiumCyan.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Progress dots
            AnimatedProgressDots()
        }
    }
}

@Composable
private fun PremiumAnalyzingLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")
    
    // Multiple ring rotations at different speeds
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRotation"
    )
    
    val middleRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "middleRotation"
    )
    
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRotation"
    )
    
    // Multi-layer pulse effects
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    val iconPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconPulse"
    )
    
    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        // Multiple outer glow layers
        Box(
            modifier = Modifier
                .size(180.dp * pulse * 1.1f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PremiumCyan.copy(alpha = glowAlpha * 0.3f),
                            PremiumCyan.copy(alpha = glowAlpha * 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        Box(
            modifier = Modifier
                .size(160.dp * pulse)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PremiumCyan.copy(alpha = glowAlpha * 0.5f),
                            PremiumCyan.copy(alpha = glowAlpha * 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Outermost rotating ring
        Canvas(
            modifier = Modifier
                .size(150.dp)
                .rotate(outerRotation)
        ) {
            val strokeWidth = 4.5.dp.toPx()
            
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        PremiumCyan.copy(alpha = 0.2f),
                        PremiumCyan.copy(alpha = 0.6f),
                        PremiumCyan,
                        PremiumCyan.copy(alpha = 0.6f),
                        PremiumCyan.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                startAngle = 0f,
                sweepAngle = 300f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        // Middle rotating ring (opposite direction)
        Canvas(
            modifier = Modifier
                .size(120.dp)
                .rotate(middleRotation)
        ) {
            val strokeWidth = 3.5.dp.toPx()
            
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        PremiumCyanDark.copy(alpha = 0.4f),
                        PremiumCyanDark.copy(alpha = 0.8f),
                        PremiumCyanDark.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                ),
                startAngle = 30f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        // Inner rotating ring
        Canvas(
            modifier = Modifier
                .size(90.dp)
                .rotate(innerRotation)
        ) {
            val strokeWidth = 2.5.dp.toPx()
            
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        PremiumCyan.copy(alpha = 0.5f),
                        PremiumCyan.copy(alpha = 0.9f),
                        PremiumCyan.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                ),
                startAngle = 45f,
                sweepAngle = 220f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        // Center icon with enhanced glow
        Box(
            modifier = Modifier
                .size(68.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = PremiumCyan.copy(alpha = glowAlpha * 0.4f)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PremiumDarkCard,
                            PremiumDarkSurface,
                            PremiumDarkBackground
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PremiumCyan.copy(alpha = 0.6f),
                            PremiumBorder.copy(alpha = 0.4f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inner icon glow
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PremiumCyan.copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            Icon(
                imageVector = Icons.Rounded.GraphicEq,
                contentDescription = null,
                tint = PremiumCyan,
                modifier = Modifier
                    .size(32.dp)
                    .scale(iconPulse)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        spotColor = PremiumCyan.copy(alpha = 0.6f)
                    )
            )
        }
    }
}

@Composable
private fun AnimatedProgressDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            val delay = index * 200
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = delay),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dotAlpha$index"
            )
            
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = PremiumCyan.copy(alpha = alpha),
                        shape = CircleShape
                    )
            )
        }
    }
}

// =============================================================================
// SUCCESS STATE - PREMIUM DESIGN
// =============================================================================

@Composable
private fun SuccessState(
    diagnostic: AudioDiagnosticData,
    comprehensiveDiagnostic: com.example.autobrain.data.ai.ComprehensiveAudioDiagnostic?,
    isComprehensiveAnalyzing: Boolean,
    onPerformComprehensive: () -> Unit,
    onSaveAndContinue: () -> Unit,
    onNewDiagnostic: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isCompactScreen = screenHeight < 700.dp
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(if (isCompactScreen) 16.dp else 24.dp))
            
            // Success Header with animation
            PremiumSuccessHeader()
            
            Spacer(modifier = Modifier.height(if (isCompactScreen) 24.dp else 40.dp))
        }
        
        // Animated Score circle
        item {
            PremiumScoreCircle(
                score = diagnostic.rawScore,
                healthStatus = diagnostic.healthStatus,
                size = if (isCompactScreen) 200.dp else 240.dp
            )
            
            Spacer(modifier = Modifier.height(if (isCompactScreen) 24.dp else 32.dp))
        }
        
        // Critical warning
        if (diagnostic.criticalWarning.isNotEmpty()) {
            item {
                PremiumCriticalWarningCard(warning = diagnostic.criticalWarning)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        
        // Detected issues
        if (diagnostic.detectedIssues.isNotEmpty()) {
            item {
                PremiumSectionHeader(
                    icon = Icons.Rounded.Warning,
                    title = "Problèmes Détectés",
                    iconTint = Color(0xFFFFAB00)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            items(diagnostic.detectedIssues) { issue ->
                PremiumIssueCard(issue = issue)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        
        // Recommendations
        if (diagnostic.recommendations.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                PremiumSectionHeader(
                    icon = Icons.Rounded.Lightbulb,
                    title = "Recommandations",
                    iconTint = PremiumCyan
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            items(diagnostic.recommendations) { recommendation ->
                PremiumRecommendationCard(text = recommendation)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        // Comprehensive Analysis Button
        item {
            Spacer(modifier = Modifier.height(24.dp))
            PremiumGeminiAnalysisButton(
                isAnalyzing = isComprehensiveAnalyzing,
                onClick = onPerformComprehensive
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Comprehensive Diagnostic Results
        if (comprehensiveDiagnostic != null) {
            item {
                ComprehensiveDiagnosticCard(comprehensiveDiagnostic)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        
        // Actions
        item {
            Spacer(modifier = Modifier.height(8.dp))
            
            PremiumActionButton(
                text = "Sauvegarder & Continuer",
                icon = Icons.Rounded.Check,
                onClick = onSaveAndContinue
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            PremiumSecondaryButton(
                text = "Nouvelle Analyse",
                onClick = onNewDiagnostic
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PremiumSuccessHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "successAnim")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = SuccessGreen.copy(alpha = glowAlpha),
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Analyse Terminée",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun PremiumScoreCircle(
    score: Int,
    healthStatus: String,
    size: Dp
) {
    // Animated score value
    var animatedScore by remember { mutableIntStateOf(0) }
    LaunchedEffect(score) {
        animate(
            initialValue = 0f,
            targetValue = score.toFloat(),
            animationSpec = tween(2000, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatedScore = value.toInt()
        }
    }
    
    val scoreColor = when {
        score >= 90 -> Color(0xFF00E676)
        score >= 75 -> Color(0xFF76FF03)
        score >= 60 -> Color(0xFFFFEA00)
        score >= 40 -> Color(0xFFFF9100)
        else -> Color(0xFFFF5252)
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "scoreGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )
    
    val counterRingRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counterRingRotation"
    )
    
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Multi-layer outer glow
        Box(
            modifier = Modifier
                .size(size * glowScale * 1.15f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            scoreColor.copy(alpha = glowAlpha * 0.25f),
                            scoreColor.copy(alpha = glowAlpha * 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        Box(
            modifier = Modifier
                .size(size * glowScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            scoreColor.copy(alpha = glowAlpha * 0.4f),
                            scoreColor.copy(alpha = glowAlpha * 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Background track with enhanced styling
        Canvas(modifier = Modifier.size(size * 0.9f)) {
            val strokeWidth = 16.dp.toPx()
            val radius = (this.size.minDimension - strokeWidth) / 2
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PremiumDarkCard,
                        PremiumDarkSurface
                    )
                ),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )
        }
        
        // Animated score arc with enhanced gradient
        Canvas(modifier = Modifier.size(size * 0.9f)) {
            val strokeWidth = 16.dp.toPx()
            val sweepAngle = (animatedScore / 100f) * 360f
            
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        scoreColor.copy(alpha = 0.6f),
                        scoreColor,
                        scoreColor.copy(alpha = 0.9f),
                        scoreColor,
                        scoreColor.copy(alpha = 0.7f)
                    )
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(
                    this.size.width - strokeWidth,
                    this.size.height - strokeWidth
                )
            )
        }
        
        // Outer decorative rotating ring
        Canvas(
            modifier = Modifier
                .size(size * 0.78f)
                .rotate(ringRotation)
        ) {
            val strokeWidth = 1.5.dp.toPx()
            
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        scoreColor.copy(alpha = 0.4f),
                        scoreColor.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                startAngle = 0f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(strokeWidth, strokeWidth),
                size = Size(this.size.width - strokeWidth * 2, this.size.height - strokeWidth * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        // Inner decorative counter-rotating ring
        Canvas(
            modifier = Modifier
                .size(size * 0.7f)
                .rotate(counterRingRotation)
        ) {
            val strokeWidth = 1.dp.toPx()
            
            drawArc(
                color = scoreColor.copy(alpha = 0.3f),
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(strokeWidth, strokeWidth),
                size = Size(this.size.width - strokeWidth * 2, this.size.height - strokeWidth * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score value with glow
            Text(
                text = animatedScore.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = scoreColor,
                letterSpacing = 2.sp,
                modifier = Modifier.shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = scoreColor.copy(alpha = 0.5f)
                )
            )
            Text(
                text = "/100",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            // Enhanced health status badge
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(14.dp),
                        spotColor = scoreColor.copy(alpha = 0.4f)
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                scoreColor.copy(alpha = 0.18f),
                                scoreColor.copy(alpha = 0.12f)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        color = scoreColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    text = healthStatus,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun PremiumCriticalWarningCard(warning: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "warningPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Red.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D0A0A).copy(alpha = pulseAlpha)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Red.copy(alpha = 0.8f),
                    Color.Red.copy(alpha = 0.4f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = warning,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PremiumSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    iconTint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun PremiumIssueCard(issue: com.example.autobrain.data.local.entity.IssueData) {
    val severityColor = when (issue.severity) {
        "CRITICAL" -> Color(0xFFFF5252)
        "HIGH" -> Color(0xFFFF9100)
        "MEDIUM" -> Color(0xFFFFEA00)
        else -> Color(0xFF76FF03)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = severityColor.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PremiumDarkCard.copy(alpha = 0.9f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    severityColor.copy(alpha = 0.4f),
                    PremiumBorder.copy(alpha = 0.3f)
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = issue.soundType.replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PremiumCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = issue.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                // Severity badge
                Box(
                    modifier = Modifier
                        .background(
                            color = severityColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = severityColor.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = issue.severity,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = severityColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bottom info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(issue.confidence * 100).toInt()}% confiance",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                
                Text(
                    text = "${issue.minCost.toInt()}-${issue.maxCost.toInt()} USD",
                    style = MaterialTheme.typography.bodySmall,
                    color = PremiumCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PremiumRecommendationCard(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        PremiumDarkCard.copy(alpha = 0.8f),
                        PremiumDarkCard.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        PremiumCyan.copy(alpha = 0.3f),
                        PremiumBorder.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = PremiumCyan,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun PremiumGeminiAnalysisButton(
    isAnalyzing: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "geminiGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    val purpleGradient = listOf(
        Color(0xFF9C27B0),
        Color(0xFF7B1FA2),
        Color(0xFF6A1B9A)
    )
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = Color(0xFF9C27B0).copy(alpha = glowAlpha * 0.5f)
            ),
        enabled = !isAnalyzing,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (isAnalyzing) purpleGradient.map { it.copy(alpha = 0.5f) } else purpleGradient
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isAnalyzing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Analyse IA en cours...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GeminiIcon(size = 18.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Analyse Complète Gemini AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            "11 sections détaillées • Marché automobile",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumSecondaryButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    PremiumCyan.copy(alpha = 0.8f),
                    PremiumCyanDark.copy(alpha = 0.6f)
                )
            )
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = PremiumCyan
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// =============================================================================
// ERROR STATE - PREMIUM DESIGN
// =============================================================================

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated error icon
        val infiniteTransition = rememberInfiniteTransition(label = "errorAnim")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
        
        Box(
            modifier = Modifier
                .size(110.dp)
                .scale(pulseScale),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ErrorRed.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            // Error circle
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ErrorRedMuted,
                                ErrorRedDark
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = ErrorRed.copy(alpha = 0.6f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        Text(
            text = "Erreur",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(14.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        PremiumActionButton(
            text = "Réessayer",
            icon = Icons.Rounded.Refresh,
            onClick = onRetry
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        PremiumSecondaryButton(
            text = "Retour",
            onClick = onBack
        )
    }
}

// =============================================================================
// COMPREHENSIVE DIAGNOSTIC CARD - PREMIUM DESIGN
// =============================================================================

@Composable
private fun ComprehensiveDiagnosticCard(
    diagnostic: com.example.autobrain.data.ai.ComprehensiveAudioDiagnostic
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cardGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header with Gemini badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFBA68C8),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Analyse Complète Gemini AI",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCE93D8)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Enhanced Health Score Banner
        PremiumHealthScoreBanner(
            score = diagnostic.enhancedHealthScore,
            severity = diagnostic.primaryDiagnosis.severity,
            isSafe = diagnostic.isSafeToDrive(),
            glowAlpha = glowAlpha
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Primary Diagnosis
        PremiumDiagnosisCard(
            title = "Diagnostic Principal",
            icon = Icons.Rounded.Search,
            diagnostic = diagnostic.primaryDiagnosis
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Repair Cost
        diagnostic.getMostLikelyRepairCost()?.let { scenario ->
            PremiumRepairCostCard(scenario = scenario)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Market Value Impact
        PremiumMarketValueCard(impact = diagnostic.marketValueImpact)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Recommendations Sections
        var expandedSection by remember { mutableStateOf<String?>(null) }
        
        PremiumRecommendationSection(
            title = "Pour le Propriétaire",
            icon = Icons.Rounded.Person,
            recommendations = diagnostic.intelligentRecommendations.forCurrentOwner,
            isExpanded = expandedSection == "owner",
            onToggle = { expandedSection = if (expandedSection == "owner") null else "owner" }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        PremiumRecommendationSection(
            title = "Pour l'Acheteur",
            icon = Icons.Rounded.ShoppingCart,
            recommendations = diagnostic.intelligentRecommendations.forPotentialBuyer,
            isExpanded = expandedSection == "buyer",
            onToggle = { expandedSection = if (expandedSection == "buyer") null else "buyer" }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        PremiumRecommendationSection(
            title = "Pour le Mécanicien",
            icon = Icons.Rounded.Build,
            recommendations = diagnostic.intelligentRecommendations.forMechanic,
            isExpanded = expandedSection == "mechanic",
            onToggle = { expandedSection = if (expandedSection == "mechanic") null else "mechanic" }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // AI Confidence Footer
        PremiumAIConfidenceFooter(confidence = diagnostic.autobrainAiConfidence)
    }
}

@Composable
private fun PremiumHealthScoreBanner(
    score: Int,
    severity: String,
    isSafe: Boolean,
    glowAlpha: Float
) {
    val backgroundColor = when {
        score >= 80 -> Color(0xFF1B5E20)
        score >= 60 -> Color(0xFF5D4037)
        score >= 40 -> Color(0xFFE65100)
        else -> Color(0xFFB71C1C)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = backgroundColor.copy(alpha = glowAlpha)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.9f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Score Santé Amélioré",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "/100",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sévérité: $severity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                
                if (!isSafe) {
                    Row(
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "NE PAS CONDUIRE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumDiagnosisCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    diagnostic: com.example.autobrain.data.ai.PrimaryDiagnosis
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PremiumDarkCard.copy(alpha = 0.9f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    PremiumCyan.copy(alpha = 0.3f),
                    PremiumBorder.copy(alpha = 0.2f)
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PremiumCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Text(
                text = diagnostic.issue,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "Technique: ${diagnostic.technicalName}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null,
                        tint = PremiumCyan.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(diagnostic.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = PremiumCyan
                    )
                }
                Text(
                    text = "${diagnostic.affectedComponents.size} composants",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun PremiumRepairCostCard(
    scenario: com.example.autobrain.data.ai.RepairScenario
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PremiumDarkCard.copy(alpha = 0.9f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFD700).copy(alpha = 0.3f),
                    PremiumBorder.copy(alpha = 0.2f)
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Payments,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Coût de Réparation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Text(
                text = scenario.scenario,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Coût Total",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${scenario.totalCostUsd.toInt()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Durée",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${scenario.durationDays} jours",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Probability bar
            Column {
                Text(
                    text = "Probabilité: ${(scenario.probability * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PremiumDarkSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(scenario.probability.toFloat())
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumMarketValueCard(
    impact: com.example.autobrain.data.ai.MarketValueImpact
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PremiumDarkCard.copy(alpha = 0.9f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF4CAF50).copy(alpha = 0.3f),
                    PremiumBorder.copy(alpha = 0.2f)
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Impact sur la Valeur",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            PremiumValueRow("Valeur avant problème", "$${impact.valueBeforeIssue.toInt()}", Color.White)
            PremiumValueRow("Valeur en l'état", "$${impact.valueAsIs.toInt()}", Color(0xFFFFAB00))
            PremiumValueRow("Valeur après réparation", "$${impact.valueAfterRepair.toInt()}", Color(0xFF4CAF50))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Loss indicator
            val loss = impact.valueBeforeIssue - impact.valueAsIs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.Red.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perte estimée",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "-${loss.toInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5252)
                )
            }
        }
    }
}

@Composable
private fun PremiumValueRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PremiumRecommendationSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    recommendations: List<String>,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = PremiumDarkCard.copy(alpha = 0.8f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isExpanded) PremiumCyan.copy(alpha = 0.4f) else PremiumBorder.copy(alpha = 0.3f)
        ),
        onClick = onToggle
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PremiumCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = PremiumCyan.copy(alpha = 0.7f)
                )
            }
            
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    recommendations.forEach { rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(6.dp)
                                    .background(PremiumCyan, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = rec,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumAIConfidenceFooter(
    confidence: com.example.autobrain.data.ai.AutobrainAiConfidence
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                PremiumDarkCard.copy(alpha = 0.5f),
                RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = PremiumBorder.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            GeminiIcon(size = 16.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Confiance IA: ${(confidence.analysisConfidence * 100).toInt()}%",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        Text(
            text = confidence.geminiModelVersion,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.4f)
        )
    }
}
