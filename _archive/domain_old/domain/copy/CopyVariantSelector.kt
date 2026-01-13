package com.replysense.app.domain.copy

import kotlin.random.Random

object CopyVariantSelector {

    enum class Variant { A, B }

    fun pick(): Variant =
        if (Random.nextBoolean()) Variant.A else Variant.B
}
