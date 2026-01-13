package com.replysense.app.domain.analysis

import com.replysense.app.domain.intelligence.PhaseCAnalysisEngine
import com.replysense.app.model.AnalysisResult

/**
 * Legacy compatibility wrapper.
 * Does NOT contain logic.
 */
class AnalysisSelector(
    private val phaseCEngine: PhaseCAnalysisEngine
) {

    fun analyze(lines: List<String>): AnalysisResult {
        return phaseCEngine.analyze(lines)
    }
}
