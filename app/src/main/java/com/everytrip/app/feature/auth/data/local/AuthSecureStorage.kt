package com.everytrip.app.feature.auth.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID

@Suppress("DEPRECATION")
class AuthSecureStorage(context: Context) {
    private val appContext = context.applicationContext
    private val preferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            appContext,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun getOrCreateDeviceId(): String {
        preferences.getString(KEY_DEVICE_ID, null)?.let { return it }
        return "android_${UUID.randomUUID()}".also { deviceId ->
            preferences.edit()
                .putString(KEY_DEVICE_ID, deviceId)
                .apply()
        }
    }

    fun getAccessToken(): String? = preferences.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = preferences.getString(KEY_REFRESH_TOKEN, null)

    fun saveTokens(accessToken: String, refreshToken: String) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun clearTokens() {
        preferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    companion object {
        const val PREF_NAME = "auth_secure_storage"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
