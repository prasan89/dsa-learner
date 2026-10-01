package com.langoa.app.ui.screens.learn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.langoa.app.domain.model.LessonReward
import com.langoa.app.ui.theme.LangoaAmber
import com.langoa.app.ui.theme.LangoaAmberLight
import com.langoa.app.ui.theme.LangoaBackground
import com.langoa.app.ui.theme.LangoaCivPower
import com.langoa.app.ui.theme.LangoaCoins
import com.langoa.app.ui.theme.LangoaFood
import com.langoa.app.ui.theme.LangoaGreen
import com.langoa.app.ui.theme.LangoaMaterials
import com.langoa.app.ui.theme.LangoaOnBackground
import com.langoa.app.ui.theme.LangoaSurface
import com.langoa.app.ui.theme.LangoaXP
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val startY: Float,
    val size: Float,
    val color: Color,
    val speed: Float,
    val wobble: Float,
    val rotation: Float,
    val isRect: Boolean
)

@Composable
fun RewardScreen(
    languageCode: String,
    lessonId: String,
    onContinue: () -> Unit,
    viewModel: RewardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Stub reward if none provided
    val reward = uiState.reward ?: LessonReward(
        lessonId = lessonId,
        xpEarned = 15,
        coinsEarned = 10,
        foodEarned = 5,
        materialsEarned = 3,
        civPowerEarned = 2
    )

    var rewardsVisible by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(400)
        rewardsVisible = true
        delay(1800)
        buttonVisible = true
    }

    val titleScale by animateFloatAsState(
        targetValue = if (rewardsVisible) 1f else 0.5f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "titleScale"
    )

    // Confetti particles (generated once)
    val confettiColors = listOf(
        LangoaAmber, LangoaGreen, Color(0xFF4A90D9), LangoaCivPower,
        LangoaFood, Color.Red.copy(alpha = 0.8f), Color.Cyan.copy(alpha = 0.8f), Color.Magenta.copy(alpha = 0.7f)
    )
    val particles = remember {
        (0 until 60).map { i ->
            ConfettiParticle(
                x = (i * 0.0163f + Random.nextFloat() * 0.98f).coerceIn(0f, 1f),
                startY = -0.15f - Random.nextFloat() * 0.5f,
                size = 6f + Random.nextFloat() * 12f,
                color = confettiColors[i % confettiColors.size],
                speed = 0.3f + Random.nextFloat() * 0.5f,
                wobble = Random.nextFloat() * 3.14159f * 2f,
                rotation = Random.nextFloat() * 360f,
                isRect = Random.nextBoolean()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val confettiProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LangoaBackground),
        contentAlignment = Alignment.Center
    ) {
        // Confetti canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val y = ((p.startY + confettiProgress * p.speed * 1.5f) % 1.3f) * size.height
                val x = p.x * size.width + sin((confettiProgress * 6.28f + p.wobble) * 2f) * 30f
                if (p.isRect) {
                    drawRect(
                        color = p.color,
                        topLeft = Offset(x, y),
                        size = Size(p.size, p.size * 0.6f)
                    )
                } else {
                    drawCircle(
                        color = p.color,
                        radius = p.size / 2f,
                        center = Offset(x, y)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trophy + title
            Text(
                text = "🏆",
                fontSize = 72.sp,
                modifier = Modifier.scale(titleScale)
            )

            Text(
                text = "LESSON COMPLETE!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = LangoaAmberLight,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
                modifier = Modifier.scale(titleScale)
            )

            Text(
                text = "🎉 Outstanding work! 🎉",
                style = MaterialTheme.typography.titleMedium,
                color = LangoaOnBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Rewards card
            AnimatedVisibility(
                visible = rewardsVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 80 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LangoaSurface.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "YOU EARNED",
                            style = MaterialTheme.typography.labelLarge,
                            color = LangoaOnBackground.copy(alpha = 0.5f),
                            letterSpacing = 2.sp
                        )

                        RewardRow(icon = "⭐", label = "Experience", value = "+${reward.xpEarned} XP", color = LangoaXP)
                        RewardRow(icon = "🪙", label = "Coins", value = "+${reward.coinsEarned}", color = LangoaCoins)
                        RewardRow(icon = "🌾", label = "Food", value = "+${reward.foodEarned}", color = LangoaFood)
                        RewardRow(icon = "🧱", label = "Materials", value = "+${reward.materialsEarned}", color = LangoaMaterials)
                        RewardRow(icon = "🏰", label = "Civilization Power", value = "+${reward.civPowerEarned}", color = LangoaCivPower)

                        if (reward.isPerfect) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(LangoaAmber.copy(alpha = 0.15f))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚡", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "PERFECT SCORE! Bonus XP earned!",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = LangoaAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Continue button
            AnimatedVisibility(
                visible = buttonVisible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 40 }
            ) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LangoaGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "CONTINUE TO CITY",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardRow(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = LangoaOnBackground.copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1923)
@Composable
private fun RewardScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LangoaBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🏆", fontSize = 72.sp)
        Text("LESSON COMPLETE!", fontWeight = FontWeight.Black, color = LangoaAmberLight, fontSize = 28.sp)
        RewardRow("⭐", "Experience", "+15 XP", LangoaXP)
        RewardRow("🪙", "Coins", "+10", LangoaCoins)
    }
}
