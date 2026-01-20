package com.example.autobrain.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.autobrain.presentation.screens.aiscore.AIScoreDetailsScreen
import com.example.autobrain.presentation.screens.auth.SignInScreen
import com.example.autobrain.presentation.screens.auth.SignUpScreen
import com.example.autobrain.presentation.screens.auth.CarRegistrationScreen
import com.example.autobrain.presentation.screens.carlog.AddMaintenanceScreen
import com.example.autobrain.presentation.screens.carlog.AddReminderScreen
import com.example.autobrain.presentation.screens.carlog.CarLogScreen
import com.example.autobrain.presentation.screens.carlog.MaintenanceTimelineScreen
import com.example.autobrain.presentation.screens.carlog.UpcomingRemindersScreen
import com.example.autobrain.presentation.screens.carlog.ServiceHistoryScreen
import com.example.autobrain.presentation.screens.diagnostics.AIDiagnosticsScreen
import com.example.autobrain.presentation.screens.diagnostics.SmartAudioDiagnosticScreen
import com.example.autobrain.presentation.screens.diagnostics.VideoDiagnosticsScreen
import com.example.autobrain.presentation.screens.home.HomeScreen
import com.example.autobrain.presentation.screens.onboarding.OnboardingScreen
import com.example.autobrain.presentation.screens.price.EnhancedPriceEstimationScreen
import com.example.autobrain.presentation.screens.profile.ProfileScreen
import com.example.autobrain.presentation.screens.settings.SettingsScreen
import com.example.autobrain.presentation.screens.settings.DataControlsScreen
import com.example.autobrain.presentation.screens.splash.SplashScreen
import com.example.autobrain.presentation.screens.trust.TrustReportScreen

/**
 * AutoBrain Navigation Graph
 * AI-Focused Car Diagnostic & Valuation App
 *
 * Clean architecture - Only AI-focused features:
 * - AI Score
 * - Engine Sound Analysis
 * - Video Diagnostics
 * - AI Price Estimation
 * - Smart Car Logbook
 * - Trust Report
 */
@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // =====================================================================
        // SPLASH & ONBOARDING
        // =====================================================================
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        // =====================================================================
        // AUTHENTICATION
        // =====================================================================
        composable(Screen.SignIn.route) {
            SignInScreen(navController = navController)
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        
        composable(Screen.CarRegistration.route) {
            CarRegistrationScreen(navController = navController)
        }

        composable(Screen.ForgotPassword.route) {
            // TODO: ForgotPasswordScreen
        }

        // =====================================================================
        // MAIN HOME - AI Dashboard
        // =====================================================================
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // =====================================================================
        // AI SCORE
        // =====================================================================
        composable(Screen.AIScore.route) {
            AIScoreDetailsScreen(navController = navController)
        }

        composable(
            route = Screen.AIScoreResult.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            AIScoreDetailsScreen(navController = navController, carId = carId)
        }

        // =====================================================================
        // ENGINE SOUND ANALYSIS
        // =====================================================================
        composable(
            route = Screen.EngineSoundAnalysis.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            SmartAudioDiagnosticScreen(navController = navController, carId = carId)
        }

        composable(
            route = Screen.RecordSound.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            SmartAudioDiagnosticScreen(navController = navController, carId = carId)
        }

        composable(
            route = Screen.SoundAnalysisResult.route,
            arguments = listOf(navArgument("analysisId") { type = NavType.StringType })
        ) { backStackEntry ->
            val analysisId = backStackEntry.arguments?.getString("analysisId") ?: ""
            // Show result in EngineSoundAnalysisScreen or dedicated result screen
            SmartAudioDiagnosticScreen(navController = navController, carId = "")
        }

        // =====================================================================
        // VIDEO ANALYSIS (Smoke/Vibration)
        // =====================================================================
        composable(
            route = Screen.VideoAnalysis.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            VideoDiagnosticsScreen(navController = navController)
        }

        composable(
            route = Screen.RecordVideo.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            VideoDiagnosticsScreen(navController = navController)
        }

        // =====================================================================
        // AI DIAGNOSTICS
        // =====================================================================
        composable(Screen.AIDiagnostics.route) {
            AIDiagnosticsScreen(navController = navController)
        }

        composable(
            route = Screen.DiagnosticResult.route,
            arguments = listOf(navArgument("diagnosticId") { type = NavType.StringType })
        ) { backStackEntry ->
            val diagnosticId = backStackEntry.arguments?.getString("diagnosticId") ?: ""
            // TODO: DiagnosticResultScreen
        }

        // =====================================================================
        // AI PRICE ESTIMATION (Enhanced with Firebase + Gemini + Share)
        // =====================================================================
        composable(Screen.PriceEstimation.route) {
            EnhancedPriceEstimationScreen(navController = navController)
        }

        composable(Screen.PriceEstimationForm.route) {
            EnhancedPriceEstimationScreen(navController = navController)
        }

        composable(
            route = Screen.PriceEstimationResult.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            EnhancedPriceEstimationScreen(navController = navController)
        }

        // =====================================================================
        // SMART CAR LOGBOOK (Carnet Intelligent)
        // =====================================================================
        composable(Screen.CarLogbook.route) {
            CarLogScreen(navController = navController)
        }

        composable(Screen.AddCar.route) {
            // TODO: AddCarScreen
        }

        composable(
            route = Screen.CarDetail.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            // TODO: CarDetailScreen
        }

        composable(Screen.Reminders.route) {
            UpcomingRemindersScreen(navController = navController)
        }

        composable(Screen.MaintenanceTimelineSimple.route) {
            MaintenanceTimelineScreen(navController = navController)
        }

        composable(Screen.ServiceHistory.route) {
            ServiceHistoryScreen(navController = navController)
        }

        composable(
            route = Screen.AddMaintenance.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            AddMaintenanceScreen(navController = navController)
        }

        composable(
            route = Screen.AddReminder.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            AddReminderScreen(navController = navController)
        }

        // =====================================================================
        // TRUST REPORT (Anti-Scam)
        // =====================================================================
        composable(
            route = Screen.TrustReport.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            TrustReportScreen(navController = navController, carId = carId)
        }

        composable(
            route = Screen.FullTrustReport.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            TrustReportScreen(navController = navController, carId = carId)
        }

        // =====================================================================
        // SCAN HISTORY
        // =====================================================================
        composable(Screen.ScanHistory.route) {
            // TODO: ScanHistoryScreen - for now show home
            HomeScreen(navController = navController)
        }

        // =====================================================================
        // PROFILE & SETTINGS
        // =====================================================================
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(Screen.EditProfile.route) {
            // TODO: EditProfileScreen
        }

        composable(Screen.MyCars.route) {
            CarLogScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(Screen.DataControls.route) {
            DataControlsScreen(navController = navController)
        }

        composable(Screen.Help.route) {
            // TODO: HelpScreen
        }

        composable(Screen.About.route) {
            // TODO: AboutScreen
        }

        // =====================================================================
        // AI ASSISTANT CHATBOT
        // =====================================================================
        composable(Screen.AIAssistant.route) {
            com.example.autobrain.presentation.screens.chat.ChatScreen(navController = navController)
        }
    }
}
