package com.example.autobrain.presentation.screens.diagnostics

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autobrain.R
import com.example.autobrain.data.ai.getMostLikelyRepairScenario
import com.example.autobrain.data.ai.isSafeToDrive
import com.example.autobrain.data.local.entity.VideoDiagnosticData
import com.example.autobrain.presentation.components.CameraPreview
import com.example.autobrain.presentation.components.GeminiIcon
import com.example.autobrain.presentation.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.math.cos
import kotlin.math.sin

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun VideoDiagnosticsScreen(
    navController: NavController,
    viewModel: VideoDiagnosticViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val comprehensiveVideoDiagnostic by viewModel.comprehensiveVideoDiagnostic.collectAsState()
    val isComprehensiveAnalyzing by viewModel.isComprehensiveAnalyzing.collectAsState()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    // Initialize diagnostic on first load
    LaunchedEffect(Unit) {
        viewModel.initializeDiagnostic(carId = "current_car") // In real app, pass actual car ID
    }

    Scaffold(
        containerColor = Color(0xFF0A1628),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ElectricTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A1628),
                            Color(0xFF0D1117)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is VideoDiagnosticState.Idle -> {
                    CaptureVideoState(
                        onStartCapture = {
                            if (cameraPermission.status.isGranted) {
                                viewModel.onPermissionGranted()
                            } else {
                                cameraPermission.launchPermissionRequest()
                                viewModel.onPermissionRequested()
                            }
                        }
                    )
                }
                
                is VideoDiagnosticState.PermissionRequired -> {
                    // Check permission again just in case
                    if (cameraPermission.status.isGranted) {
                        viewModel.onPermissionGranted()
                    } else {
                        // Show permission rationale or re-request
                        LaunchedEffect(Unit) {
                            cameraPermission.launchPermissionRequest()
                        }
                    }
                }

                is VideoDiagnosticState.Previewing -> {
                    CameraPreviewState(
                        onImageAnalysis = { viewModel.analyzeFrame(it) },
                        onStartRecording = { viewModel.startRecording() }
                    )
                }

                is VideoDiagnosticState.Recording -> {
                    RecordingVideoState(
                        progress = state.progress,
                        qualityStatus = state.qualityStatus,
                        isQualityGood = state.isQualityGood,
                        onImageAnalysis = { viewModel.analyzeFrame(it) },
                        onStopRecording = { viewModel.stopRecording() }
                    )
                }

                is VideoDiagnosticState.Analyzing -> {
                    AnalyzingVideoState(message = state.message)
                }

                is VideoDiagnosticState.Success -> {
                    VideoResultState(
                        diagnostic = state.diagnostic,
                        comprehensiveDiagnostic = comprehensiveVideoDiagnostic,
                        isComprehensiveAnalyzing = isComprehensiveAnalyzing,
                        onPerformComprehensive = { /* Auto-triggered, button hidden */ },
                        onViewFullReport = { 
                            navController.navigate("comprehensive_video_report/${state.diagnostic.id}")
                        },
                        onNewAnalysis = { viewModel.initializeDiagnostic("current_car") }
                    )
                }

                is VideoDiagnosticState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.retryDiagnostic() }
                    )
                }
            }
        }
    }
}

@Composable
private fun CaptureVideoState(
    onStartCapture: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "capture_animations")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                ElectricTeal.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    ElectricTeal,
                                    ElectricTeal.copy(alpha = glowAlpha)
                                )
                            ),
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "AI-POWERED DIAGNOSTICS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = ElectricTeal,
                    letterSpacing = 1.2.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Unlock Your Car's",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Hidden Secrets",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = ElectricTeal,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "10-second scan reveals what mechanics see",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .rotate(rotation * 0.1f)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    ElectricTeal.copy(alpha = 0.08f * glowAlpha),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .rotate(-rotation * 0.15f)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00FF88).copy(alpha = 0.06f * glowAlpha),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                
                EnhancedCarIllustration(rotation = rotation, glowAlpha = glowAlpha, pulseScale = pulseScale)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AIFeatureChip(
                    icon = Icons.Default.Speed,
                    text = "Real-time AI",
                    modifier = Modifier.weight(1f)
                )
                AIFeatureChip(
                    icon = Icons.Default.Shield,
                    text = "Safety Score",
                    modifier = Modifier.weight(1f)
                )
                AIFeatureChip(
                    icon = Icons.Default.Build,
                    text = "Instant Fix",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F2838)
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            ElectricTeal.copy(alpha = 0.3f),
                            Color(0xFF00FF88).copy(alpha = 0.2f),
                            ElectricTeal.copy(alpha = 0.3f)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = ElectricTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Recording Instructions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricTeal,
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    InstructionRow(
                        number = "1",
                        text = "Focus on exhaust & engine bay"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    InstructionRow(
                        number = "2",
                        text = "Keep engine running (idle)"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    InstructionRow(
                        number = "3",
                        text = "Record for 10 seconds minimum"
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ElectricTeal.copy(alpha = 0.3f * glowAlpha),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(82.dp * pulseScale)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    ElectricTeal,
                                    Color(0xFF00D9C0)
                                )
                            ),
                            CircleShape
                        )
                        .clickable(onClick = onStartCapture),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Start Recording",
                        tint = Color.Black,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "TAP TO START",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = ElectricTeal,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        Color(0xFF0A1F2E).copy(alpha = 0.6f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "100% Private • No data shared • AI on-device",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CameraPreviewState(
    onImageAnalysis: (androidx.camera.core.ImageProxy) -> Unit,
    onStartRecording: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onImageAnalysis = onImageAnalysis
        )
        
        // Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "Ready to Record",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Record Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(8.dp)
                    .background(Color.Red, CircleShape)
                    .clickable(onClick = onStartRecording)
            )
        }
    }
}

