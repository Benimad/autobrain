package com.example.autobrain.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.autobrain.R
import com.example.autobrain.presentation.theme.*
import com.example.autobrain.core.utils.*

/**
 * AutoBrain Premium UI Components
 * AI-Powered Car Diagnostic & Valuation App
 *
 * Design Language:
 * - Dark mode first (Midnight Black)
 * - Electric Teal accents
 * - Trust-focused, premium feel
 * - Clean cards with subtle glow effects
 */

// =============================================================================
// AI SCORE GAUGE - The Hero Component
// =============================================================================

@Composable
fun AIScoreGauge(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    strokeWidth: Dp = 16.dp,
    animationDuration: Int = 1500,
    showLabel: Boolean = true
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "score"
    )

    val sweepAngle by animateFloatAsState(
        targetValue = (score / 100f) * 270f,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "sweep"
    )

    val scoreColor = getScoreColor(score)
    val glowColor = scoreColor.copy(alpha = 0.3f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Glow effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokeWidth.toPx() * 2) / 2
            val center = Offset(canvasSize / 2, canvasSize / 2)

            // Background track
            drawArc(
                color = SlateGray,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(
                    (canvasSize - radius * 2) / 2,
                    (canvasSize - radius * 2) / 2
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Score arc with gradient
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        scoreColor.copy(alpha = 0.6f),
                        scoreColor,
                        scoreColor
                    )
                ),
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    (canvasSize - radius * 2) / 2,
                    (canvasSize - radius * 2) / 2
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$animatedScore",
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            if (showLabel) {
                Text(
                    text = "SCORE",
                    style = MaterialTheme.typography.labelLarge,
                    color = scoreColor,
                    letterSpacing = 4.sp
                )
            }
        }
    }
}

@Composable
fun AIScoreGaugeCompact(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val scoreColor = getScoreColor(score)

    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        scoreColor.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = 8.dp.toPx()
            val radius = (size.toPx() - strokeWidth * 2 - 16.dp.toPx()) / 2

            drawArc(
                color = SlateGray,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(
                    (this.size.width - radius * 2) / 2,
                    (this.size.height - radius * 2) / 2
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = scoreColor,
                startAngle = 135f,
                sweepAngle = (score / 100f) * 270f,
                useCenter = false,
                topLeft = Offset(
                    (this.size.width - radius * 2) / 2,
                    (this.size.height - radius * 2) / 2
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// =============================================================================
// TRUST LEVEL BADGE
// =============================================================================

@Composable
fun TrustLevelBadge(
    level: TrustLevel,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (level) {
        TrustLevel.HIGH -> SuccessGreen to "HIGH"
        TrustLevel.MEDIUM -> WarningAmber to "MEDIUM"
        TrustLevel.LOW -> ErrorRed to "LOW"
    }

    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 2.dp,
                color = color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

enum class TrustLevel { HIGH, MEDIUM, LOW }

// =============================================================================
// PREMIUM CARDS
// =============================================================================

@Composable
fun AutoBrainCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@Composable
fun FeatureCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = ElectricTeal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = iconTint.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color = ElectricTeal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = iconTint.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = iconTint.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// =============================================================================
// BUTTONS
// =============================================================================

@Composable
fun AutoBrainButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ElectricTeal,
            contentColor = TextOnAccent,
            disabledContainerColor = SlateGray,
            disabledContentColor = TextMuted
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = TextOnAccent,
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AutoBrainOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = ElectricTeal
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(
                colors = listOf(ElectricTeal, TealLight)
            )
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// =============================================================================
// RECORDING BUTTON - For Sound/Video Analysis
// =============================================================================

@Composable
fun RecordingButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isRecording) 0.6f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .size(size * scale)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isRecording) ErrorRed.copy(alpha = glowAlpha) else ElectricTeal.copy(
                            alpha = glowAlpha
                        ),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.8f)
                .background(
                    color = if (isRecording) ErrorRed else ElectricTeal,
                    shape = CircleShape
                )
                .border(
                    width = 4.dp,
                    color = if (isRecording) ErrorRedLight else TealLight,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isRecording) "Stop" else "Record",
                tint = Color.White,
                modifier = Modifier.size(size * 0.35f)
            )
        }
    }
}

// =============================================================================
// AUDIO WAVEFORM
// =============================================================================

@Composable
fun AudioWaveform(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = ElectricTeal
) {
    Canvas(modifier = modifier.height(60.dp)) {
        val barWidth = size.width / amplitudes.size.coerceAtLeast(1)
        val maxHeight = size.height

        amplitudes.forEachIndexed { index, amplitude ->
            val barHeight = (amplitude * maxHeight).coerceIn(4f, maxHeight)
            val x = index * barWidth + barWidth / 2

            drawLine(
                color = color,
                start = Offset(x, (maxHeight - barHeight) / 2),
                end = Offset(x, (maxHeight + barHeight) / 2),
                strokeWidth = barWidth * 0.6f,
                cap = StrokeCap.Round
            )
        }
    }
}

// =============================================================================
// CONFIDENCE INDICATOR
// =============================================================================

@Composable
fun ConfidenceIndicator(
    confidence: Float,
    label: String = "Confidence",
    modifier: Modifier = Modifier
) {
    val color = getConfidenceColor(confidence)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "${(confidence * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { confidence },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = SlateGray
        )
    }
}

// =============================================================================
// SEVERITY BADGE
// =============================================================================

