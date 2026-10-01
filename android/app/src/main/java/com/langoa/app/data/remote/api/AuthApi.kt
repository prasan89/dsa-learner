package com.langoa.app.data.remote.api

import com.langoa.app.data.remote.model.AuthResponse
import com.langoa.app.data.remote.model.LoginRequest
import com.langoa.app.data.remote.model.RefreshTokenRequest
import com.langoa.app.data.remote.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): AuthResponse
}
