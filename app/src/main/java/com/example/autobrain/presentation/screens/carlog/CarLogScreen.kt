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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.BuildCircle
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.autobrain.core.utils.toCurrency
import com.example.autobrain.core.utils.toFormattedDate
import com.example.autobrain.domain.model.MaintenanceRecord
import com.example.autobrain.domain.model.MaintenanceReminder
import com.example.autobrain.domain.model.MaintenanceType
import com.example.autobrain.domain.model.ReminderPriority
import com.example.autobrain.presentation.components.ModernBottomNavBar
import com.example.autobrain.presentation.navigation.Screen
import com.example.autobrain.presentation.theme.AutoBrainTheme
import com.example.autobrain.presentation.theme.DeepNavy
import com.example.autobrain.presentation.theme.ElectricTeal
import com.example.autobrain.presentation.theme.ErrorRed
import com.example.autobrain.presentation.theme.MidnightBlack
import com.example.autobrain.presentation.theme.SuccessGreen
import com.example.autobrain.presentation.theme.TextMuted
import com.example.autobrain.presentation.theme.TextOnAccent
import com.example.autobrain.presentation.theme.TextPrimary
import com.example.autobrain.presentation.theme.TextSecondary
import com.example.autobrain.presentation.theme.WarningAmber
import java.util.Locale
import java.util.concurrent.TimeUnit

// Helper functions for maintenance calculations
private fun calculateVehicleHealthScore(records: List<MaintenanceRecord>): Int {
    if (records.isEmpty()) return 85
    
    val now = System.currentTimeMillis()
    val recentRecords = records.filter { 
        val daysSince = TimeUnit.MILLISECONDS.toDays(now - it.date)
        daysSince <= 180 // Last 6 months
    }
    
    if (recentRecords.isEmpty()) return 60
    
    var score = 100
    
    // Penalize for overdue maintenance
    val lastMaintenanceDate = records.maxByOrNull { it.date }?.date ?: 0
    val daysSinceLastMaintenance = TimeUnit.MILLISECONDS.toDays(now - lastMaintenanceDate)
    if (daysSinceLastMaintenance > 180) score -= 20
    else if (daysSinceLastMaintenance > 90) score -= 10
    
    // Reward regular maintenance
    if (recentRecords.size >= 3) score += 5
    
    return score.coerceIn(0, 100)
}

private fun predictNextMaintenance(records: List<MaintenanceRecord>): String {
    if (records.isEmpty()) return "No data"
    
    val lastRecord = records.maxByOrNull { it.date } ?: return "Unknown"
    val daysSince = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastRecord.date)
    
    val nextMaintenanceDays = when (lastRecord.type) {
        MaintenanceType.OIL_CHANGE -> 90 - daysSince
        MaintenanceType.TIRE_ROTATION -> 180 - daysSince
        MaintenanceType.BRAKE_SERVICE -> 365 - daysSince
        else -> 90 - daysSince
    }
    
    return if (nextMaintenanceDays <= 0) "Overdue" 
           else if (nextMaintenanceDays <= 7) "This week"
           else if (nextMaintenanceDays <= 30) "This month"
           else "In ${nextMaintenanceDays} days"
}

