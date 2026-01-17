package com.example.autobrain.data.ai

import com.example.autobrain.data.local.entity.AudioDiagnosticData
import com.example.autobrain.data.local.entity.VideoDiagnosticData
import com.example.autobrain.domain.model.CarDetails
import com.example.autobrain.domain.model.CarLog
import com.example.autobrain.domain.model.MaintenanceType
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * AUTOBRAIN - SHARED ANALYSIS HELPER FUNCTIONS
 * 
 * Common utilities used across all three diagnostic systems:
 * - Audio comprehensive analysis
 * - Video comprehensive analysis
 * - Ultimate smart analysis
 * 
 * Centralized to avoid code duplication
 */

// =============================================================================
// DATE & TIME HELPERS
// =============================================================================

/**
 * Format timestamp to readable French date
 */
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH)
    return sdf.format(Date(timestamp))
}

/**
 * Format timestamp to short date only
 */
fun formatDateShort(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
    return sdf.format(Date(timestamp))
}

/**
 * Calculate days since timestamp
 */
fun getDaysSince(timestamp: Long): Long {
    return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - timestamp)
}

/**
 * Calculate hours since timestamp
 */
fun getHoursSince(timestamp: Long): Long {
    return TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - timestamp)
}

// =============================================================================
// MAINTENANCE ANALYSIS HELPERS
// =============================================================================

/**
 * Build detailed carnet (maintenance log) analysis
 */
fun buildDetailedCarnetAnalysis(carLog: CarLog): String {
    val recentMaintenanceText = if (carLog.maintenanceRecords.isEmpty()) {
        "  Aucun entretien enregistré ⚠️"
    } else {
        carLog.maintenanceRecords.sortedByDescending { it.date }.take(5).joinToString("\n") {
            "  - ${formatDate(it.date)}: ${it.description} (${it.mileage} km) - $${it.cost.toInt()}"
        }
    }
    
    val activeRemindersText = if (carLog.reminders.isEmpty()) {
        "  Aucun rappel configuré"
    } else {
        carLog.reminders.filter { !it.isCompleted }.joinToString("\n") {
            val status = if (it.dueDate < System.currentTimeMillis()) "⚠️ EN RETARD" else "✅ À venir"
            "  - ${it.title}: ${formatDateShort(it.dueDate)} $status"
        }
    }
    
    val documentsText = if (carLog.documents.isEmpty()) {
        "  Aucun document enregistré"
    } else {
        carLog.documents.joinToString("\n") {
            val expiry = if (it.isExpired) "❌ EXPIRÉ" else "✅ Valide jusqu'au ${formatDateShort(it.expiryDate)}"
            "  - ${it.type.name}: $expiry"
        }
    }
    
    return """
📋 **ENTRETIENS ENREGISTRÉS** (${carLog.maintenanceRecords.size}):
$recentMaintenanceText

🔔 **RAPPELS ACTIFS** (${carLog.reminders.filter { !it.isCompleted }.size}):
$activeRemindersText

📄 **DOCUMENTS**:
$documentsText

💸 **DÉPENSES TOTALES**: $${carLog.totalExpenses.toInt()}
    """.trimIndent()
}

/**
 * Grade maintenance quality (A-F)
 */
fun gradeMaintenanceQuality(carLog: CarLog): String {
    val score = scoreMaintenanceQuality(carLog)
    return when {
        score >= 90 -> "A"
        score >= 75 -> "B"
        score >= 60 -> "C"
        score >= 40 -> "D"
        else -> "F"
    }
}

/**
 * Score maintenance quality (0-100)
 */
fun scoreMaintenanceQuality(carLog: CarLog): Int {
    var score = 50 // Base score
    
    // Has records
    if (carLog.maintenanceRecords.isNotEmpty()) score += 20
    
    // Recent oil change
    val hasRecentOil = carLog.maintenanceRecords.any { 
        it.type == MaintenanceType.OIL_CHANGE && getDaysSince(it.date) < 180 
    }
    if (hasRecentOil) score += 15
    
    // CT valid
    val hasValidCT = carLog.documents.any { it.type.name.contains("TECHNICAL") && !it.isExpired }
    if (hasValidCT) score += 10
    
    // No overdue reminders
    val hasOverdue = carLog.reminders.any { !it.isCompleted && it.dueDate < System.currentTimeMillis() }
    if (!hasOverdue) score += 5
    
    return score.coerceIn(0, 100)
}

