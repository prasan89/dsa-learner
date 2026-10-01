package com.langoa.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langoa.app.domain.model.Civilization
import com.langoa.app.domain.model.Lesson
import com.langoa.app.domain.usecase.GetCivilizationUseCase
import com.langoa.app.domain.usecase.GetLessonsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BuildingUiModel(
    val id: String,
    val buildingType: String,
    val name: String,
    val level: Int,
    val emoji: String = "🏠"
)

data class LessonSummaryUiModel(
    val id: String,
    val title: String,
    val exerciseCount: Int,
    val xpReward: Int
)

data class HomeUiState(
    val civilization: Civilization? = null,
    val nextLesson: Lesson? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    // Additional M1 fields
    val civilizationName: String = "",
    val tier: String = "VILLAGE",
    val tierLevel: Int = 1,
    val totalXp: Long = 0,
    val balances: Map<String, Long> = emptyMap(),
    val buildings: List<BuildingUiModel> = emptyList(),
    val currentLesson: LessonSummaryUiModel? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCivilizationUseCase: GetCivilizationUseCase,
    private val getLessonsUseCase: GetLessonsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadData(languageCode: String) {
        viewModelScope.launch {
            getCivilizationUseCase(languageCode).collect { civ ->
                _uiState.value = _uiState.value.copy(
                    civilization = civ,
                    isLoading = false,
                    civilizationName = civ.name,
                    totalXp = civ.totalXp,
                    tierLevel = civ.tierLevel,
                    balances = civ.balances,
                    buildings = civ.buildings.map { b ->
                        BuildingUiModel(
                            id = b.id,
                            buildingType = b.buildingType,
                            name = b.displayName,
                            level = b.level,
                            emoji = buildingEmoji(b.buildingType)
                        )
                    }
                )
            }
        }
        viewModelScope.launch {
            getLessonsUseCase(languageCode).collect { lessons ->
                val nextLesson = lessons.firstOrNull { !it.isCompleted && !it.isLocked }
                _uiState.value = _uiState.value.copy(
                    nextLesson = nextLesson,
                    currentLesson = nextLesson?.let {
                        LessonSummaryUiModel(
                            id = it.id,
                            title = it.title,
                            exerciseCount = it.exerciseCount,
                            xpReward = it.xpReward
                        )
                    }
                )
            }
        }
        viewModelScope.launch {
            getCivilizationUseCase.refresh(languageCode)
        }
    }

    private fun buildingEmoji(type: String): String = when (type.uppercase()) {
        "HOUSE", "RESIDENTIAL" -> "🏠"
        "FARM" -> "🌾"
        "LEARNING_CENTER", "ACADEMY" -> "🏫"
        "MARKET" -> "🏪"
        "BARRACKS" -> "⚔"
        "LIBRARY" -> "📚"
        else -> "🏛"
    }
}
