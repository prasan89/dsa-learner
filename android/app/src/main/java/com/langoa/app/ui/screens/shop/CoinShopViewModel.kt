package com.langoa.app.ui.screens.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langoa.app.data.remote.model.CoinPackageDto
import com.langoa.app.domain.repository.CivilizationRepository
import com.langoa.app.domain.repository.CoinPurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CoinShopUiState(
    val packages: List<CoinPackageDto> = emptyList(),
    val currentCoins: Long = 0L,
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false,
    val purchaseSuccess: Boolean = false,
    val coinsAwarded: Long = 0L,
    val error: String? = null
)

@HiltViewModel
class CoinShopViewModel @Inject constructor(
    private val coinPurchaseRepository: CoinPurchaseRepository,
    private val civilizationRepository: CivilizationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoinShopUiState())
    val uiState: StateFlow<CoinShopUiState> = _uiState.asStateFlow()

    fun load(languageCode: String) {
        viewModelScope.launch {
            val packagesResult = coinPurchaseRepository.getPackages()
            packagesResult.fold(
                onSuccess = { packages ->
                    _uiState.value = _uiState.value.copy(packages = packages, isLoading = false)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load packages"
                    )
                }
            )
        }
        viewModelScope.launch {
            civilizationRepository.getCivilization(languageCode).collect { civ ->
                _uiState.value = _uiState.value.copy(currentCoins = civ?.coins ?: 0L)
            }
        }
    }

    fun onPlayPurchaseSuccess(
        packageCode: String,
        purchaseToken: String,
        orderId: String,
        languageCode: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPurchasing = true)
            val result = coinPurchaseRepository.verifyPlayPurchase(
                packageCode, purchaseToken, orderId, languageCode
            )
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        purchaseSuccess = true,
                        coinsAwarded = response.coinsAwarded,
                        currentCoins = response.newCoinBalance
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        error = e.message ?: "Purchase verification failed"
                    )
                }
            )
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

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(purchaseSuccess = false, coinsAwarded = 0L)
    }
}
