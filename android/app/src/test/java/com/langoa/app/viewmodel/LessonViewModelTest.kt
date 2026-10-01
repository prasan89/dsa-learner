package com.langoa.app.viewmodel

import app.cash.turbine.test
import com.langoa.app.domain.model.Exercise
import com.langoa.app.domain.model.Lesson
import com.langoa.app.domain.model.LessonReward
import com.langoa.app.domain.usecase.CompleteLessonUseCase
import com.langoa.app.domain.usecase.GetLessonsUseCase
import com.langoa.app.ui.screens.learn.LessonViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LessonViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getLessonsUseCase: GetLessonsUseCase
    private lateinit var completeLessonUseCase: CompleteLessonUseCase
    private lateinit var viewModel: LessonViewModel

    private val testLessons = listOf(
        Lesson(
            id = "lesson-1",
            title = "Basics 1",
            description = "Introduction to German",
            languageCode = "de",
            unitNumber = 1,
            lessonNumber = 1,
            isCompleted = false,
            isLocked = false,
            xpReward = 10,
            exerciseCount = 5
        ),
        Lesson(
            id = "lesson-2",
            title = "Basics 2",
            description = "More German basics",
            languageCode = "de",
            unitNumber = 1,
            lessonNumber = 2,
            isCompleted = false,
            isLocked = true,
            xpReward = 10,
            exerciseCount = 5
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getLessonsUseCase = mockk()
        completeLessonUseCase = mockk()
        every { getLessonsUseCase("de") } returns flowOf(testLessons)
        viewModel = LessonViewModel(getLessonsUseCase, completeLessonUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadLessons updates lessons state`() = runTest {
        viewModel.loadLessons("de")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.lessons.size)
            assertEquals("lesson-1", state.lessons[0].id)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `completeLesson calls use case with correct params`() = runTest {
        val reward = LessonReward(
            lessonId = "lesson-1",
            xpEarned = 10,
            coinsEarned = 5,
            foodEarned = 3,
            materialsEarned = 2,
            civPowerEarned = 1,
            isPerfect = true,
            streakBonus = false
        )
        coEvery {
            completeLessonUseCase("de", "lesson-1", 5, 5, 120, true)
        } returns Result.success(reward)

        var successCalled = false
        viewModel.completeLesson(
            languageCode = "de",
            lessonId = "lesson-1",
            score = 5,
            totalQuestions = 5,
            timeSpentSeconds = 120,
            isPerfect = true,
            onSuccess = { successCalled = true },
            onError = {}
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(successCalled)
    }

    @Test
    fun `completeLesson calls onError on failure`() = runTest {
        coEvery {
            completeLessonUseCase("de", "lesson-1", 3, 5, 90, false)
        } returns Result.failure(Exception("Network error"))

        var errorMessage: String? = null
        viewModel.completeLesson(
            languageCode = "de",
            lessonId = "lesson-1",
            score = 3,
            totalQuestions = 5,
            timeSpentSeconds = 90,
            isPerfect = false,
            onSuccess = {},
            onError = { errorMessage = it }
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Network error", errorMessage)
    }
}
