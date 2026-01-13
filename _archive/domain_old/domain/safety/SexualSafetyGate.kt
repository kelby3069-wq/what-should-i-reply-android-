package com.replysense.app.domain.safety

import com.replysense.app.domain.analysis.SexualContext
import com.replysense.app.domain.reply.ReplyTone

class SexualSafetyGate {

    fun allowSexualReply(context: SexualContext): Boolean {
        return context != SexualContext.PRESSURE_OR_COERCION
    }

    fun enforceTone(
        requested: ReplyTone,
        context: SexualContext
    ): ReplyTone {
        return if (context == SexualContext.PRESSURE_OR_COERCION) {
            ReplyTone.PROTECTIVE
        } else {
            requested
        }
    }
}
