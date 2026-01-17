package com.example.autobrain.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * AutoBrain Premium Color Palette
 * AI-Powered Car Evaluation App
 *
 * Design Philosophy:
 * - Trust & Technology: Deep blues and blacks
 * - AI Accent: Electric teal/cyan
 * - Clean & Premium feel
 * - Accessibility compliant
 */

// =============================================================================
// PRIMARY COLORS - Midnight Black & Deep Navy (Trust & Technology)
// =============================================================================
val MidnightBlack = Color(0xFF0D1117)          // Primary background
val DeepNavy = Color(0xFF151B23)               // Card backgrounds
val DarkNavy = Color(0xFF1C2128)               // Elevated surfaces
val SlateGray = Color(0xFF21262D)              // Input fields, borders

// =============================================================================
// AI ACCENT COLORS - Electric Teal (Innovation & Intelligence)
// =============================================================================
val ElectricTeal = Color(0xFF2DD4BF)           // Primary accent - buttons, highlights
val TealDark = Color(0xFF14B8A6)               // Pressed states
val TealLight = Color(0xFF5EEAD4)              // Hover states
val TealMuted = Color(0xFF0D9488)              // Secondary accent
val TealGlow = Color(0x332DD4BF)               // Glow effects (20% opacity)

// =============================================================================
// SUCCESS COLORS - Soft Green (Positive Results)
// =============================================================================
val SuccessGreen = Color(0xFF22C55E)           // Success states
val SuccessGreenLight = Color(0xFF4ADE80)      // Light success
val SuccessGreenDark = Color(0xFF16A34A)       // Dark success
val SuccessGreenMuted = Color(0xFF15803D)      // Muted success background

// =============================================================================
// WARNING COLORS - Amber (Caution)
// =============================================================================
val WarningAmber = Color(0xFFF59E0B)           // Warning states
val WarningAmberLight = Color(0xFFFBBF24)      // Light warning
val WarningAmberDark = Color(0xFFD97706)       // Dark warning
val WarningAmberMuted = Color(0xFFB45309)      // Muted warning background

// =============================================================================
// ERROR COLORS - Muted Red (Not Aggressive)
// =============================================================================
val ErrorRed = Color(0xFFEF4444)               // Error states
val ErrorRedLight = Color(0xFFF87171)          // Light error
val ErrorRedDark = Color(0xFFDC2626)           // Dark error
val ErrorRedMuted = Color(0xFFB91C1C)          // Muted error background

// =============================================================================
// TEXT COLORS
// =============================================================================
val TextPrimary = Color(0xFFE6EDF3)            // Primary text (high contrast)
val TextSecondary = Color(0xFF8B949E)          // Secondary text
val TextMuted = Color(0xFF6E7681)              // Muted/disabled text
val TextOnAccent = Color(0xFF0D1117)           // Text on teal buttons

// =============================================================================
// SURFACE & BACKGROUND COLORS
// =============================================================================
// Dark Mode (Primary)
val SurfaceDark = Color(0xFF0D1117)            // Main background
val SurfaceElevatedDark = Color(0xFF161B22)    // Elevated cards
val SurfaceCardDark = Color(0xFF21262D)        // Card background
val SurfaceDimDark = Color(0xFF010409)         // Dimmed background

// Light Mode
val SurfaceLight = Color(0xFFF6F8FA)           // Main background
val SurfaceElevatedLight = Color(0xFFFFFFFF)   // Elevated cards
val SurfaceCardLight = Color(0xFFFFFFFF)       // Card background
val SurfaceDimLight = Color(0xFFEAEEF2)        // Dimmed background

// =============================================================================
// BORDER & DIVIDER COLORS
// =============================================================================
val BorderDark = Color(0xFF30363D)             // Dark mode borders
val BorderLight = Color(0xFFD0D7DE)            // Light mode borders
val DividerDark = Color(0xFF21262D)            // Dark mode dividers
val DividerLight = Color(0xFFD8DEE4)           // Light mode dividers

// =============================================================================
// AI SCORE COLORS - Gradient from Red to Green
// =============================================================================
val ScoreExcellent = Color(0xFF22C55E)         // 80-100: Excellent
val ScoreGood = Color(0xFF84CC16)              // 60-79: Good  
val ScoreFair = Color(0xFFF59E0B)              // 40-59: Fair
val ScorePoor = Color(0xFFF97316)              // 20-39: Poor
val ScoreCritical = Color(0xFFEF4444)          // 0-19: Critical

