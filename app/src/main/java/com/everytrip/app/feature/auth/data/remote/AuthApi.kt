package com.everytrip.app.feature.auth.data.remote

import com.everytrip.app.BuildConfig
import com.everytrip.app.feature.auth.data.model.AuthSession
import com.everytrip.app.feature.auth.data.model.AuthUser
import com.everytrip.app.feature.auth.data.model.EmailCodeResponse
import com.everytrip.app.feature.auth.data.model.EmailVerifyResponse
import com.everytrip.app.feature.auth.data.model.SignUpResponse
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class AuthApi(
    private val baseUrl: String = BuildConfig.BACKEND_BASE_URL,
) {
    fun sendEmailCode(email: String): EmailCodeResponse {
        val json = post(
            path = "/auth/email/send-code",
            body = JSONObject().put("email", email),
        )
        val root = JSONObject(json)
        return EmailCodeResponse(
            expiresIn = root.optInt("expires_in"),
            devCode = root.optNullableString("dev_code"),
        )
    }

    fun verifyEmailCode(email: String, code: String): EmailVerifyResponse {
        val json = post(
            path = "/auth/email/verify-code",
            body = JSONObject()
                .put("email", email)
                .put("code", code),
        )
        val root = JSONObject(json)
        return EmailVerifyResponse(
            verified = root.optBoolean("verified"),
            signupDeadlineMinutes = root.optInt("signup_deadline_minutes"),
        )
    }

    fun signUp(email: String, password: String, nickname: String): SignUpResponse {
        val json = post(
            path = "/auth/signup",
            body = JSONObject()
                .put("email", email)
                .put("password", password)
                .put("nickname", nickname),
        )
        val root = JSONObject(json)
        return SignUpResponse(
            message = root.optString("message"),
            user = root.getJSONObject("user").toAuthUser(),
        )
    }

    fun login(email: String, password: String, deviceId: String): AuthSession {
        val json = post(
            path = "/auth/login",
            body = JSONObject()
                .put("email", email)
                .put("password", password)
                .put("device_id", deviceId),
        )
        return JSONObject(json).toAuthSession()
    }

    fun googleLogin(idToken: String, deviceId: String): AuthSession {
        val json = post(
            path = "/auth/google",
            body = JSONObject()
                .put("id_token", idToken)
                .put("device_id", deviceId),
        )
        return JSONObject(json).toAuthSession()
    }

    fun kakaoLogin(accessToken: String, deviceId: String): AuthSession {
        val json = post(
            path = "/auth/kakao",
            body = JSONObject()
                .put("access_token", accessToken)
                .put("device_id", deviceId),
        )
        return JSONObject(json).toAuthSession()
    }

    fun getMe(accessToken: String): AuthUser {
        val json = request(
            method = "GET",
            path = "/auth/me",
            authorization = accessToken,
        )
        return JSONObject(json).toAuthUser()
    }

    fun refresh(refreshToken: String, deviceId: String): AuthSession {
        val json = post(
            path = "/auth/refresh",
            body = JSONObject()
                .put("refresh_token", refreshToken)
                .put("device_id", deviceId),
        )
        return JSONObject(json).toAuthSession()
    }

    fun logout(refreshToken: String) {
        post(
            path = "/auth/logout",
            body = JSONObject().put("refresh_token", refreshToken),
        )
    }

    fun logoutAll(accessToken: String) {
        request(
            method = "POST",
            path = "/auth/logout-all",
            authorization = accessToken,
        )
    }

    private fun post(path: String, body: JSONObject): String {
        return request(method = "POST", path = path, body = body)
    }

    private fun request(
        method: String,
        path: String,
        body: JSONObject? = null,
        authorization: String? = null,
    ): String {
        val normalizedBaseUrl = baseUrl.trimEnd('/')
        require(normalizedBaseUrl.isNotBlank()) {
            "BACKEND_BASE_URL is not configured."
        }

        val connection = (URL("$normalizedBaseUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            if (authorization != null) {
                setRequestProperty("Authorization", "Bearer $authorization")
            }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }

        return try {
            if (body != null) {
                connection.outputStream.use { stream ->
                    stream.write(body.toString().toByteArray(Charsets.UTF_8))
                }
            }

            val responseCode = connection.responseCode
            val bodyText = connection.readBody(responseCode)
            if (responseCode !in 200..299) {
                throw AuthHttpException(
                    statusCode = responseCode,
                    detail = parseDetail(bodyText, responseCode),
                )
            }
            bodyText
        } finally {
            connection.disconnect()
        }
    }

    private fun HttpURLConnection.readBody(responseCode: Int): String {
        val stream = if (responseCode in 200..299) inputStream else errorStream
        return stream?.let {
            BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() }
        }.orEmpty()
    }

    private fun JSONObject.toAuthSession(): AuthSession {
        return AuthSession(
            accessToken = getString("access_token"),
            refreshToken = getString("refresh_token"),
            tokenType = optString("token_type", "bearer"),
            expiresIn = optInt("expires_in"),
            user = getJSONObject("user").toAuthUser(),
        )
    }

    private fun JSONObject.toAuthUser(): AuthUser {
        return AuthUser(
            userId = optInt("user_id"),
            nickname = optString("nickname"),
            authProvider = optString("auth_provider"),
            email = optString("email"),
            emailVerified = optBoolean("email_verified"),
            createdAt = optString("created_at"),
        )
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (!has(name) || isNull(name)) {
            return null
        }
        return optString(name).takeIf { it.isNotBlank() }
    }

    private fun parseDetail(body: String, responseCode: Int): String {
        val fallback = "요청을 처리하지 못했습니다. code=$responseCode"
        if (body.isBlank()) {
            return fallback
        }
        return runCatching {
            when (val detail = JSONObject(body).opt("detail")) {
                is String -> detail
                null -> fallback
                else -> detail.toString()
            }
        }.getOrDefault(fallback)
    }

    private companion object {
        const val TIMEOUT_MS = 10_000
    }
}
