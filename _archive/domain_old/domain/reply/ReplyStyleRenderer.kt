package com.replysense.app.domain.reply

import com.replysense.app.domain.voice.UserVoice

class ReplyStyleRenderer(
    private val factory: ReplyDraftFactory = ReplyDraftFactory()
) {

    fun render(
        style: SexualReplyStyle,
        voice: UserVoice
    ): String {
        val tone = ReplyToneMapper().fromSexualStyle(style)
        return factory.buildDraft(tone, voice)
    }
}
