package com.replysense.app.domain.emotion

/**
 * Detects attraction, flirtation, or explicit signals.
 *
 * IMPORTANT:
 * - Detection only (no generation)
 * - NEVER implies consent
 * - Used strictly for tone gating and safety
 */
object AttractionAnalyzer {

    fun detect(text: String): AttractionSignal {
        val lower = text.lowercase()

        return when {
            // Explicit or boundary-crossing content
            lower.contains("send nudes") ||
                    lower.contains("nudes") ||
                    lower.contains("nsfw") ||
                    lower.contains("hook up") ||
                    lower.contains("fuck") ||
                    lower.contains("sex") ->
                AttractionSignal.EXPLICIT_CONTENT

            // Clear sexual interest (non-graphic)
            lower.contains("i want you") ||
                    lower.contains("turn me on") ||
                    lower.contains("i'm into you") ->
                AttractionSignal.SEXUAL_INTEREST

            // Playful flirtation (emoji / tone based)
            lower.contains("😉") ||
                    lower.contains("😏") ||
                    lower.contains("😘") ||
                    lower.contains("tease") ->
                AttractionSignal.PLAYFUL_FLIRTATION

            else -> AttractionSignal.NONE
        }
    }
}
