package com.everytrip.app.feature.auth.data.model

data class PasswordResetCodeRequest(val email: String)

data class PasswordResetVerifyRequest(val email: String, val code: String)

data class PasswordResetVerifyResponse(val resetToken: String)

data class PasswordResetConfirmRequest(val resetToken: String, val newPassword: String)

data class PasswordResetConfirmResponse(val message: String)
