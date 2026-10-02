package com.langoa.app.data.repository

import com.langoa.app.data.local.TokenStorage
import com.langoa.app.data.remote.api.SubscriptionApi
import com.langoa.app.data.remote.model.EntitlementsResponse
import com.langoa.app.data.remote.model.SubscriptionPlanDto
import com.langoa.app.data.remote.model.VerifyPlayRequest
import com.langoa.app.domain.model.SubscriptionStatus
import com.langoa.app.domain.repository.SubscriptionRepository
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val subscriptionApi: SubscriptionApi,
    private val tokenStorage: TokenStorage
) : SubscriptionRepository {

    override suspend fun getMySubscription(): Result<SubscriptionStatus> = runCatching {
        val dto = subscriptionApi.getMySubscription()
        val status = SubscriptionStatus(
            planCode = dto.planCode,
            isPro = dto.isPro,
            expiresAt = dto.currentPeriodEnd
        )
        tokenStorage.saveSubscriptionStatus(status.planCode, status.isPro, status.expiresAt)
        status
    }.recoverCatching {
        tokenStorage.getCachedSubscriptionStatus()
    }

    override suspend fun getPlans(): Result<List<SubscriptionPlanDto>> = runCatching {
        subscriptionApi.getPlans()
    }

    override suspend fun verifyPlayPurchase(
        planCode: String,
        purchaseToken: String,
        orderId: String
    ): Result<SubscriptionStatus> = runCatching {
        val dto = subscriptionApi.verifyPlay(VerifyPlayRequest(planCode, purchaseToken, orderId))
        val status = SubscriptionStatus(
            planCode = dto.planCode,
            isPro = dto.isPro,
            expiresAt = dto.currentPeriodEnd
        )
        tokenStorage.saveSubscriptionStatus(status.planCode, status.isPro, status.expiresAt)
        status
    }

    override suspend fun restore(): Result<SubscriptionStatus> = runCatching {
        val dto = subscriptionApi.restore()
        val status = SubscriptionStatus(
            planCode = dto.planCode,
            isPro = dto.isPro,
            expiresAt = dto.currentPeriodEnd
        )
        tokenStorage.saveSubscriptionStatus(status.planCode, status.isPro, status.expiresAt)
        status
    }

    override suspend fun getEntitlements(): Result<EntitlementsResponse> = runCatching {
        subscriptionApi.getEntitlements()
    }
}
