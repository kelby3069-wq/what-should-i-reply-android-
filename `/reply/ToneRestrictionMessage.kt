package com.replysense.app.ui.reply

import com.replysense.app.domain.emotion.Emotion
import com.replysense.app.domain.emotion.EmotionResult

object ToneRestrictionMessage {

    fun message(result: EmotionResult): String? {
        return when {
            result.primaryEmotion == Emotion.ANXIETY ->
                "Playful replies are hidden to keep things calm and safe."

            result.primaryEmotion == Emotion.ANGER ->
                "Playful replies are hidden due to frustration signals."

            result.flags.isNotEmpty() ->
                "Some reply styles are hidden due to boundary or safety signals."

            else -> null
        }
    }
}
