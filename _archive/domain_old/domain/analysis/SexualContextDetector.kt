package com.replysense.app.domain.analysis

class SexualContextDetector {

    fun detect(messages: List<String>): SexualContext {
        if (messages.isEmpty()) return SexualContext.NONE

        val sexualSignals = messages.count { containsSexualCue(it) }
        val pressureSignals = messages.count { containsPressureCue(it) }

        return when {
            pressureSignals >= 2 ->
                SexualContext.PRESSURE_OR_COERCION

            sexualSignals >= 2 && messages.size >= 4 ->
                SexualContext.MUTUAL_SEXUAL

            sexualSignals == 1 ->
                SexualContext.FLIRTATIOUS

            else ->
                SexualContext.NONE
        }
    }

    private fun containsSexualCue(text: String): Boolean {
        val lowered = text.lowercase()
        return listOf(
            "😏", "😘", "🥵", "😉",
            "miss your body", "turn me on", "come over", "in bed"
        ).any { lowered.contains(it) }
    }

    private fun containsPressureCue(text: String): Boolean {
        val lowered = text.lowercase()
        return listOf(
            "come on", "just say yes", "why won't you",
            "after everything", "stop playing"
        ).any { lowered.contains(it) }
    }
}
