package com.everytrip.app.feature.auth.data.remote

import android.content.Context
import com.everytrip.app.feature.auth.data.local.AuthSecureStorage
import com.everytrip.app.feature.auth.data.model.AuthSession
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Runs authenticated requests and coordinates one refresh across the whole app. */
class AuthSessionManager private constructor(context: Context) {
    private val storage = AuthSecureStorage(context)
    private val authApi by lazy { AuthApi() }
    private val refreshMutex = Mutex()
    private val refreshScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var refreshInFlight: CompletableDeferred<AuthSession>? = null

    fun initializeDeviceId(): String = storage.initializeDeviceId()

    fun getOrCreateDeviceId(): String = storage.getOrCreateDeviceId()

    fun getStoredAccessToken(): String? = storage.getTokenPair()?.accessToken

    fun getStoredRefreshToken(): String? = storage.getTokenPair()?.refreshToken

    fun saveTokens(accessToken: String, refreshToken: String) {
        storage.saveTokens(accessToken, refreshToken)
    }

    fun clearTokens() = storage.clearTokens()

    suspend fun <T> executeAuthenticated(request: suspend (String) -> T): T {
        val accessToken = storage.getTokenPair()?.accessToken
            ?: throw SessionExpiredException()

        return try {
            request(accessToken)
        } catch (error: AuthHttpException) {
            if (error.statusCode != 401) {
                throw error
            }

            val refreshedAccessToken = refreshAfterUnauthorized(accessToken)
            try {
                request(refreshedAccessToken)
            } catch (retryError: AuthHttpException) {
                if (retryError.statusCode == 401) {
                    storage.clearTokens()
                    throw SessionExpiredException(retryError)
                }
                throw retryError
            }
        }
    }

    private suspend fun refreshAfterUnauthorized(failedAccessToken: String): String {
        val deferred = refreshMutex.withLock {
            storage.getTokenPair()?.accessToken
                ?.takeIf { it != failedAccessToken }
                ?.let { return it }

            refreshInFlight ?: CompletableDeferred<AuthSession>().also { created ->
                refreshInFlight = created
                refreshScope.launch {
                    try {
                        val session = refreshOnce()
                        created.complete(session)
                    } catch (error: Throwable) {
                        created.completeExceptionally(error)
                    } finally {
                        refreshMutex.withLock {
                            if (refreshInFlight === created) {
                                refreshInFlight = null
                            }
                        }
                    }
                }
            }
        }

        return deferred.await().accessToken
    }

    private suspend fun refreshOnce(): AuthSession {
        val refreshToken = storage.getTokenPair()?.refreshToken
            ?: throw SessionExpiredException()
        val deviceId = storage.getDeviceId()
            ?: run {
                storage.clearTokens()
                throw SessionExpiredException()
            }

        return try {
            authApi.refresh(refreshToken, deviceId).also { session ->
                storage.saveTokens(session.accessToken, session.refreshToken)
            }
        } catch (error: AuthHttpException) {
            if (error.statusCode == 401) {
                storage.clearTokens()
                throw SessionExpiredException(error)
            }
            throw error
        }
    }

    companion object {
        @Volatile
        private var instance: AuthSessionManager? = null

        fun get(context: Context): AuthSessionManager {
            return instance ?: synchronized(this) {
                instance ?: AuthSessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
