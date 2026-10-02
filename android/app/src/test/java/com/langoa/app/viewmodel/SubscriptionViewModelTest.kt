package com.langoa.app.viewmodel

import app.cash.turbine.test
import com.langoa.app.data.local.TokenStorage
import com.langoa.app.data.remote.model.SubscriptionPlanDto
import com.langoa.app.domain.model.SubscriptionStatus
import com.langoa.app.domain.repository.SubscriptionRepository
import com.langoa.app.ui.screens.paywall.SubscriptionViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val subscriptionRepository: SubscriptionRepository = mockk()
    private val tokenStorage: TokenStorage = mockk()

    private lateinit var viewModel: SubscriptionViewModel

    private val freePlan = SubscriptionPlanDto("FREE", "Free", 0, "INR", 0, null)
    private val proMonthlyPlan = SubscriptionPlanDto("PRO_MONTHLY", "Pro Monthly", 49900, "INR", 30, "langoa_pro_monthly")
    private val proStatus = SubscriptionStatus("PRO_MONTHLY", isPro = true, expiresAt = "2027-01-01")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { tokenStorage.getCachedSubscriptionStatus() } returns SubscriptionStatus.FREE
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SubscriptionViewModel(subscriptionRepository, tokenStorage)

    @Test
    fun `initial state uses cached subscription status`() = runTest {
        every { tokenStorage.getCachedSubscriptionStatus() } returns proStatus
        val vm = createViewModel()
        assertEquals(proStatus, vm.uiState.value.currentStatus)
    }

    @Test
    fun `load online fetches plans and status`() = runTest {
        coEvery { subscriptionRepository.getMySubscription() } returns Result.success(proStatus)
        coEvery { subscriptionRepository.getPlans() } returns Result.success(listOf(freePlan, proMonthlyPlan))
        val vm = createViewModel()

        vm.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)

            vm.load()
            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(proStatus, loaded.currentStatus)
            assertEquals(2, loaded.plans.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `load offline falls back to cached status`() = runTest {
        val networkError = RuntimeException("No network")
        coEvery { subscriptionRepository.getMySubscription() } returns Result.failure(networkError)
        coEvery { subscriptionRepository.getPlans() } returns Result.failure(networkError)
        val vm = createViewModel()

        vm.load()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SubscriptionStatus.FREE, vm.uiState.value.currentStatus)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `onPlayPurchaseSuccess calls verify and sets pro status`() = runTest {
        coEvery { subscriptionRepository.verifyPlayPurchase("PRO_MONTHLY", "token123", "order456") } returns Result.success(proStatus)
        val vm = createViewModel()

        vm.onPlayPurchaseSuccess("PRO_MONTHLY", "token123", "order456")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.purchaseSuccess)
        assertFalse(state.isPurchasing)
        assertEquals(proStatus, state.currentStatus)
    }

    @Test
    fun `onPlayPurchaseSuccess on failure sets error`() = runTest {
        coEvery { subscriptionRepository.verifyPlayPurchase(any(), any(), any()) } returns Result.failure(RuntimeException("Verify failed"))
        val vm = createViewModel()

        vm.onPlayPurchaseSuccess("PRO_MONTHLY", "badtoken", "order999")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isPurchasing)
        assertFalse(state.purchaseSuccess)
        assertEquals("Verify failed", state.error)
    }

    @Test
    fun `onPlayPurchaseFailure sets error`() = runTest {
        val vm = createViewModel()
        vm.onPlayPurchaseFailure(1, "User canceled")
        assertEquals("Purchase failed (1): User canceled", vm.uiState.value.error)
    }

    @Test
    fun `clearError removes error from state`() = runTest {
        val vm = createViewModel()
        vm.onPlayPurchaseFailure(1, "Some error")
        vm.clearError()
        assertNull(vm.uiState.value.error)
    }
}
