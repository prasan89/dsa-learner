package com.langoa.app.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.langoa.app.domain.model.Building
import com.langoa.app.domain.model.Civilization
import com.langoa.app.domain.model.Lesson
import com.langoa.app.ui.components.LoadingScreen
import com.langoa.app.ui.theme.LangoaAmber
import com.langoa.app.ui.theme.LangoaAmberLight
import com.langoa.app.ui.theme.LangoaBackground
import com.langoa.app.ui.theme.LangoaBlue
import com.langoa.app.ui.theme.LangoaCoins
import com.langoa.app.ui.theme.LangoaFood
import com.langoa.app.ui.theme.LangoaGreen
import com.langoa.app.ui.theme.LangoaGreenLight
import com.langoa.app.ui.theme.LangoaMaterials
import com.langoa.app.ui.theme.LangoaOnBackground
import com.langoa.app.ui.theme.LangoaSurface
import com.langoa.app.ui.theme.LangoaSurfaceVariant
import com.langoa.app.ui.theme.LangoaXP
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    languageCode: String,
    onNavigateToLesson: ((lessonId: String) -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var buildingsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(languageCode) {
        viewModel.loadData(languageCode)
    }

    LaunchedEffect(uiState.civilization) {
        if (uiState.civilization != null) {
            delay(200)
            buildingsVisible = true
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    if (uiState.isLoading && uiState.civilization == null) {
        LoadingScreen()
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
            // Top bar
            HomeTopBar(
                languageCode = languageCode,
                civilization = uiState.civilization
            )

            // Resource bar
            uiState.civilization?.let { civ ->
                ResourceChipRow(civilization = civ)
            }

            // City canvas view
            CityView(
                civilization = uiState.civilization,
                buildingsVisible = buildingsVisible,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            // Bottom card with Continue Learning
            uiState.nextLesson?.let { lesson ->
                ContinueLearningCard(
                    lesson = lesson,
                    onContinue = { onNavigateToLesson?.invoke(lesson.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } ?: run {
                // No more lessons placeholder
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = LangoaSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "All lessons complete! More coming soon.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LangoaOnBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(20.dp).fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HomeTopBar(
    languageCode: String,
    civilization: Civilization?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "LANGOA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = LangoaAmber,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(LangoaSurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (languageCode) {
                        "de" -> "🇩🇪 DE"
                        "hi" -> "🇮🇳 HI"
                        "kn" -> "🇮🇳 KN"
                        else -> languageCode.uppercase()
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = LangoaOnBackground
                )
            }
        }

        civilization?.let { civ ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(LangoaGreen.copy(alpha = 0.3f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⚔ Lv ${civ.tierLevel}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = LangoaGreenLight
                )
            }
        }
    }
}

@Composable
private fun ResourceChipRow(civilization: Civilization) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ResourceChip(icon = "⭐", value = civilization.totalXp.toLong(), color = LangoaXP, modifier = Modifier.weight(1f))
        ResourceChip(icon = "🪙", value = civilization.coins.toLong(), color = LangoaCoins, modifier = Modifier.weight(1f))
        ResourceChip(icon = "🌾", value = civilization.food.toLong(), color = LangoaFood, modifier = Modifier.weight(1f))
        ResourceChip(icon = "🧱", value = civilization.materials.toLong(), color = LangoaMaterials, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ResourceChip(
    icon: String,
    value: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LangoaSurfaceVariant)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatResourceCount(value),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

private fun formatResourceCount(value: Long): String = when {
    value >= 1_000_000 -> "${value / 1_000_000}M"
    value >= 1_000 -> "${value / 1_000}K"
    else -> value.toString()
}

@Composable
private fun CityView(
    civilization: Civilization?,
    buildingsVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LangoaSurfaceVariant),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCityScene(this, civilization)
        }

        // Overlay labels
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // City tier label top-left
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${civilization?.name ?: "My Civilization"} • Village Lv ${civilization?.tierLevel ?: 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = LangoaAmberLight
                )
            }

            // Building count bottom-right
            if (civilization != null && civilization.buildings.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${civilization.buildings.size} buildings",
                        style = MaterialTheme.typography.labelSmall,
                        color = LangoaOnBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private fun drawCityScene(scope: DrawScope, civilization: Civilization?) {
    val w = scope.size.width
    val h = scope.size.height

    // Sky gradient
    scope.drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A1628),
                Color(0xFF1A2B45),
                Color(0xFF2A4060)
            ),
            startY = 0f,
            endY = h * 0.7f
        ),
        size = scope.size
    )

    // Stars
    val starPositions = listOf(
        0.1f to 0.05f, 0.3f to 0.1f, 0.5f to 0.03f, 0.7f to 0.08f,
        0.9f to 0.05f, 0.2f to 0.18f, 0.6f to 0.15f, 0.85f to 0.2f,
        0.45f to 0.22f, 0.15f to 0.28f, 0.75f to 0.12f
    )
    starPositions.forEach { (rx, ry) ->
        scope.drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = 2.5f,
            center = Offset(w * rx, h * ry)
        )
    }

    // Moon
    scope.drawCircle(
        color = Color(0xFFFFF5CC),
        radius = 28f,
        center = Offset(w * 0.82f, h * 0.1f)
    )
    scope.drawCircle(
        color = Color(0xFF1A2B45),
        radius = 22f,
        center = Offset(w * 0.86f, h * 0.08f)
    )

    // Ground base
    val groundTop = h * 0.62f
    scope.drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1E4020), Color(0xFF163018), Color(0xFF0F2010)),
            startY = groundTop,
            endY = h
        ),
        topLeft = Offset(0f, groundTop),
        size = Size(w, h - groundTop),
        cornerRadius = CornerRadius(16f, 16f)
    )

    // Ground accent / path
    scope.drawRoundRect(
        color = Color(0xFF2A5C2C).copy(alpha = 0.4f),
        topLeft = Offset(w * 0.35f, groundTop),
        size = Size(w * 0.3f, (h - groundTop)),
        cornerRadius = CornerRadius(8f, 0f)
    )

    // Buildings
    val buildings = civilization?.buildings ?: emptyList()
    if (buildings.isEmpty()) {
        // Default placeholder buildings
        drawBuilding(scope, w * 0.15f, groundTop, w * 0.12f, h * 0.18f, "HOUSE", 0)
        drawBuilding(scope, w * 0.35f, groundTop, w * 0.15f, h * 0.28f, "LEARNING_CENTER", 1)
        drawBuilding(scope, w * 0.58f, groundTop, w * 0.13f, h * 0.20f, "FARM", 2)
        drawBuilding(scope, w * 0.77f, groundTop, w * 0.10f, h * 0.14f, "HOUSE", 3)
    } else {
        val buildingPositions = listOf(
            0.08f to 0.80f, 0.25f to 0.75f, 0.45f to 0.82f,
            0.62f to 0.77f, 0.78f to 0.80f
        )
        buildings.take(5).forEachIndexed { index, building ->
            val (rx, ry) = buildingPositions[index]
            val bw = w * 0.13f
            val bh = h * when (building.buildingType) {
                "LEARNING_CENTER", "ACADEMY" -> 0.30f
                "HOUSE", "RESIDENTIAL" -> 0.18f
                "FARM" -> 0.12f
                else -> 0.20f
            }
            drawBuilding(scope, w * rx, groundTop, bw, bh, building.buildingType, index)
        }
    }
}

