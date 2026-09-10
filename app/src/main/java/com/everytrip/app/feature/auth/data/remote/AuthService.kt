package com.everytrip.app.feature.auth.data.remote

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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Tag

internal interface AuthService {
    @POST("auth/email/send-code")
    suspend fun sendEmailCode(@Body request: EmailCodeRequest): EmailCodeResponse

    @POST("auth/email/verify-code")
    suspend fun verifyEmailCode(@Body request: EmailVerifyRequest): EmailVerifyResponse

    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): SignUpResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthSession

    @POST("auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): AuthSession

    @POST("auth/kakao")
    suspend fun kakaoLogin(@Body request: KakaoLoginRequest): AuthSession

    @GET("auth/me")
    suspend fun getMe(@Tag authToken: AuthToken): AuthUser

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthSession

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest)

    @POST("auth/logout-all")
    suspend fun logoutAll(@Tag authToken: AuthToken)
}
