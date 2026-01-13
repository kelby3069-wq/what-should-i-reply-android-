package com.replysense.app.domain.emotion

enum class EmotionSeverity {
    CALM,
    UNCERTAIN,
    DISTRESSED,
    ABUSIVE
}

enum class AttractionSignal {
    NONE,
    INTEREST,
    SEXUAL_INTEREST
}

data class EmotionResult(
    val severity: EmotionSeverity,
    val confidence: Float,
    val attractionSignal: AttractionSignal
)
