package com.replysense.app.domain.reply

class ReplyToneMapper {

    fun fromSexualStyle(style: SexualReplyStyle): ReplyTone {
        return when (style) {
            SexualReplyStyle.SOFT_FLIRT -> ReplyTone.WARM
            SexualReplyStyle.PLAYFUL_CONFIDENT -> ReplyTone.PLAYFUL
            SexualReplyStyle.CURIOUS_SHY -> ReplyTone.WARM
            SexualReplyStyle.WARM_DIRECT -> ReplyTone.DIRECT
            SexualReplyStyle.HOLDING_BACK -> ReplyTone.CALM
        }
    }
}