/**
 * Get latest mileage from maintenance records
 */
fun getLatestMileage(carLog: CarLog): Int {
    return carLog.maintenanceRecords
        .maxByOrNull { it.mileage }
        ?.mileage ?: 100000 // Default if no records
}

/**
 * Check if timing belt has been serviced
 */
fun hasTimingBeltService(carLog: CarLog): Boolean {
    return carLog.maintenanceRecords.any { 
        it.description.contains("distribution", ignoreCase = true) ||
        it.description.contains("courroie", ignoreCase = true) ||
        it.description.contains("timing belt", ignoreCase = true)
    }
}

/**
 * Check if brake service is recent
 */
fun hasRecentBrakeService(carLog: CarLog): Boolean {
    return carLog.maintenanceRecords.any { 
        it.type == MaintenanceType.BRAKE_SERVICE && getDaysSince(it.date) < 365
    }
}

// =============================================================================
// MARKET DATA HELPERS
// =============================================================================

/**
 * Build market data context for prompt
 */
fun buildMarketDataContext(marketData: MarketData, carDetails: CarDetails): String {
    val comparablesText = if (marketData.similarListings.isEmpty()) {
        "  Aucune annonce comparable trouvée"
    } else {
        marketData.similarListings.take(3).joinToString("\n") { listing ->
            "  - ${listing.vehicle} (${listing.year}, ${listing.mileage} km): $${listing.price.toInt()} - ${listing.location}"
        }
    }
    
    return """
📊 Prix Moyen Marché: $${marketData.averageMarketPrice.toInt()}
📈 Fourchette: ${marketData.priceRange.first.toInt()} - $${marketData.priceRange.second.toInt()}
🔢 Annonces similaires: ${marketData.similarListings.size}
🏆 Popularité modèle: ${if (isPopularModel(carDetails.make)) "ÉLEVÉE" else "MOYENNE"}
📅 Données à jour: ${formatDateShort(marketData.lastUpdated)}
🌍 Tendance: ${marketData.marketTrend.uppercase()}

**COMPARABLES**:
$comparablesText
    """.trimIndent()
}

/**
 * Check if car brand is popular in Morocco
 */
fun isPopularModel(make: String): Boolean {
    val popularBrands = listOf(
        "DACIA", "RENAULT", "PEUGEOT", "CITROEN", "VOLKSWAGEN",
        "TOYOTA", "HYUNDAI", "KIA", "FIAT", "FORD", "SEAT",
        "SKODA", "NISSAN", "SUZUKI", "MITSUBISHI"
    )
    return popularBrands.any { it.equals(make, ignoreCase = true) }
}

// =============================================================================
// DIAGNOSTIC TREND HELPERS
// =============================================================================

/**
 * Build audio diagnostic trend summary
 */
fun buildAudioTrendSummary(diagnostics: List<AudioDiagnosticData>): String {
    if (diagnostics.size < 2) return "Pas assez d'historique"
    
    val scores = diagnostics.sortedBy { it.createdAt }.map { it.rawScore }
    val trend = when {
        scores.last() < scores.first() - 10 -> "📉 Dégradation significative"
        scores.last() < scores.first() -> "📉 Dégradation légère"
        scores.last() > scores.first() + 10 -> "📈 Amélioration significative"
        scores.last() > scores.first() -> "📈 Amélioration légère"
        else -> "➡️ Stable"
    }
    
    return "$trend (${scores.joinToString(" → ")})"
}

/**
 * Build video diagnostic trend summary
 */
