package com.langoa.app.repository

import com.langoa.app.data.local.TokenStorage
import com.langoa.app.data.remote.api.SubscriptionApi
import com.langoa.app.data.remote.model.SubscriptionPlanDto
import com.langoa.app.data.remote.model.SubscriptionStatusDto
import com.langoa.app.data.remote.model.VerifyPlayRequest
import com.langoa.app.data.repository.SubscriptionRepositoryImpl
import com.langoa.app.domain.model.SubscriptionStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionRepositoryTest {

    private val subscriptionApi: SubscriptionApi = mockk()
    private val tokenStorage: TokenStorage = mockk(relaxed = true)
    private lateinit var repository: SubscriptionRepositoryImpl

    private val proDto = SubscriptionStatusDto("PRO_MONTHLY", true, "2026-01-01", "2027-01-01")
    private val proStatus = SubscriptionStatus("PRO_MONTHLY", true, "2027-01-01")

    @Before
    fun setup() {
        repository = SubscriptionRepositoryImpl(subscriptionApi, tokenStorage)
    }

    @Test
    fun `getMySubscription success caches and returns status`() = runTest {
        coEvery { subscriptionApi.getMySubscription() } returns proDto

        val result = repository.getMySubscription()

        assertTrue(result.isSuccess)
        assertEquals(proStatus, result.getOrNull())
        coVerify { tokenStorage.saveSubscriptionStatus("PRO_MONTHLY", true, "2027-01-01") }
    }

    @Test
    fun `getMySubscription offline returns cached status`() = runTest {
        coEvery { subscriptionApi.getMySubscription() } throws RuntimeException("Network error")
        every { tokenStorage.getCachedSubscriptionStatus() } returns SubscriptionStatus.FREE

        val result = repository.getMySubscription()

        assertTrue(result.isSuccess)
        assertEquals(SubscriptionStatus.FREE, result.getOrNull())
    }

    @Test
    fun `getPlans returns plan list`() = runTest {
        val plans = listOf(
            SubscriptionPlanDto("FREE", "Free", 0, "INR", 0, null),
            SubscriptionPlanDto("PRO_MONTHLY", "Pro Monthly", 49900, "INR", 30, "langoa_pro_monthly")
        )
        coEvery { subscriptionApi.getPlans() } returns plans

        val result = repository.getPlans()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `verifyPlayPurchase success caches and returns status`() = runTest {
        coEvery {
            subscriptionApi.verifyPlay(VerifyPlayRequest("PRO_MONTHLY", "token123", "order456"))
        } returns proDto

        val result = repository.verifyPlayPurchase("PRO_MONTHLY", "token123", "order456")

        assertTrue(result.isSuccess)
        assertEquals(proStatus, result.getOrNull())
        coVerify { tokenStorage.saveSubscriptionStatus("PRO_MONTHLY", true, "2027-01-01") }
    }

    @Test
    fun `verifyPlayPurchase failure returns failure result`() = runTest {
        coEvery { subscriptionApi.verifyPlay(any()) } throws RuntimeException("Server error")

        val result = repository.verifyPlayPurchase("PRO_MONTHLY", "badtoken", "order999")

        assertTrue(result.isFailure)
    }
}
