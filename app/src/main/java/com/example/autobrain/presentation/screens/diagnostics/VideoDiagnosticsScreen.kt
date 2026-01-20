package com.example.autobrain.presentation.screens.diagnostics

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
                        onPerformComprehensive = { viewModel.performComprehensiveAnalysis(state.diagnostic) },
                        onViewFullReport = { /* TODO: Navigate to details */ },
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Capture Diagnostic Video",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(60.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            CarWireframeIllustration()
        }

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F2838)
            ),
            border = BorderStroke(1.dp, Color(0xFF1C2838))
        ) {
            Text(
                text = "Focus camera on exhaust & engine bay for 10s",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = ElectricTeal,
                    shape = CircleShape
                )
                .clickable(onClick = onStartCapture),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Capture",
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ensure car is parked on a level surface,\nengine running for sound check.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))
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
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricTeal,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "View Full Report",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Comprehensive Analysis Button
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onPerformComprehensive,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isComprehensiveAnalyzing,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0), // Purple for AI
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF4A148C).copy(alpha = 0.5f)
            )
        ) {
            if (isComprehensiveAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Analyse IA en cours...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "🎬 Analyse Vidéo Complète Gemini AI",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "10 sections • ML Kit + Audio corrélation",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // Display comprehensive results if available
        if (comprehensiveDiagnostic != null) {
            Spacer(modifier = Modifier.height(24.dp))
            ComprehensiveVideoDiagnosticCard(comprehensiveDiagnostic!!)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onNewAnalysis) {
            Text("New Analysis", color = TextSecondary)
        }

        Spacer(modifier = Modifier.height(24.dp))
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
private fun CarWireframeIllustration() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val color = Color(0xFF2DD4BF)
        val strokeWidth = 2.dp.toPx()

        val brakeRadius = 60f
        val brakeX = centerX - 80f
        val brakeY = centerY + 40f

        drawCircle(
            color = color,
            radius = brakeRadius,
            center = Offset(brakeX, brakeY),
            style = Stroke(width = strokeWidth)
        )

        for (i in 0..5) {
            val angle = (i * 60f) * (Math.PI / 180f).toFloat()
            val x1 = brakeX + (brakeRadius * 0.3f) * cos(angle)
            val y1 = brakeY + (brakeRadius * 0.3f) * sin(angle)
            val x2 = brakeX + brakeRadius * cos(angle)
            val y2 = brakeY + brakeRadius * sin(angle)
            
            drawLine(
                color = color,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = strokeWidth
            )
        }

        drawCircle(
            color = color,
            radius = 8f,
            center = Offset(brakeX, brakeY),
            style = Stroke(width = strokeWidth)
        )

        val framePoints = listOf(
            Offset(centerX - 120f, centerY - 60f),
            Offset(centerX + 120f, centerY - 60f),
            Offset(centerX + 100f, centerY + 60f),
            Offset(centerX - 100f, centerY + 60f)
        )

        val path = Path().apply {
            moveTo(framePoints[0].x, framePoints[0].y)
            framePoints.forEach { point ->
                lineTo(point.x, point.y)
            }
            close()
        }

        drawPath(
            path = path,
            color = color.copy(alpha = 0.4f),
            style = Stroke(width = strokeWidth)
        )

        drawLine(
            color = color.copy(alpha = 0.3f),
            start = Offset(centerX - 100f, centerY - 40f),
            end = Offset(centerX + 100f, centerY - 40f),
            strokeWidth = strokeWidth
        )

        val highlightRadius = 85f
        drawCircle(
            color = color.copy(alpha = 0.1f),
            radius = highlightRadius,
            center = Offset(brakeX, brakeY)
        )

        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = highlightRadius,
            center = Offset(brakeX, brakeY),
            style = Stroke(width = strokeWidth * 2)
        )
    }
}
