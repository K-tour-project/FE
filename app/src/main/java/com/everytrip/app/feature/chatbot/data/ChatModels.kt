package com.everytrip.app.feature.chatbot.data

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val query: String,
    val sessionId: String? = null,
    val userId: String? = null,
)

data class ChatResponse(
    val answer: String,
    val intent: String? = null,
    val places: List<ChatPlace>? = null,
    val works: List<ChatWork>? = null,
    val course: ChatCourse? = null,
)

data class ChatWork(
    val id: Int? = null,
    val title: String,
    val type: String? = null,
    @SerializedName("releaseDate")
    val releaseDate: String? = null,
    val genres: List<String>? = null,
    @SerializedName("posterUrl")
    val posterUrl: String? = null,
    val overview: String? = null,
    val rating: Double? = null,
    @SerializedName("filmingLocations")
    val filmingLocations: List<ChatFilmingLocation>? = null,
)

data class ChatFilmingLocation(
    @SerializedName("placeId")
    val placeId: String,
    val name: String,
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

data class ChatCourse(
    val description: String? = null,
    val totalPlaces: Int? = null,
    val estimatedMinutes: Int? = null,
    val transport: String? = null,
    val startLocation: String? = null,
    val regions: List<String>? = null,
    val works: List<String>? = null,
    val stops: List<ChatCourseStop>? = null,
)

data class ChatCourseStop(
    val order: Int,
    val placeId: String,
    val placeName: String,
    val workTitle: String? = null,
    val address: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val posterUrl: String? = null,
    val sceneDesc: String? = null,
    val stayMinutes: Int? = null,
    val travelMinutesFromPrev: Int? = null,
)
