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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.langoa.app.domain.model.Building
import com.langoa.app.domain.model.BuildingDefinition
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
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.ui.platform.LocalContext

@Composable
fun BuildScreen(
    languageCode: String,
    viewModel: BuildViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var buildingToBuild by remember { mutableStateOf<BuildingDefinition?>(null) }
    var buildingToUpgrade by remember { mutableStateOf<Pair<String, BuildingDefinition>?>(null) } // buildingId to def
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(languageCode) {
        viewModel.loadData(languageCode)
        visible = true
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    if (uiState.isLoading && uiState.civilization == null) {
        LoadingScreen()
        return
    }

    // Build confirmation dialog
    buildingToBuild?.let { def ->
        val nextCfg = def.nextLevelConfig
        AlertDialog(
            onDismissRequest = { buildingToBuild = null },
            containerColor = LangoaSurface,
            title = {
                Text(
                    text = "Build ${def.displayName}?",
                    color = LangoaOnBackground,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("This will cost:", color = LangoaOnBackground.copy(alpha = 0.7f))
                    if (nextCfg != null) {
                        if (nextCfg.coinCost > 0) CostRow("🪙", "${nextCfg.coinCost} Coins", LangoaCoins)
                        if (nextCfg.foodCost > 0) CostRow("🌾", "${nextCfg.foodCost} Food", LangoaFood)
                        if (nextCfg.materialCost > 0) CostRow("🧱", "${nextCfg.materialCost} Materials", LangoaMaterials)
                        if (nextCfg.woodCost > 0) CostRow("🪵", "${nextCfg.woodCost} Wood", LangoaAmberLight)
                        if (nextCfg.requiredLessonsCompleted > 0) {
                            Text(
                                text = "Requires ${nextCfg.requiredLessonsCompleted} lessons",
                                style = MaterialTheme.typography.bodySmall,
                                color = LangoaXP,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.buildBuilding(languageCode, def.buildingType)
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

    // Upgrade confirmation dialog
    buildingToUpgrade?.let { (buildingId, def) ->
        val nextCfg = def.nextLevelConfig
        AlertDialog(
            onDismissRequest = { buildingToUpgrade = null },
            containerColor = LangoaSurface,
            title = {
                Text(
                    text = "Upgrade ${def.displayName} to Lv ${(def.currentLevel) + 1}?",
                    color = LangoaOnBackground,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Upgrade cost:", color = LangoaOnBackground.copy(alpha = 0.7f))
                    if (nextCfg != null) {
                        if (nextCfg.coinCost > 0) CostRow("🪙", "${nextCfg.coinCost} Coins", LangoaCoins)
                        if (nextCfg.foodCost > 0) CostRow("🌾", "${nextCfg.foodCost} Food", LangoaFood)
                        if (nextCfg.materialCost > 0) CostRow("🧱", "${nextCfg.materialCost} Materials", LangoaMaterials)
                        if (nextCfg.woodCost > 0) CostRow("🪵", "${nextCfg.woodCost} Wood", LangoaAmberLight)
                    } else {
                        Text("Max level reached!", color = LangoaGreenLight)
                    }
                }
            },
            confirmButton = {
                if (nextCfg != null) {
                    Button(
                        onClick = {
                            viewModel.upgradeBuilding(languageCode, buildingId)
                            buildingToUpgrade = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LangoaGreen)
                    ) {
                        Text("UPGRADE", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { buildingToUpgrade = null }) {
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
            // Offline banner
            val ctx = LocalContext.current
            val isOffline = remember {
                val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val caps = cm.getNetworkCapabilities(cm.activeNetwork)
                caps == null || !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
            if (isOffline) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LangoaAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.WifiOff, contentDescription = null, tint = LangoaAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Build features require network", style = MaterialTheme.typography.bodySmall, color = LangoaAmber)
                }
            }

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
                            ResourcePill("🪵", civ.wood.toString(), LangoaAmberLight)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val definitions = uiState.buildingDefinitions
            if (definitions.isEmpty()) {
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
                // Find instance IDs for buildings that are already built
                val builtInstances = uiState.civilization?.buildings ?: emptyList()
                val instanceByType = builtInstances.groupBy { it.buildingType }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    items(definitions) { def ->
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 40 }
                        ) {
                            val instances = instanceByType[def.buildingType] ?: emptyList()
                            val firstInstance = instances.firstOrNull()
                            BuildingCard(
                                definition = def,
                                instanceId = firstInstance?.id,
                                instanceCount = instances.size,
                                onBuild = { buildingToBuild = def },
                                onUpgrade = { id -> buildingToUpgrade = id to def },
                                isBuildingInProgress = uiState.isBuildingInProgress
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuildingCard(
    definition: BuildingDefinition,
    instanceId: String?,
    instanceCount: Int,
    onBuild: () -> Unit,
    onUpgrade: (String) -> Unit,
    isBuildingInProgress: Boolean
) {
    val buildingEmoji = when (definition.buildingType.uppercase()) {
        "HOUSE", "RESIDENTIAL" -> "🏠"
        "FARM" -> "🌾"
        "LEARNING_CENTER", "ACADEMY" -> "🏫"
        "MARKET" -> "🏪"
        "BARRACKS" -> "⚔"
        "LIBRARY" -> "📚"
        "WORKSHOP" -> "🔨"
        "SCHOOL" -> "🎒"
        "PARK" -> "🌳"
        else -> "🏛"
    }

    val isBuilt = definition.currentLevel > 0
    val isLocked = !definition.isUnlocked
    val isMaxLevel = definition.currentLevel >= definition.maxLevel
    val nextCfg = definition.nextLevelConfig

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isLocked) 0.5f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = if (isBuilt) LangoaGreen.copy(alpha = 0.12f) else LangoaSurface
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        text = definition.displayName,
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
                                text = "Lv ${definition.currentLevel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LangoaGreenLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (instanceCount > 1) {
                            Text(
                                text = "×$instanceCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = LangoaOnBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                if (definition.description.isNotEmpty()) {
                    Text(
                        text = definition.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = LangoaOnBackground.copy(alpha = 0.55f),
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }

                // Cost preview for next action
                if (!isBuilt && nextCfg != null) {
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (nextCfg.coinCost > 0) SmallCostPill("🪙 ${nextCfg.coinCost}", LangoaCoins)
                        if (nextCfg.foodCost > 0) SmallCostPill("🌾 ${nextCfg.foodCost}", LangoaFood)
                        if (nextCfg.materialCost > 0) SmallCostPill("🧱 ${nextCfg.materialCost}", LangoaMaterials)
                        if (nextCfg.woodCost > 0) SmallCostPill("🪵 ${nextCfg.woodCost}", LangoaAmberLight)
                    }
                    if (nextCfg.requiredLessonsCompleted > 0) {
                        Text(
                            text = "Requires ${nextCfg.requiredLessonsCompleted} lessons",
                            style = MaterialTheme.typography.labelSmall,
                            color = LangoaXP,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else if (isBuilt && !isMaxLevel && nextCfg != null) {
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Upgrade cost:",
                            style = MaterialTheme.typography.labelSmall,
                            color = LangoaOnBackground.copy(alpha = 0.5f)
                        )
                        if (nextCfg.coinCost > 0) SmallCostPill("🪙 ${nextCfg.coinCost}", LangoaCoins)
                        if (nextCfg.woodCost > 0) SmallCostPill("🪵 ${nextCfg.woodCost}", LangoaAmberLight)
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
                isLocked -> {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = LangoaOnBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(22.dp)
                    )
                }
                isBuilt && isMaxLevel -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LangoaGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "MAX",
                            style = MaterialTheme.typography.labelSmall,
                            color = LangoaGreenLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                isBuilt && instanceId != null -> {
                    Button(
                        onClick = { onUpgrade(instanceId) },
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
                definition.canBuild -> {
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
                else -> {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "Cannot build",
                        tint = LangoaOnBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(22.dp)
                    )
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
