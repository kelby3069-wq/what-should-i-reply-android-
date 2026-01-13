package com.replysense.app.ui.reply

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.replysense.app.domain.reply.SexualReplyStyle

@Composable
fun TonePicker(
    onSelected: (SexualReplyStyle) -> Unit
) {
    Column {
        Text("How do you want to respond?")

        SexualReplyStyle.values().forEach { style ->
            TextButton(onClick = { onSelected(style) }) {
                Text(styleLabel(style))
            }
        }
    }
}

private fun styleLabel(style: SexualReplyStyle): String {
    return when (style) {
        SexualReplyStyle.SOFT_FLIRT -> "Soft flirt"
        SexualReplyStyle.PLAYFUL_CONFIDENT -> "Playful confident"
        SexualReplyStyle.CURIOUS_SHY -> "Curious / shy"
        SexualReplyStyle.WARM_DIRECT -> "Warm direct"
        SexualReplyStyle.HOLDING_BACK -> "Hold back"
    }
}
