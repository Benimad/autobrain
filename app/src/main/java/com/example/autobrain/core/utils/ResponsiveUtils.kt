package com.example.autobrain.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive Design Utilities for AutoBrain
 * 
 * Provides adaptive sizing and spacing based on screen size
 * Ensures consistent design across all Android devices (phones, tablets, foldables)
 */

/**
 * Screen size categories based on Material Design guidelines
 */
enum class WindowSizeClass {
    COMPACT,    // Phones in portrait (width < 600dp)
    MEDIUM,     // Large phones, small tablets, foldables (600dp <= width < 840dp)
    EXPANDED    // Tablets, foldables in landscape (width >= 840dp)
}

/**
 * Get current window size class based on screen width
 */
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    return when {
        screenWidth < 600 -> WindowSizeClass.COMPACT
        screenWidth < 840 -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}

/**
 * Adaptive spacing system that scales with screen size
 */
object AdaptiveSpacing {
    
    @Composable
    fun extraSmall(): Dp {
        return when (rememberWindowSizeClass()) {
            WindowSizeClass.COMPACT -> 4.dp
            WindowSizeClass.MEDIUM -> 6.dp
            WindowSizeClass.EXPANDED -> 8.dp
        }
    }
    
    @Composable
    fun small(): Dp {
        return when (rememberWindowSizeClass()) {
            WindowSizeClass.COMPACT -> 8.dp
            WindowSizeClass.MEDIUM -> 10.dp
            WindowSizeClass.EXPANDED -> 12.dp
        }
    }
    
    @Composable
    fun medium(): Dp {
        return when (rememberWindowSizeClass()) {
            WindowSizeClass.COMPACT -> 16.dp
            WindowSizeClass.MEDIUM -> 20.dp
            WindowSizeClass.EXPANDED -> 24.dp
        }
    }
    
    @Composable
    fun large(): Dp {
        return when (rememberWindowSizeClass()) {
            WindowSizeClass.COMPACT -> 24.dp
            WindowSizeClass.MEDIUM -> 28.dp
            WindowSizeClass.EXPANDED -> 32.dp
        }
    }
    
    @Composable
    fun extraLarge(): Dp {
        return when (rememberWindowSizeClass()) {
            WindowSizeClass.COMPACT -> 32.dp
            WindowSizeClass.MEDIUM -> 40.dp
            WindowSizeClass.EXPANDED -> 48.dp
        }
    }
}

/**
 * Adaptive content width for better reading experience on large screens
 */
object AdaptiveContentWidth {
    
    /**
     * Maximum width for content to prevent text from being too wide on tablets
     */
    @Composable
    fun maxContentWidth(): Dp {
        return when (rememberWindowSizeClass()) {
            WindowSizeClass.COMPACT -> Dp.Unspecified // Full width on phones
            WindowSizeClass.MEDIUM -> 680.dp
            WindowSizeClass.EXPANDED -> 840.dp
        }
    }
    
    /**
     * Number of columns for grid layouts
     */
    @Composable
    fun gridColumns(): Int {
        return when (rememberWindowSizeClass()) {
            WindowSizeClass.COMPACT -> 1
            WindowSizeClass.MEDIUM -> 2
            WindowSizeClass.EXPANDED -> 3
        }
    }
}

/**
 * Adaptive font scale for different screen sizes
 */
object AdaptiveFontScale {
    
    @Composable
    fun scale(): Float {
        return when (rememberWindowSizeClass()) {
            WindowSizeClass.COMPACT -> 1.0f
            WindowSizeClass.MEDIUM -> 1.1f
            WindowSizeClass.EXPANDED -> 1.2f
        }
    }
}

/**
 * Check if device is in landscape orientation
 */
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > configuration.screenHeightDp
}

/**
 * Check if device is a tablet (screen width >= 600dp)
 */
@Composable
fun isTablet(): Boolean {
    val windowSizeClass = rememberWindowSizeClass()
    return windowSizeClass != WindowSizeClass.COMPACT
}

/**
 * Get adaptive card elevation based on screen size
 */
