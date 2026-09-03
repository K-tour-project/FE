package com.everytrip.app.feature.auth.presentation.login

import com.everytrip.app.feature.auth.data.model.AuthUser

data class LoginUiState(
    val isCheckingSession: Boolean = true,
    val isLoading: Boolean = false,
    val user: AuthUser? = null,
    val message: String? = null,
)
