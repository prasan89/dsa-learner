package com.langoa.app.data.remote.api

import com.langoa.app.data.remote.model.CreateOrderRequest
import com.langoa.app.data.remote.model.CreateOrderResponse
import com.langoa.app.data.remote.model.EntitlementsResponse
import com.langoa.app.data.remote.model.SubscriptionPlanDto
import com.langoa.app.data.remote.model.SubscriptionStatusDto
import com.langoa.app.data.remote.model.VerifyPlayRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SubscriptionApi {
    @GET("api/v1/subscriptions/me")
    suspend fun getMySubscription(): SubscriptionStatusDto

    @GET("api/v1/subscriptions/plans")
    suspend fun getPlans(): List<SubscriptionPlanDto>

    @POST("api/v1/subscriptions/order")
    suspend fun createOrder(@Body request: CreateOrderRequest): CreateOrderResponse

    @POST("api/v1/subscriptions/verify-play")
    suspend fun verifyPlay(@Body request: VerifyPlayRequest): SubscriptionStatusDto

    @POST("api/v1/subscriptions/restore")
    suspend fun restore(): SubscriptionStatusDto

    @GET("api/v1/subscriptions/entitlements")
    suspend fun getEntitlements(): EntitlementsResponse
}
