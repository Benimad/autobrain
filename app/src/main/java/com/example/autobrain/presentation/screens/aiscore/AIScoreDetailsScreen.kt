package com.example.autobrain.presentation.screens.aiscore

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autobrain.domain.model.*
import com.example.autobrain.presentation.navigation.Screen
import com.example.autobrain.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIScoreDetailsScreen(
    navController: NavController,
    carId: String = "current",
    viewModel: AIScoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNavIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MidnightBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AI Score",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
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
                    containerColor = MidnightBlack
                )
            )
        },
        bottomBar = {
            AutoBrainBottomNav(
                selectedIndex = selectedNavIndex,
                onItemSelected = { index ->
                    selectedNavIndex = index
                    when (index) {
                        0 -> navController.navigate(Screen.Home.route)
                        1 -> navController.navigate(Screen.ScanHistory.route)
                        2 -> navController.navigate(Screen.Profile.route)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ElectricTeal)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Animated AI Score Circle
                uiState.scoreResult?.let { result ->
                    AnimatedAIScoreCircle(
                        score = result.finalScore,
                        category = result.scoreCategory.displayName
                    )
                } ?: AnimatedAIScoreCircle(score = 94, category = "Excellent")

                Spacer(modifier = Modifier.height(16.dp))

                // How this score is calculated link
                Text(
                    text = "How this score is calculated",
                    fontSize = 14.sp,
                    color = ElectricTeal,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { viewModel.toggleBreakdownDialog() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Price Estimate Card
                uiState.scoreResult?.priceEstimate?.let { estimate ->
                    PriceEstimateCard(estimate)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Buyer Advice Card
                uiState.scoreResult?.let { result ->
                    BuyerAdviceCard(
                        advice = result.buyerAdvice,
                        riskLevel = result.riskLevel
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Score Breakdown Card
                uiState.scoreResult?.let { result ->
                    ScoreBreakdownCard(breakdown = result.breakdown)
                } ?: DefaultScoreBreakdownCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Detailed Issues List
                uiState.scoreResult?.issues?.let { issues ->
                    if (issues.totalIssues() > 0) {
                        IssuesListCard(issues)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
    
    // Show breakdown dialog if needed
    if (uiState.showBreakdownDialog) {
        ScoreCalculationDialog(
            onDismiss = { viewModel.toggleBreakdownDialog() }
        )
    }
}

@Composable
private fun AnimatedAIScoreCircle(
    score: Int,
    category: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scoreGlow")
    
    // Glow animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    // Score animation on load
    var animatedScore by remember { mutableIntStateOf(0) }
    var animatedSweep by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(score) {
        // Animate score count up
        val duration = 1500
        val startTime = System.currentTimeMillis()
        while (animatedScore < score) {
            val elapsed = System.currentTimeMillis() - startTime
            val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            animatedScore = (score * progress).toInt()
            animatedSweep = (score / 100f) * 360f * progress
            kotlinx.coroutines.delay(16)
        }
        animatedScore = score
        animatedSweep = (score / 100f) * 360f
    }
    
    Box(
        modifier = Modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow effect
        Box(
            modifier = Modifier
                .size(240.dp)
                .blur(30.dp)
                .alpha(glowAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ElectricTeal.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
        
        // Score ring
        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 14.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            // Background circle
            drawCircle(
                color = Color(0xFF1C2128),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            
            // Animated progress arc with gradient
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF0D9488),
                        Color(0xFF14B8A6),
                        Color(0xFF2DD4BF),
                        Color(0xFF5EEAD4),
                        Color(0xFF2DD4BF),
                        Color(0xFF14B8A6),
                        Color(0xFF0D9488)
                    )
                ),
                startAngle = -90f,
                sweepAngle = animatedSweep,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                ),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
        }
        
        // Inner dark circle
        Box(
            modifier = Modifier
                .size(180.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0D1B2A),
                            Color(0xFF0A1628)
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = animatedScore.toString(),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = category,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = ElectricTeal
                )
            }
        }
    }
}

@Composable
private fun ScoreBreakdownCard(breakdown: AIScoreBreakdown) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Score Breakdown",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Engine & Transmission
            AnimatedScoreBreakdownItem(
                icon = Icons.Outlined.Settings,
                title = "Engine & Transmission",
                score = breakdown.technicalScore.engineSoundScore,
                color = getScoreColor(breakdown.technicalScore.engineSoundScore)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Chassis & Body
            AnimatedScoreBreakdownItem(
                icon = Icons.Outlined.CheckCircle,
                title = "Chassis & Body",
                score = breakdown.technicalScore.videoAnalysisScore,
                color = getScoreColor(breakdown.technicalScore.videoAnalysisScore)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Electrical & Battery (derived from maintenance score)
            AnimatedScoreBreakdownItem(
                icon = Icons.Outlined.BatteryChargingFull,
                title = "Electrical & Battery",
                score = breakdown.maintenanceScore.overallScore * 70 / 100,
                color = getScoreColor(breakdown.maintenanceScore.overallScore * 70 / 100)
            )
        }
    }
}

@Composable
private fun DefaultScoreBreakdownCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Score Breakdown",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedScoreBreakdownItem(
                icon = Icons.Outlined.Settings,
                title = "Engine & Transmission",
                score = 98,
                color = ElectricTeal
            )

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedScoreBreakdownItem(
                icon = Icons.Outlined.CheckCircle,
                title = "Chassis & Body",
                score = 85,
                color = ElectricTeal
            )

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedScoreBreakdownItem(
                icon = Icons.Outlined.BatteryChargingFull,
                title = "Electrical & Battery",
                score = 70,
                color = WarningAmber
            )
        }
    }
}

