package com.langoa.app.domain.model

data class SubscriptionStatus(
    val planCode: String = "FREE",
    val isPro: Boolean = false,
    val expiresAt: String? = null
) {
    companion object {
        val FREE = SubscriptionStatus()
    }
}