@Composable
private fun RecordingVideoState(
    progress: Float,
    qualityStatus: String,
    isQualityGood: Boolean,
    onImageAnalysis: (androidx.camera.core.ImageProxy) -> Unit,
    onStopRecording: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onImageAnalysis = onImageAnalysis
        )
        
        // Progress Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(if (isQualityGood) SuccessGreen else WarningAmber, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = qualityStatus,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Bottom Controls
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = ElectricTeal,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(20.dp)
                        .background(Color.Red, RoundedCornerShape(8.dp))
                        .clickable(onClick = onStopRecording)
                )
            }
        }
    }
}

@Composable
private fun AnalyzingVideoState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "analyzing")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing)
                ),
                label = "rotation"
            )

            CircularProgressIndicator(
                modifier = Modifier
                    .size(60.dp)
                    .rotate(rotation),
                color = ElectricTeal,
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = message,
                fontSize = 20.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun VideoResultState(
    diagnostic: VideoDiagnosticData,
    comprehensiveDiagnostic: com.example.autobrain.data.ai.ComprehensiveVideoDiagnostic?,
    isComprehensiveAnalyzing: Boolean,
    onPerformComprehensive: () -> Unit,
    onViewFullReport: () -> Unit,
    onNewAnalysis: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Video Analysis Complete",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 32.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Success Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = Color(0xFF0F2A3F),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = ElectricTeal,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Results Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F2838)
            ),
            border = BorderStroke(1.dp, Color(0xFF1C2838))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Observations",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (diagnostic.detectedIssues.isEmpty()) {
                    ObservationItem(
                        icon = Icons.Default.CheckCircle,
                        text = "No significant issues detected.",
                        iconColor = SuccessGreen
                    )
                } else {
                    diagnostic.detectedIssues.forEach { issue ->
                        ObservationItem(
                            icon = Icons.Default.Warning,
                            text = issue.description,
                            iconColor = WarningAmber
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                
                // Show recommendations if any (from Gemini)
                if (diagnostic.recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Recommendations",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    diagnostic.recommendations.forEach { rec ->
                        ObservationItem(
                            icon = Icons.Default.Info,
                            text = rec,
                            iconColor = ElectricTeal
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onViewFullReport,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = comprehensiveDiagnostic != null && !isComprehensiveAnalyzing,
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricTeal,
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFF1C2838)
            )
        ) {
            if (isComprehensiveAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Analyse IA en cours...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(Icons.Default.Description, null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (comprehensiveDiagnostic != null) "Voir Rapport Complet" else "Analyse en cours...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Display comprehensive results if available
        if (comprehensiveDiagnostic != null) {
            Spacer(modifier = Modifier.height(24.dp))
            ComprehensiveVideoDiagnosticCard(comprehensiveDiagnostic!!)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// =============================================================================
// COMPREHENSIVE VIDEO DIAGNOSTIC CARD
// =============================================================================

@Composable
private fun ComprehensiveVideoDiagnosticCard(
    diagnostic: com.example.autobrain.data.ai.ComprehensiveVideoDiagnostic
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header
        Text(
            text = "🎬 Analyse Vidéo Complète Gemini AI",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFBA68C8),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Enhanced Visual Score Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    diagnostic.enhancedVisualScore >= 80 -> Color(0xFF1B5E20)
                    diagnostic.enhancedVisualScore >= 60 -> Color(0xFFF57F17)
                    diagnostic.enhancedVisualScore >= 40 -> Color(0xFFE65100)
                    else -> Color(0xFFB71C1C)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Score Visuel Amélioré",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${diagnostic.enhancedVisualScore}/100",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sécurité: ${diagnostic.safetyAssessment.roadworthiness}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                
                // Safety Warning
                if (!diagnostic.isSafeToDrive()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "⚠️ CONDUITE DÉCONSEILLÉE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Smoke Analysis
        if (diagnostic.smokeDeepAnalysis.typeDetected != "none" && diagnostic.smokeDeepAnalysis.typeDetected != "unknown") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F2838)
                ),
                border = BorderStroke(1.dp, Color(0xFF1C2838))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💨 Analyse Fumée",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricTeal
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Type: ${diagnostic.smokeDeepAnalysis.typeDetected.uppercase()}",
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = diagnostic.smokeDeepAnalysis.technicalDiagnosis,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Vibration Analysis
        if (diagnostic.vibrationEngineeringAnalysis.vibrationSourceDiagnosis != "N/A") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F2838)
                ),
                border = BorderStroke(1.dp, Color(0xFF1C2838))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚡ Analyse Vibrations",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricTeal
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Source: ${diagnostic.vibrationEngineeringAnalysis.vibrationSourceDiagnosis}",
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fréquence: ${diagnostic.vibrationEngineeringAnalysis.vibrationFrequencyEstimation}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Multimodal Correlation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F2838)
            ),
            border = BorderStroke(1.dp, Color(0xFF1C2838))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔗 Corrélation Audio-Vidéo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricTeal
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Score: ${(diagnostic.combinedAudioVideoDiagnosis.correlationScore * 100).toInt()}%",
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = diagnostic.combinedAudioVideoDiagnosis.comprehensiveRootCause,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Most Likely Repair Scenario
        diagnostic.getMostLikelyRepairScenario()?.let { scenario ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F2838)
                ),
                border = BorderStroke(1.dp, Color(0xFF1C2838))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💰 Réparation Probable",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricTeal
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = scenario.scenarioName,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Coût Total",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "$${scenario.totalCostUsd.toInt()}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricTeal
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Probabilité",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "${(scenario.successProbability * 100).toInt()}%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Safety Assessment
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F2838)
            ),
            border = BorderStroke(1.dp, Color(0xFF1C2838))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🚦 Évaluation Sécurité",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricTeal
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Probabilité de panne (30 jours): ${(diagnostic.safetyAssessment.breakdownProbabilityNext30Days * 100).toInt()}%",
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                
                if (diagnostic.safetyAssessment.drivingRestrictions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    diagnostic.safetyAssessment.drivingRestrictions.forEach { restriction ->
                        if (restriction.isNotBlank()) {
                            Text(
                                text = "• $restriction",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // AI Confidence
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F2838).copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, Color(0xFF1C2838))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    GeminiIcon(size = 14.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Confiance IA: ${(diagnostic.autobrainVideoConfidence.confidenceThisAnalysis * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ML Kit + ${diagnostic.autobrainVideoConfidence.geminiModel}",
                    fontSize = 11.sp,
                    color = TextSecondary.copy(alpha = 0.7f)
                )
            }
        }
        
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = ErrorRed,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal)
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun ObservationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = TextPrimary,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun AIFeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F2838).copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, ElectricTeal.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ElectricTeal.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricTeal,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InstructionRow(
    number: String,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ElectricTeal.copy(alpha = 0.3f),
                            ElectricTeal.copy(alpha = 0.1f)
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = ElectricTeal
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EnhancedCarIllustration(
    rotation: Float,
    glowAlpha: Float,
    pulseScale: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val accentColor = Color(0xFF00FF88)
            val scanRadius = 220f
            
            for (i in 0..2) {
                val scanLineY = centerY - scanRadius + (i * 150f) + ((rotation * 3f) % 450f)
                if (scanLineY >= centerY - scanRadius && scanLineY <= centerY + scanRadius) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                accentColor.copy(alpha = 0.3f * glowAlpha),
                                accentColor.copy(alpha = 0.7f * glowAlpha),
                                accentColor.copy(alpha = 0.9f * glowAlpha),
                                accentColor.copy(alpha = 0.7f * glowAlpha),
                                accentColor.copy(alpha = 0.3f * glowAlpha),
                                Color.Transparent
                            )
                        ),
                        start = Offset(centerX - scanRadius, scanLineY),
                        end = Offset(centerX + scanRadius, scanLineY),
                        strokeWidth = 6.dp.toPx()
                    )
                }
            }
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.2f * glowAlpha),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = 250f
                ),
                radius = 250f,
                center = Offset(centerX, centerY)
            )
        }
        
        Image(
            painter = painterResource(id = R.drawable.noslogon),
            contentDescription = "Logo",
            modifier = Modifier
                .size(350.dp * pulseScale)
        )
    }
}
