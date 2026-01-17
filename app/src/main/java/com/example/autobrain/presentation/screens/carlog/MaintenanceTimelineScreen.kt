package com.example.autobrain.presentation.screens.carlog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.autobrain.presentation.navigation.Screen
import com.example.autobrain.presentation.theme.*
import com.example.autobrain.core.utils.toFormattedDate
import com.example.autobrain.core.utils.toCurrency

/**
 * Maintenance Timeline Screen
 * 🔥 NOW FULLY DYNAMIC:
 * - Firebase + Room database integration
 * - Real maintenance history timeline
 * - Gemini 2.0 Flash AI insights
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceTimelineScreen(
    navController: NavController,
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
                            imageVector = Icons.Default.ArrowBack,
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
                        ReminderIcon(Icons.Default.Build, MidnightBlack)
                        ReminderIcon(Icons.Default.CarRepair, MidnightBlack)
                        ReminderIcon(Icons.Default.DirectionsCar, MidnightBlack)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // 🔥 DYNAMIC TIMELINE from real data
            if (maintenanceRecords.isNotEmpty()) {
                maintenanceRecords.forEachIndexed { index, record ->
                    TimelineItem(
                        icon = when (record.type) {
                            com.example.autobrain.domain.model.MaintenanceType.OIL_CHANGE -> Icons.Outlined.OilBarrel
                            com.example.autobrain.domain.model.MaintenanceType.BRAKE_SERVICE -> Icons.Outlined.Build
                            com.example.autobrain.domain.model.MaintenanceType.TIRE_ROTATION -> Icons.Outlined.TireRepair
                            com.example.autobrain.domain.model.MaintenanceType.GENERAL_INSPECTION -> Icons.Outlined.CheckCircle
                            else -> Icons.Outlined.Settings
                        },
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
                    showLine = false
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
    tint: Color
) {
    Box(
        modifier = Modifier
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
    showLine: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                .weight(1f),
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
    navController: NavController
) {
    Scaffold(
        containerColor = MidnightBlack,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Upcoming Remiboars",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Tikter values its inasanor and cases it or oior plar-rt\nebore fekrergi rlemiroe Heere ae tow tongibans.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.weight(0.3f))

            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            color = Color(0xFF0F2838),
                            shape = CircleShape
                        )
                        .border(
                            width = 3.dp,
                            color = ElectricTeal,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Wrench",
                        tint = ElectricTeal,
                        modifier = Modifier.size(90.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Oil Change due in 1,000 km",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "3,000 km",
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricTeal,
                    contentColor = MidnightBlack
                )
            ) {
                Text(
                    text = "Schedule Service",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
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
                            imageVector = Icons.Default.ArrowBack,
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
                label = "Accueil",
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
                icon = Icons.Outlined.EventNote,
                label = "Carnet",
                selected = selectedIndex == 2,
                onClick = { /* Already on Carnet */ }
            )
            BottomNavItem(
                icon = Icons.Outlined.Person,
                label = "Profil",
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
