package com.replysense.app.domain.intelligence.inference

import com.replysense.app.domain.intelligence.features.PhaseCFeatures
import com.replysense.app.domain.intelligence.keymoments.KeyMomentExtractor
import com.replysense.app.model.AnalysisResult

class PhaseCInferenceEngine(
    private val deterministicEngine: DeterministicInferenceEngine,
    private val keyMomentExtractor: KeyMomentExtractor
) {

    fun infer(
        lines: List<String>,
        features: PhaseCFeatures
    ): AnalysisResult {
        val base = deterministicEngine.infer(features)
        val moments = keyMomentExtractor.extract(lines)

        return base.copy(
            keyMoments = moments
        )
    }
}
