package com.example.autobrain.core.utils

/**
 * A generic wrapper class for API responses and data operations
 * Used throughout the app to handle loading, success, and error states
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Success<T>(data: T?) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}
