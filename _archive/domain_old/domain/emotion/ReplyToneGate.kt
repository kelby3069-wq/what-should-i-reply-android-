package com.replysense.app.domain.emotion

import com.replysense.app.ui.reply.ReplyTone

object ReplyToneGate {

    fun allowedTones(severity: EmotionSeverity): Set<ReplyTone> {
        return when (severity) {
            EmotionSeverity.LOW -> setOf(
                ReplyTone.NEUTRAL,
                ReplyTone.WARM,
                ReplyTone.PLAYFUL,
                ReplyTone.DIRECT
            )

            EmotionSeverity.MEDIUM -> setOf(
                ReplyTone.NEUTRAL,
                ReplyTone.WARM,
                ReplyTone.DIRECT
            )

            EmotionSeverity.HIGH -> setOf(
                ReplyTone.NEUTRAL,
                ReplyTone.FIRM
            )
        }
    }
}
