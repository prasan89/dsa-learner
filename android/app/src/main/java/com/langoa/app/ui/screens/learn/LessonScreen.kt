package com.langoa.app.ui.screens.learn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.langoa.app.domain.model.Exercise
import com.langoa.app.domain.model.LessonReward
import com.langoa.app.ui.components.LoadingScreen
import com.langoa.app.ui.theme.LangoaAmber
import com.langoa.app.ui.theme.LangoaBackground
import com.langoa.app.ui.theme.LangoaBlue
import com.langoa.app.ui.theme.LangoaError
import com.langoa.app.ui.theme.LangoaGreen
import com.langoa.app.ui.theme.LangoaGreenLight
import com.langoa.app.ui.theme.LangoaOnBackground
import com.langoa.app.ui.theme.LangoaSurface
import com.langoa.app.ui.theme.LangoaSurfaceVariant
import com.langoa.app.ui.theme.LangoaXP

sealed class ExerciseState {
    object Unanswered : ExerciseState()
    data class Answered(val isCorrect: Boolean, val selectedAnswer: String) : ExerciseState()
}

data class LessonUiState(
    val isLoading: Boolean = true,
    val lessonTitle: String = "",
    val exercises: List<Exercise> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val exerciseState: ExerciseState = ExerciseState.Unanswered,
    val score: Int = 0,
    val totalAnswered: Int = 0,
    val isComplete: Boolean = false,
    val xpGainedSoFar: Int = 0,
    val error: String? = null
)