private fun drawBuilding(
    scope: DrawScope,
    x: Float,
    groundY: Float,
    width: Float,
    height: Float,
    type: String,
    index: Int
) {
    val baseColor = when (type) {
        "HOUSE", "RESIDENTIAL" -> Color(0xFF8B6914)
        "FARM" -> Color(0xFF4A7C2F)
        "LEARNING_CENTER", "ACADEMY" -> Color(0xFF2A5C8A)
        "MARKET" -> Color(0xFF8B3A14)
        else -> Color(0xFF556677)
    }
    val roofColor = when (type) {
        "HOUSE", "RESIDENTIAL" -> Color(0xFF6B4A0A)
        "FARM" -> Color(0xFF3A5C22)
        "LEARNING_CENTER", "ACADEMY" -> Color(0xFF1A3C6A)
        else -> Color(0xFF334455)
    }

    // Shadow
    scope.drawRoundRect(
        color = Color.Black.copy(alpha = 0.35f),
        topLeft = Offset(x + 4f, groundY - height + 6f),
        size = Size(width, height),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Main body
    scope.drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(baseColor.copy(alpha = 0.95f), baseColor.copy(alpha = 0.75f)),
            startY = groundY - height,
            endY = groundY
        ),
        topLeft = Offset(x, groundY - height),
        size = Size(width, height),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Roof / top detail
    when (type) {
        "HOUSE", "RESIDENTIAL" -> {
            // Triangle roof
            val roofPath = Path().apply {
                moveTo(x - 4f, groundY - height)
                lineTo(x + width / 2f, groundY - height - height * 0.3f)
                lineTo(x + width + 4f, groundY - height)
                close()
            }
            scope.drawPath(roofPath, color = roofColor)
        }
        "LEARNING_CENTER", "ACADEMY" -> {
            // Flat top with battlements
            scope.drawRoundRect(
                color = roofColor,
                topLeft = Offset(x - 2f, groundY - height - 8f),
                size = Size(width + 4f, 12f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            // Small tower on top
            scope.drawRoundRect(
                color = Color(0xFF3A6CA0),
                topLeft = Offset(x + width * 0.3f, groundY - height - 22f),
                size = Size(width * 0.4f, 18f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        "FARM" -> {
            // Flat slanted roof
            scope.drawRoundRect(
                color = roofColor,
                topLeft = Offset(x - 2f, groundY - height - 6f),
                size = Size(width + 4f, 10f),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
        else -> {
            scope.drawRoundRect(
                color = roofColor,
                topLeft = Offset(x, groundY - height - 6f),
                size = Size(width, 10f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }

    // Door
    scope.drawRoundRect(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = Offset(x + width * 0.38f, groundY - height * 0.35f),
        size = Size(width * 0.24f, height * 0.32f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Window(s)
    scope.drawRoundRect(
        color = Color(0xFFFFFAC8).copy(alpha = 0.7f),
        topLeft = Offset(x + width * 0.12f, groundY - height * 0.65f),
        size = Size(width * 0.22f, height * 0.2f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    if (type == "LEARNING_CENTER" || type == "ACADEMY") {
        scope.drawRoundRect(
            color = Color(0xFFFFFAC8).copy(alpha = 0.7f),
            topLeft = Offset(x + width * 0.65f, groundY - height * 0.65f),
            size = Size(width * 0.22f, height * 0.2f),
            cornerRadius = CornerRadius(3f, 3f)
        )
    }

    // Outline
    scope.drawRoundRect(
        color = baseColor.copy(alpha = 0.4f),
        topLeft = Offset(x, groundY - height),
        size = Size(width, height),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 1.5f)
    )
}

@Composable
private fun ContinueLearningCard(
    lesson: Lesson,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LangoaSurface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Continue Learning",
                style = MaterialTheme.typography.labelLarge,
                color = LangoaAmber,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LangoaOnBackground
            )
            Text(
                text = "${lesson.exerciseCount} exercises • +${lesson.xpReward} XP",
                style = MaterialTheme.typography.bodySmall,
                color = LangoaOnBackground.copy(alpha = 0.55f),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LangoaGreen,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "CONTINUE",
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1923)
@Composable
private fun HomeScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LangoaBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "LANGOA",
            fontWeight = FontWeight.Black,
            color = LangoaAmber,
            fontSize = 24.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(LangoaSurfaceVariant)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCityScene(this, null)
            }
        }
    }
}
