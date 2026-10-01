package com.langoa.app.ui.screens.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langoa.app.domain.model.LessonReward
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class RewardUiState(
    val reward: LessonReward? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class RewardViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RewardUiState())
    val uiState: StateFlow<RewardUiState> = _uiState.asStateFlow()

    fun setReward(reward: LessonReward) {
        _uiState.value = _uiState.value.copy(reward = reward)
    }
}
