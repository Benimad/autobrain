package com.example.autobrain.presentation.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.autobrain.R
import com.example.autobrain.core.utils.*
import com.example.autobrain.presentation.components.*
import com.example.autobrain.presentation.navigation.Screen
import com.example.autobrain.presentation.theme.*
import com.example.autobrain.presentation.components.AnimatedBackground
import com.example.autobrain.presentation.components.AnimatedEntrance
import com.example.autobrain.presentation.components.GlassmorphicCard

data class DiagnosticItem(
    val id: String,
    val title: String,
    val status: String,
    val statusColor: Color,
    val icon: ImageVector,
    val value: String? = null
)

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AutoBrainTheme {
        HomeScreenContent(
            navController = rememberNavController()
        )
    }
}

@Composable
fun HomeScreenContent(
    navController: NavController
) {
    val sampleDiagnostics = listOf(
        DiagnosticItem(
            id = "1",
            title = "Engine Sound",
            status = "Good",
            statusColor = SuccessGreen,
            icon = Icons.Outlined.Mic
        ),
        DiagnosticItem(
            id = "2",
            title = "Video Analysis",
            status = "Warning",
            statusColor = WarningAmber,
            icon = Icons.Outlined.Videocam
        ),
        DiagnosticItem(
            id = "3",
            title = "AI Diagnostic",
            status = "Excellent",
            statusColor = SuccessGreen,
            icon = Icons.Outlined.Description
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            AutoBrainBottomNav(
                selectedIndex = 0,
                onItemSelected = {}
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = ElectricTeal,
                contentColor = MidnightBlack,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Diagnostic",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBlack)
        ) {
            AnimatedBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
            // Top Bar with greeting and profile
            TopGreetingSection(
                userName = "John Doe",
                carModel = "BMW X5",
                carYear = "2022",
                onProfileClick = {}
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Car Image - Large and prominent
            AIScoreCard(
                score = 94,
                carModel = "BMW X5",
                carYear = "2022",
                riskLevel = "Low",
                carImageUrl = null,
                onClick = {},
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Stats Cards Section
            CarStatsSection(
                aiScore = 94,
                riskLevel = "Low",
                totalKm = 45000,
                daysUntilService = 15,
                kmUntilService = 2500
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Quick Actions Row
            Text(
                text = "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Outlined.Mic,
                    label = "Audio\nDiagnostic",
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
                QuickActionButton(
                    icon = Icons.Outlined.Videocam,
                    label = "Video\nDiagnostic",
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
                QuickActionButton(
                    icon = Icons.Outlined.Description,
                    label = "AI Complete\nDiagnostic",
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Recent Diagnostics Section
            Text(
                text = "Recent Diagnostics",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Recent Diagnostics
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sampleDiagnostics) { diagnostic ->
                    DiagnosticMiniCard(
                        item = diagnostic,
                        onClick = {}
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Smart Logbook Card
            SmartLogbookCard(
                daysUntilService = 15,
                kmUntilService = 2500,
                urgencyLevel = "normal",
                onClick = {},
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(100.dp)) // Space for FAB
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            ModernBottomNavBar(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "home" -> { /* Already on Home */ }
                        "ai_diagnostics" -> navController.navigate(Screen.AIDiagnostics.route)
                        "car_logbook" -> navController.navigate(Screen.CarLogbook.route)
                        "ai_assistant" -> navController.navigate(Screen.AIAssistant.route)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBlack)
        ) {
            AnimatedBackground()
            
            AdaptiveContentContainer {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(
                            state = scrollState,
                            enabled = true
                        )
                ) {
            AnimatedEntrance(
                visible = isVisible,
                delay = 0
            ) {
                TopGreetingSection(
                    userName = uiState.userName,
                    carModel = "${uiState.carMake} ${uiState.carModel}",
                    carYear = uiState.carYear,
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AnimatedEntrance(
                visible = isVisible,
                delay = 100
            ) {
                AIScoreCard(
                    score = uiState.aiScore,
                    carModel = "${uiState.carMake} ${uiState.carModel}",
                    carYear = uiState.carYear,
                    riskLevel = uiState.riskLevel,
                    carImageUrl = uiState.carImageUrl,
                    onClick = { navController.navigate(Screen.AIScore.route) },
                    modifier = Modifier.padding(horizontal = AdaptiveSpacing.medium())
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            AnimatedEntrance(
                visible = isVisible,
                delay = 200
            ) {
                CarStatsSection(
                    aiScore = uiState.aiScore,
                    riskLevel = uiState.riskLevel,
                    totalKm = uiState.carKilometers,
                    daysUntilService = uiState.daysUntilService,
                    kmUntilService = uiState.kmUntilService
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AnimatedEntrance(
                visible = isVisible,
                delay = 300
            ) {
                Column {
                    Text(
                        text = "Quick Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = AdaptiveSpacing.medium())
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AdaptiveSpacing.medium()),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Outlined.Mic,
                            label = "Audio\nDiagnostic",
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                navController.navigate(Screen.EngineSoundAnalysis.route)
                            }
                        )
                        QuickActionButton(
                            icon = Icons.Outlined.Videocam,
                            label = "Video\nDiagnostic",
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                navController.navigate(Screen.VideoAnalysis.route)
                            }
                        )
                        QuickActionButton(
                            icon = Icons.Outlined.Description,
                            label = "AI Complete\nDiagnostic",
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                navController.navigate(Screen.AIDiagnostics.route)
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AnimatedEntrance(
                visible = isVisible,
                delay = 400
            ) {
                Text(
                    text = "Recent Diagnostics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = AdaptiveSpacing.medium())
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            AnimatedEntrance(
                visible = isVisible,
                delay = 450
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = AdaptiveSpacing.medium()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.recentDiagnostics) { diagnostic ->
                        DiagnosticMiniCard(
                            item = diagnostic,
                            onClick = { 
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
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AnimatedEntrance(
                visible = isVisible,
                delay = 500
            ) {
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
            }
            
            Spacer(modifier = Modifier.height(100.dp)) // Space for FAB
                }
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
    val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = when (hour) {
        in 0..11 -> "Good Morning,"
        in 12..16 -> "Good Afternoon,"
        else -> "Good Evening,"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AdaptiveSpacing.medium(), vertical = AdaptiveSpacing.medium()),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logowitoutbg),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .alpha(0.9f),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = greeting,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Text(
                text = userName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$carModel $carYear",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
        
        // Profile Avatar with notification badge
        Box {
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
            // Notification badge
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(ErrorRed)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun AIScoreCard(
    score: Int,
    carModel: String,
    carYear: String,
    riskLevel: String,
    carImageUrl: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var showScoreBadge by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "car_card_scale"
    )
    
    val scoreBadgeAlpha by animateFloatAsState(
        targetValue = if (showScoreBadge) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "score_badge_alpha"
    )
    
    val scoreBadgeScale by animateFloatAsState(
        targetValue = if (showScoreBadge) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "score_badge_scale"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "car_card_glow")
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow_alpha"
    )
    
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmer_offset"
    )
    
    val rotatingAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotating_angle"
    )
    
    val carFloatY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "car_float_y"
    )
    
    val carScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "car_scale"
    )
    
    val carRotation by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "car_rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(550.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        showScoreBadge = !showScoreBadge
                        onClick()
                    }
                )
            },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepNavy,
                            Color(0xFF1a2332),
                            DeepNavy
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = 150.dp, y = (-50).dp)
                    .rotate(rotatingAngle)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ElectricTeal.copy(alpha = glowAlpha * 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .offset(x = (-80).dp, y = 200.dp)
                    .rotate(-rotatingAngle)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00D4FF).copy(alpha = glowAlpha * 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .offset(y = 150.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                ElectricTeal.copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            startX = shimmerOffset - 500f,
                            endX = shimmerOffset + 500f
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        ElectricTeal.copy(alpha = 0.3f),
                                        Color(0xFF00D4FF).copy(alpha = 0.3f)
                                    )
                                ),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                ElectricTeal.copy(alpha = glowAlpha * 0.5f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = ElectricTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "AI SCORE $score%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = ElectricTeal.copy(alpha = 0.7f),
                        letterSpacing = 2.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(30.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .offset(y = (150 + carFloatY).dp)
                            .scale(carScale * 0.95f)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MidnightBlack.copy(alpha = 0.6f),
                                        MidnightBlack.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .blur(25.dp)
                    )
                    
                    if (!carImageUrl.isNullOrBlank()) {
                        GlideImage(
                            model = carImageUrl,
                            contentDescription = carModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .offset(y = carFloatY.dp)
                                .scale(carScale * 1.15f)
                                .rotate(carRotation),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = carModel,
                            modifier = Modifier
                                .size(220.dp)
                                .offset(y = carFloatY.dp)
                                .scale(carScale)
                                .rotate(carRotation)
                                .alpha(0.7f),
                            tint = ElectricTeal.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(y = 6.dp)
                                .alpha(glowAlpha * 0.6f)
                        ) {
                            Text(
                                text = carModel.uppercase(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = ElectricTeal.copy(alpha = 0.4f),
                                letterSpacing = 2.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.blur(8.dp)
                            )
                        }
                        
                        Text(
                            text = carModel.uppercase(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            style = TextStyle(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White,
                                        ElectricTeal.copy(alpha = 0.9f),
                                        Color(0xFF00D4FF)
                                    )
                                ),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 1
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(3.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            ElectricTeal,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = carYear,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary.copy(alpha = 0.8f),
                            letterSpacing = 4.sp
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(3.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            ElectricTeal,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (riskLevel) {
                                        "Low" -> SuccessGreen
                                        "Medium" -> WarningAmber
                                        else -> ErrorRed
                                    }
                                )
                        )
                        
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        Text(
                            text = riskLevel.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when (riskLevel) {
                                "Low" -> SuccessGreen
                                "Medium" -> WarningAmber
                                else -> ErrorRed
                            },
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CarStatsSection(
    aiScore: Int,
    riskLevel: String,
    totalKm: Int,
    daysUntilService: Int,
    kmUntilService: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AdaptiveSpacing.medium()),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Health Status Card (Left)
        HealthScoreCard(
            modifier = Modifier
                .weight(1.3f)
                .height(336.dp),
            aiScore = aiScore,
            riskLevel = riskLevel,
            onClick = {}
        )
        
        // Right side stats
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Distance Card
            StatCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                title = "Distance",
                value = "${totalKm / 1000}",
                unit = "km",
                icon = Icons.Default.Map,
                accentColor = ElectricTeal,
                onClick = {}
            )
            
            // Service Status Card
            StatCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                title = "Service",
                value = daysUntilService.toString(),
                unit = "days",
                icon = Icons.Default.Build,
                accentColor = when {
                    daysUntilService < 7 -> ErrorRed
                    daysUntilService < 15 -> WarningAmber
                    else -> SuccessGreen
                },
                onClick = {}
            )
        }
    }
}

@Composable
private fun HealthScoreCard(
    modifier: Modifier = Modifier,
    aiScore: Int,
    riskLevel: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "health_card_scale"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "health_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "health_pulse_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "health_glow_alpha"
    )
    
    val healthColor = when (riskLevel) {
        "Low" -> SuccessGreen
        "Medium" -> WarningAmber
        else -> ErrorRed
    }

    Card(
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavy),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    ElectricTeal.copy(alpha = glowAlpha),
                    healthColor.copy(alpha = glowAlpha * 0.5f)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepNavy,
                            ElectricTeal.copy(alpha = 0.12f),
                            healthColor.copy(alpha = 0.08f)
                        )
                    )
                )
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = ElectricTeal.copy(alpha = 0.06f),
                modifier = Modifier
                    .size(180.dp)
                    .offset(x = 60.dp, y = 140.dp)
                    .rotate(-20f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Health",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                ElectricTeal.copy(alpha = 0.2f),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = ElectricTeal,
                            modifier = Modifier
                                .size(28.dp)
                                .scale(pulseScale)
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = aiScore.toString(),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = (-2).sp
                        )
                        Text(
                            text = "%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(healthColor)
                        )
                        Text(
                            text = "$riskLevel Risk",
                            fontSize = 15.sp,
                            color = healthColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
                
                Text(
                    text = "AI Health Score",
                    fontSize = 13.sp,
                    color = ElectricTeal,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "stat_card_scale"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "stat_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "stat_pulse_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "stat_glow_alpha"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavy),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    accentColor.copy(alpha = glowAlpha),
                    accentColor.copy(alpha = glowAlpha * 0.5f)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepNavy,
                            accentColor.copy(alpha = 0.12f)
                        )
                    )
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.06f),
                modifier = Modifier
                    .size(110.dp)
                    .offset(x = 50.dp, y = 50.dp)
                    .rotate(-20f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                accentColor.copy(alpha = 0.2f),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier
                                .size(28.dp)
                                .scale(pulseScale)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = value,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = (-1.5).sp
                    )
                    Text(
                        text = " $unit",
                        fontSize = 18.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                    )
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
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "button_scale"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow_alpha"
    )

    Card(
        modifier = modifier
            .height(130.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavy),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    ElectricTeal.copy(alpha = glowAlpha),
                    ElectricTeal.copy(alpha = glowAlpha * 0.5f)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepNavy,
                            ElectricTeal.copy(alpha = 0.15f)
                        )
                    )
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricTeal.copy(alpha = 0.05f),
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 40.dp, y = 40.dp)
                    .rotate(-15f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            ElectricTeal.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ElectricTeal,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(pulseScale)
                    )
                }

                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    letterSpacing = (-0.3).sp
                )
            }
        }
    }
}

@Composable
private fun DiagnosticMiniCard(
    item: DiagnosticItem,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.width(150.dp),
        onClick = onClick,
        backgroundColor = Color(0xFF1F2937).copy(alpha = 0.3f)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.status,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
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
    
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(urgencyColor.copy(alpha = 0.15f)),
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
                    text = "Smart Logbook",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Service: $daysUntilService days / $kmUntilService km",
                    fontSize = 13.sp,
                    color = urgencyColor
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f)
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
