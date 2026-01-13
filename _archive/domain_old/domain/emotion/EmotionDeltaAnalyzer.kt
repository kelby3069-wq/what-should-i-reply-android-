package com.replysense.app.domain.emotion

object EmotionDeltaAnalyzer {

    fun describeChange(
        previous: EmotionResult?,
        current: EmotionResult
    ): List<String> {
        if (previous == null) return emptyList()

        val changes = mutableListOf<String>()

        if (previous.primaryEmotion != current.primaryEmotion) {
            changes += "Emotional tone shifted from ${previous.primaryEmotion.name.lowercase()} to ${current.primaryEmotion.name.lowercase()}."
        }

        if (current.confidence < previous.confidence) {
            changes += "Signals are less clear than before."
        }

        if (current.flags.size > previous.flags.size) {
            changes += "New boundary or behavior concerns appeared."
        }

        if (previous.attractionSignal != current.attractionSignal) {
            changes += "The interaction tone changed (e.g., playful vs serious)."
        }

        return changes
    }
}
