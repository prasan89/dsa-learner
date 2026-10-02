package com.langoa.app.ui.screens.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.langoa.app.domain.model.Lesson
import com.langoa.app.domain.model.LessonReward
import com.langoa.app.domain.usecase.CompleteLessonUseCase
import com.langoa.app.domain.usecase.GetLessonsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LearnUiState(
    val lessons: List<Lesson> = emptyList(),
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null,
    val completedReward: LessonReward? = null
)

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val getLessonsUseCase: GetLessonsUseCase,
    private val completeLessonUseCase: CompleteLessonUseCase,
    private val rewardResultStore: RewardResultStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearnUiState())
    val uiState: StateFlow<LearnUiState> = _uiState.asStateFlow()

    fun loadLessons(languageCode: String) {
        viewModelScope.launch {
            getLessonsUseCase(languageCode).collect { lessons ->
                _uiState.value = _uiState.value.copy(
                    lessons = lessons,
                    isLoading = false,
                    isOffline = !isOnline(),
                    isEmpty = lessons.isEmpty()
                )
            }
        }
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun completeLesson(
        languageCode: String,
        lessonId: String,
        score: Int,
        totalQuestions: Int,
        timeSpentSeconds: Int,
        isPerfect: Boolean,
        onSuccess: (LessonReward) -> Unit,
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
                onSuccess = { reward ->
                    _uiState.value = _uiState.value.copy(completedReward = reward)
                    rewardResultStore.lastReward = reward
                    onSuccess(reward)
                },
                onFailure = { e -> onError(e.message ?: "Failed to complete lesson") }
            )
        }
    }
}

