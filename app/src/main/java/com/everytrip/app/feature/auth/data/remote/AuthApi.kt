package com.everytrip.app.feature.auth.data.remote

import com.everytrip.app.core.network.NetworkProvider
import com.everytrip.app.core.network.AuthToken
import com.everytrip.app.feature.auth.data.model.AuthSession
import com.everytrip.app.feature.auth.data.model.AuthUser
import com.everytrip.app.feature.auth.data.model.EmailCodeRequest
import com.everytrip.app.feature.auth.data.model.EmailCodeResponse
import com.everytrip.app.feature.auth.data.model.EmailVerifyRequest
import com.everytrip.app.feature.auth.data.model.EmailVerifyResponse
import com.everytrip.app.feature.auth.data.model.GoogleLoginRequest
import com.everytrip.app.feature.auth.data.model.KakaoLoginRequest
import com.everytrip.app.feature.auth.data.model.LoginRequest
import com.everytrip.app.feature.auth.data.model.LogoutRequest
import com.everytrip.app.feature.auth.data.model.RefreshRequest
import com.everytrip.app.feature.auth.data.model.SignUpRequest
import com.everytrip.app.feature.auth.data.model.SignUpResponse
import com.google.gson.JsonParser
import retrofit2.HttpException

class AuthApi private constructor(
    private val service: AuthService,
) {
    constructor() : this(NetworkProvider.create(AuthService::class.java))

    suspend fun sendEmailCode(email: String): EmailCodeResponse = execute {
        service.sendEmailCode(EmailCodeRequest(email))
    }

    suspend fun verifyEmailCode(email: String, code: String): EmailVerifyResponse = execute {
        service.verifyEmailCode(EmailVerifyRequest(email, code))
    }

    suspend fun signUp(email: String, password: String, nickname: String): SignUpResponse = execute {
        service.signUp(SignUpRequest(email, password, nickname))
    }

    suspend fun login(email: String, password: String, deviceId: String): AuthSession = execute {
        service.login(LoginRequest(email, password, deviceId))
    }

    suspend fun googleLogin(idToken: String, deviceId: String): AuthSession = execute {
        service.googleLogin(GoogleLoginRequest(idToken, deviceId))
    }

    suspend fun kakaoLogin(accessToken: String, deviceId: String): AuthSession = execute {
        service.kakaoLogin(KakaoLoginRequest(accessToken, deviceId))
    }

    suspend fun getMe(accessToken: String): AuthUser = execute {
        service.getMe(AuthToken(accessToken))
    }

    suspend fun refresh(refreshToken: String, deviceId: String): AuthSession = execute {
        service.refresh(RefreshRequest(refreshToken, deviceId))
    }

    suspend fun logout(refreshToken: String) = execute {
        service.logout(LogoutRequest(refreshToken))
    }

    suspend fun logoutAll(accessToken: String) = execute {
        service.logoutAll(AuthToken(accessToken))
    }

    private suspend fun <T> execute(request: suspend () -> T): T {
        return try {
            request()
        } catch (error: HttpException) {
            throw error.toAuthHttpException()
        }
    }

    private fun HttpException.toAuthHttpException(): AuthHttpException {
        val statusCode = code()
        val responseBody = response()?.errorBody()?.string().orEmpty()
        return AuthHttpException(
            statusCode = statusCode,
            detail = parseDetail(responseBody, statusCode),
        )
    }

    private fun parseDetail(body: String, responseCode: Int): String {
        val fallback = "요청을 처리하지 못했습니다. code=$responseCode"
        if (body.isBlank()) return fallback

        return runCatching {
            val detail = JsonParser.parseString(body).asJsonObject.get("detail")
                ?: return@runCatching fallback
            if (detail.isJsonPrimitive && detail.asJsonPrimitive.isString) {
                detail.asString
            } else {
                detail.toString()
            }
        }.getOrDefault(fallback)
    }
}
