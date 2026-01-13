package com.replysense.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun IntentMismatchWarning(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = "Right now, it seems like you may be looking for more connection than the other person is offering. That doesn’t mean you did anything wrong — but it does affect what responses will land well.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
