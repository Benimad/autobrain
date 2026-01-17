package com.example.autobrain.domain.model

data class CarLog(
    val id: String = "",
    val userId: String = "",
    val carDetails: CarDetails? = null,
    val maintenanceRecords: List<MaintenanceRecord> = emptyList(),
    val reminders: List<MaintenanceReminder> = emptyList(),
    val documents: List<CarDocument> = emptyList(),
    val totalExpenses: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class MaintenanceRecord(
    val id: String = "",
    val type: MaintenanceType = MaintenanceType.OIL_CHANGE,
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val mileage: Int = 0,
    val cost: Double = 0.0,
    val serviceProvider: String = "",
    val notes: String = "",
    val images: List<String> = emptyList(),
    val nextServiceDue: Long = 0
)

enum class MaintenanceType {
    OIL_CHANGE,
    TIRE_ROTATION,
    BRAKE_SERVICE,
    ENGINE_TUNE_UP,
    BATTERY_REPLACEMENT,
    AIR_FILTER,
    TRANSMISSION_SERVICE,
    COOLANT_FLUSH,
    GENERAL_INSPECTION,
    REPAIR,
    OTHER
}

data class MaintenanceReminder(
    val id: String = "",
    val type: MaintenanceType = MaintenanceType.OIL_CHANGE,
    val title: String = "",
    val description: String = "",
    val dueDate: Long = 0,
    val dueMileage: Int = 0,
    @com.google.firebase.firestore.PropertyName("completed")
    val isCompleted: Boolean = false,
    val notificationDaysBefore: Int = 7,
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ReminderPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class CarDocument(
    val id: String = "",
    val type: DocumentType = DocumentType.INSURANCE,
    val title: String = "",
    val documentUrl: String = "",
    val issueDate: Long = 0,
    val expiryDate: Long = 0,
    val notes: String = "",
    val isExpired: Boolean = false,
    val reminderSet: Boolean = false
)

enum class DocumentType {
    INSURANCE,
    REGISTRATION,
    TECHNICAL_INSPECTION,
    WARRANTY,
    PURCHASE_AGREEMENT,
    OTHER
}
