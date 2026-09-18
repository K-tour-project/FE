package com.everytrip.app.feature.auth.data.repository

import android.content.Context
import com.everytrip.app.feature.auth.data.model.AuthSession
import com.everytrip.app.feature.auth.data.model.AuthUser
import com.everytrip.app.feature.auth.data.model.EmailCodeResponse
import com.everytrip.app.feature.auth.data.model.EmailVerifyResponse
import com.everytrip.app.feature.auth.data.model.SignUpResponse
import com.everytrip.app.feature.auth.data.model.ProfileImageUpload
import com.everytrip.app.feature.auth.data.model.PasswordResetConfirmResponse
import com.everytrip.app.feature.auth.data.local.AuthSecureStorage
import com.everytrip.app.feature.auth.data.remote.AuthApi
import com.everytrip.app.feature.auth.data.remote.AuthSessionManager

class AuthRepositoryImpl(
    context: Context,
    private val authApi: AuthApi = AuthApi(),
) : AuthRepository {
    private val sessionManager = AuthSessionManager.get(context)
    private val secureStorage = AuthSecureStorage(context)

    override fun getOrCreateDeviceId(): String = sessionManager.getOrCreateDeviceId()

    override fun getStoredAccessToken(): String? = sessionManager.getStoredAccessToken()

    override fun getStoredRefreshToken(): String? = sessionManager.getStoredRefreshToken()

    override suspend fun sendEmailCode(email: String): EmailCodeResponse = authApi.sendEmailCode(email)

    override suspend fun verifyEmailCode(email: String, code: String): EmailVerifyResponse =
        authApi.verifyEmailCode(email, code)

    override suspend fun sendPasswordResetCode(email: String) {
        secureStorage.clearPasswordResetToken()
        authApi.sendPasswordResetCode(email)
    }

    override suspend fun verifyPasswordResetCode(email: String, code: String) {
        secureStorage.clearPasswordResetToken()
        val token = authApi.verifyPasswordResetCode(email, code).resetToken
        require(token.isNotBlank()) { "인증 응답에 재설정 토큰이 없습니다." }
        secureStorage.savePasswordResetToken(token)
    }

    override suspend fun confirmPasswordReset(password: String): PasswordResetConfirmResponse {
        val token = secureStorage.getPasswordResetToken()
            ?: throw IllegalStateException("이메일 인증을 다시 진행해 주세요.")
        return try {
            authApi.confirmPasswordReset(token, password).also {
                secureStorage.clearPasswordResetToken()
                sessionManager.clearTokens()
            }
        } catch (error: Exception) {
            if (error is com.everytrip.app.feature.auth.data.remote.AuthHttpException && error.statusCode == 400) {
                secureStorage.clearPasswordResetToken()
            }
            throw error
        }
    }

    override fun clearPasswordResetToken() = secureStorage.clearPasswordResetToken()

    override suspend fun signUp(
        email: String,
        password: String,
        nickname: String,
        profileImage: ProfileImageUpload?,
    ): SignUpResponse = authApi.signUp(email, password, nickname, profileImage)

    override suspend fun login(email: String, password: String): AuthSession {
        val session = authApi.login(
            email = email,
            password = password,
            deviceId = sessionManager.getOrCreateDeviceId(),
        )
        sessionManager.saveTokens(session.accessToken, session.refreshToken)
        return session
    }

    override suspend fun googleLogin(idToken: String): AuthSession {
        val session = authApi.googleLogin(
            idToken = idToken,
            deviceId = sessionManager.getOrCreateDeviceId(),
        )
        sessionManager.saveTokens(session.accessToken, session.refreshToken)
        return session
    }

    override suspend fun kakaoLogin(accessToken: String): AuthSession {
        val session = authApi.kakaoLogin(
            accessToken = accessToken,
            deviceId = sessionManager.getOrCreateDeviceId(),
        )
        sessionManager.saveTokens(session.accessToken, session.refreshToken)
        return session
    }

    override suspend fun getMe(): AuthUser {
        return sessionManager.executeAuthenticated { accessToken -> authApi.getMe(accessToken) }
    }

    override suspend fun logout() {
        val refreshToken = sessionManager.getStoredRefreshToken()
        runCatching {
            if (refreshToken != null) {
                authApi.logout(refreshToken)
            }
        }
        sessionManager.clearTokens()
    }

    override suspend fun logoutAll() {
        runCatching {
            if (sessionManager.getStoredAccessToken() != null) {
                sessionManager.executeAuthenticated { accessToken ->
                    authApi.logoutAll(accessToken)
                }
            }
        }
        sessionManager.clearTokens()
    }

    override fun clearTokens() {
        sessionManager.clearTokens()
    }
}
