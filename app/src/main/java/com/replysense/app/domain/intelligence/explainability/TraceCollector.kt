package com.replysense.app.domain.intelligence.explainability

/**
 * Collects explainability traces during Phase C inference.
 * Safe to ignore entirely when Phase C is disabled.
 */
class TraceCollector {

    private val traces = mutableListOf<ExplainabilityTrace>()

    fun addTrace(
        conclusion: String,
        reasons: List<String>,
        signals: List<TraceSignal> = emptyList(),
        confidenceAdjustments: List<String> = emptyList()
    ) {
        traces += ExplainabilityTrace(
            conclusion = conclusion,
            reasons = reasons,
            signals = signals,
            confidenceAdjustments = confidenceAdjustments
        )
    }

    fun snapshot(): List<ExplainabilityTrace> = traces.toList()

    fun clear() {
        traces.clear()
    }
}
