package com.replysense.app.domain

enum class RecoverabilityLevel {
    HIGH,
    MEDIUM,
    LOW
}

enum class DecisionGuidance {
    CONTINUE,
    PAUSE,
    RESET_TONE
}

data class RecoverabilityResult(
    val level: RecoverabilityLevel,
    val decision: DecisionGuidance,
    val rationale: String
)
