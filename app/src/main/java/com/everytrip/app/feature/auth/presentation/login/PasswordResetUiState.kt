package com.everytrip.app.feature.auth.presentation.login

data class PasswordResetUiState(
    val email: String = "",
    val isSending: Boolean = false,
    val isVerifying: Boolean = false,
    val isConfirming: Boolean = false,
    val codeSent: Boolean = false,
    val verified: Boolean = false,
    val resendAvailableAt: Long = 0L,
    val codeExpiresAt: Long = 0L,
    val message: String? = null,
    val completed: Boolean = false,
)
