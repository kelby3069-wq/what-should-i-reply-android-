package com.replysense.app.ui.reply

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.replysense.app.ui.components.GlassCard

@Composable
fun RateReplyScreen(
    replyText: String,
    onRate: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard {
            Text(
                text = replyText,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = "Was this reply helpful?",
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..5).forEach { rating ->
                OutlinedButton(
                    onClick = { onRate(rating) }
                ) {
                    Text(rating.toString())
                }
            }
        }
    }
}
