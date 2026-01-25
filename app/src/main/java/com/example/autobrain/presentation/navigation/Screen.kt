package com.example.autobrain.presentation.navigation

/**
 * AutoBrain Navigation Routes
 * AI-Powered Car Evaluation App
 *
 * Clean Architecture - Only AI-focused features
 * No marketplace, no listings, no chat, no maps
 */
sealed class Screen(val route: String) {

    // =========================================================================
    // AUTHENTICATION & ONBOARDING
    // =========================================================================
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object SignIn : Screen("sign_in")
    object SignUp : Screen("sign_up")
    object CarRegistration : Screen("car_registration")
    object ForgotPassword : Screen("forgot_password")

    // =========================================================================
    // MAIN APP - DASHBOARD
    // =========================================================================
    object Home : Screen("home")

    // =========================================================================
    // AI SCORE (Core Feature)
    // =========================================================================
    object AIScore : Screen("ai_score")
    object AIScoreResult : Screen("ai_score_result/{carId}") {
        fun createRoute(carId: String) = "ai_score_result/$carId"
    }

    object ScoreBreakdown : Screen("score_breakdown/{carId}") {
        fun createRoute(carId: String) = "score_breakdown/$carId"
    }

    // =========================================================================
    // ENGINE SOUND ANALYSIS
    // =========================================================================
    object EngineSoundAnalysis : Screen("engine_sound_analysis/{carId}") {
        fun createRoute(carId: String) = "engine_sound_analysis/$carId"
    }
    object RecordSound : Screen("record_sound/{carId}") {
        fun createRoute(carId: String) = "record_sound/$carId"
    }
    object SoundAnalysisResult : Screen("sound_analysis_result/{analysisId}") {
        fun createRoute(analysisId: String) = "sound_analysis_result/$analysisId"
    }

    // =========================================================================
    // VIDEO ANALYSIS (Smoke/Vibration)
    // =========================================================================
    object VideoAnalysis : Screen("video_analysis/{carId}") {
        fun createRoute(carId: String) = "video_analysis/$carId"
    }
    object RecordVideo : Screen("record_video/{carId}") {
        fun createRoute(carId: String) = "record_video/$carId"
    }
    object VideoAnalysisResult : Screen("video_analysis_result/{analysisId}") {
        fun createRoute(analysisId: String) = "video_analysis_result/$analysisId"
    }
    object ComprehensiveVideoReport : Screen("comprehensive_video_report/{diagnosticId}") {
        fun createRoute(diagnosticId: String) = "comprehensive_video_report/$diagnosticId"
    }
    object VideoPlayback : Screen("video_playback/{diagnosticId}") {
        fun createRoute(diagnosticId: String) = "video_playback/$diagnosticId"
    }

    // =========================================================================
    // AI PRICE ESTIMATION
    // =========================================================================
    object PriceEstimation : Screen("price_estimation")
    object PriceEstimationForm : Screen("price_estimation_form")
    object PriceEstimationResult : Screen("price_result/{carId}") {
        fun createRoute(carId: String) = "price_result/$carId"
    }

    // =========================================================================
    // AI DIAGNOSTICS
    // =========================================================================
    object AIDiagnostics : Screen("ai_diagnostics")
    object DiagnosticResult : Screen("diagnostic_result/{diagnosticId}") {
        fun createRoute(diagnosticId: String) = "diagnostic_result/$diagnosticId"
    }

    // =========================================================================
    // SMART CAR LOGBOOK
    // =========================================================================
    object CarLogbook : Screen("car_logbook")
    object AddCar : Screen("add_car")
    object CarDetail : Screen("car_detail/{carId}") {
        fun createRoute(carId: String) = "car_detail/$carId"
    }

    object MaintenanceTimeline : Screen("maintenance_timeline/{carId}") {
        fun createRoute(carId: String) = "maintenance_timeline/$carId"
    }

    object AddMaintenance : Screen("add_maintenance/{carId}") {
        fun createRoute(carId: String) = "add_maintenance/$carId"
    }

    object MaintenanceDetail : Screen("maintenance_detail/{maintenanceId}") {
        fun createRoute(maintenanceId: String) = "maintenance_detail/$maintenanceId"
    }

    object Documents : Screen("documents/{carId}") {
        fun createRoute(carId: String) = "documents/$carId"
    }

    object Reminders : Screen("reminders")
    object AddReminder : Screen("add_reminder/{carId}") {
        fun createRoute(carId: String) = "add_reminder/$carId"
    }

    object MaintenanceTimelineSimple : Screen("maintenance_timeline_simple")
    object ServiceHistory : Screen("service_history")

    // =========================================================================
    // TRUST REPORT (Anti-Scam)
    // =========================================================================
    object TrustReport : Screen("trust_report/{carId}") {
        fun createRoute(carId: String) = "trust_report/$carId"
    }

    object FullTrustReport : Screen("full_trust_report/{carId}") {
        fun createRoute(carId: String) = "full_trust_report/$carId"
    }

    // =========================================================================
    // SCAN HISTORY
    // =========================================================================
    object ScanHistory : Screen("scan_history")
    object ScanDetail : Screen("scan_detail/{scanId}") {
        fun createRoute(scanId: String) = "scan_detail/$scanId"
    }

    // =========================================================================
    // PROFILE & SETTINGS
    // =========================================================================
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object MyCars : Screen("my_cars")
    object Settings : Screen("settings")
    object DataControls : Screen("data_controls")
    object Help : Screen("help")
    object About : Screen("about")

    // =========================================================================
    // AI ASSISTANT CHATBOT
    // =========================================================================
    object AIAssistant : Screen("ai_assistant")
}

/**
 * Bottom Navigation Items - 4 core features for AutoBrain
 * Clean, dark-themed navigation with Electric Teal accents
 */
enum class BottomNavItem(
    val route: String,
    val title: String,
    val index: Int
) {
    HOME("home", "Home", 0),
    DIAGNOSTICS("ai_diagnostics", "Diagnostics", 1),
    LOGBOOK("car_logbook", "Logbook", 2),
    PROFILE("profile", "Profile", 3)
}
