package com.replysense.app.domain.paywall

import kotlin.random.Random

object PaywallVariantManager {

    enum class Variant { VALUE, CALM }

    private val variant: Variant by lazy {
        if (Random.nextBoolean()) Variant.VALUE else Variant.CALM
    }

    fun current(): Variant = variant
}
