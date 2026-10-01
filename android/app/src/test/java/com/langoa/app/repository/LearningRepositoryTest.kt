package com.langoa.app.repository

import com.langoa.app.data.local.dao.CachedLessonDao
import com.langoa.app.data.local.entity.CachedLesson
import com.langoa.app.data.remote.api.LearningApi
import com.langoa.app.data.remote.model.CurriculumDto
import com.langoa.app.data.remote.model.LessonCompletionRequest
import com.langoa.app.data.remote.model.LessonCompletionResponse
import com.langoa.app.data.remote.model.UnitDto
import com.langoa.app.data.remote.model.LessonDto
import com.langoa.app.data.repository.LearningRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LearningRepositoryTest {

    private lateinit var learningApi: LearningApi
    private lateinit var cachedLessonDao: CachedLessonDao
    private lateinit var repository: LearningRepositoryImpl

    private val testCachedLesson = CachedLesson(
        id = "lesson-1",
        title = "Basics 1",
        description = "Intro",
        languageCode = "de",
        unitNumber = 1,
        lessonNumber = 1,
        isCompleted = false,
        isLocked = false,
        xpReward = 10,
        exerciseCount = 5
    )

    @Before
    fun setup() {
        learningApi = mockk()
        cachedLessonDao = mockk(relaxed = true)
        repository = LearningRepositoryImpl(learningApi, cachedLessonDao)
    }

    @Test
    fun `getLessonsForLanguage returns flow from dao`() = runTest {
        every { cachedLessonDao.getLessonsByLanguage("de") } returns flowOf(listOf(testCachedLesson))

        val flow = repository.getLessonsForLanguage("de")
        var collected: List<com.langoa.app.domain.model.Lesson>? = null
        flow.collect { collected = it }

        assertEquals(1, collected?.size)
        assertEquals("lesson-1", collected?.get(0)?.id)
    }

    @Test
    fun `refreshCurriculum fetches from API and inserts into DB`() = runTest {
        val curriculum = CurriculumDto(
            languageCode = "de",
            units = listOf(
                UnitDto(
                    id = "unit-1",
                    title = "Unit 1",
                    description = "First unit",
                    unitNumber = 1,
                    lessons = listOf(
                        LessonDto(
                            id = "lesson-1",
                            title = "Basics 1",
                            description = "Intro",
                            lessonNumber = 1,
                            unitNumber = 1,
                            languageCode = "de",
                            isCompleted = false,
                            isLocked = false,
                            xpReward = 10,
                            exerciseCount = 5
                        )
                    )
                )
            )
        )
        coEvery { learningApi.getCurriculum("de") } returns curriculum

        val result = repository.refreshCurriculum("de")

        assertTrue(result.isSuccess)
        coVerify { cachedLessonDao.clearLessonsForLanguage("de") }
        coVerify { cachedLessonDao.insertLessons(any()) }
    }

    @Test
    fun `refreshCurriculum returns failure on API error`() = runTest {
        coEvery { learningApi.getCurriculum("de") } throws Exception("Network error")

        val result = repository.refreshCurriculum("de")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `completeLesson marks lesson as completed in DB`() = runTest {
        val completionResponse = LessonCompletionResponse(
            lessonId = "lesson-1",
            xpEarned = 10,
            coinsEarned = 5,
            foodEarned = 3,
            materialsEarned = 2,
            civPowerEarned = 1,
            isPerfect = true,
            streakBonus = false,
            newTotalXp = 110,
            newLevel = 2
        )
        coEvery { learningApi.completeLesson(any(), any(), any()) } returns completionResponse

        val result = repository.completeLesson(
            languageCode = "de",
            lessonId = "lesson-1",
            score = 5,
            totalQuestions = 5,
            timeSpentSeconds = 120,
            isPerfect = true
        )

        assertTrue(result.isSuccess)
        coVerify { cachedLessonDao.markLessonCompleted("lesson-1") }
        assertEquals(10, result.getOrNull()?.xpEarned)
    }
}
