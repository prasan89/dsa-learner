package com.langoa.app.domain.repository

import com.langoa.app.data.remote.model.AuthResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(email: String, password: String, name: String): Result<AuthResponse>
    fun isLoggedIn(): Boolean
    fun getSelectedLanguage(): String?
    suspend fun saveSelectedLanguage(languageCode: String)
    suspend fun logout()
}
