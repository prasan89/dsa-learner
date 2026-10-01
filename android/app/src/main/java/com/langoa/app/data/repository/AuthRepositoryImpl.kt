package com.langoa.app.data.repository

import com.langoa.app.data.local.TokenStorage
import com.langoa.app.data.remote.api.AuthApi
import com.langoa.app.data.remote.model.AuthResponse
import com.langoa.app.data.remote.model.LoginRequest
import com.langoa.app.data.remote.model.RegisterRequest
import com.langoa.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            tokenStorage.saveTokens(response.accessToken, response.refreshToken)
            tokenStorage.saveUserId(response.user.id)
            tokenStorage.saveUserEmail(response.user.email)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<AuthResponse> {
        return try {
            val response = authApi.register(RegisterRequest(email, password, name))
            tokenStorage.saveTokens(response.accessToken, response.refreshToken)
            tokenStorage.saveUserId(response.user.id)
            tokenStorage.saveUserEmail(response.user.email)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isLoggedIn(): Boolean = tokenStorage.isLoggedIn()

    override fun getSelectedLanguage(): String? = tokenStorage.getSelectedLanguage()

    override suspend fun saveSelectedLanguage(languageCode: String) {
        tokenStorage.saveSelectedLanguage(languageCode)
    }

    override suspend fun logout() {
        tokenStorage.clearAll()
    }
}
