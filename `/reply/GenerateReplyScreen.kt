package com.replysense.app.ui.reply

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.replysense.app.ui.components.ReplyToneSelector

@Composable
fun GenerateReplyScreen(
    suggestedReplies: List<String>,
    modifier: Modifier = Modifier
) {
    var selectedTone by remember { mutableStateOf(ReplyTone.CALM) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Suggested Replies",
            style = MaterialTheme.typography.titleMedium
        )

        ReplyToneSelector(
            selected = selectedTone,
            onSelected = { selectedTone = it }
        )

        suggestedReplies.forEach { reply ->
            Text(
                text = "• $reply",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
