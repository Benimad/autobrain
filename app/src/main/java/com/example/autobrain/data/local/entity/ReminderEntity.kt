package com.example.autobrain.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for maintenance reminders
 * Used for oil changes, inspections, insurance renewals
 */
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val carId: String,

    // Reminder details
    val type: String, // OIL_CHANGE, TECHNICAL_INSPECTION, INSURANCE, MAINTENANCE, CUSTOM
    val title: String,
    val description: String,

    // Due information
    val dueDate: Long,
    val dueMileage: Int = 0,

    // Notification settings
    val reminderDaysBefore: Int = 7,
    val isNotificationEnabled: Boolean = true,
    val notificationSent: Boolean = false,

    // Status
    val isCompleted: Boolean = false,
    val completedAt: Long = 0,
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, CRITICAL

    // Recurrence
    val isRecurring: Boolean = false,
    val recurringIntervalDays: Int = 0,
    val recurringIntervalMileage: Int = 0,

    // Sync status
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Domain model for Reminder
 */
data class Reminder(
    val id: String = "",
    val userId: String = "",
    val carId: String = "",
    val type: ReminderType = ReminderType.MAINTENANCE,
    val title: String = "",
    val description: String = "",
    val dueDate: Long = 0,
    val dueMileage: Int = 0,
    val reminderDaysBefore: Int = 7,
    val isNotificationEnabled: Boolean = true,
    val notificationSent: Boolean = false,
    val isCompleted: Boolean = false,
    val completedAt: Long = 0,
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    val isRecurring: Boolean = false,
    val recurringIntervalDays: Int = 0,
    val recurringIntervalMileage: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ReminderType {
    OIL_CHANGE,
    TECHNICAL_INSPECTION,
    INSURANCE,
    MAINTENANCE,
    TIRE_CHANGE,
    BRAKE_SERVICE,
    BATTERY_CHECK,
    CUSTOM
}

enum class ReminderPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

// Extension functions
fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        userId = userId,
        carId = carId,
        type = try {
            ReminderType.valueOf(type)
        } catch (e: Exception) {
            ReminderType.MAINTENANCE
        },
        title = title,
        description = description,
        dueDate = dueDate,
        dueMileage = dueMileage,
        reminderDaysBefore = reminderDaysBefore,
        isNotificationEnabled = isNotificationEnabled,
        notificationSent = notificationSent,
        isCompleted = isCompleted,
        completedAt = completedAt,
        priority = try {
            ReminderPriority.valueOf(priority)
        } catch (e: Exception) {
            ReminderPriority.MEDIUM
        },
        isRecurring = isRecurring,
        recurringIntervalDays = recurringIntervalDays,
        recurringIntervalMileage = recurringIntervalMileage,
        createdAt = createdAt
    )
}

fun Reminder.toReminderEntity(isSynced: Boolean = false): ReminderEntity {
    return ReminderEntity(
        id = id,
        userId = userId,
        carId = carId,
        type = type.name,
        title = title,
        description = description,
        dueDate = dueDate,
        dueMileage = dueMileage,
        reminderDaysBefore = reminderDaysBefore,
        isNotificationEnabled = isNotificationEnabled,
        notificationSent = notificationSent,
        isCompleted = isCompleted,
        completedAt = completedAt,
        priority = priority.name,
        isRecurring = isRecurring,
        recurringIntervalDays = recurringIntervalDays,
        recurringIntervalMileage = recurringIntervalMileage,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
}

// Firestore conversion
fun Reminder.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "userId" to userId,
        "carId" to carId,
        "type" to type.name,
        "title" to title,
        "description" to description,
        "dueDate" to dueDate,
        "dueMileage" to dueMileage,
        "reminderDaysBefore" to reminderDaysBefore,
        "isNotificationEnabled" to isNotificationEnabled,
        "notificationSent" to notificationSent,
        "completed" to isCompleted,  // Fixed: was "isCompleted", should be "completed" to match Firebase
        "completedAt" to completedAt,
        "priority" to priority.name,
        "isRecurring" to isRecurring,
        "recurringIntervalDays" to recurringIntervalDays,
        "recurringIntervalMileage" to recurringIntervalMileage,
        "createdAt" to createdAt
    )
}

fun Map<String, Any?>.toReminder(): Reminder {
    return Reminder(
        id = this["id"] as? String ?: "",
        userId = this["userId"] as? String ?: "",
        carId = this["carId"] as? String ?: "",
        type = try {
            ReminderType.valueOf(this["type"] as? String ?: "")
        } catch (e: Exception) {
            ReminderType.MAINTENANCE
        },
        title = this["title"] as? String ?: "",
        description = this["description"] as? String ?: "",
        dueDate = this["dueDate"] as? Long ?: 0,
        dueMileage = (this["dueMileage"] as? Long)?.toInt() ?: 0,
        reminderDaysBefore = (this["reminderDaysBefore"] as? Long)?.toInt() ?: 7,
        isNotificationEnabled = this["isNotificationEnabled"] as? Boolean ?: true,
        notificationSent = this["notificationSent"] as? Boolean ?: false,
        isCompleted = (this["completed"] as? Boolean) ?: (this["isCompleted"] as? Boolean) ?: false,  // Try "completed" first, fallback to "isCompleted"
        completedAt = this["completedAt"] as? Long ?: 0,
        priority = try {
            ReminderPriority.valueOf(this["priority"] as? String ?: "")
        } catch (e: Exception) {
            ReminderPriority.MEDIUM
        },
        isRecurring = this["isRecurring"] as? Boolean ?: false,
        recurringIntervalDays = (this["recurringIntervalDays"] as? Long)?.toInt() ?: 0,
        recurringIntervalMileage = (this["recurringIntervalMileage"] as? Long)?.toInt() ?: 0,
        createdAt = this["createdAt"] as? Long ?: System.currentTimeMillis()
    )
}
