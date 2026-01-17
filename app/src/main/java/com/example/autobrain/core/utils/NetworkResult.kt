package com.example.autobrain.core.utils

/**
 * Network operation result wrapper
 */
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val message: String, val exception: Exception? = null) : NetworkResult<T>()
    class Loading<T> : NetworkResult<T>()
}
