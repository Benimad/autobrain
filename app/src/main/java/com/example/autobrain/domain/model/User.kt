package com.example.autobrain.domain.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val phoneNumber: String = "",
    val age: Int = 0,
    val role: UserRole = UserRole.REGULAR_USER,
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val carDetails: CarDetails? = null,
    val providerDetails: ProviderDetails? = null,
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    REGULAR_USER,
    MECHANIC,
    TOW_OPERATOR,
    GARAGE_OWNER
}

data class CarDetails(
    val make: String = "",      // e.g., "Toyota"
    val model: String = "",     // e.g., "Corolla"
    val year: Int = 0,          // e.g., 2018
    val vin: String = "",       // Vehicle Identification Number
    val color: String = "",
    val licensePlate: String = "",
    val carImageUrl: String = ""
)

data class ProviderDetails(
    val businessName: String = "",
    val services: List<String> = emptyList(),
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val location: GeoLocation? = null,
    val priceRange: String = "",
    val workingHours: String = "",
    val description: String = "",
    val certifications: List<String> = emptyList()
)

data class GeoLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val city: String = "",
    val country: String = ""
)
