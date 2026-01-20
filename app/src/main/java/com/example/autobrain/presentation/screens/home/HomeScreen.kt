package com.example.autobrain.presentation.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autobrain.core.utils.*
import com.example.autobrain.presentation.components.*
import com.example.autobrain.presentation.navigation.Screen
import com.example.autobrain.presentation.theme.*

data class DiagnosticItem(
    val id: String,
    val title: String,
    val status: String,
    val statusColor: Color,
    val icon: ImageVector,
    val value: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // Get dynamic UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedNavIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MidnightBlack,
        bottomBar = {
            AutoBrainBottomNav(
                selectedIndex = selectedNavIndex,
                onItemSelected = { index ->
                    selectedNavIndex = index
                    when (index) {
                        0 -> { /* Already on Home */ }
                        1 -> navController.navigate(Screen.AIDiagnostics.route)
                        2 -> navController.navigate(Screen.CarLogbook.route)
                        3 -> navController.navigate(Screen.AIAssistant.route)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AIDiagnostics.route) },
                containerColor = ElectricTeal,
                contentColor = MidnightBlack,
                shape = CircleShape,
                modifier = Modifier.size(adaptiveFABSize())
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Diagnostic",
                    modifier = Modifier.size(adaptiveIconSize(28.dp))
                )
            }
        }
    ) { paddingValues ->
        AdaptiveContentContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
            // Top Bar with greeting and profile
            TopGreetingSection(
                userName = uiState.userName,
                carModel = "${uiState.carMake} ${uiState.carModel}",
                carYear = uiState.carYear,
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
            
            Spacer(modifier = Modifier.height(AdaptiveSpacing.large()))
            
            // AI Score Card - Large (Dynamic from database)
            AIScoreCard(
                score = uiState.aiScore,
                carModel = "${uiState.carMake} ${uiState.carModel}",
                carYear = uiState.carYear,
                riskLevel = uiState.riskLevel,
                onClick = { navController.navigate(Screen.AIScore.route) },
                modifier = Modifier.padding(horizontal = AdaptiveSpacing.medium())
            )
            
            Spacer(modifier = Modifier.height(AdaptiveSpacing.large()))
            
            // Quick Actions Row
            Text(
                text = "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = AdaptiveSpacing.medium())
            )
            
            Spacer(modifier = Modifier.height(AdaptiveSpacing.small()))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AdaptiveSpacing.medium()),
                horizontalArrangement = Arrangement.spacedBy(AdaptiveSpacing.small())
            ) {
                QuickActionButton(
                    icon = Icons.Outlined.Mic,
                    label = "Audio\nDiagnostic",
                    modifier = Modifier.weight(1f),
                    onClick = { 
                        // Navigate to Smart Audio Diagnostic
                        navController.navigate(Screen.EngineSoundAnalysis.route)
                    }
                )
                QuickActionButton(
                    icon = Icons.Outlined.Videocam,
                    label = "Video\nDiagnostic",
                    modifier = Modifier.weight(1f),
                    onClick = { 
                        // Navigate to Video Diagnostic
                        navController.navigate(Screen.VideoAnalysis.route)
                    }
                )
                QuickActionButton(
                    icon = Icons.Outlined.Description,
                    label = "AI Complete\nDiagnostic",
                    modifier = Modifier.weight(1f),
                    onClick = { 
                        // Navigate to AI Enhanced Diagnostics
                        navController.navigate(Screen.AIDiagnostics.route)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(AdaptiveSpacing.large()))
            
            // Recent Diagnostics Section
            Text(
                text = "Recent Diagnostics",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = AdaptiveSpacing.medium())
            )
            
            Spacer(modifier = Modifier.height(AdaptiveSpacing.small()))
            
            // Recent Diagnostics (Dynamic from database)
            LazyRow(
                contentPadding = PaddingValues(horizontal = AdaptiveSpacing.medium()),
                horizontalArrangement = Arrangement.spacedBy(AdaptiveSpacing.small())
            ) {
                items(uiState.recentDiagnostics) { diagnostic ->
                    DiagnosticMiniCard(
                        item = diagnostic,
                        onClick = { 
                            // Navigate to diagnostic detail based on type
                            when {
                                diagnostic.title.contains("Audio", ignoreCase = true) -> {
                                    navController.navigate(Screen.EngineSoundAnalysis.route)
                                }
                                diagnostic.title.contains("Vidéo", ignoreCase = true) -> {
                                    navController.navigate(Screen.VideoAnalysis.route)
                                }
                                else -> {
                                    navController.navigate(Screen.AIDiagnostics.route)
                                }
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AdaptiveSpacing.large()))
            
            // Smart Logbook Card (Dynamic)
            SmartLogbookCard(
                daysUntilService = uiState.daysUntilService,
                kmUntilService = uiState.kmUntilService,
                urgencyLevel = when {
                    uiState.daysUntilService < 7 -> "urgent"
                    uiState.daysUntilService < 15 -> "warning"
                    else -> "normal"
                },
                onClick = { navController.navigate(Screen.CarLogbook.route) },
                modifier = Modifier.padding(horizontal = AdaptiveSpacing.medium())
            )
            
            Spacer(modifier = Modifier.height(100.dp)) // Space for FAB
            }
        }
    }
}

