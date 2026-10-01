package com.langoa.app.ui.screens.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.langoa.app.ui.theme.LangoaAmber
import com.langoa.app.ui.theme.LangoaAmberLight
import com.langoa.app.ui.theme.LangoaBackground
import com.langoa.app.ui.theme.LangoaCivPower
import com.langoa.app.ui.theme.LangoaCoins
import com.langoa.app.ui.theme.LangoaFood
import com.langoa.app.ui.theme.LangoaGreen
import com.langoa.app.ui.theme.LangoaGreenLight
import com.langoa.app.ui.theme.LangoaMaterials
import com.langoa.app.ui.theme.LangoaOnBackground
import com.langoa.app.ui.theme.LangoaSurface
import com.langoa.app.ui.theme.LangoaSurfaceVariant
import com.langoa.app.ui.theme.LangoaXP

@Composable
fun ProfileScreen() {
    Scaffold(containerColor = LangoaBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LangoaBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(LangoaGreen.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(LangoaAmber, LangoaGreen)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚔", fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Language Explorer",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = LangoaOnBackground
                    )
                    Text(
                        text = "player@langoa.app",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LangoaOnBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Level badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(LangoaAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "🏆  Civilization Builder  •  Level 3",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = LangoaAmberLight
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // Stats section
                Text(
                    text = "YOUR STATS",
                    style = MaterialTheme.typography.labelLarge,
                    color = LangoaOnBackground.copy(alpha = 0.45f),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("⭐", "Total XP", "240", LangoaXP, modifier = Modifier.weight(1f))
                    StatCard("📚", "Lessons", "12", LangoaGreenLight, modifier = Modifier.weight(1f))
                    StatCard("🔥", "Streak", "5 days", LangoaAmber, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Language badges
                Text(
                    text = "LANGUAGE BADGES",
                    style = MaterialTheme.typography.labelLarge,
                    color = LangoaOnBackground.copy(alpha = 0.45f),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LanguageBadge(
                        flag = "🇩🇪",
                        language = "German",
                        progress = "A1 — 12 lessons",
                        isActive = true,
                        modifier = Modifier.weight(1f)
                    )
                    LanguageBadge(
                        flag = "🇮🇳",
                        language = "Hindi",
                        progress = "Not started",
                        isActive = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Resources summary
                Text(
                    text = "CIVILIZATION RESOURCES",
                    style = MaterialTheme.typography.labelLarge,
                    color = LangoaOnBackground.copy(alpha = 0.45f),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LangoaSurface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ResourceStatRow("🪙", "Coins", "150", LangoaCoins)
                        ResourceStatRow("🌾", "Food", "80", LangoaFood)
                        ResourceStatRow("🧱", "Materials", "45", LangoaMaterials)
                        ResourceStatRow("🏰", "Civ Power", "22", LangoaCivPower)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Achievements teaser
                Text(
                    text = "ACHIEVEMENTS",
                    style = MaterialTheme.typography.labelLarge,
                    color = LangoaOnBackground.copy(alpha = 0.45f),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                listOf(
                    Triple("🏛", "First Steps", "Completed your first lesson"),
                    Triple("⚔", "Builder", "Constructed a building"),
                    Triple("🌍", "Explorer", "Reached 100 XP")
                ).forEach { (icon, title, desc) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = LangoaSurface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(LangoaAmber.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = icon, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = LangoaOnBackground
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LangoaOnBackground.copy(alpha = 0.5f)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text("✓", color = LangoaGreenLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: String,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = LangoaSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = LangoaOnBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LanguageBadge(
    flag: String,
    language: String,
    progress: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) LangoaGreen.copy(alpha = 0.12f) else LangoaSurfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = flag, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = language,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isActive) LangoaOnBackground else LangoaOnBackground.copy(alpha = 0.4f)
            )
            Text(
                text = progress,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) LangoaGreenLight else LangoaOnBackground.copy(alpha = 0.3f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

@Composable
private fun ResourceStatRow(icon: String, label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = LangoaOnBackground.copy(alpha = 0.65f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1923)
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen()
}
