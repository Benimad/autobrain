package com.example.autobrain.domain.logic

import com.example.autobrain.domain.model.MaintenanceRecord
import com.example.autobrain.domain.model.MaintenanceType
import com.example.autobrain.domain.model.ReminderPriority
import java.util.concurrent.TimeUnit

/**
 * 🧠 INTELLIGENT REMINDER ENGINE
 * 
 * Advanced algorithms for calculating optimal maintenance reminders
 * Based on:
 * - Industry standards for each maintenance type
 * - Vehicle's actual maintenance history patterns
 * - Mileage and time-based triggers
 * - Cost optimization
 * - Risk assessment
 */
object IntelligentReminderEngine {
    
    /**
     * Standard maintenance intervals (km)
     * Based on automotive industry standards
     */
    private val STANDARD_KM_INTERVALS = mapOf(
        MaintenanceType.OIL_CHANGE to 5000,
        MaintenanceType.TIRE_ROTATION to 10000,
        MaintenanceType.BRAKE_SERVICE to 20000,
        MaintenanceType.ENGINE_TUNE_UP to 30000,
        MaintenanceType.BATTERY_REPLACEMENT to 40000,
        MaintenanceType.AIR_FILTER to 15000,
        MaintenanceType.TRANSMISSION_SERVICE to 60000,
        MaintenanceType.COOLANT_FLUSH to 40000,
        MaintenanceType.GENERAL_INSPECTION to 10000,
        MaintenanceType.REPAIR to 0, // No standard interval
        MaintenanceType.OTHER to 0
    )
    
    /**
     * Standard time intervals (months)
     */
    private val STANDARD_TIME_INTERVALS = mapOf(
        MaintenanceType.OIL_CHANGE to 6,
        MaintenanceType.TIRE_ROTATION to 6,
        MaintenanceType.BRAKE_SERVICE to 12,
        MaintenanceType.ENGINE_TUNE_UP to 24,
        MaintenanceType.BATTERY_REPLACEMENT to 36,
        MaintenanceType.AIR_FILTER to 12,
        MaintenanceType.TRANSMISSION_SERVICE to 36,
        MaintenanceType.COOLANT_FLUSH to 24,
        MaintenanceType.GENERAL_INSPECTION to 12,
        MaintenanceType.REPAIR to 0,
        MaintenanceType.OTHER to 0
    )
    
    /**
     * Estimated costs in USD
     */
    private val ESTIMATED_COSTS = mapOf(
        MaintenanceType.OIL_CHANGE to Pair(250.0, 400.0),
        MaintenanceType.TIRE_ROTATION to Pair(150.0, 300.0),
        MaintenanceType.BRAKE_SERVICE to Pair(800.0, 1500.0),
        MaintenanceType.ENGINE_TUNE_UP to Pair(500.0, 1000.0),
        MaintenanceType.BATTERY_REPLACEMENT to Pair(600.0, 1200.0),
        MaintenanceType.AIR_FILTER to Pair(100.0, 200.0),
        MaintenanceType.TRANSMISSION_SERVICE to Pair(1500.0, 3000.0),
        MaintenanceType.COOLANT_FLUSH to Pair(300.0, 600.0),
        MaintenanceType.GENERAL_INSPECTION to Pair(200.0, 400.0),
        MaintenanceType.REPAIR to Pair(0.0, 0.0),
        MaintenanceType.OTHER to Pair(0.0, 0.0)
    )
    