fun buildVideoTrendSummary(diagnostics: List<VideoDiagnosticData>): String {
    if (diagnostics.size < 2) return "Pas assez d'historique"
    
    val scores = diagnostics.sortedBy { it.createdAt }.map { it.finalScore }
    val trend = when {
        scores.last() < scores.first() - 10 -> "📉 Dégradation significative"
        scores.last() < scores.first() -> "📉 Dégradation légère"
        scores.last() > scores.first() + 10 -> "📈 Amélioration significative"
        scores.last() > scores.first() -> "📈 Amélioration légère"
        else -> "➡️ Stable"
    }
    
    return "$trend (${scores.joinToString(" → ")})"
}

/**
 * Calculate average score from audio diagnostics
 */
fun calculateAverageAudioScore(diagnostics: List<AudioDiagnosticData>): Int {
    return if (diagnostics.isEmpty()) {
        70 // Default
    } else {
        (diagnostics.sumOf { it.rawScore } / diagnostics.size).coerceIn(0, 100)
    }
}

/**
 * Calculate average score from video diagnostics
 */
fun calculateAverageVideoScore(diagnostics: List<VideoDiagnosticData>): Int {
    return if (diagnostics.isEmpty()) {
        70 // Default
    } else {
        (diagnostics.sumOf { it.finalScore } / diagnostics.size).coerceIn(0, 100)
    }
}

// =============================================================================
// COST & VALUE HELPERS
// =============================================================================

/**
 * Calculate total maintenance cost for last 12 months
 */
fun calculateMaintenanceCost12Months(carLog: CarLog): Int {
    val oneYearAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)
    return carLog.maintenanceRecords
        .filter { it.date >= oneYearAgo }
        .sumOf { it.cost }
        .toInt()
}

/**
 * Estimate age-based depreciation percentage
 */
fun calculateAgeDepreciation(year: Int): Int {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val age = currentYear - year
    return when {
        age <= 2 -> 10
        age <= 5 -> 20
        age <= 10 -> 35
        age <= 15 -> 50
        else -> 70
    }
}

/**
 * Estimate mileage-based depreciation percentage
 */
fun calculateMileageDepreciation(mileage: Int): Int {
    return when {
        mileage < 50000 -> 5
        mileage < 100000 -> 15
        mileage < 150000 -> 25
        mileage < 200000 -> 40
        else -> 60
    }
}

// =============================================================================
// STATUS & VALIDATION HELPERS
// =============================================================================

/**
 * Get Contrôle Technique (CT) status
 */
fun getCtStatus(carLog: CarLog): String {
    val ct = carLog.documents.find { it.type.name.contains("TECHNICAL") }
    return ct?.let {
        if (it.isExpired) {
            "EXPIRÉ depuis ${getDaysSince(it.expiryDate)} jours - Renouveler immédiatement"
        } else {
            val daysLeft = TimeUnit.MILLISECONDS.toDays(it.expiryDate - System.currentTimeMillis())
            "Valide jusqu'au ${formatDateShort(it.expiryDate)} (${daysLeft} jours restants)"
        }
    } ?: "Non renseigné - Vérifier immédiatement"
}

/**
 * Check if car has valid insurance
 */
fun hasValidInsurance(carLog: CarLog): Boolean {
    return carLog.documents.any { 
        it.type.name.contains("INSURANCE") && !it.isExpired
    }
}

/**
 * Get days since last oil change
 */
fun getDaysSinceLastOilChange(carLog: CarLog): Long {
    val lastOilChange = carLog.maintenanceRecords
        .filter { it.type == MaintenanceType.OIL_CHANGE }
        .maxByOrNull { it.date }
    
    return lastOilChange?.let { getDaysSince(it.date) } ?: 365L
}

// =============================================================================
// URGENCY & SEVERITY HELPERS
// =============================================================================

/**
 * Combine audio and video urgency levels
 */
