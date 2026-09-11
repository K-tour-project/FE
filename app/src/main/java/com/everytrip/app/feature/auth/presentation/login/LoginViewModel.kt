package com.everytrip.app.feature.auth.presentation.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.everytrip.app.feature.auth.data.remote.AuthHttpException
import com.everytrip.app.feature.auth.data.remote.SessionExpiredException
import com.everytrip.app.feature.auth.data.repository.AuthRepository
import com.everytrip.app.feature.auth.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val repository: AuthRepository = AuthRepositoryImpl(application)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkExistingSession()
    }

    fun checkExistingSession() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(sessionCheckState = SessionCheckState.Checking, message = null)
            }

            if (repository.getStoredAccessToken() == null) {
                _uiState.update {
                    it.copy(
                        sessionCheckState = SessionCheckState.Unauthenticated,
                        user = null,
                    )
                }
                return@launch
            }

            runCatching { repository.getMe() }
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            sessionCheckState = SessionCheckState.Authenticated,
                            user = user,
                            message = null,
                        )
                    }
                }
                .onFailure { error ->
                    if (error is SessionExpiredException) {
                        repository.clearTokens()
                        _uiState.update {
                            it.copy(
                                sessionCheckState = SessionCheckState.Unauthenticated,
                                user = null,
                                message = null,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                sessionCheckState = SessionCheckState.RetryableError,
                                user = null,
                                message = null,
                            )
                        }
                    }
                }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            runCatching { repository.login(email.trim(), password) }
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            sessionCheckState = SessionCheckState.Authenticated,
                            isLoading = false,
                            user = session.user,
                            message = null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            sessionCheckState = SessionCheckState.Unauthenticated,
                            isLoading = false,
                            user = null,
                            message = error.toLoginMessage(),
                        )
                    }
                }
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            runCatching { repository.googleLogin(idToken) }
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            sessionCheckState = SessionCheckState.Authenticated,
                            isLoading = false,
                            user = session.user,
                            message = null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, message = error.toSocialMessage("구글")) }
                }
        }
    }

    fun setGoogleSignInLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading, message = if (isLoading) null else it.message) }
    }

    fun kakaoLogin(accessToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            runCatching { repository.kakaoLogin(accessToken) }
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            sessionCheckState = SessionCheckState.Authenticated,
                            isLoading = false,
                            user = session.user,
                            message = null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, message = error.toSocialMessage("카카오")) }
                }
        }
    }

    fun showMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update {
                LoginUiState(sessionCheckState = SessionCheckState.Unauthenticated)
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun Throwable.toLoginMessage(): String {
        return when (this) {
            is AuthHttpException -> when (statusCode) {
                401 -> "이메일 또는 비밀번호가 올바르지 않습니다."
                409 -> detail
                422 -> "입력값을 확인해 주세요."
                else -> detail
            }
            else -> message ?: "로그인에 실패했습니다."
        }
    }

    private fun Throwable.toSocialMessage(provider: String): String {
        return when (this) {
            is AuthHttpException -> when (statusCode) {
                401 -> "$provider 로그인을 다시 시도해 주세요."
                409 -> detail
                502 -> "잠시 후 다시 시도해 주세요."
                422 -> "로그인 정보가 누락되었습니다."
                else -> detail
            }
            else -> message ?: "$provider 로그인에 실패했습니다."
        }
    }

}
