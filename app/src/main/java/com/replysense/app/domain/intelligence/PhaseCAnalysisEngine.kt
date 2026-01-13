package com.replysense.app.domain.intelligence

import com.replysense.app.domain.intelligence.features.PhaseCFeatureExtractor
import com.replysense.app.domain.intelligence.inference.PhaseCInferenceEngine
import com.replysense.app.domain.intelligence.parsing.SemanticParser
import com.replysense.app.model.AnalysisResult

/**
 * Orchestrates Phase C execution.
 * NO fallback. NO UI logic. NO null dependencies.
 */
class PhaseCAnalysisEngine(
    private val parser: SemanticParser,
    private val inferenceEngine: PhaseCInferenceEngine,
    private val featureExtractor: PhaseCFeatureExtractor
) {

    fun analyze(rawLines: List<String>): AnalysisResult {
        val parsedLines = parser.parse(rawLines)
        val features = featureExtractor.extract(parsedLines)
        return inferenceEngine.infer(parsedLines, features)
    }
}
