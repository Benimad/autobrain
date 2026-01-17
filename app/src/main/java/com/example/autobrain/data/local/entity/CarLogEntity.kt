package com.example.autobrain.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.autobrain.domain.model.CarDetails
import com.example.autobrain.domain.model.CarLog

@Entity(tableName = "car_logs")
data class CarLogEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val make: String = "",
    val model: String = "",
    val year: Int = 0,
    val vin: String = "",
    val color: String = "",
    val licensePlate: String = "",
    val totalExpenses: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

fun CarLogEntity.toDomain(): CarLog {
    return CarLog(
        id = id,
        userId = userId,
        carDetails = CarDetails(
            make = make,
            model = model,
            year = year,
            vin = vin,
            color = color,
            licensePlate = licensePlate
        ),
        totalExpenses = totalExpenses,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun CarLog.toEntity(): CarLogEntity {
    return CarLogEntity(
        id = id,
        userId = userId,
        make = carDetails?.make ?: "",
        model = carDetails?.model ?: "",
        year = carDetails?.year ?: 0,
        vin = carDetails?.vin ?: "",
        color = carDetails?.color ?: "",
        licensePlate = carDetails?.licensePlate ?: "",
        totalExpenses = totalExpenses,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
