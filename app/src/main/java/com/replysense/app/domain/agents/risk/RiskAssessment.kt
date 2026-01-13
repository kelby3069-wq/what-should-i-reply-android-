package com.replysense.domain.agents.risk

/**
 * RiskAssessment
 *
 * Quantifies conversational risk.
 * Values normalized to 0.0–1.0.
 */
data class RiskAssessment(
    val escalationProbability: Float,
    val misinterpretationProbability: Float,
    val confidence: Float
)