    /**
     * CORE ALGORITHM: Calculate optimal reminder for maintenance type
     * 
     * Logic:
     * 1. Analyze past maintenance history for this type
     * 2. Calculate average interval between services
     * 3. Use learned pattern OR fallback to industry standards
     * 4. Apply safety margin (remind earlier for critical services)
     * 5. Consider current mileage and date
     */
    fun calculateOptimalReminder(
        maintenanceType: MaintenanceType,
        currentMileage: Int,
        maintenanceHistory: List<MaintenanceRecord>,
        currentDate: Long = System.currentTimeMillis()
    ): ReminderCalculation {
        
        // Filter history for this specific maintenance type
        val relevantHistory = maintenanceHistory
            .filter { it.type == maintenanceType }
            .sortedByDescending { it.date }
        
        // Get last maintenance of this type
        val lastMaintenance = relevantHistory.firstOrNull()
        
        // Calculate learned interval if we have history
        val learnedKmInterval = if (relevantHistory.size >= 2) {
            calculateLearnedKmInterval(relevantHistory)
        } else null
        
        val learnedTimeInterval = if (relevantHistory.size >= 2) {
            calculateLearnedTimeInterval(relevantHistory)
        } else null
        
        // Use learned pattern OR standard intervals
        val kmInterval = learnedKmInterval ?: STANDARD_KM_INTERVALS[maintenanceType] ?: 10000
        val timeIntervalMonths = learnedTimeInterval ?: STANDARD_TIME_INTERVALS[maintenanceType] ?: 12
        
        // Calculate due mileage
        val lastServiceMileage = lastMaintenance?.mileage ?: 0
        val dueMileage = lastServiceMileage + kmInterval
        val kmRemaining = dueMileage - currentMileage
        
        // Calculate due date
        val lastServiceDate = lastMaintenance?.date ?: currentDate
        val timeIntervalMillis = TimeUnit.DAYS.toMillis(timeIntervalMonths * 30L)
        val dueDate = lastServiceDate + timeIntervalMillis
        val daysRemaining = TimeUnit.MILLISECONDS.toDays(dueDate - currentDate)
        
        // Calculate priority based on urgency
        val priority = calculatePriority(kmRemaining, daysRemaining.toInt(), maintenanceType)
        
        // Estimate cost
        val estimatedCost = estimateCost(maintenanceType, relevantHistory)
        
        // Generate intelligent description
        val description = generateIntelligentDescription(
            maintenanceType,
            kmRemaining,
            daysRemaining.toInt(),
            lastMaintenance
        )
        
        // Calculate urgency score (0-100, 100 = most urgent)
        val urgencyScore = calculateUrgencyScore(kmRemaining, daysRemaining.toInt())
        
        return ReminderCalculation(
            maintenanceType = maintenanceType,
            dueMileage = dueMileage,
            dueDate = dueDate,
            kmRemaining = kmRemaining,
            daysRemaining = daysRemaining.toInt(),
            priority = priority,
            estimatedCost = estimatedCost,
            description = description,
            urgencyScore = urgencyScore,
            isOverdue = kmRemaining < 0 || daysRemaining < 0,
            lastServiceDate = lastMaintenance?.date,
            lastServiceMileage = lastMaintenance?.mileage,
            usedLearnedPattern = learnedKmInterval != null || learnedTimeInterval != null
        )
    }
    
    /**
     * Learn KM interval from historical pattern
     * Uses weighted average (more weight to recent services)
     */
    private fun calculateLearnedKmInterval(history: List<MaintenanceRecord>): Int? {
        if (history.size < 2) return null
        
        val intervals = mutableListOf<Int>()
        for (i in 0 until history.size - 1) {
            val interval = history[i].mileage - history[i + 1].mileage
            if (interval > 0) {
                intervals.add(interval)
            }
        }
        
        if (intervals.isEmpty()) return null
        
        // Weighted average (recent intervals have more weight)
        var weightedSum = 0.0
        var totalWeight = 0.0
        intervals.forEachIndexed { index, interval ->
            val weight = (index + 1).toDouble() // More recent = higher weight
            weightedSum += interval * weight
            totalWeight += weight
        }
        
        return (weightedSum / totalWeight).toInt()
    }
    
    /**
     * Learn time interval from historical pattern (in months)
     */
    private fun calculateLearnedTimeInterval(history: List<MaintenanceRecord>): Int? {
        if (history.size < 2) return null
        
        val intervals = mutableListOf<Int>()
        for (i in 0 until history.size - 1) {
            val daysDiff = TimeUnit.MILLISECONDS.toDays(history[i].date - history[i + 1].date)
            val months = (daysDiff / 30).toInt()
            if (months > 0) {
                intervals.add(months)
            }
        }
        
        if (intervals.isEmpty()) return null
        
        // Weighted average
        var weightedSum = 0.0
        var totalWeight = 0.0
        intervals.forEachIndexed { index, interval ->
            val weight = (index + 1).toDouble()
            weightedSum += interval * weight
            totalWeight += weight
        }
        
        return (weightedSum / totalWeight).toInt()
    }
    
    /**
     * PRIORITY CALCULATION ENGINE
     * 
     * Logic:
     * - CRITICAL: Overdue or very close to due
     * - HIGH: Within warning threshold
     * - MEDIUM: Upcoming soon
     * - LOW: Future service
     */
    private fun calculatePriority(
        kmRemaining: Int,
        daysRemaining: Int,
        type: MaintenanceType
    ): ReminderPriority {
        // Critical services (safety-related)
        val isCritical = type in listOf(
            MaintenanceType.BRAKE_SERVICE,
            MaintenanceType.ENGINE_TUNE_UP,
            MaintenanceType.BATTERY_REPLACEMENT
        )
        
        return when {
            // Overdue
            kmRemaining < 0 || daysRemaining < 0 -> ReminderPriority.CRITICAL
            
            // Very close (less than 500 km or 7 days)
            kmRemaining < 500 || daysRemaining < 7 -> {
                if (isCritical) ReminderPriority.CRITICAL else ReminderPriority.HIGH
            }
            
            // Warning zone (less than 1000 km or 14 days)
            kmRemaining < 1000 || daysRemaining < 14 -> {
                if (isCritical) ReminderPriority.HIGH else ReminderPriority.MEDIUM
            }
            
            // Upcoming (less than 2000 km or 30 days)
            kmRemaining < 2000 || daysRemaining < 30 -> ReminderPriority.MEDIUM
            
            // Future service
            else -> ReminderPriority.LOW
        }
    }
    
