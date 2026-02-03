package com.example.autobrain.presentation.screens.carlog

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autobrain.presentation.theme.ElectricTeal
import com.example.autobrain.presentation.theme.ErrorRed
import com.example.autobrain.presentation.theme.SlateGray
import com.example.autobrain.presentation.theme.SuccessGreen
import com.example.autobrain.presentation.theme.TextMuted
import com.example.autobrain.presentation.theme.TextPrimary
import com.example.autobrain.presentation.theme.TextSecondary
import com.example.autobrain.presentation.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    lastMaintenanceDate: Long?,
    modifier: Modifier = Modifier,
    onNextMaintenanceClick: (() -> Unit)? = null
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .scale(scale)
            .alpha(alpha),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = ElectricTeal.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1B2838),
                            Color(0xFF0F1923),
                            Color(0xFF1A2332),
                            Color(0xFF0D1620)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        ) {
            // Premium automotive decorative elements
            AutomotiveDecorativeElements()
            
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Premium Cost Display
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    ElectricTeal.copy(alpha = 0.12f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
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
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = ElectricTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TOTAL INVESTMENT",
                                fontSize = 11.sp,
                                color = ElectricTeal,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = formatCurrency(totalExpenses),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                letterSpacing = (-1.5).sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    SlateGray.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ElectricTeal.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalRecords service records",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        if (avgCostPerMonth > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(12.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    WarningAmber,
                                                    WarningAmber.copy(alpha = 0.3f)
                                                )
                                            ),
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${formatCurrency(avgCostPerMonth)}/month average",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Premium Speedometer-style Health Gauge
                    PremiumHealthGauge(
                        score = healthScore,
                        modifier = Modifier.size(120.dp)
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
                    LastMaintenanceInfo(lastMaintenanceDate = lastMaintenanceDate)
                }
            }
        }
    }
}

@Composable
private fun PremiumHealthGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing), label = "health_score"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "gauge_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )
    
    val color = getHealthScoreColor(score)
    val label = getHealthScoreLabel(score)
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = color.copy(alpha = 0.15f * glowPulse),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Background arc (darker track)
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            drawArc(
                color = SlateGray.copy(alpha = 0.25f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Speedometer tick marks
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            for (i in 0..10) {
                val angle = 135f + (270f * i / 10f)
                val tickColor = if (i <= (animatedScore / 10).toInt()) {
                    color.copy(alpha = 0.4f)
                } else {
                    SlateGray.copy(alpha = 0.2f)
                }
                drawArc(
                    color = tickColor,
                    startAngle = angle - 1f,
                    sweepAngle = 2f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        
        // Animated health score arc (premium gradient)
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val arcColors = when {
                score >= 80 -> listOf(
                    SuccessGreen.copy(alpha = 0.4f),
                    SuccessGreen,
                    SuccessGreen.copy(alpha = 0.9f),
                    Color(0xFF00FF88)
                )
                score >= 60 -> listOf(
                    WarningAmber.copy(alpha = 0.4f),
                    WarningAmber,
                    Color(0xFFFFD700)
                )
                else -> listOf(
                    ErrorRed.copy(alpha = 0.4f),
                    ErrorRed,
                    Color(0xFFFF4444)
                )
            }
            drawArc(
                brush = Brush.sweepGradient(
                    colors = arcColors
                ),
                startAngle = 135f,
                sweepAngle = (270f * (animatedScore / 100f)),
                useCenter = false,
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Center display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = color.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = animatedScore.toInt().toString(),
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = color,
                letterSpacing = (-1).sp
            )
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                color = color.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "HEALTH",
                fontSize = 8.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun NextMaintenanceAlert(
    nextMaintenance: String,
    healthScore: Int,
    modifier: Modifier = Modifier
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
    
    val infiniteTransition = rememberInfiniteTransition(label = "alert_pulse")
    val alertPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = iconColor.copy(alpha = 0.4f * alertPulse)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            iconColor.copy(alpha = 0.08f),
                            Color.Transparent,
                            iconColor.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                iconColor.copy(alpha = 0.25f),
                                iconColor.copy(alpha = 0.1f)
                            )
                        ),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(iconColor, CircleShape)
                            .alpha(alertPulse)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NEXT SERVICE",
                        fontSize = 10.sp,
                        color = iconColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = nextMaintenance,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = iconColor.copy(alpha = 0.6f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun LastMaintenanceInfo(
    lastMaintenanceDate: Long,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.FRANCE) }
    val dateStr = dateFormatter.format(Date(lastMaintenanceDate))
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SuccessGreen.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Last maintenance: $dateStr",
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun AutomotiveDecorativeElements(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "automotive_decorative")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )
    
    Box(modifier = modifier.fillMaxSize()) {
        // Premium teal glow (top-left)
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-60).dp, y = (-60).dp)
                .rotate(rotation * 0.5f)
                .alpha(0.06f * pulse)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ElectricTeal,
                            ElectricTeal.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
        
        // Speedometer accent glow (right side)
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = 280.dp, y = 30.dp)
                .rotate(-rotation * 0.3f)
                .alpha(0.05f * pulse)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00D4FF),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
        
        // Bottom accent glow
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = 100.dp, y = 200.dp)
                .rotate(rotation * 0.7f)
                .alpha(0.04f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SuccessGreen.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
        
        // Racing stripe effect (subtle diagonal gradient overlay)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.03f)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            ElectricTeal.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 300f)
                    )
                )
        )
    }
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
