package com.replysense.app.domain

object IntentMismatchDetector {

    fun shouldShowWarning(
        userIntent: ConversationIntent,
        otherIntent: ConversationIntent,
        trajectory: ConversationTrajectory,
        recoverability: Recoverability
    ): Boolean {

        if (userIntent == ConversationIntent.UNKNOWN ||
            otherIntent == ConversationIntent.UNKNOWN
        ) return false

        var signals = 0

        if (userIntent != otherIntent) signals++
        if (trajectory != ConversationTrajectory.BUILDING) signals++
        if (recoverability != Recoverability.LIKELY) signals++

        return signals >= 2
    }
}
