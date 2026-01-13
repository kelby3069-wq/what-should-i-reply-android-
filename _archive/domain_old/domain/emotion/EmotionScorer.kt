package com.replysense.app.domain.emotion

class EmotionScorer {

    fun score(
        messages: List<String>
    ): AttractionSignal {

        val joined = messages.joinToString(" ").lowercase()

        return when {
            joined.contains("sex") ||
                    joined.contains("hook up") ->
                AttractionSignal.SEXUAL_INTEREST

            joined.contains("haha") ||
                    joined.contains("lol") ->
                AttractionSignal.PLAYFUL_FLIRTATION

            else -> AttractionSignal.NONE
        }
    }
}
