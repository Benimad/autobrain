package com.example.autobrain.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * AutoBrain Premium Theme
 * AI-Powered Car Evaluation App
 *
 * Design System:
 * - Primary: Dark Mode First (Midnight Black)
 * - Accent: Electric Teal (AI/Innovation feel)
 * - Clean, Premium, Trust-focused
 */

// =============================================================================
// DARK COLOR SCHEME (Primary - Default)
// =============================================================================
private val DarkColorScheme = darkColorScheme(
    // Primary - Electric Teal (AI Accent)
    primary = ElectricTeal,
    onPrimary = TextOnAccent,
    primaryContainer = TealMuted,
    onPrimaryContainer = TealLight,

    // Secondary - Muted Teal
    secondary = TealDark,
    onSecondary = TextOnAccent,
    secondaryContainer = SlateGray,
    onSecondaryContainer = TextPrimary,

    // Tertiary - Success Green
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    tertiaryContainer = SuccessGreenMuted,
    onTertiaryContainer = SuccessGreenLight,

    // Error - Muted Red
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedMuted,
    onErrorContainer = ErrorRedLight,

    // Background & Surface - Midnight Black
    background = MidnightBlack,
    onBackground = TextPrimary,
    surface = MidnightBlack,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = TextSecondary,

    // Containers
    surfaceContainerLowest = SurfaceDimDark,
    surfaceContainerLow = MidnightBlack,
    surfaceContainer = DeepNavy,
    surfaceContainerHigh = DarkNavy,
    surfaceContainerHighest = SlateGray,

    // Outline & Dividers
    outline = BorderDark,
    outlineVariant = DividerDark,

    // Inverse colors
    inverseSurface = SurfaceLight,
    inverseOnSurface = Color(0xFF1A1B1E),
    inversePrimary = TealDark
)

// =============================================================================
// LIGHT COLOR SCHEME (Alternative)
// =============================================================================
private val LightColorScheme = lightColorScheme(
    // Primary - Teal Dark (more contrast on light)
    primary = TealDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2F5EA),
    onPrimaryContainer = TealMuted,

    // Secondary
    secondary = TealMuted,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6FFFA),
    onSecondaryContainer = Color(0xFF0D5751),

    // Tertiary - Success Green
    tertiary = SuccessGreenDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCFCE7),
    onTertiaryContainer = SuccessGreenMuted,

    // Error
    error = ErrorRedDark,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = ErrorRedMuted,

    // Background & Surface - Clean White
    background = SurfaceLight,
    onBackground = Color(0xFF1A1B1E),
    surface = SurfaceElevatedLight,
    onSurface = Color(0xFF1A1B1E),
    surfaceVariant = SurfaceDimLight,
    onSurfaceVariant = Color(0xFF49454F),

    // Containers
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAFAFA),
    surfaceContainer = SurfaceLight,
    surfaceContainerHigh = Color(0xFFEEF0F2),
    surfaceContainerHighest = Color(0xFFE5E7EB),

    // Outline & Dividers
    outline = BorderLight,
    outlineVariant = DividerLight,

    // Inverse colors
    inverseSurface = MidnightBlack,
    inverseOnSurface = TextPrimary,
    inversePrimary = ElectricTeal
)

// =============================================================================
// EXTENDED COLORS - App-specific colors accessible throughout the app
// =============================================================================
data class ExtendedColors(
    val electricTeal: Color = ElectricTeal,
    val tealGlow: Color = TealGlow,
    val success: Color = SuccessGreen,
    val warning: Color = WarningAmber,
    val scoreExcellent: Color = ScoreExcellent,
    val scoreGood: Color = ScoreGood,
    val scoreFair: Color = ScoreFair,
    val scorePoor: Color = ScorePoor,
    val scoreCritical: Color = ScoreCritical,
    val cardBackground: Color = DeepNavy,
    val elevatedSurface: Color = DarkNavy,
    val shimmerBase: Color = ShimmerBase,
    val shimmerHighlight: Color = ShimmerHighlight
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }

// =============================================================================
// THEME COMPOSABLE
// =============================================================================
@Composable
fun AutoBrainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Respect system theme preference for better adaptability
    dynamicColor: Boolean = false, // Can be enabled for Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val extendedColors = if (darkTheme) {
        ExtendedColors(
            cardBackground = DeepNavy,
            elevatedSurface = DarkNavy,
            shimmerBase = ShimmerBase,
            shimmerHighlight = ShimmerHighlight
        )
    } else {
        ExtendedColors(
            cardBackground = SurfaceElevatedLight,
            elevatedSurface = SurfaceLight,
            shimmerBase = Color(0xFFE5E7EB),
            shimmerHighlight = Color(0xFFF3F4F6)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Adaptive status bar and navigation bar colors
            val statusBarColor = if (darkTheme) MidnightBlack else SurfaceLight
            val navBarColor = if (darkTheme) MidnightBlack else SurfaceLight
            
            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = navBarColor.toArgb()
            
            // Set appropriate icon colors for status/nav bars
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AutoBrainTypography,
            content = content
        )
    }
}

// Extension to access extended colors easily
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current