@Composable
fun adaptiveCardElevation(): Dp {
    return when (rememberWindowSizeClass()) {
        WindowSizeClass.COMPACT -> 2.dp
        WindowSizeClass.MEDIUM -> 3.dp
        WindowSizeClass.EXPANDED -> 4.dp
    }
}

/**
 * Get adaptive icon size based on screen size
 */
@Composable
fun adaptiveIconSize(baseSize: Dp = 24.dp): Dp {
    return when (rememberWindowSizeClass()) {
        WindowSizeClass.COMPACT -> baseSize
        WindowSizeClass.MEDIUM -> baseSize * 1.15f
        WindowSizeClass.EXPANDED -> baseSize * 1.3f
    }
}

/**
 * Get adaptive button height
 */
@Composable
fun adaptiveButtonHeight(): Dp {
    return when (rememberWindowSizeClass()) {
        WindowSizeClass.COMPACT -> 48.dp
        WindowSizeClass.MEDIUM -> 52.dp
        WindowSizeClass.EXPANDED -> 56.dp
    }
}

/**
 * Get adaptive corner radius
 */
@Composable
fun adaptiveCornerRadius(baseRadius: Dp = 16.dp): Dp {
    return when (rememberWindowSizeClass()) {
        WindowSizeClass.COMPACT -> baseRadius
        WindowSizeClass.MEDIUM -> baseRadius * 1.2f
        WindowSizeClass.EXPANDED -> baseRadius * 1.4f
    }
}

/**
 * Get adaptive text size scale
 */
@Composable
fun adaptiveTextScale(): Float {
    return when (rememberWindowSizeClass()) {
        WindowSizeClass.COMPACT -> 1.0f
        WindowSizeClass.MEDIUM -> 1.1f
        WindowSizeClass.EXPANDED -> 1.15f
    }
}

/**
 * Get adaptive image size
 */
@Composable
fun adaptiveImageSize(baseSize: Dp = 100.dp): Dp {
    return when (rememberWindowSizeClass()) {
        WindowSizeClass.COMPACT -> baseSize
        WindowSizeClass.MEDIUM -> baseSize * 1.3f
        WindowSizeClass.EXPANDED -> baseSize * 1.5f
    }
}

/**
 * Get adaptive card padding
 */
@Composable
fun adaptiveCardPadding(): Dp {
    return when (rememberWindowSizeClass()) {
        WindowSizeClass.COMPACT -> 16.dp
        WindowSizeClass.MEDIUM -> 20.dp
        WindowSizeClass.EXPANDED -> 24.dp
    }
}

/**
 * Get adaptive FAB size
 */
@Composable
fun adaptiveFABSize(): Dp {
    return when (rememberWindowSizeClass()) {
        WindowSizeClass.COMPACT -> 56.dp
        WindowSizeClass.MEDIUM -> 64.dp
        WindowSizeClass.EXPANDED -> 72.dp
    }
}

/**
 * Get adaptive minimum touch target
 */
@Composable
fun adaptiveMinTouchTarget(): Dp {
    return when (rememberWindowSizeClass()) {
        WindowSizeClass.COMPACT -> 48.dp
        WindowSizeClass.MEDIUM -> 52.dp
        WindowSizeClass.EXPANDED -> 56.dp
    }
}

/**
 * Check if should use two-column layout
 */
@Composable
fun shouldUseTwoColumns(): Boolean {
    return rememberWindowSizeClass() != WindowSizeClass.COMPACT
}

/**
 * Check if should use three-column layout
 */
@Composable
fun shouldUseThreeColumns(): Boolean {
    return rememberWindowSizeClass() == WindowSizeClass.EXPANDED
}

/**
 * Get optimal list item height
 */
@Composable
fun adaptiveListItemHeight(): Dp {
    return when (rememberWindowSizeClass()) {
        WindowSizeClass.COMPACT -> 72.dp
        WindowSizeClass.MEDIUM -> 80.dp
        WindowSizeClass.EXPANDED -> 88.dp
    }
}
