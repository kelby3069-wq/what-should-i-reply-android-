package com.replysense.app.domain.emotion

object EmotionExplainer {

    fun explain(result: EmotionResult): List<String> {
        val reasons = mutableListOf<String>()

        when (result.primaryEmotion) {
            Emotion.ANGER ->
                reasons += "Wording and tone suggest frustration or anger."

            Emotion.ANXIETY ->
                reasons += "Language shows uncertainty or worry."

            Emotion.SADNESS ->
                reasons += "Message carries emotional heaviness or hurt."

            Emotion.AFFECTION ->
                reasons += "There are signs of warmth or emotional openness."

            Emotion.GRATITUDE ->
                reasons += "Message expresses appreciation."

            Emotion.NEUTRAL ->
                reasons += "The message is emotionally neutral."

            else -> {
                reasons += "The emotional tone is unclear or mixed."
            }
        }

        if (result.flags.contains(RedFlag.ONE_SIDED)) {
            reasons += "Conversation effort appears one-sided."
        }

        if (result.flags.contains(RedFlag.CONTROL)) {
            reasons += "Some phrasing suggests controlling behavior."
        }

        if (result.flags.contains(RedFlag.BOUNDARY_VIOLATION)) {
            reasons += "A personal boundary may have been crossed."
        }

        when (result.attractionSignal) {
            AttractionSignal.SEXUAL_INTEREST ->
                reasons += "This reads as sexual interest, not consent."

            AttractionSignal.EXPLICIT_CONTENT ->
                reasons += "Explicit content limits safe reply options."

            AttractionSignal.PLAYFUL_FLIRTATION ->
                reasons += "Playful or flirtatious tone detected."

            else -> {}
        }

        if (result.confidence < 0.4f) {
            reasons += "Signals are mixed, so conclusions are tentative."
        }

        return reasons
    }
}
