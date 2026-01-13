package com.replysense.app.domain.intelligence.confidence

/**
 * Normalized inputs used to calculate confidence.
 */
data class ConfidenceInputs(
    val signalCount: Int,
    val agreeingSignals: Int,
    val contradictingSignals: Int,
    val ambiguousSignals: Int
)
