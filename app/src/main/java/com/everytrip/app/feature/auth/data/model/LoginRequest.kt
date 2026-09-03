package com.everytrip.app.feature.auth.data.model

data class LoginRequest(
    val email: String,
    val password: String,
    val deviceId: String,
)

data class GoogleLoginRequest(
    val idToken: String,
    val deviceId: String,
)

data class KakaoLoginRequest(
    val accessToken: String,
    val deviceId: String,
)

data class RefreshRequest(
    val refreshToken: String,
    val deviceId: String,
)

data class LogoutRequest(
    val refreshToken: String,
)
