package com.replysense.app.ui.reply

data class ReplyOptionUi(
    val tone: ReplyTone,
    val text: String,
    val highlighted: Boolean = false
)
