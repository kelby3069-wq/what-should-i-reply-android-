package com.replysense.app.domain

enum class SexualContextLevel {
    NONE,
    FLIRTATIOUS,
    EXPLICIT
}

enum class SexualGateDecision {
    ALLOW,
    RESTRICT,
    BLOCK
}

data class SexualContextResult(
    val level: SexualContextLevel,
    val decision: SexualGateDecision,
    val reason: String
)
