package com.langoa.app.ui.screens.shop

import android.app.Activity
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.langoa.app.data.remote.model.CoinPackageDto
import com.langoa.app.ui.components.LoadingScreen
import com.langoa.app.ui.theme.LangoaAmber
import com.langoa.app.ui.theme.LangoaBackground
import com.langoa.app.ui.theme.LangoaGreen
import com.langoa.app.ui.theme.LangoaOnBackground
import com.langoa.app.ui.theme.LangoaSurface
import com.langoa.app.ui.theme.LangoaSurfaceVariant

@Composable
fun CoinShopScreen(
    languageCode: String,
    onBack: () -> Unit,
    viewModel: CoinShopViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var productDetailsMap by remember { mutableStateOf<Map<String, ProductDetails>>(emptyMap()) }

    val purchasesUpdatedListener = remember {
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    val productId = purchase.products.firstOrNull() ?: continue
                    val packageCode = uiState.packages
                        .firstOrNull { it.playProductId == productId }
                        ?.packageCode ?: continue
                    viewModel.onPlayPurchaseSuccess(
                        packageCode, purchase.purchaseToken, purchase.orderId ?: "", languageCode
                    )
                }
            } else if (billingResult.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
                viewModel.onPlayPurchaseFailure(billingResult.responseCode, billingResult.debugMessage)
            }
        }
    }

    val billingClient = remember {
        BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
    }

    DisposableEffect(Unit) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {}
            override fun onBillingServiceDisconnected() {}
        })
        onDispose { billingClient.endConnection() }
    }

    LaunchedEffect(Unit) {
        viewModel.load(languageCode)
    }

    LaunchedEffect(uiState.packages, billingClient) {
        val productIds = uiState.packages.mapNotNull { it.playProductId }
        if (productIds.isNotEmpty() && billingClient.isReady) {
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productIds.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                })
                .build()
            billingClient.queryProductDetailsAsync(params) { _, details ->
                productDetailsMap = details.associateBy { it.productId }
            }
        }
    }

    if (uiState.purchaseSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSuccess() },
            title = { Text("Coins Added!") },
            text = { Text("+${uiState.coinsAwarded} coins added to your wallet. New balance: ${uiState.currentCoins}") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSuccess() }) { Text("OK") }
            }
        )
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(uiState.error ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
            }
        )
    }

    Scaffold(containerColor = LangoaBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LangoaBackground)
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = LangoaOnBackground)
                }
                Text(
                    "Coin Shop",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LangoaOnBackground,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(LangoaSurfaceVariant, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Current: ${uiState.currentCoins} coins",
                        style = MaterialTheme.typography.labelMedium,
                        color = LangoaAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState.isLoading) {
                LoadingScreen()
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(uiState.packages) { pkg ->
                    CoinPackageCard(
                        pkg = pkg,
                        isPurchasing = uiState.isPurchasing,
                        onBuy = {
                            val productDetails = productDetailsMap[pkg.playProductId]
                            if (productDetails != null) {
                                val productDetailsParams = BillingFlowParams.ProductDetailsParams
                                    .newBuilder()
                                    .setProductDetails(productDetails)
                                    .build()
                                val flowParams = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(listOf(productDetailsParams))
                                    .build()
                                billingClient.launchBillingFlow(context as Activity, flowParams)
                            } else {
                                viewModel.onPlayPurchaseFailure(0, "Product not available in store")
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun CoinPackageCard(
    pkg: CoinPackageDto,
    isPurchasing: Boolean,
    onBuy: () -> Unit
) {
    val emoji = when {
        pkg.coinAmount >= 5000 -> "🏆"
        pkg.coinAmount >= 1000 -> "💰"
        else -> "👛"
    }
    val priceDisplay = "₹${pkg.pricePaise / 100}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LangoaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                Column {
                    Text(
                        pkg.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LangoaOnBackground
                    )
                    Text(
                        "${pkg.coinAmount} coins",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LangoaAmber,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (isPurchasing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = LangoaGreen,
                    strokeWidth = 3.dp
                )
            } else {
                Button(
                    onClick = onBuy,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LangoaGreen)
                ) {
                    Text(priceDisplay, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
