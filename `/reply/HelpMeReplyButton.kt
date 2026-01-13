package com.replysense.app.ui.reply

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HelpMeReplyButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    if (!enabled) return

    Button(onClick = onClick) {
        Text("Help me reply")
    }
}
