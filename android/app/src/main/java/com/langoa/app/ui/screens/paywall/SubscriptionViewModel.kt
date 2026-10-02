package com.langoa.app.ui.screens.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langoa.app.data.local.TokenStorage
import com.langoa.app.data.remote.model.SubscriptionPlanDto
import com.langoa.app.domain.model.SubscriptionStatus
import com.langoa.app.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    val plans: List<SubscriptionPlanDto> = emptyList(),
    val currentStatus: SubscriptionStatus = SubscriptionStatus.FREE,
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false,
    val purchaseSuccess: Boolean = false,
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

    fun onPlayPurchaseSuccess(planCode: String, purchaseToken: String, orderId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPurchasing = true, error = null)
            subscriptionRepository.verifyPlayPurchase(planCode, purchaseToken, orderId)
                .onSuccess { status ->
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
