package com.everytrip.app.feature.chatbot.presentation

import com.everytrip.app.feature.chatbot.data.ChatCourse
import com.everytrip.app.feature.chatbot.data.ChatPlace
import com.everytrip.app.feature.chatbot.data.ChatWork

data class ChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage.Bot("안녕하세요! 작품 촬영지와 여행 정보를 물어보세요."),
    ),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface ChatMessage {
    val text: String

    data class User(override val text: String) : ChatMessage
    data class Bot(
        override val text: String,
        val intent: String? = null,
        val works: List<ChatWork> = emptyList(),
        val places: List<ChatPlace> = emptyList(),
        val course: ChatCourse? = null,
    ) : ChatMessage
}
