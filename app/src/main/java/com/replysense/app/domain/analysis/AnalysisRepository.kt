package com.replysense.app.domain.analysis

import com.replysense.app.domain.intelligence.PhaseCAnalysisEngine
import com.replysense.app.model.AnalysisResult

/**
 * Sole owner of fallback logic.
 */
class AnalysisRepository(
    private val phaseCEngine: PhaseCAnalysisEngine
) {

    fun analyze(lines: List<String>): AnalysisResult {
        return try {
            phaseCEngine.analyze(lines)
        } catch (e: Exception) {
            FakeAnalysisResultProvider.sample()
        }
    }
}
