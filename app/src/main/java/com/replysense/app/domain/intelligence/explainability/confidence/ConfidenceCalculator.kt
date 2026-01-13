package com.replysense.app.domain.intelligence.confidence

import kotlin.math.max
import kotlin.math.min

/**
 * Deterministic confidence calculator.
 */
object ConfidenceCalculator {

    fun calculate(
        domain: ConfidenceDomain,
        inputs: ConfidenceInputs
    ): ConfidenceScore {

        val rationale = mutableListOf<String>()

        if (inputs.signalCount == 0) {
            return ConfidenceScore(
                domain = domain,
                score = 0.25f,
                rationale = listOf("No strong signals detected")
            )
        }

        var score = 0.5f

        // Agreement increases confidence
        val agreementRatio =
            inputs.agreeingSignals.toFloat() / inputs.signalCount.toFloat()

        score += agreementRatio * 0.4f
        rationale += "Agreement ratio: ${"%.2f".format(agreementRatio)}"

        // Contradictions reduce confidence
        if (inputs.contradictingSignals > 0) {
            val penalty = inputs.contradictingSignals * 0.15f
            score -= penalty
            rationale += "Contradicting signals detected (-${"%.2f".format(penalty)})"
        }

        // Ambiguity soft penalty
        if (inputs.ambiguousSignals > 0) {
            val ambiguityPenalty = inputs.ambiguousSignals * 0.05f
            score -= ambiguityPenalty
            rationale += "Ambiguous signals detected (-${"%.2f".format(ambiguityPenalty)})"
        }

        score = min(1.0f, max(0.1f, score))

        return ConfidenceScore(
            domain = domain,
            score = score,
            rationale = rationale
        )
    }
}