private fun calculateAverageMonthlyCost(records: List<MaintenanceRecord>): Double {
    if (records.isEmpty()) return 0.0
    
    val now = System.currentTimeMillis()
    val oneYearAgo = now - TimeUnit.DAYS.toMillis(365)
    
    val recentRecords = records.filter { it.date >= oneYearAgo }
    if (recentRecords.isEmpty()) return 0.0
    
    val totalCost = recentRecords.sumOf { it.cost }
    val monthsCovered = recentRecords.size.coerceAtLeast(1)
    
    return totalCost / monthsCovered
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun CarLogScreenPreview() {
    val navController = rememberNavController()
    AutoBrainTheme {
        CarLogScreenContent(navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarLogScreenContent(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val sampleRecords = listOf(
        MaintenanceRecord(
            id = "1",
            type = MaintenanceType.OIL_CHANGE,
            description = "Oil Change",
            date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30),
            cost = 45.0,
            mileage = 15000,
            serviceProvider = "AutoService Pro",
            notes = "Synthetic oil used"
        ),
        MaintenanceRecord(
            id = "2",
            type = MaintenanceType.BRAKE_SERVICE,
            description = "Brake Pads Replacement",
            date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90),
            cost = 180.0,
            mileage = 14500,
            serviceProvider = "Brake Masters",
            notes = "Front brake pads replaced"
        )
    )
    
    val healthScore = calculateVehicleHealthScore(sampleRecords)
    val nextMaintenance = predictNextMaintenance(sampleRecords)
    val avgCostPerMonth = calculateAverageMonthlyCost(sampleRecords)

    Scaffold(
        containerColor = MidnightBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = ElectricTeal,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Smart Logbook",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = TextPrimary
                            )
                            Text(
                                "Auto tracking",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AddMaintenance.createRoute("default")) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = ElectricTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                    actionIconContentColor = ElectricTeal
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddMaintenance.createRoute("default")) },
                containerColor = ElectricTeal,
                contentColor = MidnightBlack,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enhanced Summary Card
            item {
                EnhancedSmartSummaryCard(
                    totalRecords = sampleRecords.size,
                    totalExpenses = sampleRecords.sumOf { it.cost },
                    nextMaintenance = nextMaintenance,
                    healthScore = healthScore,
                    avgCostPerMonth = avgCostPerMonth,
                    lastMaintenanceDate = sampleRecords.maxByOrNull { it.date }?.date
                )
            }
            
            // Recent Records
            items(sampleRecords) { record ->
                MaintenanceRecordCard(
                    record = record,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun MaintenanceRecordCard(
    modifier: Modifier = Modifier,
    record: MaintenanceRecord,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "card_scale")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavy.copy(alpha = 0.7f)),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Service Icon with dynamic background
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        ElectricTeal.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getMaintenanceTypeIcon(record.type),
                    contentDescription = null,
                    tint = ElectricTeal,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = TextMuted, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = record.date.toFormattedDate(),
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.Speed, null, tint = TextMuted, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${record.mileage} km",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = record.cost.toCurrency(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = "Total Paid",
                    fontSize = 9.sp,
                    color = ElectricTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarLogScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: CarLogViewModel = hiltViewModel()
) {
    val carLogState by viewModel.carLogState.collectAsState()
    val remindersState by viewModel.remindersState.collectAsState()
    val smartRemindersState by viewModel.smartRemindersState.collectAsState()
    
    // AI Advice dialog state
    var showAIDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(MidnightBlack)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Smart Logbook",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = TextPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(SuccessGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Connected & Live",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(20.dp))
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { /* Settings or Filter */ },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(DeepNavy.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Filter", tint = ElectricTeal, modifier = Modifier.size(20.dp))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = TextPrimary
                    )
                )
            },
        bottomBar = {
            ModernBottomNavBar(
                currentRoute = "car_logbook",
                onNavigate = { route ->
                    when (route) {
                        "home" -> navController.navigate(Screen.Home.route)
                        "ai_diagnostics" -> navController.navigate(Screen.AIDiagnostics.route)
                        "car_logbook" -> { /* Already on Carlog */ }
                        "ai_assistant" -> navController.navigate(Screen.AIAssistant.route)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val carId = when (val state = carLogState) {
                        is CarLogState.Success -> state.carLog.id
                        else -> ""
                    }
                    navController.navigate(Screen.AddMaintenance.createRoute(carId))
                },
                containerColor = ElectricTeal,
                contentColor = MidnightBlack,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log Service", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enhanced Summary Card with Health Score
            item {
                when (val state = carLogState) {
                    is CarLogState.Success -> {
                        val records = state.carLog.maintenanceRecords
                        val healthScore = calculateVehicleHealthScore(records)
                        val nextMaintenance = predictNextMaintenance(records)
                        val avgCostPerMonth = calculateAverageMonthlyCost(records)
                        
                        EnhancedSmartSummaryCard(
                            totalRecords = records.size,
                            totalExpenses = state.carLog.totalExpenses,
                            nextMaintenance = nextMaintenance,
                            healthScore = healthScore,
                            avgCostPerMonth = avgCostPerMonth,
                            lastMaintenanceDate = records.maxByOrNull { it.date }?.date
                        )
                    }

                    else -> {
                        EnhancedSmartSummaryCard(
                            totalRecords = 0,
                            totalExpenses = 0.0,
                            nextMaintenance = "Add your first maintenance",
                            healthScore = 0,
                            avgCostPerMonth = 0.0,
                            lastMaintenanceDate = null
                        )
                    }
                }
            }

            // Professional Action Buttons
            item {
                val user by viewModel.currentUser.collectAsState()
                ProfessionalActionButtonsSection(
                    onAddMaintenance = { 
                        val carId = when (val state = carLogState) {
                            is CarLogState.Success -> state.carLog.id
                            else -> user?.uid ?: ""
                        }
                        navController.navigate(Screen.AddMaintenance.createRoute(carId))
                    },
                    onAIAdvice = { 
                        showAIDialog = true
                        viewModel.getSmartMaintenanceAnalysis()
                    },
                    onViewReminders = { navController.navigate(Screen.Reminders.route) }
                )
            }

            // Upcoming Reminders Section
            item {
                SectionHeader(
                    title = "Upcoming reminders",
                    subtitle = "Don't miss any due date",
                    actionText = "View all",
                    onActionClick = { navController.navigate(Screen.Reminders.route) }
                )
            }

            // Reminders
            when (val state = remindersState) {
                is RemindersState.Success -> {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.reminders.take(5)) { reminder ->
                                SmartReminderCard(
                                    reminder = reminder,
                                    onComplete = { viewModel.markReminderCompleted(reminder.id) }
                                )
                            }
                        }
                    }
                }

                is RemindersState.Empty -> {
                    item {
                        val user by viewModel.currentUser.collectAsState()
                        EmptyRemindersCard(
                            onAddReminder = { 
                                val carId = when (val state = carLogState) {
                                    is CarLogState.Success -> state.carLog.userId
                                    else -> user?.uid ?: "default"
                                }
                                navController.navigate(Screen.AddReminder.createRoute(carId))
                            }
                        )
                    }
                }

                else -> {}
            }

            // Maintenance History Section
            item {
                SectionHeader(
                    title = "Maintenance history",
                    subtitle = "All your records",
                    actionText = null,
                    onActionClick = null
                )
            }

            when (val state = carLogState) {
                is CarLogState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ElectricTeal)
                        }
                    }
                }

                is CarLogState.Success -> {
                    val sortedRecords =
                        state.carLog.maintenanceRecords.sortedByDescending { it.date }
                    
                    // Add timeline header
                    item {
                        Text(
                            text = "Timeline",
                            style = MaterialTheme.typography.titleMedium,
                            color = ElectricTeal,
                            modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp)
                        )
                    }

                    items(sortedRecords) { record ->
                        TimelineNode(
                            record = record,
                            isLast = record == sortedRecords.last(),
                            onClick = {
                                navController.navigate(
                                    Screen.MaintenanceDetail.createRoute(record.id)
                                )
                            }
                        )
                    }
                }

                is CarLogState.Empty -> {
                    item {
                        val user by viewModel.currentUser.collectAsState()
                        EmptyMaintenanceCard(
                            onAddMaintenance = { 
                                val carId = user?.uid ?: "default"
                                navController.navigate(Screen.AddMaintenance.createRoute(carId)) 
                            }
                        )
                    }
                }

                is CarLogState.Error -> {
                    item {
                        ErrorCard(
                            message = state.message,
                            onRetry = { viewModel.refresh() },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
    
    // AI Dialog
    if (showAIDialog) {
        AIAdviceDialog(
            smartRemindersState = smartRemindersState,
            onDismiss = { 
                showAIDialog = false
                viewModel.resetSmartReminders()
            }
        )
    }
    }
}

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "section_header_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "header_glow"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(32.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    ElectricTeal.copy(alpha = glowAlpha),
                                    ElectricTeal.copy(alpha = glowAlpha * 0.4f)
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = (-0.8).sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(
                                    ElectricTeal.copy(alpha = glowAlpha),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }
            }

            if (actionText != null && onActionClick != null) {
                Box(
                    modifier = Modifier
                        .clickable { onActionClick() }
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    ElectricTeal.copy(alpha = 0.2f),
                                    ElectricTeal.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            ElectricTeal.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = actionText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricTeal,
                            letterSpacing = 0.3.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = ElectricTeal,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyRemindersCard(
    modifier: Modifier = Modifier,
    onAddReminder: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No reminders",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "Ajoutez des rappels pour ne rien oublier",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddReminder,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricTeal,
                    contentColor = TextOnAccent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add reminder")
            }
        }
    }
}

