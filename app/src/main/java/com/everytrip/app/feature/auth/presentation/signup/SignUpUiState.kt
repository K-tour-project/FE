package com.everytrip.app.feature.auth.presentation.signup

data class SignUpUiState(
    val isSendingCode: Boolean = false,
    val isVerifyingCode: Boolean = false,
    val isSigningUp: Boolean = false,
    val codeSent: Boolean = false,
    val emailVerified: Boolean = false,
    val expiresIn: Int = 0,
    val devCode: String? = null,
    val signupCompleted: Boolean = false,
    val message: String? = null,
)
