package com.replysense.app.domain.intelligence.validation

import com.replysense.app.domain.intelligence.explainability.TraceSignal

/**
 * Detects conflicts and ambiguity from observed trace signals.
 * Deterministic, rule-based.
 */
object AmbiguityDetector {

    data class Result(
        val conflicts: List<ConflictSignal>,
        val ambiguities: List<AmbiguitySignal>
    )

    fun detect(signals: List<TraceSignal>): Result {
        val conflicts = mutableListOf<ConflictSignal>()
        val ambiguities = mutableListOf<AmbiguitySignal>()

        val byName = signals.groupBy { it.name }

        // Apology language + blame language
        if (byName.containsKey("apology_language") && byName.containsKey("blame_language")) {
            conflicts += ConflictSignal(
                type = ConflictType.APOLOGY_WITH_BLAME,
                description = "Apology language present alongside blame statements",
                messageIndices = collectIndices(
                    byName["apology_language"],
                    byName["blame_language"]
                )
            )
        }

        // Repair attempt followed by escalation
        if (byName.containsKey("repair_attempt") && byName.containsKey("escalation_keyword")) {
            conflicts += ConflictSignal(
                type = ConflictType.REPAIR_WITH_ESCALATION,
                description = "Repair attempt followed by renewed escalation",
                messageIndices = collectIndices(
                    byName["repair_attempt"],
                    byName["escalation_keyword"]
                )
            )
        }

        // Warm affect + withdrawal behavior
        if (byName.containsKey("warm_affect") && byName.containsKey("withdrawal_behavior")) {
            conflicts += ConflictSignal(
                type = ConflictType.WARMTH_WITH_WITHDRAWAL,
                description = "Warm emotional language paired with withdrawal behavior",
                messageIndices = collectIndices(
                    byName["warm_affect"],
                    byName["withdrawal_behavior"]
                )
            )
        }

        // Ambiguity: too few strong signals
        if (signals.size < 3) {
            ambiguities += AmbiguitySignal(
                description = "Insufficient signals to draw high-confidence conclusions",
                messageIndices = signals.mapNotNull { it.messageIndex }
            )
        }

        // Ambiguity: mixed polarity without dominance
        val polaritySignals = signals.filter {
            it.name == "positive_sentiment" || it.name == "negative_sentiment"
        }

        if (polaritySignals.size >= 2) {
            ambiguities += AmbiguitySignal(
                description = "Mixed sentiment signals without clear dominance",
                messageIndices = polaritySignals.mapNotNull { it.messageIndex }
            )
        }

        return Result(
            conflicts = conflicts,
            ambiguities = ambiguities
        )
    }

    private fun collectIndices(vararg lists: List<TraceSignal>?): List<Int> {
        return lists
            .filterNotNull()
            .flatten()
            .mapNotNull { it.messageIndex }
            .distinct()
            .sorted()
    }
}
