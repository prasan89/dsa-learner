package com.langoa.app.ui.screens.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langoa.app.data.remote.model.LessonOverviewDto
import com.langoa.app.domain.repository.LearningRepository
import com.langoa.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LessonOverviewUiState(
    val overview: LessonOverviewDto? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LessonOverviewViewModel @Inject constructor(
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonOverviewUiState())
    val uiState: StateFlow<LessonOverviewUiState> = _uiState.asStateFlow()

    fun load(languageCode: String, lessonId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            learningRepository.getLessonOverview(languageCode, lessonId).fold(
                onSuccess = { _uiState.value = LessonOverviewUiState(overview = it, isLoading = false) },
                onFailure = { _uiState.value = LessonOverviewUiState(isLoading = false, error = it.message) }
            )
        }
    }
}

@Composable
fun LessonOverviewScreen(
    languageCode: String,
    lessonId: String,
    onBack: () -> Unit,
    onStartLesson: () -> Unit,
    viewModel: LessonOverviewViewModel = hiltViewModel()
) {
    LaunchedEffect(lessonId) { viewModel.load(languageCode, lessonId) }
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LangoaBackground)
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = LangoaAmber
            )
            uiState.error != null -> Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(uiState.error ?: "Error", color = LangoaError)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { viewModel.load(languageCode, lessonId) }) { Text("Retry") }
            }
            uiState.overview != null -> LessonOverviewContent(
                overview = uiState.overview!!,
                onBack = onBack,
                onStartLesson = onStartLesson
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonOverviewContent(
    overview: LessonOverviewDto,
    onBack: () -> Unit,
    onStartLesson: () -> Unit
) {
    Scaffold(
        containerColor = LangoaBackground,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = LangoaOnBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LangoaBackground)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = onStartLesson,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LangoaAmber)
                ) {
                    Text(
                        "Start Lesson",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CEFR badge + time
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LangoaAmber.copy(alpha = 0.2f)
                ) {
                    Text(
                        overview.cefrLevel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = LangoaAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Text(
                    "~${overview.estimatedMinutes} min",
                    color = LangoaOnBackground.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
                Text(
                    "${overview.exerciseCount} exercises",
                    color = LangoaOnBackground.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }

            // Title
            Text(
                overview.title,
                style = MaterialTheme.typography.headlineMedium,
                color = LangoaOnBackground,
                fontWeight = FontWeight.ExtraBold
            )

            // Unit name
            if (!overview.unitDisplayName.isNullOrBlank()) {
                Text(
                    overview.unitDisplayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LangoaOnBackground.copy(alpha = 0.6f)
                )
            }

            // Can-do statement
            if (overview.canDo.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LangoaSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "You will be able to",
                            style = MaterialTheme.typography.labelMedium,
                            color = LangoaAmber
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            overview.canDo,
                            style = MaterialTheme.typography.bodyLarge,
                            color = LangoaOnBackground
                        )
                    }
                }
            }

            // Objectives
            if (overview.objectives.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "You'll learn",
                        style = MaterialTheme.typography.titleMedium,
                        color = LangoaOnBackground,
                        fontWeight = FontWeight.Bold
                    )
                    overview.objectives.forEach { obj ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("•", color = LangoaAmber, fontWeight = FontWeight.Bold)
                            Text(obj, style = MaterialTheme.typography.bodyMedium, color = LangoaOnBackground)
                        }
                    }
                }
            }

            // Skills
            if (overview.skillFocus.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    overview.skillFocus.forEach { skill ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = LangoaBlue.copy(alpha = 0.15f)
                        ) {
                            Text(
                                skill,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = LangoaBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Rewards
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LangoaSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⭐", fontSize = 24.sp)
                        Text("+${overview.xpReward} XP", color = LangoaXP, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = LangoaSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🪙", fontSize = 24.sp)
                        Text("+${overview.coinsReward}", color = LangoaAmber, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
