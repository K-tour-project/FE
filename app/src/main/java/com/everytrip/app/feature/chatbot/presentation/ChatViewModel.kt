package com.everytrip.app.feature.chatbot.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.everytrip.app.feature.chatbot.data.ChatRepository
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository(),
) : ViewModel() {
    private val sessionId = "session-${UUID.randomUUID()}"
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(rawQuery: String) {
        val query = rawQuery.trim()
        if (query.isEmpty() || _uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage.User(query),
                isLoading = true,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            runCatching {
                repository.sendMessage(query = query, sessionId = sessionId)
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        messages = it.messages + ChatMessage.Bot(
                            text = response.answer,
                            intent = response.intent,
                            places = response.places.orEmpty(),
                        ),
                        isLoading = false,
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "답변을 불러오지 못했어요. 다시 시도해 주세요.",
                    )
                }
            }
        }
    }

    fun consumeError() = _uiState.update { it.copy(errorMessage = null) }
}
