package com.example.autobrain.presentation.screens.price

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
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
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState) {
                            is PriceEstimationUiState.Input -> "Estimate Car Value"
                            is PriceEstimationUiState.Calculating -> "Calculating..."
                            is PriceEstimationUiState.Result -> "Price Estimation"
                            is PriceEstimationUiState.Error -> "Estimation"
                        },
                        color = Color(0xFF00FFFF),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF00FFFF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1117)
                )
            )
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Enter car details for AI price estimation",
            fontSize = 14.sp,
            color = Color(0xFF7F8C8D)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Brand Field
        OutlinedTextField(
            value = brand,
            onValueChange = onBrandChange,
            label = { Text("Brand", color = Color(0xFF7F8C8D)) },
            placeholder = { Text("RS6", color = Color(0xFF4A5568)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFFF),
                unfocusedBorderColor = Color(0xFF1C2838),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF00FFFF)
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Model Field
        OutlinedTextField(
            value = model,
            onValueChange = onModelChange,
            label = { Text("Model", color = Color(0xFF7F8C8D)) },
            placeholder = { Text("APT", color = Color(0xFF4A5568)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFFF),
                unfocusedBorderColor = Color(0xFF1C2838),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF00FFFF)
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Year Field
        OutlinedTextField(
            value = year,
            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) onYearChange(it) },
            label = { Text("Year", color = Color(0xFF7F8C8D)) },
            placeholder = { Text("2024", color = Color(0xFF4A5568)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFFF),
                unfocusedBorderColor = Color(0xFF1C2838),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF00FFFF)
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Mileage Field (Highlighted like in screenshot)
        OutlinedTextField(
            value = mileage,
            onValueChange = { if (it.all { char -> char.isDigit() }) onMileageChange(it) },
            label = { Text("Mileage (km)", color = Color(0xFF00FFFF)) },
            placeholder = { Text("200000", color = Color(0xFF4A5568)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFFF),
                unfocusedBorderColor = Color(0xFF00FFFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF00FFFF),
                focusedLabelColor = Color(0xFF00FFFF),
                unfocusedLabelColor = Color(0xFF00FFFF)
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Condition Selector
        Text(
            text = "Condition",
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Excellent", "Good", "Fair").forEach { cond ->
                Button(
                    onClick = { onConditionChange(cond) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (condition == cond) Color(0xFF00FFFF) else Color(0xFF1C2838),
                        contentColor = if (condition == cond) Color.Black else Color(0xFF7F8C8D)
                    )
                ) {
                    Text(
                        text = cond,
                        fontWeight = if (condition == cond) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1C2838).copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFF00FFFF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Prices are estimated based on current market listings and demand.",
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF),
                    lineHeight = 18.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Calculate Button
        Button(
            onClick = onCalculate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = brand.isNotBlank() && model.isNotBlank() && year.length == 4 && mileage.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00FFFF),
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFF003A5C)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Calculate Price",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
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
            
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(100.dp),
                    color = Color(0xFF00FFFF),
                    strokeWidth = 4.dp
                )
                
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = Color(0xFF00FFFF),
                    modifier = Modifier.size(48.dp)
                )
            }
            
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Main Price Card (Matching first screenshot)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1C2838)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Estimated Value",
                    fontSize = 16.sp,
                    color = Color(0xFF9CA3AF)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Large Price Display
                Text(
                    text = "$${formatPrice(result.minPrice)} - $${formatPrice(result.maxPrice)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 42.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Confidence Indicator
                val confidenceText = when {
                    result.confidence >= 0.8f -> "High Confidence"
                    result.confidence >= 0.6f -> "Medium Confidence"
                    else -> "Low Confidence"
                }
                val confidenceColor = when {
                    result.confidence >= 0.8f -> Color(0xFF10B981)
                    result.confidence >= 0.6f -> Color(0xFF10B981)
                    else -> Color(0xFFFBBF24)
                }
                
                Text(
                    text = confidenceText,
                    fontSize = 14.sp,
                    color = confidenceColor
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Progress Bar
                LinearProgressIndicator(
                    progress = { result.confidence },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = confidenceColor,
                    trackColor = Color(0xFF374151)
                )
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
                    tint = Color(0xFFFBBF24),
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
                text = "Erreur",
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
                Text("Réessayer", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
