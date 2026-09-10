package com.everytrip.app.feature.auth.data.repository

import android.content.Context
import com.everytrip.app.feature.auth.data.local.AuthSecureStorage
import com.everytrip.app.feature.auth.data.model.AuthSession
import com.everytrip.app.feature.auth.data.model.AuthUser
import com.everytrip.app.feature.auth.data.model.EmailCodeResponse
import com.everytrip.app.feature.auth.data.model.EmailVerifyResponse
import com.everytrip.app.feature.auth.data.model.SignUpResponse
import com.everytrip.app.feature.auth.data.remote.AuthApi
import com.everytrip.app.feature.auth.data.remote.AuthSessionManager

class AuthRepositoryImpl(
    context: Context,
    private val authApi: AuthApi = AuthApi(),
) : AuthRepository {
    private val storage = AuthSecureStorage(context)
    private val sessionManager = AuthSessionManager.get(context)

    override fun initializeDeviceId(): String {
        if (storage.getDeviceId() == null &&
            (storage.getAccessToken() != null || storage.getRefreshToken() != null)
        ) {
            storage.clearTokens()
        }
        return storage.getOrCreateDeviceId()
    }

    override fun getOrCreateDeviceId(): String = sessionManager.getOrCreateDeviceId()

    override fun getStoredAccessToken(): String? = storage.getAccessToken()

    override fun getStoredRefreshToken(): String? = storage.getRefreshToken()

    override suspend fun sendEmailCode(email: String): EmailCodeResponse = authApi.sendEmailCode(email)

    override suspend fun verifyEmailCode(email: String, code: String): EmailVerifyResponse =
        authApi.verifyEmailCode(email, code)

    override suspend fun signUp(email: String, password: String, nickname: String): SignUpResponse =
        authApi.signUp(email, password, nickname)

    override suspend fun login(email: String, password: String): AuthSession {
        val session = authApi.login(
            email = email,
            password = password,
            deviceId = storage.getOrCreateDeviceId(),
        )
        storage.saveTokens(session.accessToken, session.refreshToken)
        return session
    }

    override suspend fun googleLogin(idToken: String): AuthSession {
        val session = authApi.googleLogin(
            idToken = idToken,
            deviceId = storage.getOrCreateDeviceId(),
        )
        storage.saveTokens(session.accessToken, session.refreshToken)
        return session
    }

    override suspend fun kakaoLogin(accessToken: String): AuthSession {
        val session = authApi.kakaoLogin(
            accessToken = accessToken,
            deviceId = storage.getOrCreateDeviceId(),
        )
        storage.saveTokens(session.accessToken, session.refreshToken)
        return session
    }

    override suspend fun getMe(): AuthUser {
        return sessionManager.getAuthenticatedUser { accessToken -> authApi.getMe(accessToken) }
    }

    override suspend fun refresh(): AuthSession {
        val refreshToken = storage.getRefreshToken() ?: throw IllegalStateException("No refresh token.")
        val session = authApi.refresh(
            refreshToken = refreshToken,
            deviceId = storage.getOrCreateDeviceId(),
        )
        storage.saveTokens(session.accessToken, session.refreshToken)
        return session
    }

    override suspend fun logout() {
        val refreshToken = storage.getRefreshToken()
        runCatching {
            if (refreshToken != null) {
                authApi.logout(refreshToken)
            }
        }
        storage.clearTokens()
    }

    override suspend fun logoutAll() {
        val accessToken = storage.getAccessToken()
        runCatching {
            if (accessToken != null) {
                authApi.logoutAll(accessToken)
            }
        }
        storage.clearTokens()
    }

    override fun clearTokens() {
        storage.clearTokens()
    }
}
