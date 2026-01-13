package com.replysense.app

import com.replysense.app.domain.intelligence.PhaseCAnalysisEngine
import com.replysense.app.domain.intelligence.features.PhaseCFeatureExtractor
import com.replysense.app.domain.intelligence.inference.DeterministicInferenceEngine
import com.replysense.app.domain.intelligence.inference.PhaseCInferenceEngine
import com.replysense.app.domain.intelligence.keymoments.KeyMomentExtractor
import com.replysense.app.domain.intelligence.parsing.SimpleSemanticParser

object AppConfig {

    /**
     * Canonical Phase C engine wiring.
     * Matches actual constructor signatures.
     */
    val phaseCAnalysisEngine: PhaseCAnalysisEngine by lazy {
        PhaseCAnalysisEngine(
            featureExtractor = PhaseCFeatureExtractor(),
            parser = SimpleSemanticParser(),
            inferenceEngine = PhaseCInferenceEngine(
                deterministicEngine = DeterministicInferenceEngine(),
                keyMomentExtractor = KeyMomentExtractor()
            )
        )
    }
}
