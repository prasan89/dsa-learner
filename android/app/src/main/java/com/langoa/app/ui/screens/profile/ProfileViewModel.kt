package com.langoa.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langoa.app.domain.model.Civilization
import com.langoa.app.domain.usecase.GetCivilizationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LanguageProgressUiModel(
    val languageCode: String,
    val displayName: String,
    val cefrLevel: String,
    val progressPercent: Float
)

data class ProfileUiState(
    val displayName: String = "Language Explorer",
    val explorerLevel: Int = 1,
    val dayStreak: Int = 0,
    val totalXp: Long = 0L,
    val tier: String = "VILLAGE",
    val languageProgress: List<LanguageProgressUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCivilizationUseCase: GetCivilizationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun load(languageCode: String) {
        viewModelScope.launch {
            getCivilizationUseCase(languageCode).collect { civ ->
                if (civ == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@collect
                }
                _uiState.value = ProfileUiState(
                    displayName = "${civ.name} Builder",
                    explorerLevel = civ.tierLevel,
                    totalXp = civ.totalXp,
                    tier = "VILLAGE",
                    languageProgress = listOf(
                        LanguageProgressUiModel(
                            languageCode = languageCode,
                            displayName = languageDisplayName(languageCode),
                            cefrLevel = "A1",
                            progressPercent = (civ.totalXp % 1000).toFloat() / 1000f
                        )
                    ),
                    isLoading = false
                )
            }
        }
    }

    private fun languageDisplayName(code: String) = when (code) {
        "de" -> "German"
        "hi" -> "Hindi"
        "kn" -> "Kannada"
        "fr" -> "French"
        "es" -> "Spanish"
        else -> code.uppercase()
    }
}