@Composable
fun SeverityBadge(
    severity: String,
    modifier: Modifier = Modifier
) {
    val color = getSeverityColor(severity)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = severity.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

// =============================================================================
// PRICE RANGE DISPLAY
// =============================================================================

@Composable
fun PriceRangeDisplay(
    minPrice: Int,
    maxPrice: Int,
    currency: String = "$",
    confidence: Float = 0.8f,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${formatPrice(minPrice)} - ${formatPrice(maxPrice)} $currency",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        val confidenceLabel = when {
            confidence >= 0.8f -> "High Confidence"
            confidence >= 0.5f -> "Medium Confidence"
            else -> "Low Confidence"
        }

        Text(
            text = confidenceLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = getConfidenceColor(confidence)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { confidence },
            modifier = Modifier
                .width(200.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = getConfidenceColor(confidence),
            trackColor = SlateGray
        )
    }
}

private fun formatPrice(price: Int): String {
    return price.toString().reversed().chunked(3).joinToString(",").reversed()
}

// =============================================================================
// BOTTOM NAVIGATION BAR - AutoBrain Style
// =============================================================================

@Composable
fun AutoBrainBottomNav(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MidnightBlack,
        contentColor = TextPrimary,
        tonalElevation = 0.dp
    ) {
        autoBrainNavItems.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selectedIndex == index) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ElectricTeal,
                    selectedTextColor = ElectricTeal,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = ElectricTeal.copy(alpha = 0.1f)
                )
            )
        }
    }
}

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val route: String
)

val autoBrainNavItems = listOf(
    NavItem(
        label = "Home",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        route = "home"
    ),
    NavItem(
        label = "Diagnostics",
        icon = Icons.Outlined.Search,
        selectedIcon = Icons.Filled.Search,
        route = "ai_diagnostics"
    ),
    NavItem(
        label = "Carnet",
        icon = Icons.Outlined.DirectionsCar,
        selectedIcon = Icons.Filled.DirectionsCar,
        route = "car_logbook"
    ),
    NavItem(
        label = "AI Assistant",
        icon = Icons.Outlined.Psychology,
        selectedIcon = Icons.Filled.Psychology,
        route = "ai_assistant"
    )
)

// =============================================================================
// LOADING STATES
// =============================================================================

@Composable
fun AutoBrainLoadingIndicator(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = ElectricTeal,
            strokeWidth = 4.dp
        )

        if (message != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

// =============================================================================
// ERROR / EMPTY STATES
// =============================================================================

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = ElectricTeal.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricTeal,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            AutoBrainButton(
                text = actionText,
                onClick = onAction,
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

// =============================================================================
// ISSUE / WARNING CARD
// =============================================================================

@Composable
fun IssueCard(
    title: String,
    description: String,
    severity: String,
    icon: ImageVector = Icons.Default.Warning,
    modifier: Modifier = Modifier
) {
    val color = getSeverityColor(severity)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            SeverityBadge(severity = severity)
        }
    }
}

// =============================================================================
// TOP APP BAR
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoBrainTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = ElectricTeal
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MidnightBlack,
            titleContentColor = TextPrimary
        )
    )
}

// =============================================================================
// GEMINI AI ICONS - Using Official Gemini Logo from Web
// =============================================================================

private const val GEMINI_LOGO_URL = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Google_Gemini_logo.svg/512px-Google_Gemini_logo.svg.png"
private const val GEMINI_ICON_URL = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1d/Google_Gemini_icon_2025.svg/512px-Google_Gemini_icon_2025.svg.png"
private const val GEMINI_SPARKLE_URL = "https://www.gstatic.com/lamda/images/gemini_sparkle_v002_d4735304ff6292a690345.svg"

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GeminiIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.Unspecified,
    useRemote: Boolean = true
) {
    GlideImage(
        model = GEMINI_ICON_URL,
        contentDescription = "Gemini AI",
        modifier = modifier.size(size)
    )
}

@Composable
fun GeminiIconWithGlow(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    animated: Boolean = true,
    useRemote: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gemini_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (animated) {
            Box(
                modifier = Modifier
                    .size(size * 1.5f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ElectricTeal.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        GeminiIcon(
            size = size,
            tint = Color.Unspecified,
            useRemote = useRemote
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GeminiSparkleIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.Unspecified,
    useRemote: Boolean = true
) {
    GlideImage(
        model = GEMINI_SPARKLE_URL,
        contentDescription = "Gemini AI",
        modifier = modifier.size(size)
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GeminiBadgeIcon(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    tint: Color = Color.Unspecified
) {
    GlideImage(
        model = GEMINI_LOGO_URL,
        contentDescription = "Powered by Gemini AI",
        modifier = modifier.size(size)
    )
}

@Composable
fun GeminiBadge(
    text: String = "Powered by Gemini AI",
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF1A1A2E),
    textColor: Color = ElectricTeal,
    useRemote: Boolean = true
) {
    Row(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GeminiIcon(size = 16.dp, useRemote = useRemote)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun GeminiPoweredFooter(
    modifier: Modifier = Modifier,
    showVersion: Boolean = true,
    useRemote: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            GeminiIcon(size = 20.dp, useRemote = useRemote)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Powered by Google Gemini AI",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
        
        if (showVersion) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Gemini 2.5 Pro • Global Market 2026",
                fontSize = 10.sp,
                color = TextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun GeminiAnalyzingIndicator(
    message: String = "Analyzing with Gemini AI...",
    modifier: Modifier = Modifier,
    useRemote: Boolean = true
) {
    Row(
        modifier = modifier
            .background(
                color = Color(0xFF1A1A2E).copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GeminiIconWithGlow(size = 20.dp, useRemote = useRemote)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "This may take a moment...",
                fontSize = 11.sp,
                color = TextSecondary.copy(alpha = 0.8f)
            )
        }
    }
}

// Note: Adaptive components (AdaptiveScaffold, AdaptiveContentContainer, AdaptiveCard, AdaptiveGrid)
// are defined in AdaptiveScaffold.kt for better organization
