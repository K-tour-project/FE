package com.everytrip.app.feature.auth.presentation.login

import android.app.Application
import android.os.SystemClock
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
    private val _resetState = MutableStateFlow(PasswordResetUiState())
    val resetState: StateFlow<PasswordResetUiState> = _resetState.asStateFlow()
    private var resetVersion = 0
    private var cooldownEmail = ""
    private var cooldownUntil = 0L

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

    fun resetEmailChanged(email: String) {
        if (email.trim() == _resetState.value.email) return
        resetVersion++
        repository.clearPasswordResetToken()
        val normalizedEmail = email.trim()
        _resetState.value = PasswordResetUiState(email = normalizedEmail,
            resendAvailableAt = if (normalizedEmail == cooldownEmail) cooldownUntil else 0L)
    }

    fun sendPasswordResetCode(email: String) {
        val normalizedEmail = email.trim()
        if (_resetState.value.isSending ||
            (normalizedEmail == cooldownEmail && SystemClock.elapsedRealtime() < cooldownUntil)) return
        val version = resetVersion
        viewModelScope.launch {
            repository.clearPasswordResetToken()
            _resetState.update { it.copy(email = normalizedEmail, isSending = true, verified = false, message = null) }
            runCatching { repository.sendPasswordResetCode(normalizedEmail) }
                .onSuccess {
                    val now = SystemClock.elapsedRealtime()
                    cooldownEmail = normalizedEmail
                    cooldownUntil = now + 60_000
                    if (version != resetVersion) return@onSuccess
                    _resetState.update {
                        it.copy(isSending = false, codeSent = true, resendAvailableAt = cooldownUntil,
                            codeExpiresAt = now + 180_000,
                            message = "인증코드를 발송했습니다. 3분 안에 입력해 주세요.")
                    }
                }
                .onFailure { error ->
                    if (version != resetVersion) return@onFailure
                    _resetState.update { it.copy(isSending = false, message = when (error) {
                        is AuthHttpException -> when (error.statusCode) {
                            404 -> "가입되지 않은 이메일입니다."
                            409 -> "구글 또는 카카오 로그인 계정은 비밀번호를 재설정할 수 없습니다."
                            429 -> "잠시 후 다시 요청해 주세요."
                            422 -> "이메일 형식을 확인해 주세요."
                            else -> error.detail
                        }
                        else -> error.message ?: "인증코드 발송에 실패했습니다."
                    }) }
                }
        }
    }

    fun verifyPasswordResetCode(email: String, code: String) {
        if (_resetState.value.isVerifying || !_resetState.value.codeSent) return
        if (SystemClock.elapsedRealtime() >= _resetState.value.codeExpiresAt) {
            _resetState.update { it.copy(message = "인증코드가 만료되었습니다. 코드를 다시 요청해 주세요.") }
            return
        }
        val version = resetVersion
        viewModelScope.launch {
            _resetState.update { it.copy(isVerifying = true, message = null) }
            runCatching { repository.verifyPasswordResetCode(email.trim(), code.trim()) }
                .onSuccess {
                    if (version != resetVersion) {
                        repository.clearPasswordResetToken()
                        return@onSuccess
                    }
                    _resetState.update { it.copy(isVerifying = false, verified = true,
                        message = "이메일 인증이 완료되었습니다.") }
                }
                .onFailure { error ->
                    if (version != resetVersion) return@onFailure
                    _resetState.update { it.copy(isVerifying = false, message = when (error) {
                        is AuthHttpException -> when (error.statusCode) {
                            400 -> "인증코드가 일치하지 않거나 만료되었습니다."
                            429 -> "시도 횟수를 초과했습니다. 인증코드를 다시 요청해 주세요."
                            else -> error.detail
                        }
                        else -> error.message ?: "인증코드 확인에 실패했습니다."
                    }) }
                }
        }
    }

    fun confirmPasswordReset(password: String, passwordCheck: String) {
        val error = when {
            !_resetState.value.verified -> "이메일 인증을 먼저 완료해 주세요."
            password != passwordCheck -> "비밀번호가 일치하지 않습니다."
            password.length < 8 -> "비밀번호는 8자 이상이어야 합니다."
            !password.any(Char::isLetter) -> "비밀번호에는 영문이 포함되어야 합니다."
            !password.any(Char::isDigit) -> "비밀번호에는 숫자가 포함되어야 합니다."
            password.toByteArray(Charsets.UTF_8).size > 72 -> "비밀번호는 최대 72바이트까지 가능합니다."
            else -> null
        }
        if (error != null) {
            _resetState.update { it.copy(message = error) }
            return
        }
        viewModelScope.launch {
            _resetState.update { it.copy(isConfirming = true, message = null) }
            runCatching { repository.confirmPasswordReset(password) }
                .onSuccess { response ->
                    _uiState.update { it.copy(message = response.message.ifBlank { "비밀번호가 변경되었습니다. 다시 로그인해주세요." },
                        sessionCheckState = SessionCheckState.Unauthenticated, user = null) }
                    _resetState.update { it.copy(isConfirming = false, completed = true,
                        message = response.message.ifBlank { "비밀번호가 변경되었습니다. 다시 로그인해주세요." }) }
                }
                .onFailure { failure ->
                    _resetState.update { it.copy(isConfirming = false, verified = failure !is AuthHttpException || failure.statusCode != 400,
                        message = when (failure) {
                            is AuthHttpException -> when (failure.statusCode) {
                                400 -> "재설정 토큰이 만료되었거나 이미 사용되었습니다. 인증을 다시 진행해 주세요."
                                422 -> "비밀번호 형식을 확인해 주세요."
                                else -> failure.detail
                            }
                            else -> failure.message ?: "비밀번호 변경에 실패했습니다."
                        }) }
                }
        }
    }

    fun dismissPasswordReset() {
        resetVersion++
        repository.clearPasswordResetToken()
        _resetState.value = PasswordResetUiState()
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
