package com.langoa.app.ui.screens.build

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langoa.app.domain.model.BuildingDefinition
import com.langoa.app.domain.model.Civilization
import com.langoa.app.domain.usecase.BuildBuildingUseCase
import com.langoa.app.domain.usecase.GetCivilizationUseCase
import com.langoa.app.domain.usecase.UpgradeBuildingUseCase
import com.langoa.app.domain.repository.CivilizationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BuildUiState(
    val civilization: Civilization? = null,
    val buildingDefinitions: List<BuildingDefinition> = emptyList(),
    val isLoading: Boolean = true,
    val isBuildingInProgress: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class BuildViewModel @Inject constructor(
    private val getCivilizationUseCase: GetCivilizationUseCase,
    private val buildBuildingUseCase: BuildBuildingUseCase,
    private val upgradeBuildingUseCase: UpgradeBuildingUseCase,
    private val civilizationRepository: CivilizationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuildUiState())
    val uiState: StateFlow<BuildUiState> = _uiState.asStateFlow()

    fun loadData(languageCode: String) {
        viewModelScope.launch {
            getCivilizationUseCase(languageCode).collect { civ ->
                _uiState.value = _uiState.value.copy(civilization = civ, isLoading = false)
            }
        }
        viewModelScope.launch {
            civilizationRepository.getBuildingDefinitions(languageCode).fold(
                onSuccess = { defs ->
                    _uiState.value = _uiState.value.copy(buildingDefinitions = defs)
                },
                onFailure = { /* silently ignore — buildings list is still useful */ }
            )
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
                        isBuildingInProgress = false,
                        successMessage = "Building placed!"
                    )
                    loadDefinitions(languageCode)
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

    fun upgradeBuilding(languageCode: String, buildingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBuildingInProgress = true)
            val result = upgradeBuildingUseCase(languageCode, buildingId)
            result.fold(
                onSuccess = { civ ->
                    _uiState.value = _uiState.value.copy(
                        civilization = civ,
                        isBuildingInProgress = false,
                        successMessage = "Building upgraded!"
                    )
                    loadDefinitions(languageCode)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Upgrade failed",
                        isBuildingInProgress = false
                    )
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }

    private fun loadDefinitions(languageCode: String) {
        viewModelScope.launch {
            civilizationRepository.getBuildingDefinitions(languageCode).fold(
                onSuccess = { defs ->
                    _uiState.value = _uiState.value.copy(buildingDefinitions = defs)
                },
                onFailure = { /* best-effort refresh */ }
            )
        }
    }
}
