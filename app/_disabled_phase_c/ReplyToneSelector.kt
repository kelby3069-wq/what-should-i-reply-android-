package com.replysense.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.replysense.app.ui.reply.ReplyTone

@Composable
fun ReplyToneSelector(
    selected: ReplyTone,
    onSelected: (ReplyTone) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReplyTone.values().forEach { tone ->
            Text(
                text = tone.name.lowercase().replaceFirstChar { it.uppercase() },
                style = if (tone == selected)
                    MaterialTheme.typography.labelLarge
                else
                    MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onSelected(tone) }
            )
        }
    }
}
