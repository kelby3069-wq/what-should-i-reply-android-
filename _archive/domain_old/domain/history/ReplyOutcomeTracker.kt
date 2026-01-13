package com.replysense.app.domain.history

import com.replysense.app.domain.emotion.ReplyTone

object ReplyOutcomeTracker {

    private val toneSuccess = mutableMapOf<ReplyTone, Int>()

    fun record(tone: ReplyTone, helpful: Boolean) {
        if (helpful) {
            toneSuccess[tone] = (toneSuccess[tone] ?: 0) + 1
        }
    }

    fun preferredOrder(tones: Set<ReplyTone>): List<ReplyTone> {
        return tones.sortedByDescending { toneSuccess[it] ?: 0 }
    }
}
