package com.langoa.app.ui.screens.paywall

import android.app.Activity
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.langoa.app.data.remote.model.SubscriptionPlanDto
import com.langoa.app.ui.theme.LangoaAmber
import com.langoa.app.ui.theme.LangoaAmberLight
import com.langoa.app.ui.theme.LangoaBackground
import com.langoa.app.ui.theme.LangoaGreen
import com.langoa.app.ui.theme.LangoaGreenLight
import com.langoa.app.ui.theme.LangoaOnBackground
import com.langoa.app.ui.theme.LangoaSurface
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    onUpgradeSuccess: () -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var billingClient by remember { mutableStateOf<BillingClient?>(null) }
    var productDetailsMap by remember { mutableStateOf<Map<String, ProductDetails>>(emptyMap()) }
    var selectedPlanCode by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LaunchedEffect(uiState.purchaseSuccess) {
        if (uiState.purchaseSuccess) {
            onUpgradeSuccess()
        }
    }

    LaunchedEffect(uiState.restoreSuccess) {
        if (uiState.restoreSuccess) {
            val message = if (uiState.currentStatus.isPro) "Pro subscription restored!" else "No active subscription found"
            scope.launch { snackbarHostState.showSnackbar(message) }
            viewModel.clearRestoreSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch { snackbarHostState.showSnackbar(error) }
            viewModel.clearError()
        }
    }

    val purchasesUpdatedListener = remember {
        PurchasesUpdatedListener { result, purchases ->
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    purchases?.firstOrNull()?.let { purchase ->
                        val planCode = selectedPlanCode ?: return@let
                        viewModel.onPlayPurchaseSuccess(
                            planCode = planCode,
                            purchaseToken = purchase.purchaseToken,
                            orderId = purchase.orderId ?: "",
                            billingClient = billingClient
                        )
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    // silently ignore
                }
                else -> {
                    viewModel.onPlayPurchaseFailure(result.responseCode, result.debugMessage)
                }
            }
        }
    }

    DisposableEffect(context) {
        val client = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    billingClient = client
                }
            }
            override fun onBillingServiceDisconnected() {
                billingClient = null
            }
        })

        onDispose { client.endConnection() }
    }

    LaunchedEffect(uiState.plans, billingClient) {
        val client = billingClient ?: return@LaunchedEffect
        val productIds = uiState.plans
            .filter { it.planCode != "FREE" && it.playProductId != null }
            .mapNotNull { it.playProductId }
        if (productIds.isEmpty()) return@LaunchedEffect

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productIds.map { id ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            })
            .build()

        client.queryProductDetailsAsync(params) { _, details ->
            productDetailsMap = details.associateBy { it.productId }
        }
    }

    Scaffold(
        containerColor = LangoaBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Upgrade to Pro", fontWeight = FontWeight.Bold, color = LangoaOnBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = LangoaOnBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LangoaBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LangoaBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(LangoaAmber.copy(alpha = 0.18f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(LangoaAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = LangoaAmber, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Unlock the full curriculum",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = LangoaOnBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your first 10 lessons are free. Go Pro to access everything.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LangoaOnBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (uiState.currentStatus.isPro) {
                // Already Pro
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = LangoaGreen.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = LangoaGreenLight, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Langova Pro ✓", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = LangoaGreenLight)
                            uiState.currentStatus.expiresAt?.let { iso ->
                                val formatted = try {
                                    val inst = java.time.Instant.parse(iso)
                                    val fmt = java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.ENGLISH)
                                    "Active until: ${fmt.format(inst.atZone(java.time.ZoneId.systemDefault()))}"
                                } catch (e: Exception) { "Pro Active" }
                                Text(formatted, style = MaterialTheme.typography.bodySmall, color = LangoaOnBackground.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // Feature bullets
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "WHAT YOU GET",
                    style = MaterialTheme.typography.labelLarge,
                    color = LangoaOnBackground.copy(alpha = 0.45f),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                listOf(
                    "Full curriculum access — all lessons unlocked",
                    "All exercise types — translation, fill-in, speaking",
                    "Unlimited daily practice",
                    "Priority content updates"
                ).forEach { feature ->
                    Row(
                        modifier = Modifier.padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(LangoaAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", color = LangoaAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(feature, style = MaterialTheme.typography.bodyMedium, color = LangoaOnBackground)
                    }
                }
            }

            // Plan cards
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LangoaAmber)
                }
            } else {
                val paidPlans = uiState.plans.filter { it.planCode != "FREE" }
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Text(
                        text = "CHOOSE A PLAN",
                        style = MaterialTheme.typography.labelLarge,
                        color = LangoaOnBackground.copy(alpha = 0.45f),
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    paidPlans.forEach { plan ->
                        PlanCard(
                            plan = plan,
                            isLoading = uiState.isPurchasing,
                            onSubscribe = {
                                if (!uiState.isPurchasing) {
                                    selectedPlanCode = plan.planCode
                                    val client = billingClient
                                    val productId = plan.playProductId
                                    if (client != null && productId != null) {
                                        val details = productDetailsMap[productId]
                                        if (details != null) {
                                            val offerToken = details.subscriptionOfferDetails
                                                ?.firstOrNull()?.offerToken ?: ""
                                            val productDetailsParams = BillingFlowParams.ProductDetailsParams
                                                .newBuilder()
                                                .setProductDetails(details)
                                                .setOfferToken(offerToken)
                                                .build()
                                            val flowParams = BillingFlowParams.newBuilder()
                                                .setProductDetailsParamsList(listOf(productDetailsParams))
                                                .build()
                                            client.launchBillingFlow(context as Activity, flowParams)
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Play Billing not available — try again later")
                                            }
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Connecting to Play Store...")
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { viewModel.restorePurchase() },
                enabled = !uiState.isRestoring,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isRestoring) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = LangoaOnBackground.copy(alpha = 0.5f))
                } else {
                    Text("Restore Purchases", color = LangoaOnBackground.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlanDto,
    isLoading: Boolean,
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAnnual = plan.planCode.contains("ANNUAL", ignoreCase = true)
    val priceRupees = plan.pricePaise / 100.0
    val priceLabel = "₹${priceRupees.toInt()} / ${if (plan.intervalDays >= 365) "year" else "month"}"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isAnnual) Modifier.border(2.dp, LangoaAmber.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = LangoaSurface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = plan.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = LangoaOnBackground
                    )
                    Text(
                        text = priceLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = LangoaAmberLight
                    )
                }
                if (isAnnual) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LangoaAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("BEST VALUE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = LangoaAmber)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSubscribe,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LangoaAmber),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Subscribe", fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
        }
    }
}
