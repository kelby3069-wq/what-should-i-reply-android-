package com.replysense.domain.agents.intent

/**
 * Canonical intent taxonomy.
 *
 * This enum MUST stay in sync with IntentAgent.
 * Do not add values without updating agent logic.
 */
enum class IntentType {
    INFORMATION_SEEKING,
    REASSURANCE_SEEKING,
    BOUNDARY_TESTING,
    CONTROL_ATTEMPT,
    AFFILIATION,
    DISENGAGEMENT,
    AMBIGUOUS
}

data class IntentResult(
    val primaryIntent: IntentType,
    val confidence: Float
)
