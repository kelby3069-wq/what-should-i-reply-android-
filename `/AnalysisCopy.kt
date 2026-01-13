package com.replysense.app._disabled_phase_c

object AnalysisCopy {

    fun overview(result: AnalysisResult): String {
        return when (result.emotionalSummary.trajectory) {
            com.replysense.app.domain.analysis.ConversationTrajectory.IMPROVING ->
                "The conversation is stabilizing and showing signs of improvement."

            com.replysense.app.domain.analysis.ConversationTrajectory.STABLE ->
                "The conversation is steady, with no major shifts in tone."

            com.replysense.app.domain.analysis.ConversationTrajectory.ESCALATING ->
                "Tension is increasing, and the dynamic has become more strained."
        }
    }

    fun contribution(result: AnalysisResult): String {
        return when (result.userContribution) {
            UserContribution.NONE ->
                "Nothing you said directly caused the shift."

            UserContribution.MINOR ->
                "A small change in your wording or timing may have contributed."

            UserContribution.SIGNIFICANT ->
                "One of your messages played a meaningful role in how the tone shifted."
        }
    }

    fun recoverability(result: AnalysisResult): String {
        return when (result.recoverability) {
            Recoverability.HIGH ->
                "This conversation is likely recoverable with calm, aligned communication."

            Recoverability.MEDIUM ->
                "Repair is possible, but timing and tone matter."

            Recoverability.LOW ->
                "At this point, recovery is unlikely without significant change or distance."
        }
    }

    fun paywallTeaser(): String =
        "The deeper analysis explains why it shifted, what you did or didn’t cause, and whether it’s actually recoverable — with specific message-level evidence."
}