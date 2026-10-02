package com.langoa.app.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.langoa.app.data.local.dao.CachedLessonDao
import com.langoa.app.data.local.dao.PendingSyncDao
import com.langoa.app.data.local.entity.CachedLesson
import com.langoa.app.data.local.entity.PendingSync
import com.langoa.app.data.remote.api.LearningApi
import com.langoa.app.data.remote.model.ExerciseDto
import com.langoa.app.data.remote.model.LessonCompletionRequest
import com.langoa.app.data.remote.model.LessonCompletionResponse
import com.langoa.app.data.remote.model.LessonOverviewDto
import com.langoa.app.domain.model.Exercise
import com.langoa.app.domain.model.ExerciseType
import com.langoa.app.domain.model.Lesson
import com.langoa.app.domain.model.LessonReward
import com.langoa.app.domain.repository.LearningRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LearningRepositoryImpl @Inject constructor(
    private val learningApi: LearningApi,
    private val cachedLessonDao: CachedLessonDao,
    private val pendingSyncDao: PendingSyncDao,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) : LearningRepository {

    override fun getLessonsForLanguage(languageCode: String): Flow<List<Lesson>> {
        return cachedLessonDao.getLessonsByLanguage(languageCode).map { cached ->
            cached.map { it.toDomain() }
        }
    }

    override suspend fun getLessonDetail(languageCode: String, lessonId: String): Result<Lesson> {
        return try {
            val dto = learningApi.getLesson(languageCode, lessonId)
            val exercises = dto.exercises.map { mapExerciseDto(it) }

            // Update cached exercises while we have them.
            val cached = cachedLessonDao.getLessonById(lessonId)
            if (cached != null) {
                cachedLessonDao.insertLesson(
                    cached.copy(
                        exercisesJson = gson.toJson(dto.exercises),
                        cachedExercisesAt = System.currentTimeMillis()
                    )
                )
            }

            Result.success(buildLesson(dto.id, dto.title, dto.description, dto.languageCode,
                dto.unitNumber, dto.lessonNumber, exercises))
        } catch (e: Exception) {
            // Offline fallback: use cached exercises if available.
            val cached = cachedLessonDao.getLessonById(lessonId)
            if (cached != null && cached.exercisesJson.isNotEmpty()) {
                val exercises = deserializeExercises(cached.exercisesJson)
                Result.success(cached.toDomain().copy(exercises = exercises))
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getLessonOverview(languageCode: String, lessonId: String): Result<LessonOverviewDto> {
        return try {
            Result.success(learningApi.getLessonOverview(languageCode, lessonId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeLesson(
        languageCode: String,
        lessonId: String,
        score: Int,
        totalQuestions: Int,
        timeSpentSeconds: Int,
        isPerfect: Boolean
    ): Result<LessonReward> {
        // Optimistically mark completed in local cache.
        cachedLessonDao.markLessonCompleted(lessonId)

        return if (isOnline()) {
            try {
                val request = LessonCompletionRequest(score = score)
                val response = learningApi.completeLesson(languageCode, lessonId, request)
                Result.success(mapReward(response))
            } catch (e: Exception) {
                // Network failed despite appearing online — queue for later.
                enqueuePendingCompletion(languageCode, lessonId, score)
                Result.failure(e)
            }
        } else {
            enqueuePendingCompletion(languageCode, lessonId, score)
            // Return a minimal offline reward so the UI can continue.
            Result.success(LessonReward(
                lessonId = lessonId,
                lessonStatus = "COMPLETED",
                score = score,
                nextLevelUnlocked = false,
                isPending = true
            ))
        }
    }

    override suspend fun refreshCurriculum(languageCode: String): Result<Unit> {
        return try {
            val curriculum = learningApi.getCurriculum(languageCode)
            val cachedLessons = curriculum.units.flatMap { unit ->
                unit.lessons.map { lesson ->
                    // Preserve existing exercisesJson if already cached.
                    val existing = cachedLessonDao.getLessonById(lesson.id)
                    CachedLesson(
                        id = lesson.id,
                        title = lesson.title,
                        description = lesson.description,
                        languageCode = lesson.languageCode,
                        unitNumber = lesson.unitNumber,
                        lessonNumber = lesson.lessonNumber,
                        isCompleted = lesson.isCompleted,
                        isLocked = lesson.isLocked,
                        xpReward = lesson.xpReward,
                        exerciseCount = lesson.exerciseCount,
                        exercisesJson = existing?.exercisesJson ?: "",
                        cachedExercisesAt = existing?.cachedExercisesAt ?: 0L
                    )
                }
            }
            cachedLessonDao.clearLessonsForLanguage(languageCode)
            cachedLessonDao.insertLessons(cachedLessons)

            // Cache exercises for all lessons (max 5 in parallel to avoid rate limiting).
            val lessonsNeedingCache = cachedLessons.filter { it.exercisesJson.isEmpty() }
            val chunks = lessonsNeedingCache.chunked(5)
            for (chunk in chunks) {
                coroutineScope {
                    chunk.map { cachedLesson ->
                        async {
                            try {
                                val dto = learningApi.getLesson(languageCode, cachedLesson.id)
                                cachedLessonDao.insertLesson(
                                    cachedLesson.copy(
                                        exercisesJson = gson.toJson(dto.exercises),
                                        cachedExercisesAt = System.currentTimeMillis()
                                    )
                                )
                            } catch (_: Exception) {
                                // Individual lesson fetch failure is non-fatal during prefetch.
                            }
                        }
                    }.awaitAll()
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private suspend fun enqueuePendingCompletion(languageCode: String, lessonId: String, score: Int) {
        val payload = gson.toJson(mapOf(
            "languageCode" to languageCode,
            "lessonId" to lessonId,
            "score" to score
        ))
        pendingSyncDao.insert(PendingSync(
            id = UUID.randomUUID().toString(),
            operationType = "COMPLETE_LESSON",
            payload = payload,
            createdAt = System.currentTimeMillis()
        ))
    }

    private fun deserializeExercises(json: String): List<Exercise> {
        return try {
            val type = object : TypeToken<List<ExerciseDto>>() {}.type
            val dtos: List<ExerciseDto> = gson.fromJson(json, type)
            dtos.map { mapExerciseDto(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun mapExerciseDto(exerciseDto: ExerciseDto): Exercise {
        return when (ExerciseType.valueOf(exerciseDto.type.uppercase())) {
            ExerciseType.VOCABULARY -> Exercise.VocabularyExercise(
                id = exerciseDto.id,
                question = exerciseDto.question,
                options = exerciseDto.options ?: emptyList(),
                correctAnswer = exerciseDto.correctAnswer,
                explanation = exerciseDto.explanation ?: ""
            )
            ExerciseType.MULTIPLE_CHOICE -> Exercise.MultipleChoiceExercise(
                id = exerciseDto.id,
                question = exerciseDto.question,
                options = exerciseDto.options ?: emptyList(),
                correctAnswer = exerciseDto.correctAnswer,
                explanation = exerciseDto.explanation ?: ""
            )
            ExerciseType.TRANSLATE_TO_TARGET -> {
                val options = exerciseDto.options
                if (!options.isNullOrEmpty()) {
                    Exercise.TranslateChoiceExercise(
                        id = exerciseDto.id,
                        prompt = exerciseDto.prompt ?: exerciseDto.question,
                        choices = options,
                        correctAnswer = exerciseDto.correctAnswer,
                        literalHelp = exerciseDto.hint ?: ""
                    )
                } else {
                    Exercise.TranslateToTargetExercise(
                        id = exerciseDto.id,
                        question = exerciseDto.question,
                        correctAnswer = exerciseDto.correctAnswer,
                        hint = exerciseDto.hint ?: ""
                    )
                }
            }
            ExerciseType.LISTEN_CHOOSE -> Exercise.ListenChooseExercise(
                id = exerciseDto.id,
                audioText = exerciseDto.question,
                choices = exerciseDto.options ?: emptyList(),
                correctAnswer = exerciseDto.correctAnswer
            )
            ExerciseType.SENTENCE_CONSTRUCT -> Exercise.SentenceConstructExercise(
                id = exerciseDto.id,
                prompt = exerciseDto.prompt ?: exerciseDto.question,
                wordBankItems = exerciseDto.wordBankItems ?: emptyList(),
                correctAnswer = exerciseDto.correctAnswer
            )
            else -> Exercise.MultipleChoiceExercise(
                id = exerciseDto.id,
                question = exerciseDto.question,
                options = exerciseDto.options ?: emptyList(),
                correctAnswer = exerciseDto.correctAnswer
            )
        }
    }

    private fun buildLesson(
        id: String, title: String, description: String, languageCode: String,
        unitNumber: Int, lessonNumber: Int, exercises: List<Exercise>
    ) = Lesson(
        id = id,
        title = title,
        description = description,
        languageCode = languageCode,
        unitNumber = unitNumber,
        lessonNumber = lessonNumber,
        exercises = exercises,
        exerciseCount = exercises.size
    )

    private fun mapReward(response: LessonCompletionResponse) = LessonReward(
        lessonId = response.lessonId,
        lessonStatus = response.lessonStatus,
        score = response.score,
        nextLevelUnlocked = response.nextLevelUnlocked,
        nextCefrLevel = response.nextCefrLevel,
        xpEarned = response.xpEarned,
        coinsEarned = response.coinsEarned,
        foodEarned = response.foodEarned,
        materialsEarned = response.materialsEarned,
        woodEarned = response.woodEarned,
        civilizationPowerEarned = response.civilizationPowerEarned,
        newBalances = response.newBalances,
        tierUpgraded = response.tierUpgraded,
        newTier = response.newTier,
        unlockedBuildingTypes = response.unlockedBuildingTypes,
        completedQuestKeys = response.completedQuestKeys,
        unlockedAchievementKeys = response.unlockedAchievementKeys
    )

    private fun CachedLesson.toDomain() = Lesson(
        id = id,
        title = title,
        description = description,
        languageCode = languageCode,
        unitNumber = unitNumber,
        lessonNumber = lessonNumber,
        isCompleted = isCompleted,
        isLocked = isLocked,
        xpReward = xpReward,
        exerciseCount = exerciseCount
    )
}