// =============================================================================
// CONFIDENCE LEVEL COLORS
// =============================================================================
val ConfidenceHigh = Color(0xFF22C55E)         // High confidence
val ConfidenceMedium = Color(0xFFF59E0B)       // Medium confidence
val ConfidenceLow = Color(0xFFEF4444)          // Low confidence

// =============================================================================
// SEVERITY LEVEL COLORS (For Diagnostics)
// =============================================================================
val SeverityLow = Color(0xFF22C55E)            // Low severity - green
val SeverityMedium = Color(0xFFF59E0B)         // Medium severity - amber
val SeverityHigh = Color(0xFFF97316)           // High severity - orange
val SeverityCritical = Color(0xFFEF4444)       // Critical - red

// =============================================================================
// GRADIENT BRUSHES - Premium UI Effects
// =============================================================================

// Main background gradient (subtle)
val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        MidnightBlack,
        DeepNavy
    )
)

// Card gradient (for premium cards)
val CardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF161B22),
        Color(0xFF0D1117)
    )
)

// AI Score circle gradient
val AIScoreGradient = Brush.sweepGradient(
    colors = listOf(
        ElectricTeal,
        TealLight,
        ElectricTeal
    )
)

// Button gradient
val ButtonGradient = Brush.horizontalGradient(
    colors = listOf(
        ElectricTeal,
        TealDark
    )
)

// Success gradient
val SuccessGradient = Brush.horizontalGradient(
    colors = listOf(
        SuccessGreen,
        SuccessGreenLight
    )
)

// Glow effect for AI elements
val AIGlowGradient = Brush.radialGradient(
    colors = listOf(
        TealGlow,
        Color.Transparent
    )
)

// Score ring gradient based on score value
fun getScoreGradient(score: Int): Brush = Brush.sweepGradient(
    colors = when {
        score >= 80 -> listOf(SuccessGreen, SuccessGreenLight, SuccessGreen)
        score >= 60 -> listOf(ScoreGood, Color(0xFFA3E635), ScoreGood)
        score >= 40 -> listOf(WarningAmber, WarningAmberLight, WarningAmber)
        score >= 20 -> listOf(ScorePoor, Color(0xFFFB923C), ScorePoor)
        else -> listOf(ErrorRed, ErrorRedLight, ErrorRed)
    }
)

// =============================================================================
// BOTTOM NAVIGATION COLORS
// =============================================================================
val BottomNavBackground = Color(0xFF0D1117)
val BottomNavSelected = ElectricTeal
val BottomNavUnselected = Color(0xFF6E7681)

// =============================================================================
// SHIMMER/LOADING COLORS
// =============================================================================
val ShimmerBase = Color(0xFF21262D)
val ShimmerHighlight = Color(0xFF30363D)

// =============================================================================
// HELPER FUNCTIONS
// =============================================================================

/**
 * Get color for AI Score value
 */
fun getScoreColor(score: Int): Color = when {
    score >= 80 -> ScoreExcellent
    score >= 60 -> ScoreGood
    score >= 40 -> ScoreFair
    score >= 20 -> ScorePoor
    else -> ScoreCritical
}

/**
 * Get color for confidence level
 */
fun getConfidenceColor(confidence: Float): Color = when {
    confidence >= 0.8f -> ConfidenceHigh
    confidence >= 0.5f -> ConfidenceMedium
    else -> ConfidenceLow
}

/**
 * Get color for severity level
 */
fun getSeverityColor(severity: String): Color = when (severity.lowercase()) {
    "low" -> SeverityLow
    "medium" -> SeverityMedium
    "high" -> SeverityHigh
    "critical" -> SeverityCritical
    else -> TextSecondary
}

// =============================================================================
// COLOR ALIASES - For backwards compatibility with existing UI code
// =============================================================================
val PrimaryBlue = ElectricTeal
val SecondaryRed = ErrorRed
val WarningOrange = ScorePoor
val BackgroundLight = SurfaceLight
val AccentBlue = TealLight
val OnlineIndicator = SuccessGreen
val RatingStarColor = WarningAmber
val ChipBackground = SlateGray
val SenderBubble = DarkNavy
val ReceiverBubble = DeepNavy
val ProgressTrack = SlateGray
val HeroCardGradient = CardGradient
val PremiumBlueGradient = ButtonGradient
val StatCardBattery = SuccessGreen
val StatCardDistance = ElectricTeal
val StatCardWeather = WarningAmber
val BatteryFull = SuccessGreen
val BatteryMedium = WarningAmber
val BatteryLow = ErrorRed
