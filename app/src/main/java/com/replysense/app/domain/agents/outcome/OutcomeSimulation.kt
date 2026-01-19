package com.replysense.app.domain.agents.outcome

/**
 * OutcomeSimulation
 *
 * Describes a likely conversational trajectory IF the user replies now.
 * This is reflective, not prescriptive.
 */
data class OutcomeSimulation(
    val shortTermOutcome: String,
    val longTermRisk: Float
)
