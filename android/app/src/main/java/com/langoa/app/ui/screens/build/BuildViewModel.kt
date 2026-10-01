package com.langoa.app.ui.screens.build

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langoa.app.domain.model.Building
import com.langoa.app.domain.model.Civilization
import com.langoa.app.domain.usecase.BuildBuildingUseCase
import com.langoa.app.domain.usecase.GetCivilizationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BuildUiState(
    val civilization: Civilization? = null,
    val availableBuildings: List<Building> = emptyList(),
    val isLoading: Boolean = true,
    val isBuildingInProgress: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BuildViewModel @Inject constructor(
    private val getCivilizationUseCase: GetCivilizationUseCase,
    private val buildBuildingUseCase: BuildBuildingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuildUiState())
    val uiState: StateFlow<BuildUiState> = _uiState.asStateFlow()

    fun loadData(languageCode: String) {
        viewModelScope.launch {
            getCivilizationUseCase(languageCode).collect { civ ->
                _uiState.value = _uiState.value.copy(civilization = civ, isLoading = false)
            }
        }
    }

    fun buildBuilding(languageCode: String, buildingType: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBuildingInProgress = true)
            val result = buildBuildingUseCase(languageCode, buildingType)
            result.fold(
                onSuccess = { civ ->
                    _uiState.value = _uiState.value.copy(
                        civilization = civ,
                        isBuildingInProgress = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Build failed",
                        isBuildingInProgress = false
                    )
                }
            )
        }
    }
}
