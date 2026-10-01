package com.langoa.app.ui.screens.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langoa.app.domain.model.Lesson
import com.langoa.app.domain.usecase.CompleteLessonUseCase
import com.langoa.app.domain.usecase.GetLessonsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LearnUiState(
    val lessons: List<Lesson> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val getLessonsUseCase: GetLessonsUseCase,
    private val completeLessonUseCase: CompleteLessonUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearnUiState())
    val uiState: StateFlow<LearnUiState> = _uiState.asStateFlow()

    fun loadLessons(languageCode: String) {
        viewModelScope.launch {
            getLessonsUseCase(languageCode).collect { lessons ->
                _uiState.value = _uiState.value.copy(lessons = lessons, isLoading = false)
            }
        }
    }

    fun completeLesson(
        languageCode: String,
        lessonId: String,
        score: Int,
        totalQuestions: Int,
        timeSpentSeconds: Int,
        isPerfect: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = completeLessonUseCase(
                languageCode = languageCode,
                lessonId = lessonId,
                score = score,
                totalQuestions = totalQuestions,
                timeSpentSeconds = timeSpentSeconds,
                isPerfect = isPerfect
            )
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { e -> onError(e.message ?: "Failed to complete lesson") }
            )
        }
    }
}
