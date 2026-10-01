package com.langoa.app.domain.model

sealed class Exercise(open val id: String, val type: ExerciseType) {
    data class VocabularyExercise(
        override val id: String,
        val question: String,
        val options: List<String>,
        val correctAnswer: String,
        val explanation: String = ""
    ) : Exercise(id, ExerciseType.VOCABULARY)

    data class TranslateToTargetExercise(
        override val id: String,
        val question: String,
        val correctAnswer: String,
        val hint: String = ""
    ) : Exercise(id, ExerciseType.TRANSLATE_TO_TARGET)

    data class MultipleChoiceExercise(
        override val id: String,
        val question: String,
        val options: List<String>,
        val correctAnswer: String,
        val explanation: String = ""
    ) : Exercise(id, ExerciseType.MULTIPLE_CHOICE)

    data class SentenceConstructExercise(
        override val id: String,
        val prompt: String,
        val wordBankItems: List<String>,
        val correctAnswer: String
    ) : Exercise(id, ExerciseType.SENTENCE_CONSTRUCT)
}

enum class ExerciseType {
    VOCABULARY,
    TRANSLATE_TO_TARGET,
    MULTIPLE_CHOICE,
    SENTENCE_CONSTRUCT,
    LISTEN_CHOOSE,
    LISTEN_TYPE,
    SPEAK_PROMPT,
    MATCH,
    FILL_BLANK,
    REORDER,
    CONVERSATION
}