@Composable
private fun EmptyMaintenanceCard(
    modifier: Modifier = Modifier,
    onAddMaintenance: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(ElectricTeal.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = ElectricTeal,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "No records",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Start tracking your vehicle's maintenance",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddMaintenance,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricTeal,
                    contentColor = TextOnAccent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add maintenance",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricTeal,
                    contentColor = TextOnAccent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

// =============================================================================
// AI ADVICE DIALOG (GEMINI)
// =============================================================================

@Composable
private fun AIAdviceDialog(
    smartRemindersState: SmartRemindersState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepNavy,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D))
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text(
                        "Gemini AI advice",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "Intelligent analysis",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            when (smartRemindersState) {
                is SmartRemindersState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = ElectricTeal,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Gemini is analyzing your vehicle...",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                is SmartRemindersState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Main advice
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = ElectricTeal.copy(alpha = 0.15f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = ElectricTeal,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            "Main advice",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ElectricTeal
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        smartRemindersState.advice,
                                        fontSize = 14.sp,
                                        color = TextPrimary,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                        
                        // Suggested maintenance
                        if (smartRemindersState.suggestedMaintenance.isNotEmpty()) {
                            item {
                                Text(
                                    "Recommended maintenance",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            
                            items(smartRemindersState.suggestedMaintenance) { suggestion ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(ElectricTeal, CircleShape)
                                    )
                                    Text(
                                        suggestion,
                                        fontSize = 14.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        
                        // Footer Gemini
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Powered by ",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    "Google Gemini",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4285F4)
                                )
                            }
                        }
                    }
                }
                
                is SmartRemindersState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            smartRemindersState.message,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Check your internet connection",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                is SmartRemindersState.Idle -> {
                    Text(
                        "Ready to analyze...",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricTeal,
                    contentColor = TextOnAccent
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun TimelineNode(
    modifier: Modifier = Modifier,
    record: MaintenanceRecord,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val iconColor = when {
        record.type.name.contains("OIL", ignoreCase = true) -> ElectricTeal
        record.type.name.contains("BRAKE", ignoreCase = true) -> ErrorRed
        record.type.name.contains("TIRE", ignoreCase = true) -> SuccessGreen
        record.type.name.contains("INSPECTION", ignoreCase = true) -> WarningAmber
        else -> ElectricTeal
    }
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "timeline_scale"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "timeline_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(28.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                iconColor.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                iconColor.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            radius = 50f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(DeepNavy, CircleShape)
                        .border(
                            width = 2.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    iconColor.copy(alpha = pulseAlpha),
                                    iconColor.copy(alpha = pulseAlpha * 0.6f)
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .weight(1f)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    iconColor.copy(alpha = 0.4f),
                                    iconColor.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, bottom = 28.dp)
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
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                DeepNavy.copy(alpha = 0.85f),
                                DeepNavy.copy(alpha = 0.65f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                iconColor.copy(alpha = 0.4f),
                                iconColor.copy(alpha = 0.15f)
                            )
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        iconColor.copy(alpha = 0.25f),
                                        iconColor.copy(alpha = 0.15f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                iconColor.copy(alpha = 0.4f),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getMaintenanceTypeIcon(record.type),
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(14.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = record.description,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    null,
                                    tint = iconColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = record.date.toFormattedDate(),
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Speed,
                                    null,
                                    tint = iconColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${record.mileage} km",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = record.cost.toCurrency(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    SuccessGreen.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    SuccessGreen.copy(alpha = 0.4f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Completed",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SuccessGreen,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🎴 SMART REMINDER CARD
 * Displays upcoming maintenance reminders with priority and urgency
 */
@Composable
private fun SmartReminderCard(
    modifier: Modifier = Modifier,
    reminder: MaintenanceReminder,
    onComplete: () -> Unit
) {
    val priorityColor = when (reminder.priority) {
        ReminderPriority.CRITICAL -> ErrorRed
        ReminderPriority.HIGH -> Color(0xFFFB923C)
        ReminderPriority.MEDIUM -> Color(0xFFFBBF24)
        ReminderPriority.LOW -> ElectricTeal
    }
    
    val typeIcon = when (reminder.type) {
        MaintenanceType.OIL_CHANGE -> Icons.Outlined.WaterDrop
        MaintenanceType.BRAKE_SERVICE -> Icons.Outlined.BuildCircle
        MaintenanceType.TIRE_ROTATION -> Icons.Outlined.Album
        MaintenanceType.ENGINE_TUNE_UP -> Icons.Outlined.Settings
        MaintenanceType.BATTERY_REPLACEMENT -> Icons.Outlined.BatteryChargingFull
        else -> Icons.Outlined.Build
    }
    
    val daysRemaining = ((reminder.dueDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
    val isOverdue = daysRemaining < 0
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "reminder_card_scale"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "reminder_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    Card(
        modifier = modifier
            .width(300.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DeepNavy.copy(alpha = 0.9f),
                            DeepNavy.copy(alpha = 0.7f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            priorityColor.copy(alpha = glowAlpha * 0.6f),
                            priorityColor.copy(alpha = glowAlpha * 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        priorityColor.copy(alpha = 0.25f),
                                        priorityColor.copy(alpha = 0.15f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = priorityColor.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = priorityColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Surface(
                        color = priorityColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = when (reminder.priority) {
                                ReminderPriority.CRITICAL -> "CRITICAL"
                                ReminderPriority.HIGH -> "HIGH"
                                ReminderPriority.MEDIUM -> "MEDIUM"
                                ReminderPriority.LOW -> "LOW"
                            },
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = priorityColor,
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = reminder.title.ifEmpty { getMaintenanceTypeLabel(reminder.type) },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    letterSpacing = (-0.5).sp
                )
                
                if (reminder.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = reminder.description,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isOverdue) ErrorRed.copy(alpha = 0.15f) else ElectricTeal.copy(alpha = 0.1f),
                                RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.dp,
                                if (isOverdue) ErrorRed.copy(alpha = 0.3f) else ElectricTeal.copy(alpha = 0.2f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = if (isOverdue) ErrorRed else ElectricTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOverdue) "Overdue" else "Due in",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isOverdue) "${-daysRemaining}d ago" else "${daysRemaining}d",
                                fontSize = 16.sp,
                                color = if (isOverdue) ErrorRed else TextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    
                    if (reminder.dueMileage > 0) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    priorityColor.copy(alpha = 0.1f),
                                    RoundedCornerShape(14.dp)
                                )
                                .border(
                                    1.dp,
                                    priorityColor.copy(alpha = 0.2f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Speed,
                                        contentDescription = null,
                                        tint = priorityColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Mileage",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${String.format("%,d", reminder.dueMileage)}km",
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        priorityColor.copy(alpha = 0.3f),
                                        priorityColor.copy(alpha = 0.2f)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.dp,
                                priorityColor.copy(alpha = 0.5f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = priorityColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Mark as Done",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = priorityColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
