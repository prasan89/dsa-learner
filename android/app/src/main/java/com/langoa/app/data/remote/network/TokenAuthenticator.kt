package com.langoa.app.data.remote.network

import com.langoa.app.auth.AuthEventBus
import com.langoa.app.data.local.TokenStorage
import com.langoa.app.data.remote.api.AuthApi
import com.langoa.app.data.remote.model.RefreshTokenRequest
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val authApi: Lazy<AuthApi>,
    private val authEventBus: AuthEventBus
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.contains("/api/auth/refresh")) {
            runBlocking { tokenStorage.clearAll() }
            authEventBus.postLogout()
            return null
        }

        if (responseCount(response) >= 2) return null

        // runBlocking is acceptable here — called on OkHttp's background thread pool.
        val refreshToken = tokenStorage.getRefreshToken() ?: run {
            authEventBus.postLogout()
            return null
        }

        return runBlocking {
            try {
                val authResponse = authApi.get().refresh(RefreshTokenRequest(refreshToken))
                tokenStorage.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${authResponse.accessToken}")
                    .build()
            } catch (e: Exception) {
                tokenStorage.clearAll()
                authEventBus.postLogout()
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
