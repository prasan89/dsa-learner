package com.langoa.app.ui.screens.build

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.langoa.app.domain.model.Building
import com.langoa.app.domain.model.Civilization
import com.langoa.app.ui.components.LoadingScreen
import com.langoa.app.ui.theme.LangoaAmber
import com.langoa.app.ui.theme.LangoaAmberLight
import com.langoa.app.ui.theme.LangoaBackground
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
fun BuildScreen(
    languageCode: String,
    viewModel: BuildViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var buildingToBuild by remember { mutableStateOf<Building?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(languageCode) {
        viewModel.loadData(languageCode)
        visible = true
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    if (uiState.isLoading && uiState.civilization == null) {
        LoadingScreen()
        return
    }

    // Confirmation dialog
    buildingToBuild?.let { building ->
        AlertDialog(
            onDismissRequest = { buildingToBuild = null },
            containerColor = LangoaSurface,
            title = {
                Text(
                    text = "Build ${building.name}?",
                    color = LangoaOnBackground,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("This will cost:", color = LangoaOnBackground.copy(alpha = 0.7f))
                    if (building.coinCost > 0) CostRow("🪙", "${building.coinCost} Coins", LangoaCoins)
                    if (building.foodCost > 0) CostRow("🌾", "${building.foodCost} Food", LangoaFood)
                    if (building.materialsCost > 0) CostRow("🧱", "${building.materialsCost} Materials", LangoaMaterials)
                    Text(
                        text = "Grants +${building.civPowerGrant} Civilization Power",
                        style = MaterialTheme.typography.bodySmall,
                        color = LangoaGreenLight,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.buildBuilding(languageCode, building.buildingType)
                        buildingToBuild = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LangoaAmber)
                ) {
                    Text("BUILD", fontWeight = FontWeight.Bold, color = Color(0xFF1A1000))
                }
            },
            dismissButton = {
                TextButton(onClick = { buildingToBuild = null }) {
                    Text("Cancel", color = LangoaOnBackground.copy(alpha = 0.6f))
                }
            }
        )
    }

    Scaffold(
        containerColor = LangoaBackground,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data, containerColor = LangoaSurfaceVariant, contentColor = LangoaOnBackground)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LangoaBackground)
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(LangoaGreen.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Text(
                        text = "BUILD YOUR CIVILIZATION",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = LangoaOnBackground,
                        letterSpacing = 1.sp
                    )
                    uiState.civilization?.let { civ ->
                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ResourcePill("🪙", civ.coins.toString(), LangoaCoins)
                            ResourcePill("🌾", civ.food.toString(), LangoaFood)
                            ResourcePill("🧱", civ.materials.toString(), LangoaMaterials)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            uiState.civilization?.let { civ ->
                if (civ.buildings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("🏗", fontSize = 56.sp)
                            Text(
                                text = "Complete lessons to unlock buildings!",
                                style = MaterialTheme.typography.titleMedium,
                                color = LangoaOnBackground.copy(alpha = 0.55f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                    ) {
                        items(civ.buildings) { building ->
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 40 }
                            ) {
                                BuildingCard(
                                    building = building,
                                    onBuild = { buildingToBuild = building },
                                    onUpgrade = { viewModel.buildBuilding(languageCode, building.buildingType) },
                                    isBuildingInProgress = uiState.isBuildingInProgress
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuildingCard(
    building: Building,
    onBuild: () -> Unit,
    onUpgrade: () -> Unit,
    isBuildingInProgress: Boolean
) {
    val buildingEmoji = when (building.buildingType.uppercase()) {
        "HOUSE", "RESIDENTIAL" -> "🏠"
        "FARM" -> "🌾"
        "LEARNING_CENTER", "ACADEMY" -> "🏫"
        "MARKET" -> "🏪"
        "BARRACKS" -> "⚔"
        "LIBRARY" -> "📚"
        else -> "🏛"
    }

    val isBuilt = building.isUnlocked
    val isLocked = building.xpRequirement > 0 // simplified lock check

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isLocked && !isBuilt) 0.5f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = if (isBuilt) LangoaGreen.copy(alpha = 0.12f) else LangoaSurface
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Building emoji icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LangoaSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = buildingEmoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = building.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LangoaOnBackground
                    )
                    if (isBuilt) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LangoaGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Lv ${building.level}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LangoaGreenLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (building.description.isNotEmpty()) {
                    Text(
                        text = building.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = LangoaOnBackground.copy(alpha = 0.55f),
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }

                // Cost row
                if (!isBuilt) {
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (building.coinCost > 0) SmallCostPill("🪙 ${building.coinCost}", LangoaCoins)
                        if (building.foodCost > 0) SmallCostPill("🌾 ${building.foodCost}", LangoaFood)
                        if (building.materialsCost > 0) SmallCostPill("🧱 ${building.materialsCost}", LangoaMaterials)
                    }
                    if (building.xpRequirement > 0) {
                        Text(
                            text = "Requires ${building.xpRequirement} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = LangoaXP,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action button
            when {
                isBuildingInProgress -> {
                    CircularProgressIndicator(
                        color = LangoaAmber,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                }
                isBuilt -> {
                    Button(
                        onClick = onUpgrade,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LangoaGreen.copy(alpha = 0.25f),
                            contentColor = LangoaGreenLight
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("UPGRADE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
                isLocked -> {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = LangoaOnBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(22.dp)
                    )
                }
                else -> {
                    Button(
                        onClick = onBuild,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LangoaAmber,
                            contentColor = Color(0xFF1A1000)
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("BUILD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourcePill(icon: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LangoaSurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = value, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SmallCostPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CostRow(icon: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1923)
@Composable
private fun BuildingCardPreview() {
    Box(Modifier.background(LangoaBackground).padding(16.dp)) {
        BuildingCard(
            building = Building(
                id = "1", buildingType = "LEARNING_CENTER", name = "Academy",
                level = 1, isUnlocked = false, coinCost = 50, foodCost = 20,
                materialsCost = 30, civPowerGrant = 10, description = "Center of learning",
                xpRequirement = 100
            ),
            onBuild = {}, onUpgrade = {}, isBuildingInProgress = false
        )
    }
}
