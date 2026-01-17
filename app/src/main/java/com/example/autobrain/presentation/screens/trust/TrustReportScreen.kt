package com.example.autobrain.presentation.screens.trust

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.autobrain.presentation.components.*
import com.example.autobrain.presentation.theme.*
import com.example.autobrain.core.utils.*

/**
 * Trust Report / Anti-Scam Screen
 * Shows vehicle trust level with detailed risk assessment
 *
 * Design matching reference mockups:
 * - Trust level badge (HIGH/MEDIUM/LOW)
 * - Scan score percentage
 * - Red flags list
 * - Buyer advice
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustReportScreen(
    navController: NavController,
    carId: String = "current"
) {
    // Simulated trust report data
    val trustReport = remember {
        TrustReportData(
            trustLevel = TrustLevel.HIGH,
            scanScore = 92,
            redFlags = emptyList(),
            warnings = listOf(
                TrustIssue(
                    title = "Service Records Gap",
                    description = "Service history for 2020-2021 is missing",
                    severity = "medium"
                )
            ),
            positives = listOf(
                "No major accidents detected",
                "Mileage consistent with service records",
                "Single owner vehicle",
                "All scheduled maintenance completed"
            ),
            buyerAdvice = "This vehicle appears to be in good condition. Consider verifying the missing service records for 2020-2021 before finalizing the purchase."
        )
    }

    AdaptiveScaffold(
        title = "Trust Report",
        showBackButton = true,
        onBackClick = { navController.popBackStack() },
        containerColor = MidnightBlack,
        scrollable = true,
        actions = {
            IconButton(onClick = { /* Share */ }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = TextPrimary,
                    modifier = Modifier.size(adaptiveIconSize())
                )
            }
        }
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AdaptiveSpacing.large()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Trust Level Hero
            TrustLevelHero(
                trustLevel = trustReport.trustLevel,
                scanScore = trustReport.scanScore
            )

            Spacer(modifier = Modifier.height(AdaptiveSpacing.large()))

            // Red Flags Section (if any)
            if (trustReport.redFlags.isNotEmpty()) {
                RedFlagsSection(redFlags = trustReport.redFlags)
                Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))
            }

            // Warnings Section (if any)
            if (trustReport.warnings.isNotEmpty()) {
                WarningsSection(warnings = trustReport.warnings)
                Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))
            }

            // Positive Findings
            if (trustReport.positives.isNotEmpty()) {
                PositivesSection(positives = trustReport.positives)
                Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))
            }

            // Buyer Advice
            BuyerAdviceSection(advice = trustReport.buyerAdvice)

            Spacer(modifier = Modifier.height(AdaptiveSpacing.extraLarge()))

            // Actions
            AutoBrainButton(
                text = "View Full Report",
                onClick = { /* Navigate to full report */ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (trustReport.trustLevel == TrustLevel.LOW) {
                AutoBrainOutlinedButton(
                    text = "Find Independent Inspectors",
                    onClick = { /* Navigate */ },
                    icon = Icons.Default.Search
                )
            }
        }
    }
}

@Composable
private fun TrustLevelHero(
    trustLevel: TrustLevel,
    scanScore: Int
) {
    val (bgColor, textColor, label) = when (trustLevel) {
        TrustLevel.HIGH -> Triple(SuccessGreen, SuccessGreen, "HIGH")
        TrustLevel.MEDIUM -> Triple(WarningAmber, WarningAmber, "MEDIUM")
        TrustLevel.LOW -> Triple(ErrorRed, ErrorRed, "LOW")
    }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(adaptiveCornerRadius(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = bgColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(adaptiveCardPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Trust Level Badge
            Box(
                modifier = Modifier
                    .size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    bgColor.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = bgColor.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .border(
                            width = 3.dp,
                            color = bgColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Trust Level",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )

            Text(
                text = "Based on comprehensive AI analysis",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Divider
            HorizontalDivider(color = BorderDark)

            Spacer(modifier = Modifier.height(24.dp))

            // Scan Score
            Text(
                text = "Scan Score",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$scanScore%",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { scanScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = bgColor,
                trackColor = SlateGray
            )
        }
    }
}

@Composable
private fun RedFlagsSection(redFlags: List<TrustIssue>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(adaptiveCornerRadius()),
        colors = CardDefaults.cardColors(
            containerColor = ErrorRed.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(adaptiveCardPadding())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Red Flags",
                    style = MaterialTheme.typography.titleMedium,
                    color = ErrorRed,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            redFlags.forEach { issue ->
                IssueItem(issue = issue, iconColor = ErrorRed)
                if (issue != redFlags.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun WarningsSection(warnings: List<TrustIssue>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(adaptiveCornerRadius()),
        colors = CardDefaults.cardColors(
            containerColor = WarningAmber.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(adaptiveCardPadding())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Warnings",
                    style = MaterialTheme.typography.titleMedium,
                    color = WarningAmber,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            warnings.forEach { issue ->
                IssueItem(issue = issue, iconColor = WarningAmber)
                if (issue != warnings.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun IssueItem(
    issue: TrustIssue,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
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
        }
    }
}

@Composable
private fun PositivesSection(positives: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(adaptiveCornerRadius()),
        colors = CardDefaults.cardColors(containerColor = DeepNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(adaptiveCardPadding())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Positive Findings",
                    style = MaterialTheme.typography.titleMedium,
                    color = SuccessGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            positives.forEach { positive ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = positive,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun BuyerAdviceSection(advice: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(adaptiveCornerRadius()),
        colors = CardDefaults.cardColors(containerColor = DeepNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(adaptiveCardPadding())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = ElectricTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buyer Advice",
                    style = MaterialTheme.typography.titleMedium,
                    color = ElectricTeal,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = advice,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 24.sp
            )
        }
    }
}

data class TrustReportData(
    val trustLevel: TrustLevel,
    val scanScore: Int,
    val redFlags: List<TrustIssue>,
    val warnings: List<TrustIssue>,
    val positives: List<String>,
    val buyerAdvice: String
)

data class TrustIssue(
    val title: String,
    val description: String,
    val severity: String
)
