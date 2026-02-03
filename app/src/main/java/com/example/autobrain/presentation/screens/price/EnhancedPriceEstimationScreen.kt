package com.example.autobrain.presentation.screens.price

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autobrain.R
import com.example.autobrain.domain.model.GeminiPriceEstimation
import com.example.autobrain.presentation.components.*
import com.example.autobrain.presentation.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Enhanced Price Estimation Screen
 * With Firebase + Gemini Integration + Share Functionality
 * 
 * Matches design from screenshots with:
 * - Dark theme (#0A1628 background)
 * - Cyan accents (#00FFFF)
 * - Beautiful animations
 * - Share functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedPriceEstimationScreen(
    navController: NavController,
    viewModel: PriceEstimationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Excellent") }
    
    Scaffold(
        containerColor = Color(0xFF0A1628),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0D1117),
                                Color(0xFF0A1628).copy(alpha = 0.95f)
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    ElectricTeal.copy(alpha = 0.6f),
                                    Color(0xFF00FF88).copy(alpha = 0.6f),
                                    ElectricTeal.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            )
                        )
                        .align(Alignment.BottomCenter)
                )
                
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                ElectricTeal.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AttachMoney,
                                    contentDescription = null,
                                    tint = ElectricTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = when (uiState) {
                                        is PriceEstimationUiState.Input -> "Estimate Car Value"
                                        is PriceEstimationUiState.Calculating -> "Calculating..."
                                        is PriceEstimationUiState.Result -> "Price Estimation"
                                        is PriceEstimationUiState.Error -> "Estimation"
                                    },
                                    color = ElectricTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "AI-Powered Valuation",
                                    color = TextSecondary.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(40.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            ElectricTeal.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = ElectricTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
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
                is PriceEstimationUiState.Input -> {
                    InputStateEnhanced(
                        brand = brand,
                        onBrandChange = { brand = it },
                        model = model,
                        onModelChange = { model = it },
                        year = year,
                        onYearChange = { year = it },
                        mileage = mileage,
                        onMileageChange = { mileage = it },
                        condition = condition,
                        onConditionChange = { condition = it },
                        onCalculate = {
                            viewModel.calculatePrice(brand, model, year, mileage, condition)
                        }
                    )
                }
                
                is PriceEstimationUiState.Calculating -> {
                    CalculatingStateEnhanced()
                }
                
                is PriceEstimationUiState.Result -> {
                    ResultStateEnhanced(
                        result = state.estimation,
                        carInfo = "$brand $model $year",
                        onShare = { viewModel.shareEstimation(state.estimation, "$brand $model $year") },
                        onNewEstimation = { viewModel.resetToInput() }
                    )
                }
                
                is PriceEstimationUiState.Error -> {
                    ErrorStateEnhanced(
                        message = state.message,
                        onRetry = { viewModel.resetToInput() }
                    )
                }
            }
        }
    }
}

// =============================================================================
// INPUT STATE (Matching Screenshot Design)
// =============================================================================

