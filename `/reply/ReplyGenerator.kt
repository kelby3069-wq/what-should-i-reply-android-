package com.replysense.app.ui.reply

import com.replysense.app.model.ReplyOptionUi

object ReplyGenerator {

    fun generate(
        options: List<ReplyOptionUi>,
        selectedTone: ReplyTone
    ): String {
        return options.firstOrNull { it.tone == selectedTone }?.text
            ?: options.firstOrNull()?.text
            ?: ""
    }
}
