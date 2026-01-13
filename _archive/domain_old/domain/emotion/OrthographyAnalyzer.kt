package com.replysense.app.domain.emotion

object OrthographyAnalyzer {

    fun adjust(
        text: String,
        context: ConversationContext
    ): Pair<Float, Float> {
        var intensityDelta = 0f
        var confidenceDelta = 0f

        if (text.contains("!!")) intensityDelta += 0.2f
        if (text.contains("?!") || text.contains("!?")) intensityDelta += 0.15f
        if (text.contains("...")) confidenceDelta -= 0.1f

        if (text == text.uppercase() && context != ConversationContext.YOUTH) {
            intensityDelta += 0.2f
        }

        if (text.trim().length <= 3) {
            confidenceDelta -= 0.2f
        }

        return Pair(intensityDelta, confidenceDelta)
    }
}
