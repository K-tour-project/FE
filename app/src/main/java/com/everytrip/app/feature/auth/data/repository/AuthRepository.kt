package com.everytrip.app.feature.auth.data.repository

import com.everytrip.app.feature.auth.data.model.AuthSession
import com.everytrip.app.feature.auth.data.model.AuthUser
import com.everytrip.app.feature.auth.data.model.EmailCodeResponse
import com.everytrip.app.feature.auth.data.model.EmailVerifyResponse
import com.everytrip.app.feature.auth.data.model.SignUpResponse

interface AuthRepository {
    fun getOrCreateDeviceId(): String
    fun getStoredAccessToken(): String?
    fun getStoredRefreshToken(): String?
    suspend fun sendEmailCode(email: String): EmailCodeResponse
    suspend fun verifyEmailCode(email: String, code: String): EmailVerifyResponse
    suspend fun signUp(
        email: String,
        password: String,
        nickname: String,
        profileImageUrl: String?,
    ): SignUpResponse
    suspend fun login(email: String, password: String): AuthSession
    suspend fun googleLogin(idToken: String): AuthSession
    suspend fun kakaoLogin(accessToken: String): AuthSession
    suspend fun getMe(): AuthUser
    suspend fun logout()
    suspend fun logoutAll()
    fun clearTokens()
}
