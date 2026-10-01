package com.langoa.app.ui.screens.learn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.langoa.app.domain.model.Lesson
import com.langoa.app.ui.components.LoadingScreen
import com.langoa.app.ui.theme.LangoaAmber
import com.langoa.app.ui.theme.LangoaBackground
import com.langoa.app.ui.theme.LangoaBlue
import com.langoa.app.ui.theme.LangoaGreen
import com.langoa.app.ui.theme.LangoaGreenLight
import com.langoa.app.ui.theme.LangoaOnBackground
import com.langoa.app.ui.theme.LangoaSurface
import com.langoa.app.ui.theme.LangoaSurfaceVariant
import com.langoa.app.ui.theme.LangoaXP

@Composable
fun LearnScreen(
    languageCode: String,
    onLessonClick: (String) -> Unit,
    viewModel: LessonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(languageCode) {
        viewModel.loadLessons(languageCode)
        visible = true
    }

    if (uiState.isLoading) {
        LoadingScreen()
        return
    }

    // Group lessons by unit (treating unit as CEFR stage equivalent)
    val groupedLessons = uiState.lessons.groupBy { it.unitNumber }
    val cefrLabels = mapOf(1 to "A1 — Beginner", 2 to "A2 — Elementary", 3 to "B1 — Intermediate", 4 to "B2 — Upper-Intermediate")
    val stageTabs = listOf("Stage 1", "Stage 2", "Stage 3", "Stage 4")
    val availableStages = groupedLessons.keys.sorted()
    var selectedStage by remember { mutableStateOf(availableStages.firstOrNull() ?: 1) }

    Scaffold(containerColor = LangoaBackground) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LangoaBackground)
                .padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(LangoaBlue.copy(alpha = 0.12f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (languageCode) { "de" -> "🇩🇪"; "hi" -> "🇮🇳"; "kn" -> "🪷"; else -> "🌐" },
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = when (languageCode) { "de" -> "German"; "hi" -> "Hindi"; "kn" -> "Kannada"; else -> languageCode.uppercase() },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = LangoaOnBackground
                                )
                                val completedCount = uiState.lessons.count { it.isCompleted }
                                Text(
                                    text = "$completedCount / ${uiState.lessons.size} lessons completed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LangoaOnBackground.copy(alpha = 0.55f)
                                )
                            }
                        }
                    }
                }
            }

            // Stage tabs
            if (availableStages.size > 1) {
                item {
                    val selectedTabIndex = availableStages.indexOf(selectedStage).coerceAtLeast(0)
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = LangoaBackground,
                        contentColor = LangoaAmber,
                        indicator = { tabPositions ->
                            if (selectedTabIndex < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = LangoaAmber
                                )
                            }
                        }
                    ) {
                        availableStages.forEachIndexed { index, stage ->
                            Tab(
                                selected = stage == selectedStage,
                                onClick = { selectedStage = stage },
                                text = {
                                    Text(
                                        stageTabs.getOrElse(stage - 1) { "Stage $stage" },
                                        fontWeight = if (stage == selectedStage) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Grouped lesson sections — only for selected stage
            val filteredGroups = groupedLessons.entries
                .filter { it.key == selectedStage }
                .sortedBy { it.key }

            filteredGroups.forEach { (unitNumber, lessons) ->
                val sectionLabel = cefrLabels[unitNumber] ?: "Unit $unitNumber"
                val allCompleted = lessons.all { it.isCompleted }
                val completedInUnit = lessons.count { it.isCompleted }

                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 30 }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Unit badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (allCompleted) LangoaGreen.copy(alpha = 0.3f)
                                        else LangoaAmber.copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "A${unitNumber}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (allCompleted) LangoaGreenLight else LangoaAmber
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sectionLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = LangoaOnBackground
                                )
                                Text(
                                    text = "$completedInUnit / ${lessons.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LangoaOnBackground.copy(alpha = 0.45f)
                                )
                            }
                        }
                    }
                }

                items(lessons) { lesson ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 50 }
                    ) {
                        LessonCard(
                            lesson = lesson,
                            onClick = { if (!lesson.isLocked) onLessonClick(lesson.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonCard(
    lesson: Lesson,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        lesson.isCompleted -> LangoaGreen.copy(alpha = 0.5f)
        lesson.isLocked -> Color.Transparent
        else -> LangoaAmber.copy(alpha = 0.4f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (lesson.isLocked) 0.45f else 1f)
            .border(
                width = if (lesson.isCompleted || !lesson.isLocked) 1.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = !lesson.isLocked, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                lesson.isCompleted -> LangoaGreen.copy(alpha = 0.1f)
                lesson.isLocked -> LangoaSurfaceVariant.copy(alpha = 0.5f)
                else -> LangoaSurface
            }
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            lesson.isCompleted -> LangoaGreen.copy(alpha = 0.25f)
                            lesson.isLocked -> LangoaSurfaceVariant
                            else -> LangoaAmber.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    lesson.isCompleted -> Icon(
                        Icons.Filled.Check, null,
                        tint = LangoaGreenLight,
                        modifier = Modifier.size(20.dp)
                    )
                    lesson.isLocked -> Icon(
                        Icons.Filled.Lock, null,
                        tint = LangoaOnBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                    else -> Text(
                        text = "${lesson.lessonNumber}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = LangoaAmber
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = LangoaOnBackground
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 3.dp)
                ) {
                    Text(
                        text = "${lesson.exerciseCount} exercises",
                        style = MaterialTheme.typography.labelSmall,
                        color = LangoaOnBackground.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "⭐ +${lesson.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = LangoaXP
                    )
                }
            }

            // Right badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            lesson.isCompleted -> LangoaGreen.copy(alpha = 0.2f)
                            lesson.isLocked -> Color.Transparent
                            else -> LangoaAmber.copy(alpha = 0.15f)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = when {
                        lesson.isCompleted -> "Done ✓"
                        lesson.isLocked -> "Locked"
                        else -> "Start →"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        lesson.isCompleted -> LangoaGreenLight
                        lesson.isLocked -> LangoaOnBackground.copy(alpha = 0.3f)
                        else -> LangoaAmber
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1923)
@Composable
private fun LessonCardPreview() {
    Column(
        Modifier
            .background(LangoaBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LessonCard(
            lesson = Lesson("1", "Basic Greetings", "", "de", 1, 1, isCompleted = true, xpReward = 15, exerciseCount = 8),
            onClick = {}
        )
        LessonCard(
            lesson = Lesson("2", "Numbers 1-10", "", "de", 1, 2, isCompleted = false, xpReward = 20, exerciseCount = 10),
            onClick = {}
        )
        LessonCard(
            lesson = Lesson("3", "Colors & Objects", "", "de", 1, 3, isLocked = true, xpReward = 25, exerciseCount = 12),
            onClick = {}
        )
    }
}
