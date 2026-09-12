package com.everytrip.app.feature.chatbot.data

import com.everytrip.app.BuildConfig
import com.everytrip.app.core.network.NetworkProvider

class ChatRepository private constructor(
    private val service: ChatService,
) {
    constructor() : this(
        NetworkProvider.create(ChatService::class.java, BuildConfig.CHAT_BASE_URL),
    )

    suspend fun sendMessage(
        query: String,
        sessionId: String? = null,
        userId: String? = null,
    ): ChatResponse = service.chat(
        ChatRequest(query = query, sessionId = sessionId, userId = userId),
    )
}
