package com.everytrip.app.feature.chatbot.data

import com.google.gson.JsonElement

data class ChatRequest(
    val query: String,
    val sessionId: String? = null,
    val userId: String? = null,
)

data class ChatResponse(
    val answer: String,
    val intent: String? = null,
    val places: List<ChatPlace>? = null,
    val works: JsonElement? = null,
    val course: JsonElement? = null,
)

data class ChatPlace(
    val placeId: String,
    val placeName: String,
    val workTitle: String? = null,
    val address: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val sceneDesc: String? = null,
    val posterUrl: String? = null,
    val sido: String? = null,
    val sigungu: String? = null,
)
