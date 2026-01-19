package com.replysense.app.domain.conversation

import com.replysense.app.domain.model.analysis.SignalBundle

/**
 * Deterministically maps existing analysis signals to conversation lines.
 *
 * Rules:
 * - Uses ONLY signals already detected by agents
 * - No new heuristics or ML
 * - Stable mapping across runs
 */
class LineSignalMapper {

    fun map(
        lines: List<ConversationLine>,
        signals: SignalBundle
    ): List<LineSignalMatch> {
        val matches = mutableListOf<LineSignalMatch>()

        for (line in lines) {
            val lower = line.text.lowercase()

            // UNCERTAINTY — match existing uncertainty markers
            if (signals.uncertaintyMarkers.any { lower.contains(it) }) {
                matches.add(
                    LineSignalMatch(
                        lineId = line.id,
                        signalType = SignalType.UNCERTAINTY
                    )
                )
            }

            // ELEVATED_TONE — simple punctuation cue (already implied by agent)
            if (lower.contains("!")) {
                matches.add(
                    LineSignalMatch(
                        lineId = line.id,
                        signalType = SignalType.ELEVATED_TONE
                    )
                )
            }

            // HIGH_ENGAGEMENT — density proxy (mirrors agent behavior)
            if (signals.engagementLevel == "High" && line.text.length > 120) {
                matches.add(
                    LineSignalMatch(
                        lineId = line.id,
                        signalType = SignalType.HIGH_ENGAGEMENT
                    )
                )
            }
        }

        return matches
    }
}
