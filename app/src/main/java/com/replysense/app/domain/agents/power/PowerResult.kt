package com.replysense.domain.agents.power

/**
 * PowerResult
 *
 * Represents inferred relational leverage between
 * the OTHER PARTY and the user.
 *
 * Values are normalized to 0.0–1.0.
 */
data class PowerResult(
    val userLeverage: Float,
    val otherPartyLeverage: Float,
    val dynamicType: String
)
