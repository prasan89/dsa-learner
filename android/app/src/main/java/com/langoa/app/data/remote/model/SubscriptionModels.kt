package com.langoa.app.data.remote.model

data class SubscriptionStatusDto(
    val planCode: String,
    val isPro: Boolean,
    val currentPeriodStart: String?,
    val currentPeriodEnd: String?
)

data class SubscriptionPlanDto(
    val planCode: String,
    val displayName: String,
    val pricePaise: Int,
    val currency: String,
    val intervalDays: Int,
    val playProductId: String?,
    val features: List<String> = emptyList()
)

data class CreateOrderRequest(val planCode: String)

data class CreateOrderResponse(
    val orderId: String,
    val amountPaise: Int,
    val currency: String,
    val razorpayKeyId: String,
    val planCode: String
)

data class VerifyPlayRequest(
    val planCode: String,
    val purchaseToken: String,
    val orderId: String
)

data class EntitlementsResponse(
    val isPro: Boolean,
    val features: List<String> = emptyList()
)

