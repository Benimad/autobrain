package com.example.autobrain.presentation.screens.carlog

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autobrain.core.utils.toFormattedDate
import com.example.autobrain.core.utils.toCurrency
import com.example.autobrain.domain.model.MaintenanceRecord
import com.example.autobrain.domain.model.MaintenanceReminder
import com.example.autobrain.domain.model.MaintenanceType
import com.example.autobrain.domain.model.ReminderPriority
import com.example.autobrain.presentation.components.ModernBottomNavBar
import com.example.autobrain.presentation.navigation.Screen
import com.example.autobrain.presentation.theme.*
import java.util.Calendar
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
@Composable
fun CarLogScreen(
    navController: NavController,
    viewModel: CarLogViewModel = hiltViewModel()
) {
    val carLogState by viewModel.carLogState.collectAsState()
    val remindersState by viewModel.remindersState.collectAsState()
    val smartRemindersState by viewModel.smartRemindersState.collectAsState()
    
    // AI Advice dialog state
    var showAIDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MidnightBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.MenuBook,
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
                    IconButton(onClick = { navController.navigate(Screen.AddMaintenance.route) }) {
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
            FloatingActionButton(
                onClick = {
                    val carId = when (val state = carLogState) {
                        is CarLogState.Success -> state.carLog.userId
                        else -> "default"
                    }
                    navController.navigate(Screen.AddMaintenance.createRoute(carId))
                },
                containerColor = ElectricTeal,
                contentColor = TextOnAccent,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
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
                        EmptyMaintenanceCard(
                            onAddMaintenance = { navController.navigate(Screen.AddMaintenance.route) }
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

@Composable
private fun SmartSummaryCard(
    totalRecords: Int,
    totalExpenses: Double,
    nextMaintenance: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            DeepNavy,
                            DarkNavy,
                            Color(0xFF1A2332)
                        )
                    )
                )
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = (-40).dp, y = (-40).dp)
                    .background(
                        Color.White.copy(alpha = 0.03f),
                        CircleShape
                    )
            )

            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Vehicle summary",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = totalExpenses.toCurrency(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Total expenses",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    // Records count badge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(
                                    ElectricTeal.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = totalRecords.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricTeal
                                )
                                Text(
                                    text = "entries",
                                    fontSize = 10.sp,
                                    color = ElectricTeal.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Next maintenance alert
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = SlateGray
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = ElectricTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Next maintenance",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = nextMaintenance,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onAddMaintenance: () -> Unit,
    onViewReminders: () -> Unit,
    onViewDocuments: () -> Unit,
    onAIAdvice: () -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            QuickActionCard(
                icon = Icons.Default.Add,
                title = "Add",
                subtitle = "Maintenance",
                gradientColors = listOf(ElectricTeal, TealDark),
                onClick = onAddMaintenance
            )
        }
        item {
            QuickActionCard(
                icon = Icons.Default.AutoAwesome,
                title = "AI Advice",
                subtitle = "Gemini Smart",
                gradientColors = listOf(WarningAmber, Color(0xFFFBBF24)),
                onClick = onAIAdvice
            )
        }
        item {
            QuickActionCard(
                icon = Icons.Default.Notifications,
                title = "Reminders",
                subtitle = "Due dates",
                gradientColors = listOf(Color(0xFFB794F4), Color(0xFF9F7AEA)),
                onClick = onViewReminders
            )
        }
        item {
            QuickActionCard(
                icon = Icons.Default.Description,
                title = "Documents",
                subtitle = "Insurance, Inspection",
                gradientColors = listOf(SuccessGreen, SuccessGreenLight),
                onClick = onViewDocuments
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    actionText: String?,
    onActionClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    fontSize = 14.sp,
                    color = ElectricTeal,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ElectricTeal,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SmartReminderCard(
    reminder: com.example.autobrain.domain.model.MaintenanceReminder,
    onComplete: () -> Unit
) {
    val (iconColor, backgroundColor) = when {
        reminder.title.contains("Visite", ignoreCase = true) ->
            ErrorRed to ErrorRed.copy(alpha = 0.15f)

        reminder.title.contains("Assurance", ignoreCase = true) ->
            WarningAmber to WarningAmber.copy(alpha = 0.15f)

        reminder.title.contains("Oil change", ignoreCase = true) ->
            ElectricTeal to ElectricTeal.copy(alpha = 0.15f)

        else -> SuccessGreen to SuccessGreen.copy(alpha = 0.15f)
    }

    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(backgroundColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onComplete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Mark done",
                        tint = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = reminder.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reminder.dueDate.toFormattedDate(),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun EmptyRemindersCard(
    onAddReminder: () -> Unit
) {
    Card(
        modifier = Modifier
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
    onAddMaintenance: () -> Unit
) {
    Card(
        modifier = Modifier
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
                    imageVector = Icons.Default.MenuBook,
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
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
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
    record: MaintenanceRecord,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val (iconColor, glowColor) = when {
        record.type.name.contains("OIL", ignoreCase = true) ->
            ElectricTeal to ElectricTeal.copy(alpha = 0.3f)

        record.type.name.contains("BRAKE", ignoreCase = true) ->
            ErrorRed to ErrorRed.copy(alpha = 0.3f)

        record.type.name.contains("TIRE", ignoreCase = true) ->
            SuccessGreen to SuccessGreen.copy(alpha = 0.3f)

        record.type.name.contains("INSPECTION", ignoreCase = true) ->
            WarningAmber to WarningAmber.copy(alpha = 0.3f)

        else -> ElectricTeal to ElectricTeal.copy(alpha = 0.3f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp)
    ) {
        // Timeline Line & Dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            // Top Line (connect to previous)
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp)
                    .background(BorderDark)
            )

            // Node Dot
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(MidnightBlack, CircleShape)
                    .border(2.dp, iconColor, CircleShape)
                    .drawBehind {
                        drawCircle(
                            color = glowColor,
                            radius = size.width,
                            alpha = 0.5f
                        )
                    }
            )

            // Bottom Line (connect to next)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(BorderDark)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        // Card content
        Card(
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DeepNavy
            ),
            border = BorderStroke(1.dp, BorderDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    // Maintenance Type Title
                    Text(
                        text = record.type.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    // Description
                    Text(
                        text = record.description,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Date & Mileage Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = " ${record.date.toFormattedDate()}",
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        if (record.mileage > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = " ${String.format("%,d", record.mileage)} km",
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Service Provider
                    if (record.serviceProvider.isNotBlank()) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = " ${record.serviceProvider}",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Cost Badge
                Surface(
                    color = DeepNavy,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$${record.cost.toInt()}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricTeal
                    )
                }
            }
            
            // Next Service Due
            if (record.nextServiceDue > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = ElectricTeal.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Next: ${record.nextServiceDue.toFormattedDate()}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ElectricTeal
                    )
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
    reminder: MaintenanceReminder,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (reminder.priority) {
        ReminderPriority.CRITICAL -> ErrorRed
        ReminderPriority.HIGH -> Color(0xFFFB923C) // Orange
        ReminderPriority.MEDIUM -> Color(0xFFFBBF24) // Amber
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
    
    Card(
        modifier = modifier.width(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Icon + Priority Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            priorityColor.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = priorityColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Priority Badge
                Surface(
                    color = priorityColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = when (reminder.priority) {
                            ReminderPriority.CRITICAL -> "CRITICAL"
                            ReminderPriority.HIGH -> "HIGH"
                            ReminderPriority.MEDIUM -> "MEDIUM"
                            ReminderPriority.LOW -> "LOW"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = priorityColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title
            Text(
                text = reminder.title.ifEmpty { getMaintenanceTypeLabel(reminder.type) },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            // Description
            if (reminder.description.isNotBlank()) {
                Text(
                    text = reminder.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Due Date & Mileage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Date
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isOverdue) {
                                " Overdue by ${-daysRemaining} days"
                            } else {
                                " In $daysRemaining days"
                            },
                            fontSize = 12.sp,
                            color = if (isOverdue) ErrorRed else TextSecondary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                
                // Mileage
                if (reminder.dueMileage > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Speed,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = " ${String.format("%,d", reminder.dueMileage)} km",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Complete Button
            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = priorityColor.copy(alpha = 0.2f),
                    contentColor = priorityColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mark as done",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
