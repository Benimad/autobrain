package com.example.autobrain.presentation.screens.carlog

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autobrain.presentation.theme.*

/**
 * 🎨 PROFESSIONAL ACTION BUTTONS
 * Beautiful, animated action buttons with "wow" factor
 */
@Composable
fun ProfessionalActionButtonsSection(
    onAddMaintenance: () -> Unit,
    onAIAdvice: () -> Unit,
    onViewReminders: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Quick actions",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Add Maintenance Button
            ProfessionalActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Add,
                title = "Add",
                subtitle = "Maintenance",
                gradient = Brush.linearGradient(
                    colors = listOf(
                        ElectricTeal,
                        TealLight
                    )
                ),
                onClick = onAddMaintenance
            )
            
            // AI Advice Button
            ProfessionalActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.AutoAwesome,
                title = "AI Advice",
                subtitle = "Gemini Smart",
                gradient = Brush.linearGradient(
                    colors = listOf(
                        WarningAmber,
                        WarningAmberLight
                    )
                ),
                onClick = onAIAdvice
            )
            
            // Reminders Button
            ProfessionalActionButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Notifications,
                title = "Reminders",
                subtitle = "Due Dates",
                gradient = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF9333EA), // Purple
                        Color(0xFFC084FC)
                    )
                ),
                onClick = onViewReminders
            )
        }
    }
}

@Composable
private fun ProfessionalActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "button_scale"
    )
    
    // Shimmer animation for icon
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "shimmer_alpha"
    )
    
    Card(
        modifier = modifier
            .height(120.dp)
            .scale(scale)
            .clickable(
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon with glow effect
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(shimmerAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow background
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .alpha(0.3f)
                            .background(
                                Color.White.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                    )
                    
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/**
 * Compact version for horizontal scrolling
 */
@Composable
fun CompactActionButton(
    icon: ImageVector,
    title: String,
    backgroundColor: Color,
    iconTint: Color = Color.White,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        iconTint.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = iconTint
            )
        }
    }
}