@Composable
fun LessonScreen(
    languageCode: String,
    lessonId: String,
    onLessonComplete: (LessonReward?) -> Unit,
    onBack: () -> Unit,
    viewModel: LessonViewModel = hiltViewModel()
) {
    val lessonUiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load lesson exercises
    LaunchedEffect(lessonId) {
        viewModel.loadLessons(languageCode)
    }

    LaunchedEffect(lessonUiState.error) {
        lessonUiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    // local state for the lesson exercise engine
    var localLessonState by remember { mutableStateOf(LessonUiState()) }
    val lessons = lessonUiState.lessons
    val currentLesson = lessons.firstOrNull { it.id == lessonId }

    LaunchedEffect(currentLesson) {
        currentLesson?.let { lesson ->
            localLessonState = LessonUiState(
                isLoading = false,
                lessonTitle = lesson.title,
                exercises = lesson.exercises
            )
        }
    }

    if (lessonUiState.isLoading || (currentLesson == null && !lessonUiState.isLoading)) {
        // Show stub or loading
        if (!lessonUiState.isLoading && currentLesson == null) {
            // No exercises from API — show a simple placeholder
            LessonPlaceholder(lessonId = lessonId, onComplete = { onLessonComplete(null) }, onBack = onBack)
            return
        }
        LoadingScreen()
        return
    }

    if (localLessonState.isLoading) {
        LoadingScreen()
        return
    }

    val exercises = localLessonState.exercises

    if (localLessonState.isComplete || exercises.isEmpty()) {
        // Auto-navigate to reward
        LaunchedEffect(Unit) {
            viewModel.completeLesson(
                languageCode = languageCode,
                lessonId = lessonId,
                score = localLessonState.score,
                totalQuestions = maxOf(localLessonState.totalAnswered, 1),
                timeSpentSeconds = 60,
                isPerfect = localLessonState.score == localLessonState.totalAnswered,
                onSuccess = { reward -> onLessonComplete(reward) },
                onError = { onLessonComplete(null) }
            )
        }
        return
    }

    val currentExercise = exercises.getOrNull(localLessonState.currentExerciseIndex)
        ?: run {
            LaunchedEffect(Unit) { onLessonComplete(null) }
            return
        }

    Scaffold(
        containerColor = LangoaBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LangoaBackground)
                .padding(paddingValues)
        ) {
            // Header with progress
            LessonHeader(
                title = localLessonState.lessonTitle,
                currentIndex = localLessonState.currentExerciseIndex,
                total = exercises.size,
                xpGained = localLessonState.xpGainedSoFar,
                onBack = onBack
            )

            // Exercise progress dots
            ExerciseProgressDots(
                total = exercises.size,
                currentIndex = localLessonState.currentExerciseIndex,
                exerciseState = localLessonState.exerciseState,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Exercise content
            AnimatedContent(
                targetState = localLessonState.currentExerciseIndex,
                transitionSpec = {
                    slideInHorizontally(tween(300)) { it / 2 } + fadeIn(tween(300)) togetherWith
                            slideOutHorizontally(tween(200)) { -it / 2 } + fadeOut(tween(200))
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                label = "exerciseContent"
            ) { _ ->
                ExerciseContent(
                    exercise = currentExercise,
                    exerciseState = localLessonState.exerciseState,
                    onAnswer = { answer ->
                        val isCorrect = when (currentExercise) {
                            is Exercise.VocabularyExercise -> answer.trim().equals(currentExercise.correctAnswer.trim(), ignoreCase = true)
                            is Exercise.MultipleChoiceExercise -> answer.trim().equals(currentExercise.correctAnswer.trim(), ignoreCase = true)
                            is Exercise.TranslateToTargetExercise -> answer.trim().equals(currentExercise.correctAnswer.trim(), ignoreCase = true)
                            is Exercise.TranslateChoiceExercise -> answer.trim().equals(currentExercise.correctAnswer.trim(), ignoreCase = true)
                            is Exercise.SentenceConstructExercise -> answer.trim().equals(currentExercise.correctAnswer.trim(), ignoreCase = true)
                            is Exercise.ListenChooseExercise -> answer.trim().equals(currentExercise.correctAnswer.trim(), ignoreCase = true)
                            else -> false
                        }
                        localLessonState = localLessonState.copy(
                            exerciseState = ExerciseState.Answered(isCorrect, answer),
                            score = if (isCorrect) localLessonState.score + 1 else localLessonState.score,
                            totalAnswered = localLessonState.totalAnswered + 1,
                            xpGainedSoFar = if (isCorrect) localLessonState.xpGainedSoFar + 5 else localLessonState.xpGainedSoFar
                        )
                    }
                )
            }

            // Feedback / Continue button
            AnimatedVisibility(visible = localLessonState.exerciseState is ExerciseState.Answered) {
                val answered = localLessonState.exerciseState as? ExerciseState.Answered
                Column(modifier = Modifier.padding(16.dp)) {
                    answered?.let { state ->
                        AnswerFeedbackBanner(
                            isCorrect = state.isCorrect,
                            correctAnswer = correctAnswerFor(currentExercise)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Button(
                        onClick = {
                            val nextIndex = localLessonState.currentExerciseIndex + 1
                            if (nextIndex >= exercises.size) {
                                localLessonState = localLessonState.copy(isComplete = true)
                            } else {
                                localLessonState = localLessonState.copy(
                                    currentExerciseIndex = nextIndex,
                                    exerciseState = ExerciseState.Unanswered
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if ((localLessonState.exerciseState as? ExerciseState.Answered)?.isCorrect == true)
                                LangoaGreen else LangoaAmber,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (localLessonState.currentExerciseIndex + 1 >= exercises.size) "FINISH LESSON" else "CONTINUE",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonHeader(
    title: String,
    currentIndex: Int,
    total: Int,
    xpGained: Int,
    onBack: () -> Unit
) {
    val progress = if (total > 0) (currentIndex.toFloat() / total.toFloat()) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400),
        label = "progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = LangoaOnBackground.copy(alpha = 0.7f)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LangoaOnBackground,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(LangoaSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⭐ $xpGained",
                    style = MaterialTheme.typography.labelMedium,
                    color = LangoaXP,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(horizontal = 16.dp),
            color = LangoaGreen,
            trackColor = LangoaSurfaceVariant
        )

        Text(
            text = "${currentIndex + 1} / $total",
            style = MaterialTheme.typography.labelSmall,
            color = LangoaOnBackground.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, end = 16.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ExerciseProgressDots(
    total: Int,
    currentIndex: Int,
    exerciseState: ExerciseState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        (0 until minOf(total, 12)).forEach { i ->
            val color = when {
                i < currentIndex -> LangoaGreenLight
                i == currentIndex -> when (exerciseState) {
                    is ExerciseState.Answered -> if (exerciseState.isCorrect) LangoaGreen else LangoaError
                    else -> LangoaAmber
                }
                else -> LangoaSurfaceVariant
            }
            val size = if (i == currentIndex) 10.dp else 7.dp

            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun ExerciseContent(
    exercise: Exercise,
    exerciseState: ExerciseState,
    onAnswer: (String) -> Unit
) {
    when (exercise) {
        is Exercise.VocabularyExercise -> VocabularyExerciseView(
            exercise = exercise,
            exerciseState = exerciseState,
            onAnswer = onAnswer
        )
        is Exercise.MultipleChoiceExercise -> MultipleChoiceExerciseView(
            exercise = exercise,
            exerciseState = exerciseState,
            onAnswer = onAnswer
        )
        is Exercise.TranslateToTargetExercise -> TranslateExerciseView(
            exercise = exercise,
            exerciseState = exerciseState,
            onAnswer = onAnswer
        )
        is Exercise.TranslateChoiceExercise -> TranslateChoiceExerciseView(
            exercise = exercise,
            exerciseState = exerciseState,
            onAnswer = onAnswer
        )
        is Exercise.SentenceConstructExercise -> SentenceBuilderView(
            exercise = exercise,
            exerciseState = exerciseState,
            onAnswer = onAnswer
        )
        is Exercise.ListenChooseExercise -> ListenChooseExerciseView(
            exercise = exercise,
            exerciseState = exerciseState,
            onAnswer = onAnswer
        )
        else -> GenericExerciseView(
            question = "Answer this question",
            onAnswer = onAnswer
        )
    }
}

@Composable
private fun VocabularyExerciseView(
    exercise: Exercise.VocabularyExercise,
    exerciseState: ExerciseState,
    onAnswer: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Question card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LangoaSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "What does this mean?",
                    style = MaterialTheme.typography.labelLarge,
                    color = LangoaAmber,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = exercise.question,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = LangoaOnBackground,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Options
        exercise.options.forEach { option ->
            OptionButton(
                text = option,
                exerciseState = exerciseState,
                correctAnswer = exercise.correctAnswer,
                onAnswer = onAnswer
            )
        }
    }
}

@Composable
private fun MultipleChoiceExerciseView(
    exercise: Exercise.MultipleChoiceExercise,
    exerciseState: ExerciseState,
    onAnswer: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LangoaSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Choose the correct answer",
                    style = MaterialTheme.typography.labelLarge,
                    color = LangoaBlue,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = exercise.question,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LangoaOnBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
        exercise.options.forEach { option ->
            OptionButton(
                text = option,
                exerciseState = exerciseState,
                correctAnswer = exercise.correctAnswer,
                onAnswer = onAnswer
            )
        }
    }
}

@Composable
private fun TranslateExerciseView(
    exercise: Exercise.TranslateToTargetExercise,
    exerciseState: ExerciseState,
    onAnswer: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LangoaSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Translate to German:",
                    style = MaterialTheme.typography.labelLarge,
                    color = LangoaAmber,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = exercise.question,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = LangoaOnBackground,
                    textAlign = TextAlign.Center
                )
                if (exercise.hint.isNotEmpty()) {
                    Text(
                        text = "Hint: ${exercise.hint}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LangoaOnBackground.copy(alpha = 0.45f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        val answered = exerciseState is ExerciseState.Answered
        OutlinedTextField(
            value = inputText,
            onValueChange = { if (!answered) inputText = it },
            placeholder = {
                Text(
                    "Type your translation...",
                    color = LangoaOnBackground.copy(alpha = 0.35f)
                )
            },
            enabled = !answered,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (inputText.isNotBlank() && !answered) onAnswer(inputText)
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LangoaAmber,
                unfocusedBorderColor = LangoaSurfaceVariant,
                focusedContainerColor = LangoaSurface,
                unfocusedContainerColor = LangoaSurface,
                focusedTextColor = LangoaOnBackground,
                unfocusedTextColor = LangoaOnBackground,
                cursorColor = LangoaAmber,
                disabledContainerColor = LangoaSurface,
                disabledTextColor = LangoaOnBackground.copy(alpha = 0.6f),
                disabledBorderColor = when (exerciseState) {
                    is ExerciseState.Answered -> if (exerciseState.isCorrect) LangoaGreen else LangoaError
                    else -> LangoaSurfaceVariant
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Show correct answer on wrong
        if (exerciseState is ExerciseState.Answered && !exerciseState.isCorrect) {
            Text(
                text = "Correct: ${exercise.correctAnswer}",
                style = MaterialTheme.typography.bodyMedium,
                color = LangoaGreenLight,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(LangoaGreen.copy(alpha = 0.15f))
                    .padding(12.dp)
                    .fillMaxWidth()
            )
        }

        if (exerciseState is ExerciseState.Unanswered) {
            Button(
                onClick = { if (inputText.isNotBlank()) onAnswer(inputText) },
                enabled = inputText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LangoaAmber)
            ) {
                Text("CHECK", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
private fun GenericExerciseView(
    question: String,
    onAnswer: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LangoaSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleLarge,
                color = LangoaOnBackground,
                modifier = Modifier.padding(24.dp),
                textAlign = TextAlign.Center
            )
        }
        Button(onClick = { onAnswer("answer") }, modifier = Modifier.fillMaxWidth()) {
            Text("Submit")
        }
    }
}

@Composable
private fun TranslateChoiceExerciseView(
    exercise: Exercise.TranslateChoiceExercise,
    exerciseState: ExerciseState,
    onAnswer: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LangoaSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Translate to target language",
                    style = MaterialTheme.typography.labelMedium,
                    color = LangoaAmber
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    exercise.prompt,
                    style = MaterialTheme.typography.headlineSmall,
                    color = LangoaOnBackground,
                    fontWeight = FontWeight.Bold
                )
                if (exercise.literalHelp.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        exercise.literalHelp,
                        style = MaterialTheme.typography.bodySmall,
                        color = LangoaOnBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }
        exercise.choices.forEach { choice ->
            OptionButton(
                text = choice,
                exerciseState = exerciseState,
                correctAnswer = exercise.correctAnswer,
                onAnswer = onAnswer
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SentenceBuilderView(
    exercise: Exercise.SentenceConstructExercise,
    exerciseState: ExerciseState,
    onAnswer: (String) -> Unit
) {
    var trayWords by remember { mutableStateOf(listOf<String>()) }
    var bankWords by remember { mutableStateOf(exercise.wordBankItems.toList()) }
    val answered = exerciseState as? ExerciseState.Answered

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LangoaSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Build the sentence",
                    style = MaterialTheme.typography.labelMedium,
                    color = LangoaAmber
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    exercise.prompt,
                    style = MaterialTheme.typography.headlineSmall,
                    color = LangoaOnBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // Sentence tray
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp),
            colors = CardDefaults.cardColors(containerColor = LangoaSurface.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            FlowRow(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                trayWords.forEach { word ->
                    if (answered == null) {
                        AssistChip(
                            onClick = {
                                trayWords = trayWords - word
                                bankWords = bankWords + word
                            },
                            label = { Text(word) }
                        )
                    } else {
                        SuggestionChip(onClick = {}, label = { Text(word) })
                    }
                }
                if (trayWords.isEmpty()) {
                    Text(
                        "Tap words below to build the sentence",
                        style = MaterialTheme.typography.bodySmall,
                        color = LangoaOnBackground.copy(alpha = 0.4f),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
        // Word bank
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            bankWords.forEach { word ->
                if (answered == null) {
                    AssistChip(
                        onClick = {
                            bankWords = bankWords - word
                            trayWords = trayWords + word
                        },
                        label = { Text(word) }
                    )
                } else {
                    SuggestionChip(onClick = {}, label = { Text(word) })
                }
            }
        }
        if (answered == null) {
            Button(
                onClick = { if (trayWords.isNotEmpty()) onAnswer(trayWords.joinToString(" ")) },
                enabled = trayWords.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LangoaAmber)
            ) {
                Text("CHECK", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
private fun ListenChooseExerciseView(
    exercise: Exercise.ListenChooseExercise,
    exerciseState: ExerciseState,
    onAnswer: (String) -> Unit
) {
    var audioRevealed by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LangoaSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Listen and choose",
                    style = MaterialTheme.typography.labelMedium,
                    color = LangoaAmber
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { audioRevealed = true },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.size(72.dp)
                ) {
                    Text("▶", style = MaterialTheme.typography.headlineMedium)
                }
                if (audioRevealed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        exercise.audioText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = LangoaOnBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        exercise.choices.forEach { choice ->
            OptionButton(
                text = choice,
                exerciseState = exerciseState,
                correctAnswer = exercise.correctAnswer,
                onAnswer = onAnswer
            )
        }
    }
}

@Composable
private fun OptionButton(
    text: String,
    exerciseState: ExerciseState,
    correctAnswer: String,
    onAnswer: (String) -> Unit
) {
    val answered = exerciseState as? ExerciseState.Answered
    val isThisSelected = answered?.selectedAnswer == text
    val isCorrect = text.equals(correctAnswer, ignoreCase = true)

    val backgroundColor by animateColorAsState(
        targetValue = when {
            answered == null -> LangoaSurface
            isThisSelected && answered.isCorrect -> LangoaGreen.copy(alpha = 0.3f)
            isThisSelected && !answered.isCorrect -> LangoaError.copy(alpha = 0.3f)
            isCorrect && answered != null -> LangoaGreen.copy(alpha = 0.2f)
            else -> LangoaSurface
        },
        animationSpec = tween(300),
        label = "optionBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            answered == null -> LangoaSurfaceVariant
            isThisSelected && answered.isCorrect -> LangoaGreen
            isThisSelected && !answered.isCorrect -> LangoaError
            isCorrect && answered != null -> LangoaGreen
            else -> LangoaSurfaceVariant
        },
        animationSpec = tween(300),
        label = "optionBorder"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = answered == null) { onAnswer(text) }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = LangoaOnBackground
            )
            if (answered != null) {
                when {
                    isThisSelected && answered.isCorrect ->
                        Icon(Icons.Filled.Check, null, tint = LangoaGreen, modifier = Modifier.size(20.dp))
                    isThisSelected && !answered.isCorrect ->
                        Icon(Icons.Filled.Close, null, tint = LangoaError, modifier = Modifier.size(20.dp))
                    isCorrect ->
                        Icon(Icons.Filled.Check, null, tint = LangoaGreen, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun AnswerFeedbackBanner(
    isCorrect: Boolean,
    correctAnswer: String
) {
    val bgColor = if (isCorrect) LangoaGreen.copy(alpha = 0.15f) else LangoaError.copy(alpha = 0.12f)
    val textColor = if (isCorrect) LangoaGreenLight else LangoaError
    val icon = if (isCorrect) "✅" else "❌"
    val message = if (isCorrect) "Excellent! That's correct!" else "Not quite. Correct: $correctAnswer"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun LessonPlaceholder(
    lessonId: String,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LangoaBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📚",
                fontSize = 64.sp
            )
            Text(
                text = "Lesson $lessonId",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = LangoaOnBackground
            )
            Text(
                text = "Exercises are loading from the server.",
                style = MaterialTheme.typography.bodyMedium,
                color = LangoaOnBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LangoaGreen)
            ) {
                Text("Complete Lesson", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = LangoaSurfaceVariant)
            ) {
                Text("Back")
            }
        }
    }
}

private fun correctAnswerFor(exercise: Exercise): String = when (exercise) {
    is Exercise.VocabularyExercise -> exercise.correctAnswer
    is Exercise.MultipleChoiceExercise -> exercise.correctAnswer
    is Exercise.TranslateToTargetExercise -> exercise.correctAnswer
    is Exercise.TranslateChoiceExercise -> exercise.correctAnswer
    is Exercise.SentenceConstructExercise -> exercise.correctAnswer
    is Exercise.ListenChooseExercise -> exercise.correctAnswer
    else -> ""
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1923)
@Composable
private fun LessonScreenPreview() {
    Box(
        Modifier
            .fillMaxSize()
            .background(LangoaBackground)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "What does 'Hund' mean?",
                style = MaterialTheme.typography.headlineMedium,
                color = LangoaOnBackground,
                fontWeight = FontWeight.Bold
            )
            listOf("Dog", "Cat", "House", "Water").forEach { opt ->
                OptionButton(
                    text = opt,
                    exerciseState = ExerciseState.Unanswered,
                    correctAnswer = "Dog",
                    onAnswer = {}
                )
            }
        }
    }
}
