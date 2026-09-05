package com.everytrip.app.feature.auth.data.local

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AuthSecureStorage(context: Context) {
    private val appContext = context.applicationContext
    private val preferences: SharedPreferences by lazy {
        appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getOrCreateDeviceId(): String {
        getDeviceId()?.let { return it }
        return "android_${java.util.UUID.randomUUID()}".also { deviceId ->
            preferences.edit {
                putEncrypted(KEY_DEVICE_ID, deviceId)
            }
        }
    }

    fun getDeviceId(): String? = getEncrypted(KEY_DEVICE_ID)

    fun getAccessToken(): String? = getEncrypted(KEY_ACCESS_TOKEN)

    fun getRefreshToken(): String? = getEncrypted(KEY_REFRESH_TOKEN)

    fun saveTokens(accessToken: String, refreshToken: String) {
        preferences.edit {
            putEncrypted(KEY_ACCESS_TOKEN, accessToken)
            putEncrypted(KEY_REFRESH_TOKEN, refreshToken)
        }
    }

    fun clearTokens() {
        preferences.edit {
            remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
        }
    }

    private fun SharedPreferences.Editor.putEncrypted(key: String, value: String) {
        putString(key, encrypt(value))
    }

    private fun getEncrypted(key: String): String? {
        return preferences.getString(key, null)?.let { encrypted ->
            runCatching { decrypt(encrypted) }.getOrNull()
        }
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val decoded = Base64.decode(value, Base64.NO_WRAP)
        require(decoded.size > IV_SIZE) { "Encrypted value is invalid." }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(TAG_LENGTH_BITS, decoded.copyOf(IV_SIZE)),
        )
        return String(
            cipher.doFinal(decoded.copyOfRange(IV_SIZE, decoded.size)),
            Charsets.UTF_8,
        )
    }

    private fun getOrCreateKey(): SecretKey = synchronized(keyLock) {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
            load(null)
        }

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEY_STORE,
            ).apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(true)
                        .build(),
                )
                generateKey()
            }
        }

        (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    private companion object {
        const val PREF_NAME = "auth_secure_storage_v2"
        const val KEY_ALIAS = "everytrip_auth_key_v2"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE = 12
        const val TAG_LENGTH_BITS = 128
        val keyLock = Any()
    }
}