@Composable
private fun TopGreetingSection(
    userName: String,
    carModel: String,
    carYear: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AdaptiveSpacing.medium(), vertical = AdaptiveSpacing.medium()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello, ${userName.split(" ").firstOrNull() ?: userName}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(AdaptiveSpacing.extraSmall()))
            Text(
                text = "$carModel • $carYear",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
        
        // Profile Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(DeepNavy)
                .border(2.dp, ElectricTeal, CircleShape)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = ElectricTeal,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun AIScoreCard(
    score: Int,
    carModel: String,
    carYear: String,
    riskLevel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D1B2A)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // AI Score Circle
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow effect
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .blur(30.dp)
                        .alpha(glowAlpha)
                        .background(ElectricTeal.copy(alpha = 0.3f), CircleShape)
                )
                
                // Score ring
                Canvas(modifier = Modifier.size(200.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    // Background circle
                    drawCircle(
                        color = Color(0xFF1C2128),
                        radius = radius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                    )
                    
                    // Progress arc
                    val sweepAngle = (score / 100f) * 360f
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF5EEAD4),
                                Color(0xFF2DD4BF),
                                Color(0xFF14B8A6),
                                Color(0xFF0D9488),
                                Color(0xFF5EEAD4)
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        ),
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                    )
                }
                
                // Score text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = score.toString(),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "SCORE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = ElectricTeal,
                        letterSpacing = 3.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Car info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DeepNavy
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$carModel • $carYear",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (riskLevel == "Low") SuccessGreen else WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Risk level: $riskLevel",
                                fontSize = 14.sp,
                                color = if (riskLevel == "Low") SuccessGreen else WarningAmber
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        ),
        border = BorderStroke(1.dp, Color(0xFF30363D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricTeal,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun DiagnosticMiniCard(
    item: DiagnosticItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = ElectricTeal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.status,
                fontSize = 12.sp,
                color = item.statusColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SmartLogbookCard(
    daysUntilService: Int,
    kmUntilService: Int,
    urgencyLevel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val urgencyColor = when (urgencyLevel) {
        "urgent" -> ErrorRed
        "warning" -> WarningAmber
        else -> SuccessGreen
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(urgencyColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Build,
                    contentDescription = null,
                    tint = urgencyColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Logbook",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Next service in $daysUntilService days / $kmUntilService km",
                    fontSize = 13.sp,
                    color = urgencyColor
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
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
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home - Dashboard with AI Score
            BottomNavItem(
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Filled.Home,
                label = "Home",
                selected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            
            // Diagnostics - Audio/Video AI Analysis
            BottomNavItem(
                icon = Icons.Outlined.Mic,
                selectedIcon = Icons.Filled.Mic,
                label = "Diagnostics",
                selected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            
            // Carnet - Smart Maintenance Logbook
            BottomNavItem(
                icon = Icons.Outlined.EventNote,
                selectedIcon = Icons.Filled.EventNote,
                label = "Logbook",
                selected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
            
            // Profile - User Settings
            BottomNavItem(
                icon = Icons.Outlined.Person,
                selectedIcon = Icons.Filled.Person,
                label = "Profile",
                selected = selectedIndex == 3,
                onClick = { onItemSelected(3) }
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
