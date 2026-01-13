package com.replysense.app.domain.intelligence.explainability

/**
 * Atomic signal observed during inference.
 * Used to explain WHY a rule fired.
 */
data class TraceSignal(
    val name: String,
    val value: String,
    val messageIndex: Int? = null
)
