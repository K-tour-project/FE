package com.everytrip.app.feature.auth.data.model

data class EmailCodeRequest(
    val email: String,
)

data class EmailVerifyRequest(
    val email: String,
    val code: String,
)

data class SignUpRequest(
    val email: String,
    val password: String,
    val nickname: String,
    val profileImageUrl: String? = null,
)
