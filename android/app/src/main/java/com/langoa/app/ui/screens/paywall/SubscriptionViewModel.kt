package com.langoa.app.ui.screens.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.langoa.app.data.local.TokenStorage
import com.langoa.app.data.remote.model.SubscriptionPlanDto
import com.langoa.app.domain.model.SubscriptionStatus
import com.langoa.app.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

data class PaywallUiState(
    val plans: List<SubscriptionPlanDto> = emptyList(),
    val currentStatus: SubscriptionStatus = SubscriptionStatus.FREE,
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false,
    val isRestoring: Boolean = false,
    val purchaseSuccess: Boolean = false,
    val restoreSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallUiState(
        currentStatus = tokenStorage.getCachedSubscriptionStatus()
    ))
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val statusResult = subscriptionRepository.getMySubscription()
            val plansResult = subscriptionRepository.getPlans()
            _uiState.value = _uiState.value.copy(
                currentStatus = statusResult.getOrDefault(tokenStorage.getCachedSubscriptionStatus()),
                plans = plansResult.getOrDefault(emptyList()),
                isLoading = false
            )
        }
    }

    fun onPlayPurchaseSuccess(planCode: String, purchaseToken: String, orderId: String, billingClient: BillingClient? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPurchasing = true, error = null)
            subscriptionRepository.verifyPlayPurchase(planCode, purchaseToken, orderId)
                .onSuccess { status ->
                    billingClient?.acknowledgeSubscription(purchaseToken)
                    _uiState.value = _uiState.value.copy(
                        currentStatus = status,
                        isPurchasing = false,
                        purchaseSuccess = true
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        error = e.message ?: "Purchase verification failed"
                    )
                }
        }
    }

    fun onPlayPurchaseFailure(code: Int, message: String) {
        _uiState.value = _uiState.value.copy(
            isPurchasing = false,
            error = "Purchase failed ($code): $message"
        )
    }

    fun restorePurchase() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRestoring = true, error = null)
            subscriptionRepository.restore()
                .onSuccess { status ->
                    _uiState.value = _uiState.value.copy(
                        currentStatus = status,
                        isRestoring = false,
                        restoreSuccess = true
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isRestoring = false,
                        error = e.message ?: "Restore failed"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearRestoreSuccess() {
        _uiState.value = _uiState.value.copy(restoreSuccess = false)
    }
}

private suspend fun BillingClient.acknowledgeSubscription(purchaseToken: String) =
    suspendCancellableCoroutine { cont ->
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        acknowledgePurchase(params) { cont.resume(it) }
    }
