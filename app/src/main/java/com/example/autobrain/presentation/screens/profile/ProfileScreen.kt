package com.example.autobrain.presentation.screens.profile

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.autobrain.core.utils.*
import com.example.autobrain.presentation.components.AdaptiveContentContainer
import com.example.autobrain.presentation.navigation.Screen
import com.example.autobrain.presentation.theme.*
import kotlinx.coroutines.launch

// Data classes for Profile
data class UserCar(
    val id: String,
    val make: String,
    val model: String,
    val year: Int,
    val score: Int,
    val scoreStatus: String,
    val imageUrl: String? = null
)

data class ScanHistoryItem(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val dateGroup: String, // "Today", "October 2024", etc.
    val icon: ImageVector,
    val type: String
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var selectedNavIndex by remember { mutableIntStateOf(3) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Profile, 1 = My Cars, 2 = Scan History
    
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    
    // Get user data from ViewModel (Firebase)
    val profileState by viewModel.profileState.collectAsState()
    
    // TODO: Load user's cars from Firebase
    val userCars = remember {
        listOf(
            UserCar("1", "Dacia", "Logan", 2021, 92, "Excellent"),
            UserCar("2", "Renault", "Clio", 2018, 78, "Good"),
            UserCar("3", "Toyota", "Corolla", 2020, 85, "Excellent")
        )
    }
    
    // TODO: Load scan history from Firebase
    val scanHistory = remember {
        listOf(
            ScanHistoryItem("1", "Engine Sound Analysis", "Latest audio diagnostic completed", "Today", "Today", Icons.Outlined.Mic, "sound"),
            ScanHistoryItem("2", "Video Analysis Complete", "Smoke and vibration check", "Yesterday", "This Week", Icons.Outlined.Videocam, "video"),
            ScanHistoryItem("3", "Price Estimation", "Market value calculated", "2 days ago", "This Week", Icons.Outlined.AttachMoney, "price")
        )
    }

    // Sync pager with tab selection
    LaunchedEffect(selectedTab) {
        pagerState.animateScrollToPage(selectedTab)
    }
    
    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
    }

    Scaffold(
        containerColor = MidnightBlack,
        bottomBar = {
            AutoBrainBottomNav(
                selectedIndex = selectedNavIndex,
                onItemSelected = { index ->
                    selectedNavIndex = index
                    when (index) {
                        0 -> navController.navigate(Screen.Home.route)
                        1 -> navController.navigate(Screen.AIDiagnostics.route)
                        2 -> navController.navigate(Screen.CarLogbook.route)
                        3 -> { /* Already on Profile */ }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 2) { // Show FAB only on Scan History
                FloatingActionButton(
                    onClick = { /* Print/Export Report */ },
                    containerColor = ElectricTeal,
                    contentColor = MidnightBlack,
                    shape = CircleShape,
                    modifier = Modifier.size(adaptiveFABSize())
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(adaptiveIconSize(28.dp))
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> ProfileMainContent(
                        profileState = profileState,
                        onMyCarsClick = { 
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        onFavoriteReportsClick = { 
                            scope.launch { pagerState.animateScrollToPage(2) }
                        },
                        onHelpCenterClick = { navController.navigate(Screen.Help.route) },
                        onSettingsClick = { navController.navigate(Screen.Settings.route) },
                        onLogOutClick = {
                            viewModel.signOut {
                                navController.navigate(Screen.SignIn.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    )
                    1 -> MyCarsContent(
                        cars = userCars,
                        onCarClick = { car -> 
                            navController.navigate(Screen.CarDetail.createRoute(car.id))
                        },
                        onViewDashboardClick = { navController.navigate(Screen.Home.route) }
                    )
                    2 -> ScanHistoryContent(
                        history = scanHistory,
                        onItemClick = { /* Navigate to scan detail */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMainContent(
    profileState: ProfileState,
    onMyCarsClick: () -> Unit,
    onFavoriteReportsClick: () -> Unit,
    onHelpCenterClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogOutClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AdaptiveSpacing.large()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(AdaptiveSpacing.extraLarge()))

        when (profileState) {
            is ProfileState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(adaptiveIconSize(48.dp)),
                    color = ElectricTeal
                )
            }
            is ProfileState.Success -> {
                profileState.user.carDetails?.let { carDetails ->
                    if (carDetails.carImageUrl.isNotBlank()) {
                        CarImageSection(
                            imageUrl = carDetails.carImageUrl,
                            carMake = carDetails.make,
                            carModel = carDetails.model,
                            carYear = carDetails.year,
                            viewModel = viewModel
                        )
                    } else {
                        NoCarImagePlaceholder(
                            carMake = carDetails.make,
                            carModel = carDetails.model,
                            carYear = carDetails.year
                        )
                    }
                } ?: run {
                    NoCarDataPlaceholder()
                }

                Spacer(modifier = Modifier.height(AdaptiveSpacing.large()))
            }
            is ProfileState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(adaptiveIconSize(48.dp))
                    )
                    Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))
                    Text(
                        text = profileState.message,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AdaptiveSpacing.large()))

        // My Cars Button (Highlighted)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onMyCarsClick),
            shape = RoundedCornerShape(adaptiveCornerRadius()),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            border = BorderStroke(2.dp, ElectricTeal)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(adaptiveCardPadding()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    tint = ElectricTeal,
                    modifier = Modifier.size(adaptiveIconSize(28.dp))
                )
                Spacer(modifier = Modifier.width(AdaptiveSpacing.medium()))
                Text(
                    text = "My Cars",
                    fontSize = (16.sp.value * adaptiveTextScale()).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ElectricTeal,
                    modifier = Modifier.size(adaptiveIconSize())
                )
            }
        }

        Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))

        // Favorite Reports
        ProfileMenuButton(
            icon = Icons.Outlined.Star,
            text = "Favorite Reports",
            onClick = onFavoriteReportsClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Help Center
        ProfileMenuButton(
            icon = Icons.Outlined.HelpOutline,
            text = "Help Center",
            onClick = onHelpCenterClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Settings
        ProfileMenuButton(
            icon = Icons.Outlined.Settings,
            text = "Settings",
            onClick = onSettingsClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Log Out
        ProfileMenuButton(
            icon = Icons.Outlined.Logout,
            text = "Log Out",
            onClick = onLogOutClick
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MyCarsContent(
    cars: List<UserCar>,
    onCarClick: (UserCar) -> Unit,
    onViewDashboardClick: () -> Unit
) {
    AdaptiveContentContainer {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Text(
                text = "My Cars",
                fontSize = (28.sp.value * adaptiveTextScale()).sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(
                    horizontal = AdaptiveSpacing.large(),
                    vertical = AdaptiveSpacing.medium()
                )
            )
            
            // Subtitle
            Text(
                text = "${cars.size} vehicle${if (cars.size != 1) "s" else ""} registered",
                fontSize = (14.sp.value * adaptiveTextScale()).sp,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = AdaptiveSpacing.large())
            )
            
            Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))
            
            // Cars List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = AdaptiveSpacing.large()),
                verticalArrangement = Arrangement.spacedBy(AdaptiveSpacing.small())
            ) {
                items(cars) { car ->
                    CarListItem(
                        car = car,
                        onClick = { onCarClick(car) }
                    )
                }
            }
            
            // View Dashboard Button
            Button(
                onClick = onViewDashboardClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AdaptiveSpacing.large(),
                        vertical = AdaptiveSpacing.medium()
                    )
                    .height(adaptiveButtonHeight()),
                shape = RoundedCornerShape(adaptiveCornerRadius()),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricTeal,
                    contentColor = MidnightBlack
                )
            ) {
                Text(
                    text = "View Dashboard",
                    fontSize = (16.sp.value * adaptiveTextScale()).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CarListItem(
    car: UserCar,
    onClick: () -> Unit
) {
    val statusColor = when {
        car.score >= 80 -> SuccessGreen
        car.score >= 60 -> WarningAmber
        else -> ErrorRed
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = adaptiveListItemHeight())
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(adaptiveCornerRadius()),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(adaptiveCardPadding()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Car Image placeholder
            Box(
                modifier = Modifier
                    .size(adaptiveImageSize(60.dp))
                    .clip(RoundedCornerShape(adaptiveCornerRadius(8.dp)))
                    .background(SlateGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(adaptiveIconSize(30.dp))
                )
            }
            
            Spacer(modifier = Modifier.width(AdaptiveSpacing.small()))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${car.make} ${car.model} • ${car.year}",
                    fontSize = (16.sp.value * adaptiveTextScale()).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(AdaptiveSpacing.extraSmall()))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI Score: ${car.score}/100",
                        fontSize = (13.sp.value * adaptiveTextScale()).sp,
                        color = statusColor
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(adaptiveIconSize(20.dp))
            )
        }
    }
}

@Composable
private fun ScanHistoryContent(
    history: List<ScanHistoryItem>,
    onItemClick: (ScanHistoryItem) -> Unit
) {
    val groupedHistory = history.groupBy { it.dateGroup }
    
    AdaptiveContentContainer {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Text(
                text = "Scan History",
                fontSize = (28.sp.value * adaptiveTextScale()).sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(
                    horizontal = AdaptiveSpacing.large(),
                    vertical = AdaptiveSpacing.medium()
                )
            )
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = AdaptiveSpacing.large(),
                    vertical = AdaptiveSpacing.small()
                )
            ) {
                groupedHistory.forEach { (group, items) ->
                    item {
                        Text(
                            text = group,
                            fontSize = (14.sp.value * adaptiveTextScale()).sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = AdaptiveSpacing.small())
                        )
                    }
                    
                    items(items) { item ->
                        ScanHistoryListItem(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                        Spacer(modifier = Modifier.height(AdaptiveSpacing.small()))
                    }
                }
            }
            
            // Print Report Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AdaptiveSpacing.large(),
                        vertical = AdaptiveSpacing.medium()
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Print Report",
                    fontSize = (16.sp.value * adaptiveTextScale()).sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ScanHistoryListItem(
    item: ScanHistoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = adaptiveListItemHeight())
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(adaptiveCornerRadius(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(adaptiveCardPadding()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = ElectricTeal,
                modifier = Modifier.size(adaptiveIconSize(20.dp))
            )
            
            Spacer(modifier = Modifier.width(AdaptiveSpacing.small()))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = (15.sp.value * adaptiveTextScale()).sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AdaptiveSpacing.extraSmall()))
                    Text(
                        text = item.description,
                        fontSize = (12.sp.value * adaptiveTextScale()).sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(adaptiveIconSize(20.dp))
            )
        }
    }
}

