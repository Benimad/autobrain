package com.example.autobrain.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Extension functions for common operations
 */

// Time formatting
fun Long.toTimeAgo(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "À l'instant"
        diff < TimeUnit.HOURS.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            "Il y a $minutes min"
        }

        diff < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            "Il y a $hours h"
        }

        diff < TimeUnit.DAYS.toMillis(7) -> {
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            "Il y a $days jour${if (days > 1) "s" else ""}"
        }

        else -> this.toFormattedDate()
    }
}

fun Long.toFormattedDate(pattern: String = "dd/MM/yyyy"): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(pattern, Locale.FRANCE)
    return formatter.format(date)
}

fun Long.toFormattedTime(pattern: String = "HH:mm"): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(pattern, Locale.FRANCE)
    return formatter.format(date)
}

fun Long.toFormattedDateTime(pattern: String = "dd/MM/yyyy HH:mm"): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(pattern, Locale.FRANCE)
    return formatter.format(date)
}

// Distance calculations
fun calculateDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val earthRadius = 6371.0 // Radius in kilometers

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)

    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return earthRadius * c
}

// Price formatting
fun Double.toCurrency(): String {
    return String.format(Locale.US, "$%.2f", this)
}

// String validations
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPhoneNumber(): Boolean {
    // Phone number format: starts with 0 or +212, followed by 9 digits
    val pattern = "^(\\+212|0)[5-7][0-9]{8}$".toRegex()
    return pattern.matches(this)
}

fun String.isValidPassword(): Boolean {
    // Minimum 8 characters, at least one letter and one number
    return this.length >= 8 &&
            this.any { it.isLetter() } &&
            this.any { it.isDigit() }
}

// Rating formatting
fun Double.toRatingString(): String {
    return String.format(Locale.FRANCE, "%.1f", this)
}

// List extensions
fun <T> List<T>.secondOrNull(): T? {
    return if (this.size >= 2) this[1] else null
}
