package com.everytrip.app.feature.auth.data.model

data class AuthUser(
    val userId: Int,
    val nickname: String,
    val authProvider: String,
    val email: String,
    val emailVerified: Boolean,
    val createdAt: String,
)

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val user: AuthUser,
)

data class EmailCodeResponse(
    val expiresIn: Int,
    val devCode: String?,
)

data class EmailVerifyResponse(
    val verified: Boolean,
    val signupDeadlineMinutes: Int,
)

data class SignUpResponse(
    val message: String,
    val user: AuthUser,
)
