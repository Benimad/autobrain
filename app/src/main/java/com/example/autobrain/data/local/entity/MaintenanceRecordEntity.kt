package com.example.autobrain.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.autobrain.domain.model.MaintenanceRecord
import com.example.autobrain.domain.model.MaintenanceType

@Entity(tableName = "maintenance_records")
data class MaintenanceRecordEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val type: String,
    val description: String,
    val date: Long,
    val mileage: Int,
    val cost: Double,
    val serviceProvider: String,
    val notes: String,
    val images: String, // JSON string of list
    val nextServiceDue: Long,
    val isSynced: Boolean = false
)

fun MaintenanceRecordEntity.toDomain(): MaintenanceRecord {
    return MaintenanceRecord(
        id = id,
        type = MaintenanceType.valueOf(type),
        description = description,
        date = date,
        mileage = mileage,
        cost = cost,
        serviceProvider = serviceProvider,
        notes = notes,
        images = if (images.isNotBlank()) images.split(",") else emptyList(),
        nextServiceDue = nextServiceDue
    )
}

fun MaintenanceRecord.toEntity(userId: String, isSynced: Boolean = true): MaintenanceRecordEntity {
    return MaintenanceRecordEntity(
        id = id,
        userId = userId,
        type = type.name,
        description = description,
        date = date,
        mileage = mileage,
        cost = cost,
        serviceProvider = serviceProvider,
        notes = notes,
        images = images.joinToString(","),
        nextServiceDue = nextServiceDue,
        isSynced = isSynced
    )
}