@Composable
private fun ProfileMenuButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = adaptiveMinTouchTarget())
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(adaptiveCornerRadius()),
        colors = CardDefaults.cardColors(
            containerColor = DeepNavy
        ),
        border = BorderStroke(1.dp, Color(0xFF30363D))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(adaptiveCardPadding()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(adaptiveIconSize())
            )
            Spacer(modifier = Modifier.width(AdaptiveSpacing.medium()))
            Text(
                text = text,
                fontSize = (16.sp.value * adaptiveTextScale()).sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(adaptiveIconSize(20.dp))
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
            BottomNavItem(
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Filled.Home,
                label = "Accueil",
                selected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            BottomNavItem(
                icon = Icons.Outlined.Mic,
                selectedIcon = Icons.Filled.Mic,
                label = "Diagnostics",
                selected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            BottomNavItem(
                icon = Icons.Outlined.EventNote,
                selectedIcon = Icons.Filled.EventNote,
                label = "Carnet",
                selected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
            BottomNavItem(
                icon = Icons.Outlined.Person,
                selectedIcon = Icons.Filled.Person,
                label = "Profil",
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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun CarImageSection(
    imageUrl: String,
    carMake: String,
    carModel: String,
    carYear: Int,
    viewModel: ProfileViewModel
) {
    var currentImageUrl by remember(imageUrl) { mutableStateOf(imageUrl) }
    var imageLoadAttempt by remember(imageUrl) { mutableIntStateOf(0) }
    var imageLoadFailed by remember(imageUrl) { mutableStateOf(false) }
    var isImageLoading by remember(imageUrl) { mutableStateOf(true) }
    
    LaunchedEffect(imageLoadFailed, imageLoadAttempt) {
        if (imageLoadFailed && imageLoadAttempt < 5) {
            kotlinx.coroutines.delay(800)
            imageLoadAttempt++
            val newUrl = viewModel.getFallbackImageUrl(carMake, carModel, carYear, imageLoadAttempt)
            Log.d("CarImageSection", "🔄 Attempting fallback #$imageLoadAttempt: $newUrl")
            currentImageUrl = newUrl
            imageLoadFailed = false
            isImageLoading = true
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AdaptiveSpacing.large()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(adaptiveCornerRadius(24.dp)))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1a1d29),
                            Color(0xFF0f1117)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            GlideImage(
                model = currentImageUrl,
                contentDescription = "$carMake $carModel $carYear",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Fit
            ) {
                it.error(com.example.autobrain.R.drawable.ic_launcher_foreground)
                    .placeholder(com.example.autobrain.R.drawable.ic_launcher_foreground)
                    .addListener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                        override fun onResourceReady(
                            resource: android.graphics.drawable.Drawable,
                            model: Any,
                            target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                            dataSource: com.bumptech.glide.load.DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            isImageLoading = false
                            return false
                        }
                        
                        override fun onLoadFailed(
                            e: com.bumptech.glide.load.engine.GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            imageLoadFailed = true
                            isImageLoading = false
                            return false
                        }
                    })
            }
            
            if (isImageLoading) {
                CircularProgressIndicator(
                    color = ElectricTeal,
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 3.dp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))
        
        Text(
            text = "${carModel.uppercase()} ${carMake.uppercase()} $carYear",
            fontSize = (24.sp.value * adaptiveTextScale()).sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))
        
        Button(
            onClick = { /* Navigate to change vehicle */ },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(adaptiveButtonHeight()),
            shape = RoundedCornerShape(adaptiveCornerRadius()),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricTeal,
                contentColor = MidnightBlack
            )
        ) {
            Text(
                text = "Change Vehicle",
                fontSize = (16.sp.value * adaptiveTextScale()).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NoCarImagePlaceholder(
    carMake: String,
    carModel: String,
    carYear: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AdaptiveSpacing.large()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(adaptiveCornerRadius(24.dp)))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1a1d29),
                            Color(0xFF0f1117)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = ElectricTeal.copy(alpha = 0.3f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = ElectricTeal,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fetching image...",
                    fontSize = (12.sp.value * adaptiveTextScale()).sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(AdaptiveSpacing.medium()))
        
        Text(
            text = "${carModel.uppercase()} ${carMake.uppercase()} $carYear",
            fontSize = (24.sp.value * adaptiveTextScale()).sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun NoCarDataPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AdaptiveSpacing.large()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(adaptiveCornerRadius(24.dp)))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1a1d29),
                            Color(0xFF0f1117)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = ElectricTeal.copy(alpha = 0.3f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Car Added",
                    fontSize = (18.sp.value * adaptiveTextScale()).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add your car in My Cars",
                    fontSize = (13.sp.value * adaptiveTextScale()).sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
