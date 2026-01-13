package com.replysense.app.viewmodel.ui

import androidx.lifecycle.ViewModel
import com.replysense.app.model.AnalysisResult

/**
 * Phase E — UI-only ViewModel
 * No logic, no inference, no intelligence decisions.
 */
class AnalysisOverviewViewModel(
    analysisResult: AnalysisResult,
    confidenceBand: ConfidenceBand
) : ViewModel() {

    val overallRead: String = analysisResult.overallRead

    val confidenceLabel: String = when (confidenceBand) {
        ConfidenceBand.CLEAR -> "Clear signal"
        ConfidenceBand.REASONABLY_CLEAR -> "Reasonably clear"
        ConfidenceBand.MIXED -> "Mixed signals present"
    }
}

/**
 * UI-safe confidence mapping.
 * Numeric scores are NEVER exposed to UI.
 */
enum class ConfidenceBand {
    CLEAR,
    REASONABLY_CLEAR,
    MIXED
}
