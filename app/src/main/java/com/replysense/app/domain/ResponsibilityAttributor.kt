package com.replysense.app.domain

object ResponsibilityAttributor {

    fun attribute(
        signals: AnalysisSignals,
        redFlags: List<RedFlag>
    ): ResponsibilityAttribution {

        val userDidWell = mutableListOf<String>()
        val userDidNotCause = mutableListOf<String>()
        val otherPartyActions = mutableListOf<String>()

        // What the user did well
        userDidWell += "Sought understanding rather than reacting impulsively."
        if (signals.emotionalLoad != EmotionalLoad.HIGH) {
            userDidWell += "Kept emotional intensity relatively contained."
        }

        // What the user did NOT cause
        if (signals.momentum == Momentum.DECLINING) {
            userDidNotCause += "Another person's reduced responsiveness."
        }
        if (redFlags.any { it.type == RedFlagType.GHOSTING }) {
            userDidNotCause += "Sustained disengagement from the other party."
        }

        // Other party actions
        if (signals.momentum == Momentum.DECLINING) {
            otherPartyActions += "Reduced message length or response frequency."
        }
        if (redFlags.any { it.type == RedFlagType.HOT_COLD }) {
            otherPartyActions += "Inconsistent engagement patterns."
        }

        return ResponsibilityAttribution(
            userDidWell = userDidWell.distinct(),
            userDidNotCause = userDidNotCause.distinct(),
            otherPartyActions = otherPartyActions.distinct()
        )
    }
}
