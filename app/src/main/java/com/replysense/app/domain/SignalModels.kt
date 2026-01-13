package com.replysense.app.domain

enum class Momentum {
    IMPROVING,
    STABLE,
    DECLINING
}

enum class EmotionalLoad {
    LOW,
    MEDIUM,
    HIGH
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class AnalysisSignals(
    val momentum: Momentum,
    val emotionalLoad: EmotionalLoad,
    val riskLevel: RiskLevel
)
