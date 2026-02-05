package com.example.autobrain.presentation.screens.diagnostics

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.autobrain.R
import com.example.autobrain.core.utils.*
import com.example.autobrain.presentation.components.*
import com.example.autobrain.presentation.navigation.Screen
import com.example.autobrain.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIDiagnosticsScreen(
    navController: NavController
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI Diagnostics",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightBlack.copy(alpha = 0.95f),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            ModernBottomNavBar(
                currentRoute = "ai_diagnostics",
                onNavigate = { route ->
                    when (route) {
                        "home" -> navController.navigate(Screen.Home.route)
                        "ai_diagnostics" -> { /* Already on AI Diagnostics */ }
                        "car_logbook" -> navController.navigate(Screen.CarLogbook.route)
                        "ai_assistant" -> navController.navigate(Screen.AIAssistant.route)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBlack)
        ) {
            AnimatedBackground()
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(AdaptiveSpacing.medium()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AnimatedEntrance(
                        visible = isVisible,
                        delay = 0
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logowitoutbg),
                                contentDescription = "AutoBrain Logo",
                                modifier = Modifier
                                    .size(80.dp)
                                    .alpha(0.9f),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "AutoBrain Analysis Tools",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Advanced AI-powered vehicle diagnostics",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                item {
                    AnimatedEntrance(
                        visible = isVisible,
                        delay = 100
                    ) {
                        DiagnosticToolCard(
                            title = "Engine Sound Analysis",
                            description = "Detect rattling, belt noise and mechanical problems via microphone.",
                            icon = Icons.Outlined.Mic,
                            color1 = Color(0xFF4facfe),
                            color2 = Color(0xFF00f2fe),
                            onClick = { navController.navigate(Screen.EngineSoundAnalysis.route) }
                        )
                    }
                }

                item {
                    AnimatedEntrance(
                        visible = isVisible,
                        delay = 200
                    ) {
                        DiagnosticToolCard(
                            title = "Visual Video Analysis",
                            description = "Identify exhaust smoke (color) and engine vibrations via camera.",
                            icon = Icons.Outlined.Videocam,
                            color1 = Color(0xFF43e97b),
                            color2 = Color(0xFF38f9d7),
                            onClick = { navController.navigate(Screen.VideoAnalysis.route) }
                        )
                    }
                }

                item {
                    AnimatedEntrance(
                        visible = isVisible,
                        delay = 300
                    ) {
                        DiagnosticToolCard(
                            title = "Market Price Estimation",
                            description = "Get an accurate estimate based on current market.",
                            icon = Icons.Outlined.AttachMoney,
                            color1 = Color(0xFFfa709a),
                            color2 = Color(0xFFfee140),
                            onClick = { navController.navigate(Screen.PriceEstimation.route) }
                        )
                    }
                }

                item {
                    AnimatedEntrance(
                        visible = isVisible,
                        delay = 400
                    ) {
                        DiagnosticToolCard(
                            title = "Trust Report",
                            description = "Complete anti-scam synthesis combining all analyses.",
                            icon = Icons.Outlined.VerifiedUser,
                            color1 = Color(0xFF667eea),
                            color2 = Color(0xFF764ba2),
                            onClick = { 
                                navController.navigate(Screen.AIScore.route) 
                            }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DiagnosticToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    color1: Color,
    color2: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "card_glow")
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    val iconRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_rotation"
    )
    
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepNavy.copy(alpha = 0.8f),
                            DeepNavy.copy(alpha = 0.6f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            color1.copy(alpha = glowAlpha * 0.5f),
                            color2.copy(alpha = glowAlpha * 0.5f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = (-40).dp, y = (-40).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                color1.copy(alpha = glowAlpha * 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 40.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                color2.copy(alpha = glowAlpha * 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        color1.copy(alpha = 0.2f),
                                        color2.copy(alpha = 0.2f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .blur(8.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(color1, color2)
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .rotate(iconRotation)
                                .scale(iconScale)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = color1.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
