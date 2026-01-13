package com.replysense.domain.agents.emotion

/**
 * EmotionalResult
 *
 * Represents inferred emotional tone and volatility
 * of the OTHER PARTY (not the user).
 *
 * Values are normalized to 0.0–1.0.
 */
data class EmotionalResult(
    val emotions: Map<String, Float>,
    val emotionalIntensity: Float
)
