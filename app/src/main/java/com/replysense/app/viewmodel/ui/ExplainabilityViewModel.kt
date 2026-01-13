package com.replysense.app.viewmodel.ui

import androidx.lifecycle.ViewModel

/**
 * Phase E — UI-only ViewModel
 * Holds already-translated, human-readable explanations.
 * NO logic, NO inference, NO trace exposure.
 */
class ExplainabilityViewModel(
    sections: List<ExplainabilitySection>
) : ViewModel() {

    val explainabilitySections: List<ExplainabilitySection> = sections
}

/**
 * UI-safe explainability container.
 * Content is pre-translated from internal traces elsewhere.
 */
data class ExplainabilitySection(
    val title: String,
    val summary: String,
    val details: String? = null
)
