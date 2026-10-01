package com.langoa.app.data.repository

import com.langoa.app.data.local.dao.CachedLessonDao
import com.langoa.app.data.local.entity.CachedLesson
import com.langoa.app.data.remote.api.LearningApi
import com.langoa.app.data.remote.model.LessonCompletionRequest
import com.langoa.app.domain.model.Exercise
import com.langoa.app.domain.model.ExerciseType
import com.langoa.app.domain.model.Lesson
import com.langoa.app.domain.model.LessonReward
import com.langoa.app.domain.repository.LearningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LearningRepositoryImpl @Inject constructor(
    private val learningApi: LearningApi,
    private val cachedLessonDao: CachedLessonDao
) : LearningRepository {

    override fun getLessonsForLanguage(languageCode: String): Flow<List<Lesson>> {
        return cachedLessonDao.getLessonsByLanguage(languageCode).map { cached ->
            cached.map { it.toDomain() }
        }
    }

    override suspend fun getLessonDetail(languageCode: String, lessonId: String): Result<Lesson> {
        return try {
            val dto = learningApi.getLesson(languageCode, lessonId)
            val exercises = dto.exercises.map { exerciseDto ->
                when (ExerciseType.valueOf(exerciseDto.type.uppercase())) {
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
                    ExerciseType.TRANSLATE_TO_TARGET -> Exercise.TranslateToTargetExercise(
                        id = exerciseDto.id,
                        question = exerciseDto.question,
                        correctAnswer = exerciseDto.correctAnswer,
                        hint = exerciseDto.hint ?: ""
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
            val lesson = Lesson(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                languageCode = dto.languageCode,
                unitNumber = dto.unitNumber,
                lessonNumber = dto.lessonNumber,
                exercises = exercises,
                exerciseCount = exercises.size
            )
            Result.success(lesson)
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
        return try {
            val request = LessonCompletionRequest(score = score)
            val response = learningApi.completeLesson(languageCode, lessonId, request)
            cachedLessonDao.markLessonCompleted(lessonId)
            val reward = LessonReward(
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
            Result.success(reward)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshCurriculum(languageCode: String): Result<Unit> {
        return try {
            val curriculum = learningApi.getCurriculum(languageCode)
            val cachedLessons = curriculum.units.flatMap { unit ->
                unit.lessons.map { lesson ->
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
                        exerciseCount = lesson.exerciseCount
                    )
                }
            }
            cachedLessonDao.clearLessonsForLanguage(languageCode)
            cachedLessonDao.insertLessons(cachedLessons)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
