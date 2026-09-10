package com.everytrip.app.core.network

import com.everytrip.app.BuildConfig
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object NetworkProvider {
    val gson: Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            redactHeader("Authorization")
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                if (original.header("Accept") == null) {
                    requestBuilder.header("Accept", "application/json")
                }
                original.tag(AuthToken::class.java)?.let { token ->
                    requestBuilder.header("Authorization", "Bearer ${token.value}")
                }
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(normalizeBaseUrl(BuildConfig.BACKEND_BASE_URL))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun <T> create(service: Class<T>): T = retrofit.create(service)

    private fun normalizeBaseUrl(baseUrl: String): String {
        val trimmed = baseUrl.trim()
        require(trimmed.isNotBlank()) {
            "BACKEND_BASE_URL is not configured."
        }
        require(BuildConfig.DEBUG || trimmed.startsWith("https://", ignoreCase = true)) {
            "Release BACKEND_BASE_URL must use HTTPS."
        }
        return if (trimmed.endsWith('/')) trimmed else "$trimmed/"
    }

    private const val NETWORK_TIMEOUT_SECONDS = 10L
}
