package com.langoa.app.ui.screens.world

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.langoa.app.ui.theme.LangoaAmber
import com.langoa.app.ui.theme.LangoaBackground
import com.langoa.app.ui.theme.LangoaBlue
import com.langoa.app.ui.theme.LangoaBlueDark
import com.langoa.app.ui.theme.LangoaOnBackground
import com.langoa.app.ui.theme.LangoaSurface
import com.langoa.app.ui.theme.LangoaSurfaceVariant

@Composable
fun WorldScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "globe")
    val globeScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "globeScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LangoaBackground),
        contentAlignment = Alignment.Center
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            LangoaBlue.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🌍",
                fontSize = 96.sp,
                modifier = Modifier.scale(globeScale)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "WORLD RANKINGS",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = LangoaOnBackground,
                letterSpacing = 2.sp
            )

            Text(
                text = "Coming Soon",
                style = MaterialTheme.typography.titleMedium,
                color = LangoaAmber,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            Text(
                text = "Compete with language learners from around the world. Build the most powerful civilization and climb the global leaderboard.",
                style = MaterialTheme.typography.bodyMedium,
                color = LangoaOnBackground.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Teaser cards
            listOf(
                Triple("🏆", "Global Rankings", "Top civilizations worldwide"),
                Triple("⚔", "Civilization Battles", "Challenge other players"),
                Triple("🤝", "Language Allies", "Team up with learners")
            ).forEach { (icon, title, sub) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.6f)
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = LangoaSurface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = icon, fontSize = 24.sp)
                        Column(modifier = Modifier.padding(start = 14.dp)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = LangoaOnBackground
                            )
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.bodySmall,
                                color = LangoaOnBackground.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Soon",
                            style = MaterialTheme.typography.labelSmall,
                            color = LangoaAmber.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1923)
@Composable
private fun WorldScreenPreview() {
    WorldScreen()
}
