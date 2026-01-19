package com.example.autobrain.presentation.screens.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autobrain.data.repository.GeminiChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: GeminiChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadChatHistory()
    }

    private fun loadChatHistory() {
        viewModelScope.launch {
            chatRepository.getChatHistory().collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun updateMessage(message: String) {
        _uiState.update { it.copy(currentMessage = message) }
    }

    fun sendMessage(customMessage: String? = null) {
        val messageText = customMessage ?: _uiState.value.currentMessage
        android.util.Log.d("ChatViewModel", "sendMessage called with: $messageText")
        
        if (messageText.isBlank()) {
            android.util.Log.w("ChatViewModel", "Message is blank, returning")
            return
        }

        _uiState.update {
            it.copy(
                currentMessage = "",
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                android.util.Log.d("ChatViewModel", "Calling repository.sendMessage")
                chatRepository.sendMessage(messageText)
                
                android.util.Log.d("ChatViewModel", "Message sent successfully")
                _uiState.update {
                    it.copy(isLoading = false)
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Error sending message", e)
                _uiState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun attachImage(uri: Uri) {
        viewModelScope.launch {
            // Handle image attachment
        }
    }

    fun clearChat() {
        chatRepository.clearChat()
        _uiState.update { it.copy(messages = emptyList()) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
