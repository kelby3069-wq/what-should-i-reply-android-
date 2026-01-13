package com.replysense.app.domain.intelligence.explainability

/**
 * Immutable explanation of how a single conclusion was reached.
 * INTERNAL USE ONLY — never surfaced directly to UI.
 */
data class ExplainabilityTrace(
    val conclusion: String,
    val reasons: List<String>,
    val signals: List<TraceSignal> = emptyList(),
    val confidenceAdjustments: List<String> = emptyList()
)
