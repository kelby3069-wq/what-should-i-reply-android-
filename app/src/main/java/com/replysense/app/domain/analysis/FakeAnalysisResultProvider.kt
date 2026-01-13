package com.replysense.app.domain.analysis

import com.replysense.app.model.*

object FakeAnalysisResultProvider {

    fun sample(): AnalysisResult {
        return AnalysisResult(
            overallRead = """
                This conversation shows emotional care mixed with hesitation.
                Signals are present but not landing clearly.
            """.trimIndent(),

            emotionalState = EmotionalStateResult(
                user = EmotionalRead(
                    primary = "Concern",
                    secondary = listOf("Care", "Hope"),
                    intensity = "Moderate"
                ),
                other = EmotionalRead(
                    primary = "Caution",
                    secondary = listOf("Emotional fatigue"),
                    intensity = "Moderate"
                )
            ),

            trajectory = "Unstable but recoverable",

            keyMoments = emptyList(),
            redFlags = emptyList(),

            responsibilityCheck = ResponsibilityCheck(
                userDidWell = listOf(
                    "Expressed care",
                    "Attempted understanding"
                ),
                userDidNotCause = listOf(
                    "Intentional emotional harm"
                ),
                otherPartyActions = listOf(
                    "Defensive responses",
                    "Emotional withdrawal"
                )
            ),

            recoverability = "Likely with clearer reassurance",

            coaching = null
        )
    }
}