@Composable
private fun InputStateEnhanced(
    brand: String,
    onBrandChange: (String) -> Unit,
    model: String,
    onModelChange: (String) -> Unit,
    year: String,
    onYearChange: (String) -> Unit,
    mileage: String,
    onMileageChange: (String) -> Unit,
    condition: String,
    onConditionChange: (String) -> Unit,
    onCalculate: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_animations")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .rotate(rotation * 0.1f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ElectricTeal.copy(alpha = 0.12f * glowAlpha),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .rotate(-rotation * 0.15f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00FF88).copy(alpha = 0.08f * glowAlpha),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                
                for (i in 0..2) {
                    val radius = 80f + (i * 40f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00BFFF).copy(alpha = 0.15f * glowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(centerX, centerY),
                            radius = radius
                        ),
                        radius = radius,
                        center = Offset(centerX, centerY)
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Discover Your",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Car's True Value",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = ElectricTeal,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GeminiIconWithGlow(size = 14.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI-Powered Instant Valuation",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(56.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F2838)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Why Know Your Car Value?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricTeal
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                BenefitRow(text = "Get best price when selling")
                Spacer(modifier = Modifier.height(10.dp))
                BenefitRow(text = "Know fair trade-in value")
                Spacer(modifier = Modifier.height(10.dp))
                BenefitRow(text = "Track depreciation over time")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Enter Your Car Details",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F2838)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = onBrandChange,
                    label = { Text("Brand", color = Color(0xFF7F8C8D)) },
                    placeholder = { Text("e.g. BMW", color = Color(0xFF4A5568)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color(0xFF1C2838),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = ElectricTeal,
                        focusedContainerColor = Color(0xFF0A1628),
                        unfocusedContainerColor = Color(0xFF0A1628)
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = model,
                    onValueChange = onModelChange,
                    label = { Text("Model", color = Color(0xFF7F8C8D)) },
                    placeholder = { Text("e.g. M5", color = Color(0xFF4A5568)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color(0xFF1C2838),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = ElectricTeal,
                        focusedContainerColor = Color(0xFF0A1628),
                        unfocusedContainerColor = Color(0xFF0A1628)
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = year,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) onYearChange(it) },
                        label = { Text("Year", color = Color(0xFF7F8C8D)) },
                        placeholder = { Text("2024", color = Color(0xFF4A5568)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricTeal,
                            unfocusedBorderColor = Color(0xFF1C2838),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = ElectricTeal,
                            focusedContainerColor = Color(0xFF0A1628),
                            unfocusedContainerColor = Color(0xFF0A1628)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = ElectricTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = mileage,
                        onValueChange = { if (it.all { char -> char.isDigit() }) onMileageChange(it) },
                        label = { Text("Mileage (km)", color = Color(0xFF7F8C8D)) },
                        placeholder = { Text("50000", color = Color(0xFF4A5568)) },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricTeal,
                            unfocusedBorderColor = Color(0xFF1C2838),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = ElectricTeal,
                            focusedContainerColor = Color(0xFF0A1628),
                            unfocusedContainerColor = Color(0xFF0A1628)
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = ElectricTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "Condition",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Excellent" to "🌟",
                        "Good" to "👍",
                        "Fair" to "⚠️"
                    ).forEach { (cond, emoji) ->
                        Button(
                            onClick = { onConditionChange(cond) },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (condition == cond) ElectricTeal else Color(0xFF1C2838),
                                contentColor = if (condition == cond) Color.Black else Color(0xFF7F8C8D)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = cond,
                                    fontSize = 11.sp,
                                    fontWeight = if (condition == cond) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Button(
            onClick = onCalculate,
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = brand.isNotBlank() && model.isNotBlank() && year.length == 4 && mileage.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Brush.horizontalGradient(
                    colors = listOf(
                        ElectricTeal,
                        Color(0xFF00D9C0)
                    )
                ).let { ElectricTeal },
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFF1C2838)
            )
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Get Instant Valuation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// =============================================================================
// CALCULATING STATE (With Animation)
// =============================================================================

@Composable
private fun CalculatingStateEnhanced() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated circle
            val infiniteTransition = rememberInfiniteTransition(label = "calc")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing)
                ),
                label = "rotation"
            )
            
            CircularProgressIndicator(
                modifier = Modifier.size(100.dp),
                color = Color(0xFF00FFFF),
                strokeWidth = 4.dp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Analyzing market data...",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                GeminiIconWithGlow(size = 16.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gemini AI analyzing market data",
                    fontSize = 14.sp,
                    color = Color(0xFF00FFFF)
                )
            }
        }
    }
}

// =============================================================================
// RESULT STATE (Matching Screenshot Design)
// =============================================================================

@Composable
private fun ResultStateEnhanced(
    result: GeminiPriceEstimation,
    carInfo: String,
    onShare: () -> Unit,
    onNewEstimation: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "result_animations")
    
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SuccessGreen,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "✨ Valuation Complete!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = carInfo,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = ElectricTeal,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(28.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F2838)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ElectricTeal.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Market Value",
                        fontSize = 16.sp,
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "$${formatPrice(result.minPrice)} - $${formatPrice(result.maxPrice)}",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = shimmer),
                        textAlign = TextAlign.Center,
                        lineHeight = 48.sp,
                        letterSpacing = (-0.5).sp
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    val confidenceText = when {
                        result.confidence >= 0.8f -> "🎯 High Confidence"
                        result.confidence >= 0.6f -> "👍 Medium Confidence"
                        else -> "⚠️ Low Confidence"
                    }
                    val confidenceColor = when {
                        result.confidence >= 0.8f -> SuccessGreen
                        result.confidence >= 0.6f -> Color(0xFF10B981)
                        else -> WarningAmber
                    }
                    
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = confidenceColor.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = confidenceText,
                                fontSize = 14.sp,
                                color = confidenceColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${(result.confidence * 100).toInt()}%",
                                fontSize = 14.sp,
                                color = confidenceColor,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LinearProgressIndicator(
                        progress = { result.confidence },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = confidenceColor,
                        trackColor = Color(0xFF374151)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Factors Affecting Price Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1C2838)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Factors Affecting Price",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                result.factors.forEach { factor ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (factor.isPositive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (factor.isPositive) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = factor.name,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        
                        Text(
                            text = factor.value,
                            fontSize = 14.sp,
                            color = if (factor.isPositive) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Market Note
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF78350F).copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFF00BFFF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Market prices vary based on location & demand.",
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Share Button (Matching screenshot)
        Button(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00FFFF),
                contentColor = Color.Black
            )
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Share Estimation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // New Estimation Button
        OutlinedButton(
            onClick = onNewEstimation,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FFFF)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF00FFFF)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "New Estimation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// =============================================================================
// ERROR STATE
// =============================================================================

@Composable
private fun ErrorStateEnhanced(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Error",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FFFF),
                    contentColor = Color.Black
                )
            ) {
                Text("Retry", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// =============================================================================
// HELPER FUNCTIONS
// =============================================================================

private fun formatPrice(price: Int): String {
    return price.toString().reversed().chunked(3).joinToString(",").reversed()
}

@Composable
private fun BenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(ElectricTeal, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
