package com.everytrip.app.feature.auth.presentation.signup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.everytrip.app.feature.auth.data.remote.AuthHttpException
import com.everytrip.app.feature.auth.data.repository.AuthRepository
import com.everytrip.app.feature.auth.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val repository: AuthRepository = AuthRepositoryImpl(application)
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun sendCode(email: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSendingCode = true, message = null, emailVerified = false, signupCompleted = false)
            }
            runCatching { repository.sendEmailCode(email.trim()) }
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            codeSent = true,
                            expiresIn = response.expiresIn,
                            devCode = response.devCode,
                            message = if (response.devCode == null) {
                                "인증코드를 발송했습니다. 메일이 보이지 않으면 스팸메일함을 확인해 주세요."
                            } else {
                                "개발 인증코드: ${response.devCode}\n메일이 보이지 않으면 스팸메일함을 확인해 주세요."
                            },
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isSendingCode = false, message = error.toSendCodeMessage())
                    }
                }
        }
    }

    fun verifyCode(email: String, code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingCode = true, message = null) }
            runCatching { repository.verifyEmailCode(email.trim(), code.trim()) }
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isVerifyingCode = false,
                            emailVerified = response.verified,
                            message = if (response.verified) {
                                "이메일 인증이 완료되었습니다."
                            } else {
                                "인증코드를 확인해 주세요."
                            },
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isVerifyingCode = false, message = error.toVerifyCodeMessage())
                    }
                }
        }
    }

    fun signUp(
        email: String,
        password: String,
        passwordCheck: String,
        nickname: String,
        profileImageUrl: String?,
    ) {
        val validationMessage = validateSignUp(password, passwordCheck, nickname)
        if (validationMessage != null) {
            _uiState.update { it.copy(message = validationMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSigningUp = true, message = null) }
            runCatching {
                repository.signUp(
                    email = email.trim(),
                    password = password,
                    nickname = nickname.trim(),
                    profileImageUrl = profileImageUrl,
                )
            }
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isSigningUp = false,
                            signupCompleted = true,
                            message = response.message.ifBlank { "회원가입이 완료되었습니다." },
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isSigningUp = false, message = error.toSignUpMessage())
                    }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun consumeSignupCompleted() {
        _uiState.update { it.copy(signupCompleted = false) }
    }

    private fun validateSignUp(password: String, passwordCheck: String, nickname: String): String? {
        return when {
            !_uiState.value.emailVerified -> "이메일 인증을 먼저 완료해 주세요."
            nickname.trim().isBlank() -> "닉네임을 입력해 주세요."
            password != passwordCheck -> "비밀번호가 일치하지 않습니다."
            password.length < 8 -> "비밀번호는 8자 이상이어야 합니다."
            !password.any { it.isLetter() } -> "비밀번호에는 영문이 포함되어야 합니다."
            !password.any { it.isDigit() } -> "비밀번호에는 숫자가 포함되어야 합니다."
            password.toByteArray(Charsets.UTF_8).size > 72 -> "비밀번호는 최대 72바이트까지 가능합니다."
            else -> null
        }
    }

    private fun Throwable.toSendCodeMessage(): String {
        return when (this) {
            is AuthHttpException -> when (statusCode) {
                409 -> detail
                429 -> "잠시 후 다시 시도해 주세요."
                422 -> "이메일 형식을 확인해 주세요."
                else -> detail
            }
            else -> message ?: "인증코드 발송에 실패했습니다."
        }
    }

    private fun Throwable.toVerifyCodeMessage(): String {
        return when (this) {
            is AuthHttpException -> when (statusCode) {
                400 -> detail
                429 -> "시도 횟수를 초과했습니다. 인증코드를 다시 요청해 주세요."
                422 -> "인증코드는 6자리 숫자여야 합니다."
                else -> detail
            }
            else -> message ?: "인증코드 확인에 실패했습니다."
        }
    }

    private fun Throwable.toSignUpMessage(): String {
        return when (this) {
            is AuthHttpException -> when (statusCode) {
                403 -> "이메일 인증이 만료되었습니다. 이메일 인증부터 다시 진행해 주세요."
                409 -> detail
                422 -> "비밀번호 또는 닉네임 형식을 확인해 주세요."
                else -> detail
            }
            else -> message ?: "회원가입에 실패했습니다."
        }
    }
}
