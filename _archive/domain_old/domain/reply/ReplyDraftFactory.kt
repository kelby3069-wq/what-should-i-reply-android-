package com.replysense.app.domain.reply

import com.replysense.app.domain.voice.UserVoice

class ReplyDraftFactory {

    fun buildDraft(
        tone: ReplyTone,
        voice: UserVoice
    ): String {
        val base = when (tone) {
            ReplyTone.WARM ->
                "I like the energy here."

            ReplyTone.PLAYFUL ->
                "You’re definitely flirting now."

            ReplyTone.DIRECT ->
                "I’m interested, just being honest."

            ReplyTone.CALM ->
                "I like this, just taking it slow."

            ReplyTone.FIRM ->
                "I’m not comfortable with that."

            ReplyTone.PROTECTIVE ->
                "I’m going to pause this conversation."

            else ->
                "I hear you."
        }

        return applyVoice(base, voice)
    }

    private fun applyVoice(text: String, voice: UserVoice): String {
        return when (voice) {
            UserVoice.MINIMAL ->
                text.lowercase().split(" ").take(4).joinToString(" ")

            UserVoice.CASUAL ->
                "$text 🙂"

            UserVoice.EXPRESSIVE ->
                "$text Just wanted to say that."

            UserVoice.DIRECT ->
                text

            UserVoice.SHY ->
                "I’m a little shy, but $text.lowercase()"
        }
    }
}
