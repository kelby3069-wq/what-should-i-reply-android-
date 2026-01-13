package com.replysense.app.ui.reply

import com.replysense.app.domain.emotion.UserVoiceProfile

object StyleRenderer {

    fun render(
        intent: ReplyIntent,
        tone: ReplyTone,
        voice: UserVoiceProfile
    ): String {

        val base = buildString {
            if (intent.acknowledge) append("I hear you. ")
            if (intent.boundary) append("I’m not okay with this. ")
            if (intent.close) append("I need space.")
        }.trim()

        return applyVoice(base, voice, tone)
    }

    private fun applyVoice(
        text: String,
        voice: UserVoiceProfile,
        tone: ReplyTone
    ): String {

        var result = text

        if (voice.usesLowercase) {
            result = result.lowercase()
        }

        if (voice.emojiFrequency > 0.05f && tone != ReplyTone.FIRM) {
            result += " 🙂"
        }

        if (voice.usesSoftening && tone != ReplyTone.FIRM) {
            result = "I think $result"
        }

        return result
    }
}
