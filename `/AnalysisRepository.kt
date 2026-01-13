package com.replysense.app.data

import com.replysense.app.model.ReplyOptionUi
import com.replysense.app.ui.reply.ReplyTone

class AnalysisRepository {

    fun generateReplyOptions(
        intent: String,
        attraction: AttractionSignal
    ): List<ReplyOptionUi> {

        val base = when (intent) {
            "MAINTAIN_MOMENTUM" -> "That makes sense — want to keep it light?"
            "DISENGAGE_AND_PROTECT" -> "I think I need to take a step back."
            else -> "Got it."
        }

        return listOf(
            ReplyOptionUi(
                tone = ReplyTone.NEUTRAL,
                text = base,
                highlighted = true
            ),
            ReplyOptionUi(
                tone = ReplyTone.WARM,
                text = "$base 😊"
            ),
            ReplyOptionUi(
                tone = ReplyTone.FIRM,
                text = base.replace("?", ".")
            )
        )
    }
}