@Composable
private fun AnimatedScoreBreakdownItem(
    icon: ImageVector,
    title: String,
    score: Int,
    color: Color
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(score) {
        // Animate score count up
        val duration = 800
        val startTime = System.currentTimeMillis()
        while (animatedProgress < score / 100f) {
            val elapsed = System.currentTimeMillis() - startTime
            val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            animatedProgress = (score / 100f) * progress
            kotlinx.coroutines.delay(16)
        }
        animatedProgress = score / 100f
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "(${score}%)",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Animated progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    color = Color(0xFF1C2128),
                    shape = RoundedCornerShape(3.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(6.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

@Composable
private fun ScoreCalculationDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "How AI Score is Calculated",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "The AI Score is calculated using:",
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                CalculationItem(
                    percentage = "70%",
                    title = "Technical Score",
                    description = "Engine sound analysis (40%) + Video analysis (30%)"
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                CalculationItem(
                    percentage = "20%",
                    title = "Maintenance Score",
                    description = "Oil change, technical inspection, insurance, mileage"
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                CalculationItem(
                    percentage = "10%",
                    title = "Market Score",
                    description = "Price comparison and model popularity"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Final adjustment (±10 points) by AI analysis for overall assessment.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it", color = ElectricTeal)
            }
        },
        containerColor = DeepNavy,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun CalculationItem(
    percentage: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = percentage,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricTeal,
            modifier = Modifier.width(50.dp)
        )
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

private fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> ElectricTeal
        score >= 60 -> SuccessGreen
        score >= 40 -> WarningAmber
        else -> ErrorRed
    }
}

@Composable
private fun AutoBrainBottomNav(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MidnightBlack,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Filled.Home,
                label = "Home",
                selected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            BottomNavItem(
                icon = Icons.Outlined.History,
                selectedIcon = Icons.Filled.History,
                label = "Scan History",
                selected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            BottomNavItem(
                icon = Icons.Outlined.Person,
                selectedIcon = Icons.Filled.Person,
                label = "Profile",
                selected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else icon,
            contentDescription = label,
            tint = if (selected) ElectricTeal else TextMuted,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (selected) ElectricTeal else TextMuted,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun PriceEstimateCard(estimate: PriceEstimate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2332))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ESTIMATED MARKET VALUE",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${estimate.lowPrice} - $${estimate.highPrice}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ElectricTeal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Based on ${estimate.basedOn.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun BuyerAdviceCard(advice: String, riskLevel: RiskLevel) {
    val (borderColor, icon) = when (riskLevel) {
        RiskLevel.LOW -> SuccessGreen to Icons.Default.CheckCircle
        RiskLevel.MEDIUM -> WarningAmber to Icons.Default.Warning
        RiskLevel.HIGH -> ErrorRed to Icons.Default.Dangerous
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = borderColor.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = borderColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "AI VERDICT",
                    style = MaterialTheme.typography.labelSmall,
                    color = borderColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = advice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun IssuesListCard(issues: IssuesList) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavy)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Identified Issues",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (issues.grave.isNotEmpty()) {
                IssueSection("CRITICAL ISSUES", issues.grave, ErrorRed)
            }
            if (issues.medium.isNotEmpty()) {
                if (issues.grave.isNotEmpty()) Spacer(modifier = Modifier.height(16.dp))
                IssueSection("MAJOR ISSUES", issues.medium, WarningAmber)
            }
            if (issues.minor.isNotEmpty()) {
                if (issues.grave.isNotEmpty() || issues.medium.isNotEmpty()) Spacer(modifier = Modifier.height(16.dp))
                IssueSection("MINOR OBSERVATIONS", issues.minor, ElectricTeal)
            }
        }
    }
}

@Composable
private fun IssueSection(title: String, issues: List<Issue>, color: Color) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        issues.forEach { issue ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(6.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = issue.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = issue.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    issue.estimatedRepairCost?.let { cost ->
                        Text(
                            text = "Est. repair: ${cost.start}-$${cost.endInclusive}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
