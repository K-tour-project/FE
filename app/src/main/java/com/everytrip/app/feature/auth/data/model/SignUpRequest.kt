package com.everytrip.app.feature.auth.data.model

data class EmailCodeRequest(
    val email: String,
)

data class EmailVerifyRequest(
    val email: String,
    val code: String,
)

class ProfileImageUpload(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String,
)