    /**
     * Calculate urgency score (0-100)
     */
    private fun calculateUrgencyScore(kmRemaining: Int, daysRemaining: Int): Int {
        val kmScore = when {
            kmRemaining < 0 -> 100
            kmRemaining < 500 -> 90
            kmRemaining < 1000 -> 75
            kmRemaining < 2000 -> 50
            kmRemaining < 3000 -> 30
            else -> 10
        }
        
        val timeScore = when {
            daysRemaining < 0 -> 100
            daysRemaining < 7 -> 90
            daysRemaining < 14 -> 75
            daysRemaining < 30 -> 50
            daysRemaining < 60 -> 30
            else -> 10
        }
        
        // Take the maximum of the two scores
        return maxOf(kmScore, timeScore)
    }
    
    /**
     * Estimate cost based on historical data
     */
    private fun estimateCost(
        type: MaintenanceType,
        history: List<MaintenanceRecord>
    ): Pair<Double, Double> {
        // If we have historical data, use average of last 3 services
        val historicalCosts = history.take(3).map { it.cost }.filter { it > 0 }
        
        return if (historicalCosts.isNotEmpty()) {
            val avgCost = historicalCosts.average()
            // Add ±15% margin
            Pair(avgCost * 0.85, avgCost * 1.15)
        } else {
            // Use standard estimates
            ESTIMATED_COSTS.getOrDefault(type, Pair(0.0, 0.0))
        }
    }
    
    /**
     * Generate intelligent, contextual description
     */
    private fun generateIntelligentDescription(
        type: MaintenanceType,
        kmRemaining: Int,
        daysRemaining: Int,
        lastService: MaintenanceRecord?
    ): String {
        val typeLabel = getMaintenanceTypeLabel(type)
        
        return when {
            kmRemaining < 0 && daysRemaining < 0 -> {
                "⚠️ $typeLabel overdue! Immediate service recommended"
            }
            kmRemaining < 0 -> {
                "⚠️ $typeLabel exceeded by ${-kmRemaining} km"
            }
            daysRemaining < 0 -> {
                "⚠️ $typeLabel exceeded by ${-daysRemaining} days"
            }
            kmRemaining < 500 || daysRemaining < 7 -> {
                "🔴 $typeLabel very urgent - in $kmRemaining km or $daysRemaining days"
            }
            kmRemaining < 1000 || daysRemaining < 14 -> {
                "🟠 $typeLabel soon - in $kmRemaining km or $daysRemaining days"
            }
            lastService != null -> {
                "📅 $typeLabel scheduled in $kmRemaining km (last: ${lastService.mileage} km)"
            }
            else -> {
                "📋 $typeLabel recommended in $kmRemaining km"
            }
        }
    }
    
    /**
     * Detect conflicts with existing maintenance
     * Returns true if service was done recently (within 20% of interval)
     */
    fun hasRecentMaintenance(
        type: MaintenanceType,
        currentMileage: Int,
        history: List<MaintenanceRecord>
    ): Boolean {
        val lastService = history
            .filter { it.type == type }
            .maxByOrNull { it.date }
            ?: return false
        
        val standardInterval = STANDARD_KM_INTERVALS[type] ?: 10000
        val threshold = (standardInterval * 0.2).toInt() // 20% of interval
        
        val kmSinceService = currentMileage - lastService.mileage
        return kmSinceService < threshold
    }
    
    private fun getMaintenanceTypeLabel(type: MaintenanceType): String {
        return when (type) {
            MaintenanceType.OIL_CHANGE -> "Oil change"
            MaintenanceType.TIRE_ROTATION -> "Tire rotation"
            MaintenanceType.BRAKE_SERVICE -> "Brake service"
            MaintenanceType.ENGINE_TUNE_UP -> "Engine tune-up"
            MaintenanceType.BATTERY_REPLACEMENT -> "Battery replacement"
            MaintenanceType.AIR_FILTER -> "Air filter"
            MaintenanceType.TRANSMISSION_SERVICE -> "Transmission service"
            MaintenanceType.COOLANT_FLUSH -> "Coolant flush"
            MaintenanceType.GENERAL_INSPECTION -> "General inspection"
            MaintenanceType.REPAIR -> "Repair"
            MaintenanceType.OTHER -> "Other"
        }
    }
}

/**
 * Result of reminder calculation
 */
data class ReminderCalculation(
    val maintenanceType: MaintenanceType,
    val dueMileage: Int,
    val dueDate: Long,
    val kmRemaining: Int,
    val daysRemaining: Int,
    val priority: ReminderPriority,
    val estimatedCost: Pair<Double, Double>, // min to max
    val description: String,
    val urgencyScore: Int, // 0-100
    val isOverdue: Boolean,
    val lastServiceDate: Long?,
    val lastServiceMileage: Int?,
    val usedLearnedPattern: Boolean // true if used historical data
)
