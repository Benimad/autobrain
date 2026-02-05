package com.example.autobrain.presentation.screens.carlog

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.OilBarrel
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TireRepair
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autobrain.core.utils.toCurrency
import com.example.autobrain.core.utils.toFormattedDate
import com.example.autobrain.presentation.navigation.Screen
import com.example.autobrain.presentation.theme.ElectricTeal
import com.example.autobrain.presentation.theme.MidnightBlack
import com.example.autobrain.presentation.theme.DeepNavy
import com.example.autobrain.presentation.theme.TextPrimary
import com.example.autobrain.presentation.theme.TextSecondary
import com.example.autobrain.presentation.theme.TextMuted
import com.example.autobrain.presentation.theme.WarningAmber
import com.example.autobrain.presentation.theme.SlateGray
import com.example.autobrain.presentation.theme.ErrorRed
import androidx.compose.material.icons.automirrored.outlined.EventNote
import com.example.autobrain.domain.model.CarLog
import com.example.autobrain.domain.model.MaintenanceReminder

/**
 * Maintenance Timeline Screen
 * 🔥 NOW FULLY DYNAMIC:
 * - Firebase + Room database integration
 * - Real maintenance history timeline
 * - Gemini 3 Flash AI insights
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceTimelineScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CarLogViewModel = hiltViewModel()
) {
    // 🔥 DYNAMIC DATA from Firebase + Room
    val carLogState by viewModel.carLogState.collectAsState()
    val remindersState by viewModel.remindersState.collectAsState()
    val aiAnalysisState by viewModel.aiAnalysisState.collectAsState()
    
    // Get real maintenance records
    val maintenanceRecords = when (carLogState) {
        is CarLogState.Success -> (carLogState as CarLogState.Success).carLog.maintenanceRecords
            .sortedByDescending { it.date }
        else -> emptyList()
    }
    
    val upcomingRemindersCount = when (remindersState) {
        is RemindersState.Success -> (remindersState as RemindersState.Success).reminders.size
        else -> 0
    }
    
    Scaffold(
        modifier = modifier,
        containerColor = MidnightBlack,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Maintenance Timeline",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ElectricTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightBlack
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController, selectedIndex = 0)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddMaintenance.createRoute("default")) },
                containerColor = ElectricTeal,
                contentColor = MidnightBlack,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Maintenance")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Screen.Reminders.route) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ElectricTeal
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Upcoming Reminders: $upcomingRemindersCount",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightBlack
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ReminderIcon(icon = Icons.Default.Build, tint = MidnightBlack)
                        ReminderIcon(icon = Icons.Default.CarRepair, tint = MidnightBlack)
                        ReminderIcon(icon = Icons.Default.DirectionsCar, tint = MidnightBlack)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // 🔥 DYNAMIC TIMELINE from real data
            if (maintenanceRecords.isNotEmpty()) {
                maintenanceRecords.forEachIndexed { index, record ->
                    TimelineItem(
                        icon = getMaintenanceTypeIcon(record.type),
                        title = record.description,
                        subtitle = "${record.mileage} km - ${record.cost.toCurrency()}",
                        date = record.date.toFormattedDate(),
                        isCompleted = true,
                        showLine = index < maintenanceRecords.size - 1
                    )

                    if (index < maintenanceRecords.size - 1) {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            } else {
                // Fallback to sample data if no records
                TimelineItem(
                    icon = Icons.Outlined.CheckCircle,
                    title = "No maintenance records yet",
                    subtitle = "Add your first maintenance",
                    date = "Start today",
                    isCompleted = false,
                    showLine = false,
                    onClick = { 
                        navController.navigate(Screen.AddMaintenance.createRoute("default")) 
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // ⭐ Gemini AI Insights Panel
            if (aiAnalysisState is AIAnalysisState.Success) {
                val analysis = (aiAnalysisState as AIAnalysisState.Success).analysis
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepNavy)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricTeal
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "AI Analysis Score: ${analysis.overallScore}/100",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricTeal
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = analysis.maintenanceQuality,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Trigger AI analysis on screen load
            LaunchedEffect(Unit) {
                if (maintenanceRecords.isNotEmpty() && aiAnalysisState is AIAnalysisState.Idle) {
                    viewModel.performComprehensiveAIAnalysis()
                }
            }
        }
    }
}

@Composable
private fun ReminderIcon(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .background(
                color = Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = tint.copy(alpha = 0.3f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun TimelineItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    date: String,
    isCompleted: Boolean,
    showLine: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isCompleted) ElectricTeal else DeepNavy,
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = if (isCompleted) ElectricTeal else SlateGray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MidnightBlack,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ElectricTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (showLine) {
                Canvas(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                ) {
                    val pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(10f, 10f),
                        phase = 0f
                    )
                    
                    drawLine(
                        color = ElectricTeal,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = pathEffect
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(
                    if (onClick != null) Modifier.clickable { onClick() }
                    else Modifier
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DeepNavy
            ),
            border = BorderStroke(1.dp, Color(0xFF30363D))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = ElectricTeal,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showLine) {
        Spacer(modifier = Modifier.height(0.dp))
    }

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.width(76.dp))
        Text(
            text = date,
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingRemindersScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CarLogViewModel = hiltViewModel()
) {
    val remindersState by viewModel.remindersState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val carId = currentUser?.uid ?: "default"
    
    Box(modifier = Modifier.fillMaxSize().background(MidnightBlack)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(ElectricTeal.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.2f),
                    radius = size.width * 0.6f
                )
            )
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(WarningAmber.copy(alpha = 0.04f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.8f),
                    radius = size.width * 0.5f
                )
            )
        }
        
        Scaffold(
            modifier = modifier,
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Upcoming Reminders",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = TextPrimary,
                                letterSpacing = (-0.5).sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(ElectricTeal, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Stay on top of maintenance",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ElectricTeal,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { 
                                navController.navigate(Screen.AddReminder.createRoute(carId))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Reminder",
                                tint = ElectricTeal,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { 
                        navController.navigate(Screen.AddReminder.createRoute(carId))
                    },
                    containerColor = ElectricTeal,
                    contentColor = MidnightBlack,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Reminder")
                }
            }
        ) { paddingValues ->
            when (remindersState) {
                is RemindersState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = ElectricTeal)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading reminders...", color = TextSecondary)
                        }
                    }
                }
                
                is RemindersState.Success -> {
                    val reminders = (remindersState as RemindersState.Success).reminders
                    
                    if (reminders.isEmpty()) {
                        PremiumEmptyRemindersState(
                            onAddReminder = {
                                navController.navigate(Screen.AddReminder.createRoute(carId))
                            },
                            modifier = Modifier.padding(paddingValues)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 16.dp,
                                bottom = 100.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(reminders) { reminder ->
                                PremiumReminderCard(
                                    reminder = reminder,
                                    onComplete = { viewModel.markReminderCompleted(reminder.id) },
                                    onClick = { /* Navigate to detail if needed */ }
                                )
                            }
                        }
                    }
                }
                
                is RemindersState.Empty -> {
                    PremiumEmptyRemindersState(
                        onAddReminder = {
                            navController.navigate(Screen.AddReminder.createRoute(carId))
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                
                is RemindersState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = (remindersState as RemindersState.Error).message,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceHistoryScreen(
    navController: NavController
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MidnightBlack,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Service History",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ElectricTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightBlack
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController, selectedIndex = 1)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddMaintenance.createRoute("default")) },
                containerColor = ElectricTeal,
                contentColor = MidnightBlack,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Maintenance")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TabButton(
                    text = "Scheduled",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Completed",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    ServiceHistoryItem(
                        icon = Icons.Default.CalendarMonth,
                        title = "Upcoming: Brake Pad",
                        subtitle = "Replacement - July 2024",
                        iconColor = ElectricTeal,
                        iconBackground = ElectricTeal.copy(alpha = 0.1f)
                    )
                } else {
                    ServiceHistoryItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Completed: Annual Service -",
                        subtitle = "July 2024",
                        iconColor = ElectricTeal,
                        iconBackground = ElectricTeal.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ServiceHistoryItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Completed: Annual Service -",
                        subtitle = "June 2023",
                        iconColor = ElectricTeal,
                        iconBackground = ElectricTeal.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ServiceHistoryItem(
                        icon = Icons.Default.Warning,
                        title = "Pending: Transmission Fluid Flush",
                        subtitle = "Overdue.",
                        iconColor = WarningAmber,
                        iconBackground = WarningAmber.copy(alpha = 0.1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
                Button(
                    onClick = onClick,
                    modifier = modifier.height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) ElectricTeal else DeepNavy,
                        contentColor = if (selected) MidnightBlack else TextPrimary
                    )
                ) {
                    Text(
                        text = text,
                        fontSize = 16.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
}

@Composable
private fun ServiceHistoryItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    iconBackground: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        ),
        border = BorderStroke(1.dp, Color(0xFF30363D))
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
                    .background(
                        color = iconBackground,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PremiumReminderCard(
    reminder: com.example.autobrain.domain.model.MaintenanceReminder,
    onComplete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (reminder.priority) {
        com.example.autobrain.domain.model.ReminderPriority.CRITICAL -> com.example.autobrain.presentation.theme.ErrorRed
        com.example.autobrain.domain.model.ReminderPriority.HIGH -> Color(0xFFFB923C)
        com.example.autobrain.domain.model.ReminderPriority.MEDIUM -> WarningAmber
        com.example.autobrain.domain.model.ReminderPriority.LOW -> ElectricTeal
    }
    
    val typeIcon = when (reminder.type) {
        com.example.autobrain.domain.model.MaintenanceType.OIL_CHANGE -> Icons.Outlined.OilBarrel
        com.example.autobrain.domain.model.MaintenanceType.BRAKE_SERVICE -> Icons.Outlined.Build
        com.example.autobrain.domain.model.MaintenanceType.TIRE_ROTATION -> Icons.Outlined.TireRepair
        com.example.autobrain.domain.model.MaintenanceType.ENGINE_TUNE_UP -> Icons.Outlined.Settings
        com.example.autobrain.domain.model.MaintenanceType.BATTERY_REPLACEMENT -> Icons.Default.CarRepair
        else -> Icons.Default.Build
    }
    
    val daysRemaining = ((reminder.dueDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
    val isOverdue = daysRemaining < 0
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            DeepNavy.copy(alpha = 0.9f),
                            DeepNavy.copy(alpha = 0.7f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            priorityColor.copy(alpha = 0.5f),
                            priorityColor.copy(alpha = 0.2f)
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
                            .size(60.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        priorityColor.copy(alpha = 0.3f),
                                        priorityColor.copy(alpha = 0.15f)
                                    )
                                ),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .border(
                                1.5.dp,
                                priorityColor.copy(alpha = 0.5f),
                                RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = priorityColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Surface(
                        color = priorityColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = when (reminder.priority) {
                                com.example.autobrain.domain.model.ReminderPriority.CRITICAL -> "CRITICAL"
                                com.example.autobrain.domain.model.ReminderPriority.HIGH -> "HIGH"
                                com.example.autobrain.domain.model.ReminderPriority.MEDIUM -> "MEDIUM"
                                com.example.autobrain.domain.model.ReminderPriority.LOW -> "LOW"
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = priorityColor,
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = reminder.title.ifEmpty { getMaintenanceTypeLabel(reminder.type) },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                
                if (reminder.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = reminder.description,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isOverdue) 
                                    com.example.autobrain.presentation.theme.ErrorRed.copy(alpha = 0.15f) 
                                else 
                                    ElectricTeal.copy(alpha = 0.12f),
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                if (isOverdue) 
                                    com.example.autobrain.presentation.theme.ErrorRed.copy(alpha = 0.4f) 
                                else 
                                    ElectricTeal.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = if (isOverdue) 
                                        com.example.autobrain.presentation.theme.ErrorRed 
                                    else 
                                        ElectricTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOverdue) "Overdue" else "Due in",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isOverdue) "${-daysRemaining}d ago" else "${daysRemaining}d",
                                fontSize = 18.sp,
                                color = if (isOverdue) 
                                    com.example.autobrain.presentation.theme.ErrorRed 
                                else 
                                    TextPrimary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    
                    if (reminder.dueMileage > 0) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    priorityColor.copy(alpha = 0.12f),
                                    RoundedCornerShape(16.dp)
                                )
                                .border(
                                    1.dp,
                                    priorityColor.copy(alpha = 0.3f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = priorityColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Mileage",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${String.format("%,d", reminder.dueMileage)}km",
                                    fontSize = 16.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1
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
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        priorityColor.copy(alpha = 0.3f),
                                        priorityColor.copy(alpha = 0.2f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                priorityColor.copy(alpha = 0.6f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = priorityColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Mark as Complete",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
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

@Composable
private fun PremiumEmptyRemindersState(
    onAddReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            ElectricTeal.copy(alpha = 0.15f),
                            ElectricTeal.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
            }
            
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                DeepNavy,
                                Color(0xFF1A2332)
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(
                        width = 3.dp,
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                ElectricTeal.copy(alpha = 0.6f),
                                ElectricTeal.copy(alpha = 0.3f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = ElectricTeal,
                    modifier = Modifier.size(70.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "No Reminders Yet",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = (-0.5).sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Keep your car in top shape!\nSet maintenance reminders to never miss a service date.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = onAddReminder,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                ElectricTeal,
                                Color(0xFF00BFA5)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MidnightBlack,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Add First Reminder",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = MidnightBlack,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavController,
    selectedIndex: Int
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
            BottomNavItem(
                icon = Icons.Outlined.Home,
                label = "Home",
                selected = selectedIndex == 0,
                onClick = { navController.navigate(Screen.Home.route) }
            )
            BottomNavItem(
                icon = Icons.Outlined.Mic,
                label = "Diagnostics",
                selected = selectedIndex == 1,
                onClick = { navController.navigate(Screen.AIDiagnostics.route) }
            )
            BottomNavItem(
                icon = Icons.AutoMirrored.Outlined.EventNote,
                label = "Logbook",
                selected = selectedIndex == 2,
                onClick = { /* Already on Logbook */ }
            )
            BottomNavItem(
                icon = Icons.Outlined.Person,
                label = "Profile",
                selected = selectedIndex == 3,
                onClick = { navController.navigate(Screen.Profile.route) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) ElectricTeal else TextMuted,
            modifier = Modifier.size(24.dp)
        )
        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (selected) ElectricTeal else TextMuted
            )
        }
    }
}
