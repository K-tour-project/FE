package com.everytrip.app.feature.auth.presentation.login

import com.everytrip.app.feature.auth.data.model.AuthUser

data class LoginUiState(
    val sessionCheckState: SessionCheckState = SessionCheckState.Checking,
    val isLoading: Boolean = false,
    val user: AuthUser? = null,
    val message: String? = null,
)

enum class SessionCheckState {
    Checking,
    Authenticated,
    Unauthenticated,
    RetryableError,
}
