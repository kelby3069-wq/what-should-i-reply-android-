package com.replysense.app.domain.intelligence.inference

import com.replysense.app.domain.intelligence.features.PhaseCFeatures
import com.replysense.app.model.*

class DeterministicInferenceEngine {

    fun infer(features: PhaseCFeatures): AnalysisResult {
        val escalating = features.escalationSlope > 0.5f
        val highVolatility =
            features.exclamationDensity > 0.02f ||
                    features.capitalizationRate > 0.15f ||
                    features.emotionalPunctuationBursts > 1

        return AnalysisResult(
            overallRead = overallRead(escalating, highVolatility),
            emotionalState = emotionalState(highVolatility),
            trajectory = trajectory(escalating),
            keyMoments = emptyList(),
            redFlags = emptyList(),
            responsibilityCheck = responsibility(features),
            recoverability = recoverability(features),
            coaching = coaching(escalating, highVolatility)
        )
    }

    private fun overallRead(escalating: Boolean, volatile: Boolean): String =
        when {
            escalating && volatile ->
                "The conversation escalated due to emotional overload rather than intent."
            escalating ->
                "Tension increased gradually as emotional signals were missed."
            else ->
                "The conversation shows strain but has not fully escalated."
        }

    private fun emotionalState(volatile: Boolean): EmotionalStateResult =
        EmotionalStateResult(
            user = EmotionalRead(
                primary = if (volatile) "Overloaded" else "Concerned",
                secondary = emptyList(),
                intensity = if (volatile) "High" else "Moderate"
            ),
            other = EmotionalRead(
                primary = if (volatile) "Defensive" else "Guarded",
                secondary = emptyList(),
                intensity = if (volatile) "High" else "Moderate"
            )
        )

    private fun trajectory(escalating: Boolean): String =
        if (escalating) "Escalating" else "Unstable but contained"

    private fun responsibility(features: PhaseCFeatures): ResponsibilityCheck =
        ResponsibilityCheck(
            userDidWell = listOf(
                "You stayed engaged despite rising emotional tension."
            ),
            userDidNotCause = listOf(
                "You did not introduce blame or withdrawal language."
            ),
            otherPartyActions = listOf(
                "The other party escalated through emotional signaling and blame."
            )
        )

    private fun recoverability(features: PhaseCFeatures): String =
        if (features.repairAttemptPresent)
            "Recoverable if emotional validation occurs before problem-solving."
        else
            "Recoverability depends on de-escalation and tone reset."

    private fun coaching(escalating: Boolean, volatile: Boolean): CoachingResult =
        CoachingResult(
            summary =
                if (volatile)
                    "Emotional overload prevented reassurance from landing."
                else
                    "Misalignment occurred before emotional clarity was restored.",
            suggestedReply =
                if (escalating)
                    "Acknowledge feelings directly before addressing content."
                else
                    "Clarify intent gently and invite a calmer exchange.",
            boundaryGuidance =
                "Pause the conversation if emotional intensity spikes again."
        )
}
