package com.example.autobrain.presentation.screens.carlog

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autobrain.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 💎 ENHANCED SMART SUMMARY CARD
 * Professional "wow" level card with health score, intelligent insights, and beautiful animations
 */
@Composable
fun EnhancedSmartSummaryCard(
    totalRecords: Int,
    totalExpenses: Double,
    nextMaintenance: String,
    healthScore: Int,
    avgCostPerMonth: Double,
    lastMaintenanceDate: Long?
) {
    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "card_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600), label = "card_alpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .scale(scale)
            .alpha(alpha),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepNavy,
                            DarkNavy,
                            Color(0xFF1A2332)
                        )
                    )
                )
        ) {
            // Decorative animated circles
            AnimatedDecorativeElements()
            
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Cost & Records
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = ElectricTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Total dépenses",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = formatCurrency(totalExpenses),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = (-1).sp
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(ElectricTeal.copy(alpha = 0.6f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalRecords entretiens enregistrés",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        
                        if (avgCostPerMonth > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(WarningAmber.copy(alpha = 0.6f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${formatCurrency(avgCostPerMonth)}/mois en moyenne",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Health Score Gauge
                    VehicleHealthGauge(
                        score = healthScore,
                        modifier = Modifier.size(110.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Next Maintenance Alert
                NextMaintenanceAlert(
                    nextMaintenance = nextMaintenance,
                    healthScore = healthScore
                )
                
                // Last Maintenance Info
                if (lastMaintenanceDate != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LastMaintenanceInfo(lastMaintenanceDate)
                }
            }
        }
    }
}

@Composable
private fun VehicleHealthGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing), label = "health_score"
    )
    
    val color = getHealthScoreColor(score)
    val label = getHealthScoreLabel(score)
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = SlateGray,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Animated score arc
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arcColors: List<Color> = listOf(
                color.copy(alpha = 0.3f),
                color,
                color.copy(alpha = 0.8f)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    colors = arcColors
                ),
                startAngle = 140f,
                sweepAngle = (260f * (animatedScore / 100f)),
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Score display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = animatedScore.toInt().toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = color.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Santé",
                fontSize = 9.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun NextMaintenanceAlert(
    nextMaintenance: String,
    healthScore: Int
) {
    val backgroundColor = when {
        healthScore < 40 -> ErrorRed.copy(alpha = 0.15f)
        healthScore < 70 -> WarningAmber.copy(alpha = 0.15f)
        else -> ElectricTeal.copy(alpha = 0.15f)
    }
    
    val iconColor = when {
        healthScore < 40 -> ErrorRed
        healthScore < 70 -> WarningAmber
        else -> ElectricTeal
    }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = iconColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Prochaine maintenance",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = nextMaintenance,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = iconColor.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LastMaintenanceInfo(lastMaintenanceDate: Long) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.FRANCE) }
    val dateStr = dateFormatter.format(Date(lastMaintenanceDate))
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SuccessGreen.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Dernier entretien: $dateStr",
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun AnimatedDecorativeElements() {
    val infiniteTransition = rememberInfiniteTransition(label = "decorative")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )
    
    // Large circle
    Box(
        modifier = Modifier
            .size(180.dp)
            .offset(x = (-50).dp, y = (-50).dp)
            .rotate(rotation)
            .alpha(0.05f)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        ElectricTeal,
                        Color.Transparent
                    )
                ),
                CircleShape
            )
    )
    
    // Small circle
    Box(
        modifier = Modifier
            .size(100.dp)
            .offset(x = 250.dp, y = 150.dp)
            .rotate(-rotation)
            .alpha(0.04f)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        SuccessGreen,
                        Color.Transparent
                    )
                ),
                CircleShape
            )
    )
}

private fun getHealthScoreColor(score: Int): Color {
    return when {
        score >= 80 -> SuccessGreen
        score >= 60 -> WarningAmber
        score >= 40 -> Color(0xFFFF9800)
        else -> ErrorRed
    }
}

private fun getHealthScoreLabel(score: Int): String {
    return when {
        score >= 80 -> "Excellent"
        score >= 60 -> "Good"
        score >= 40 -> "Fair"
        else -> "Poor"
    }
}

private fun formatCurrency(amount: Double): String {
    return String.format("%,.2f$", amount)
}
