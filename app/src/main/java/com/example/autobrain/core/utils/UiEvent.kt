package com.example.autobrain.core.utils

/**
 * One-time UI events (navigation, snackbars, etc.)
 */
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    object NavigateBack : UiEvent()
}
