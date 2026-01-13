package com.replysense.app.domain.intelligence.validation

/**
 * Represents ambiguity where signals are weak or unclear.
 * INTERNAL ONLY.
 */
data class AmbiguitySignal(
    val description: String,
    val messageIndices: List<Int> = emptyList()
)
