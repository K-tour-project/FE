package com.everytrip.app.feature.chatbot.data

import retrofit2.http.Body
import retrofit2.http.POST

internal interface ChatService {
    @POST("chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse
}
