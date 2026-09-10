package com.everytrip.app.feature.auth.data.remote

import android.content.Context
import com.everytrip.app.feature.auth.data.local.AuthSecureStorage
import com.everytrip.app.feature.auth.data.model.AuthSession
import com.everytrip.app.feature.auth.data.model.AuthUser
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
    private val authApi = AuthApi()
    private val refreshMutex = Mutex()
    private val refreshScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var refreshInFlight: CompletableDeferred<AuthSession>? = null

    fun getOrCreateDeviceId(): String = storage.getOrCreateDeviceId()

    suspend fun <T> executeAuthenticated(request: suspend (String) -> T): T {
        val accessToken = storage.getAccessToken()
            ?: throw IllegalStateException("No access token.")

        return try {
            request(accessToken)
        } catch (error: AuthHttpException) {
            if (error.statusCode != 401) {
                throw error
            }

            val refreshedAccessToken = refreshAfterUnauthorized(accessToken).accessToken
            try {
                request(refreshedAccessToken)
            } catch (retryError: AuthHttpException) {
                if (retryError.statusCode == 401) {
                    throw SessionExpiredException(retryError)
                }
                throw retryError
            }
        }
    }

    suspend fun getAuthenticatedUser(request: suspend (String) -> AuthUser): AuthUser {
        val accessToken = storage.getAccessToken()
            ?: throw IllegalStateException("No access token.")

        return try {
            request(accessToken)
        } catch (error: AuthHttpException) {
            if (error.statusCode != 401) {
                throw error
            }
            val refreshResult = refreshAfterUnauthorized(accessToken)
            refreshResult.user ?: request(refreshResult.accessToken)
        }
    }

    private suspend fun refreshAfterUnauthorized(failedAccessToken: String): RefreshResult {
        val deferred = refreshMutex.withLock {
            storage.getAccessToken()
                ?.takeIf { it != failedAccessToken }
                ?.let { return RefreshResult(accessToken = it) }

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

        val session = deferred.await()
        return RefreshResult(
            accessToken = session.accessToken,
            user = session.user,
        )
    }

    private suspend fun refreshOnce(): AuthSession {
        val refreshToken = storage.getRefreshToken()
            ?: throw SessionExpiredException()
        val deviceId = storage.getDeviceId()
            ?: throw SessionExpiredException()

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

    private data class RefreshResult(
        val accessToken: String,
        val user: AuthUser? = null,
    )

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
