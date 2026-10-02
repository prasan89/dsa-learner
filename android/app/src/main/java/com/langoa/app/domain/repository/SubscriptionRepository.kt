package com.langoa.app.domain.repository

import com.langoa.app.data.remote.model.SubscriptionPlanDto
import com.langoa.app.domain.model.SubscriptionStatus

interface SubscriptionRepository {
    suspend fun getMySubscription(): Result<SubscriptionStatus>
    suspend fun getPlans(): Result<List<SubscriptionPlanDto>>
    suspend fun verifyPlayPurchase(
        planCode: String,
        purchaseToken: String,
        orderId: String
    ): Result<SubscriptionStatus>
}
