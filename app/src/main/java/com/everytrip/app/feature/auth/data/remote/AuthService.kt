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
import com.everytrip.app.feature.auth.data.model.PasswordResetCodeRequest
import com.everytrip.app.feature.auth.data.model.PasswordResetVerifyRequest
import com.everytrip.app.feature.auth.data.model.PasswordResetVerifyResponse
import com.everytrip.app.feature.auth.data.model.PasswordResetConfirmRequest
import com.everytrip.app.feature.auth.data.model.PasswordResetConfirmResponse
import com.everytrip.app.feature.auth.data.model.RefreshRequest
import com.everytrip.app.feature.auth.data.model.SignUpResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Tag

internal interface AuthService {
    @POST("auth/password-reset/send-code")
    suspend fun sendPasswordResetCode(@Body request: PasswordResetCodeRequest)

    @POST("auth/password-reset/verify-code")
    suspend fun verifyPasswordResetCode(@Body request: PasswordResetVerifyRequest): PasswordResetVerifyResponse

    @POST("auth/password-reset/confirm")
    suspend fun confirmPasswordReset(@Body request: PasswordResetConfirmRequest): PasswordResetConfirmResponse

    @POST("auth/email/send-code")
    suspend fun sendEmailCode(@Body request: EmailCodeRequest): EmailCodeResponse

    @POST("auth/email/verify-code")
    suspend fun verifyEmailCode(@Body request: EmailVerifyRequest): EmailVerifyResponse

    @Multipart
    @POST("auth/signup")
    suspend fun signUp(
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("nickname") nickname: RequestBody,
        @Part profileImage: MultipartBody.Part?,
    ): SignUpResponse

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
