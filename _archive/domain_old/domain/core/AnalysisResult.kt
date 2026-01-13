package com.replysense.app.domain.core

/**
 * Single analysis output contract.
 * All analyzers produce this.
 * All UI consumes this.
 */
data class AnalysisResult(
    val primaryEmotion: Emotion,
    val severity: EmotionSeverity,
    val attractionSignal: AttractionSignal,
    val flags: Set<RedFlag>,
    val confidenceScore: Int // 0–100
)
