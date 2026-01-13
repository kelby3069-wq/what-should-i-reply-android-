package com.replysense.app.domain.intelligence.coaching

import com.replysense.app.domain.intelligence.confidence.ConfidenceDomain
import com.replysense.app.domain.intelligence.confidence.ConfidenceScore
import com.replysense.app.domain.intelligence.validation.ConflictSignal
import com.replysense.app.domain.intelligence.validation.AmbiguitySignal

/**
 * Determines HOW coaching should be delivered,
 * never WHAT the conclusions are.
 */
object CoachingPersonalizer {

    fun personalize(
        confidenceScores: Map<ConfidenceDomain, ConfidenceScore>,
        conflicts: List<ConflictSignal>,
        ambiguities: List<AmbiguitySignal>
    ): CoachingProfile {

        val rationale = mutableListOf<String>()

        val trajectoryConfidence =
            confidenceScores[ConfidenceDomain.TRAJECTORY]?.score ?: 0.5f

        val responsibilityConfidence =
            confidenceScores[ConfidenceDomain.RESPONSIBILITY]?.score ?: 0.5f

        val hasConflicts = conflicts.isNotEmpty()
        val hasAmbiguity = ambiguities.isNotEmpty()

        // ---- Tone Selection ----
        val tone = when {
            hasAmbiguity -> {
                rationale += "Ambiguous signals detected → gentler tone"
                CoachingTone.GENTLE
            }
            trajectoryConfidence < 0.45f -> {
                rationale += "Low trajectory confidence → gentler tone"
                CoachingTone.GENTLE
            }
            hasConflicts -> {
                rationale += "Conflicting signals detected → balanced tone"
                CoachingTone.BALANCED
            }
            else -> {
                rationale += "Clear signals → direct tone"
                CoachingTone.DIRECT
            }
        }

        // ---- Style Selection ----
        val style = when {
            hasConflicts && responsibilityConfidence < 0.5f -> {
                rationale += "Responsibility unclear → validation-heavy coaching"
                CoachingStyle.VALIDATION_HEAVY
            }
            hasConflicts && responsibilityConfidence >= 0.5f -> {
                rationale += "Clear responsibility despite conflict → boundary-forward coaching"
                CoachingStyle.BOUNDARY_FORWARD
            }
            trajectoryConfidence >= 0.65f -> {
                rationale += "Clear trajectory → action-oriented coaching"
                CoachingStyle.ACTION_ORIENTED
            }
            else -> {
                rationale += "Unclear direction → reconciliation-forward coaching"
                CoachingStyle.RECONCILIATION_FORWARD
            }
        }

        return CoachingProfile(
            tone = tone,
            style = style,
            rationale = rationale
        )
    }
}
