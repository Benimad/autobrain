package com.example.autobrain.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.autobrain.core.utils.AdaptiveContentWidth
import com.example.autobrain.core.utils.AdaptiveSpacing
import com.example.autobrain.core.utils.rememberWindowSizeClass
import com.example.autobrain.core.utils.WindowSizeClass

/**
 * Adaptive Scaffold for AutoBrain
 * 
 * Provides consistent layout across all screen sizes:
 * - Proper padding and spacing
 * - Centered content on large screens
 * - Responsive top bar
 * - Adaptive bottom navigation/FAB placement
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveScaffold(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.background,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    scrollable: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    val windowSizeClass = rememberWindowSizeClass()
    
    Scaffold(
        modifier = modifier,
        topBar = {
            AdaptiveTopAppBar(
                title = title,
                showBackButton = showBackButton,
                onBackClick = onBackClick,
                actions = actions
            )
        },
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        containerColor = containerColor,
        contentWindowInsets = contentWindowInsets
    ) { paddingValues ->
        if (windowSizeClass == WindowSizeClass.COMPACT) {
            // Phone: Full width content
            if (scrollable) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    content(PaddingValues(0.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    content(PaddingValues(0.dp))
                }
            }
        } else {
            // Tablet/Large screen: Centered content with max width
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.TopCenter
            ) {
                val maxWidth = AdaptiveContentWidth.maxContentWidth()
                
                if (scrollable) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = maxWidth)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = AdaptiveSpacing.medium())
                    ) {
                        content(PaddingValues(0.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .widthIn(max = maxWidth)
                            .fillMaxWidth()
                            .padding(horizontal = AdaptiveSpacing.medium())
                    ) {
                        content(PaddingValues(0.dp))
                    }
                }
            }
        }
    }
}

/**
 * Adaptive Top App Bar with consistent styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    navigationIcon: ImageVector? = if (showBackButton) Icons.AutoMirrored.Filled.ArrowBack else null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Navigate back"
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Adaptive Content Container
 * Ensures content is properly centered on large screens
 */
@Composable
fun AdaptiveContentContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val windowSizeClass = rememberWindowSizeClass()
    
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (windowSizeClass != WindowSizeClass.COMPACT) {
                        Modifier
                            .widthIn(max = AdaptiveContentWidth.maxContentWidth())
                            .padding(horizontal = AdaptiveSpacing.medium())
                    } else {
                        Modifier
                    }
                )
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

/**
 * Adaptive Card
 * Card with responsive elevation and padding
 */
@Composable
fun AdaptiveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
    } else {
        modifier.fillMaxWidth()
    }
    
    Card(
        modifier = cardModifier,
        onClick = onClick ?: {},
        enabled = onClick != null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = when (rememberWindowSizeClass()) {
                WindowSizeClass.COMPACT -> 2.dp
                WindowSizeClass.MEDIUM -> 3.dp
                WindowSizeClass.EXPANDED -> 4.dp
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(AdaptiveSpacing.medium()),
            content = content
        )
    }
}

/**
 * Adaptive Grid Layout
 * Automatically adjusts number of columns based on screen size
 */
@Composable
fun <T> AdaptiveGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    verticalSpacing: Dp? = null,
    horizontalSpacing: Dp? = null,
    itemContent: @Composable BoxScope.(item: T) -> Unit
) {
    val columns = AdaptiveContentWidth.gridColumns()
    val vSpacing = verticalSpacing ?: AdaptiveSpacing.medium()
    val hSpacing = horizontalSpacing ?: AdaptiveSpacing.medium()
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(vSpacing)
    ) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(hSpacing)
            ) {
                rowItems.forEach { item ->
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        itemContent(item)
                    }
                }
                // Fill remaining space if row is not complete
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
