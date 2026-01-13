package com.replysense.app.domain.intelligence.debug

/**
 * Formats debug snapshots for export.
 * No Android dependencies — safe for unit tests.
 */
object DebugExportFormatter {

    fun toPrettyString(snapshot: PhaseCDebugSnapshot): String {
        val sb = StringBuilder()

        sb.appendLine("=== Phase C Debug Snapshot ===")
        sb.appendLine()

        sb.appendLine("---- Explainability ----")
        snapshot.explainabilityTraces.forEach { trace ->
            sb.appendLine("Conclusion: ${trace.conclusion}")
            trace.reasons.forEach { reason ->
                sb.appendLine("  - $reason")
            }
            trace.signals.forEach { signal ->
                sb.appendLine(
                    "    • Signal: ${signal.name} = ${signal.value}" +
                            (signal.messageIndex?.let { " (msg $it)" } ?: "")
                )
            }
            trace.confidenceAdjustments.forEach {
                sb.appendLine("    • Confidence note: $it")
            }
            sb.appendLine()
        }

        sb.appendLine("---- Confidence Scores ----")
        snapshot.confidenceScores.forEach { score ->
            sb.appendLine("${score.domain}: ${"%.2f".format(score.score)}")
            score.rationale.forEach {
                sb.appendLine("  - $it")
            }
        }
        sb.appendLine()

        sb.appendLine("---- Conflicts ----")
        if (snapshot.conflicts.isEmpty()) {
            sb.appendLine("None")
        } else {
            snapshot.conflicts.forEach { conflict ->
                sb.appendLine("${conflict.type}: ${conflict.description}")
                if (conflict.messageIndices.isNotEmpty()) {
                    sb.appendLine("  Messages: ${conflict.messageIndices.joinToString()}")
                }
            }
        }
        sb.appendLine()

        sb.appendLine("---- Ambiguities ----")
        if (snapshot.ambiguities.isEmpty()) {
            sb.appendLine("None")
        } else {
            snapshot.ambiguities.forEach { ambiguity ->
                sb.appendLine(ambiguity.description)
                if (ambiguity.messageIndices.isNotEmpty()) {
                    sb.appendLine("  Messages: ${ambiguity.messageIndices.joinToString()}")
                }
            }
        }
        sb.appendLine()

        sb.appendLine("---- Coaching Profile ----")
        sb.appendLine("Tone: ${snapshot.coachingProfile.tone}")
        sb.appendLine("Style: ${snapshot.coachingProfile.style}")
        snapshot.coachingProfile.rationale.forEach {
            sb.appendLine("  - $it")
        }

        return sb.toString()
    }
}
