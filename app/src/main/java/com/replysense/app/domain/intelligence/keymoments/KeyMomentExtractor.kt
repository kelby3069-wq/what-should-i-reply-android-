package com.replysense.app.domain.intelligence.keymoments

import com.replysense.app.model.KeyMoment

/**
 * Phase C.3 — deterministic key-moment extraction.
 * No inference, no models beyond KeyMoment.
 */
class KeyMomentExtractor {

    fun extract(lines: List<String>): List<KeyMoment> {
        val moments = mutableListOf<KeyMoment>()

        lines.forEachIndexed { index, line ->
            val lower = line.lowercase()

            when {
                containsRupture(lower) ->
                    moments.add(
                        KeyMoment(
                            messageIndex = index,
                            excerpt = line,
                            explanation = "Rupture signaled through withdrawal or blame language."
                        )
                    )

                containsEscalation(line) ->
                    moments.add(
                        KeyMoment(
                            messageIndex = index,
                            excerpt = line,
                            explanation = "Escalation marked by emotional punctuation or capitalization."
                        )
                    )

                containsRepair(lower) ->
                    moments.add(
                        KeyMoment(
                            messageIndex = index,
                            excerpt = line,
                            explanation = "Repair attempt introduced to stabilize the interaction."
                        )
                    )
            }
        }

        return moments
    }

    private fun containsRupture(text: String): Boolean =
        text.contains("i'm done") ||
                text.contains("whatever") ||
                text.contains("you always") ||
                text.contains("you never")

    private fun containsEscalation(text: String): Boolean =
        text.contains("!!") || text.contains("?!") ||
                text.count(Char::isUpperCase) > text.length * 0.3

    private fun containsRepair(text: String): Boolean =
        text.contains("can we talk") ||
                text.contains("let's fix") ||
                text.contains("i want to understand")
}