fun getCombinedUrgencyLevel(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): String {
    val audioUrgency = audioData?.urgencyLevel ?: "NONE"
    val videoUrgency = videoData?.urgencyLevel ?: "NONE"
    
    return when {
        audioUrgency == "CRITICAL" || videoUrgency == "CRITICAL" -> "CRITICAL"
        audioUrgency == "HIGH" || videoUrgency == "HIGH" -> "HIGH"
        audioUrgency == "MEDIUM" || videoUrgency == "MEDIUM" -> "MEDIUM"
        audioUrgency == "LOW" || videoUrgency == "LOW" -> "LOW"
        else -> "NONE"
    }
}

/**
 * Get highest severity from diagnostics
 */
fun getHighestSeverity(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): String {
    return getCombinedUrgencyLevel(audioData, videoData)
}

// =============================================================================
// VALIDATION HELPERS
// =============================================================================

/**
 * Check if car details are complete
 */
fun isCarDetailsComplete(carDetails: CarDetails): Boolean {
    return carDetails.make.isNotEmpty() &&
           carDetails.model.isNotEmpty() &&
           carDetails.year > 1990
}

/**
 * Check if user has sufficient data for analysis
 */
fun hasSufficientDataForAnalysis(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?,
    carLog: CarLog
): Boolean {
    // At least one diagnostic OR maintenance history
    return (audioData != null || videoData != null) || carLog.maintenanceRecords.isNotEmpty()
}

/**
 * Calculate data completeness score (0-100)
 */
fun calculateDataCompletenessScore(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?,
    carLog: CarLog,
    hasMarketData: Boolean
): Int {
    var score = 0
    
    if (audioData != null) score += 30
    if (videoData != null) score += 30
    if (carLog.maintenanceRecords.isNotEmpty()) score += 25
    if (hasMarketData) score += 15
    
    return score
}

// =============================================================================
// FORMATTING HELPERS
// =============================================================================

/**
 * Format price in Dirham
 */
fun formatPrice(price: Double): String {
    return "$${price.toInt()}"
}

/**
 * Format price range
 */
fun formatPriceRange(min: Double, max: Double): String {
    return "${min.toInt()} - $${max.toInt()}"
}

/**
 * Format percentage
 */
fun formatPercentage(value: Float): String {
    return "${(value * 100).toInt()}%"
}

/**
 * Format confidence score
 */
fun formatConfidence(confidence: Float): String {
    return when {
        confidence >= 0.9f -> "Très élevée (${formatPercentage(confidence)})"
        confidence >= 0.7f -> "Élevée (${formatPercentage(confidence)})"
        confidence >= 0.5f -> "Moyenne (${formatPercentage(confidence)})"
        confidence >= 0.3f -> "Faible (${formatPercentage(confidence)})"
        else -> "Très faible (${formatPercentage(confidence)})"
    }
}

// =============================================================================
// COMPARISON HELPERS
// =============================================================================

/**
 * Compare two scores and return trend
 */
fun compareTwoScores(oldScore: Int, newScore: Int): String {
    val diff = newScore - oldScore
    return when {
        diff > 10 -> "📈 Amélioration significative (+$diff pts)"
        diff > 0 -> "📈 Légère amélioration (+$diff pts)"
        diff < -10 -> "📉 Dégradation significative ($diff pts)"
        diff < 0 -> "📉 Légère dégradation ($diff pts)"
        else -> "➡️ Stable"
    }
}

/**
 * Get the most critical issue from both diagnostics
 */
fun getMostCriticalIssue(
    audioData: AudioDiagnosticData?,
    videoData: VideoDiagnosticData?
): String {
    val audioScore = audioData?.rawScore ?: 100
    val videoScore = videoData?.finalScore ?: 100
    
    return when {
        audioScore < videoScore && audioScore < 50 -> {
            audioData?.criticalWarning?.takeIf { it.isNotEmpty() } 
                ?: "Problème moteur critique (Audio: $audioScore/100)"
        }
        videoScore < 50 -> {
            videoData?.criticalWarning?.takeIf { it.isNotEmpty() } 
                ?: "Problème visuel critique (Vidéo: $videoScore/100)"
        }
        else -> "Pas de problème critique détecté"
    }
}
