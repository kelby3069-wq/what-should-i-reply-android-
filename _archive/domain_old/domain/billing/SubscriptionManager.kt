package com.replysense.app.domain.billing

object SubscriptionManager {

    enum class Tier {
        FREE,
        PRO
    }

    private var currentTier: Tier = Tier.FREE

    fun setTier(tier: Tier) {
        currentTier = tier
    }

    fun isPro(): Boolean = currentTier == Tier.PRO
}
