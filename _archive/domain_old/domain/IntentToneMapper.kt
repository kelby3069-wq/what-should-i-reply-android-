package com.replysense.app.domain

object IntentToneMapper {

    fun recommendedTones(
        userIntent: ConversationIntent,
        otherIntent: ConversationIntent,
        trajectory: ConversationTrajectory,
        recoverability: Recoverability
    ): Set<ReplyTone> {

        // Low recoverability → avoid escalation
        if (recoverability == Recoverability.UNLIKELY) {
            return setOf(ReplyTone.FIRM)
        }

        // Clear mismatch
        if (userIntent != otherIntent) {
            return when (trajectory) {
                ConversationTrajectory.BUILDING ->
                    setOf(ReplyTone.NEUTRAL, ReplyTone.DIRECT)

                ConversationTrajectory.FLATTENING,
                ConversationTrajectory.FADING ->
                    setOf(ReplyTone.NEUTRAL, ReplyTone.FIRM)
            }
        }

        // Intent aligned
        return when (trajectory) {
            ConversationTrajectory.BUILDING ->
                setOf(ReplyTone.WARM, ReplyTone.PLAYFUL)

            ConversationTrajectory.FLATTENING ->
                setOf(ReplyTone.NEUTRAL, ReplyTone.WARM)

            ConversationTrajectory.FADING ->
                setOf(ReplyTone.NEUTRAL)
        }
    }
}
