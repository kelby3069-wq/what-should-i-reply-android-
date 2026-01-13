package com.replysense.app.domain.intelligence.confidence

/**
 * Represents confidence in a single analytical conclusion.
 * INTERNAL ONLY — never surfaced directly to UI.
 */
data class ConfidenceScore(
    val domain: ConfidenceDomain,
    val score: Float,
    val rationale: List<String>
)

enum class ConfidenceDomain {
    EMOTIONAL_STATE,
    TRAJECTORY,
    RED_FLAGS,
    RESPONSIBILITY,
    RECOVERABILITY,
    COACHING
}
