package com.langoa.app.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.google.gson.Gson
import com.langoa.app.data.local.dao.CachedLessonDao
import com.langoa.app.data.local.dao.PendingSyncDao
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
    private lateinit var pendingSyncDao: PendingSyncDao
    private lateinit var context: Context
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
        pendingSyncDao = mockk(relaxed = true)
        context = mockk(relaxed = true)
        repository = LearningRepositoryImpl(learningApi, cachedLessonDao, pendingSyncDao, Gson(), context)
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
        // Simulate lesson already cached so exercise-caching step is skipped
        coEvery { cachedLessonDao.getLessonById(any()) } returns testCachedLesson.copy(exercisesJson = "[]")

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
        // Mock connectivity to simulate online state
        val network = mockk<Network>()
        val networkCapabilities = mockk<NetworkCapabilities>()
        val connectivityManager = mockk<ConnectivityManager>()
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        val completionResponse = LessonCompletionResponse(
            lessonId = "lesson-1",
            lessonStatus = "COMPLETED",
            score = 5,
            xpEarned = 10,
            coinsEarned = 5,
            foodEarned = 3,
            materialsEarned = 2,
            civilizationPowerEarned = 1
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
        assertEquals(10L, result.getOrNull()?.xpEarned)
    }
}
