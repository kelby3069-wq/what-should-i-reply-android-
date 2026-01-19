package com.replysense.app.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.replysense.app.domain.conversation.SignalType
import com.replysense.app.domain.conversation.ConversationLine

@Composable
fun HighlightedConversation(
    lines: List<ConversationLine>,
    lineSignals: Map<Int, List<SignalType>>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Optional: scroll to first highlighted line
    LaunchedEffect(lineSignals) {
        val firstHitIndex = lines.indexOfFirst { lineSignals.containsKey(it.id) }
        if (firstHitIndex >= 0) {
            // Rough scroll positioning (safe + simple)
            scrollState.animateScrollTo(firstHitIndex * 72)
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(vertical = 8.dp)
    ) {
        for (line in lines) {
            val signals = lineSignals[line.id].orEmpty()
            LineRow(
                text = line.text,
                signals = signals
            )
        }
    }
}

@Composable
private fun LineRow(
    text: String,
    signals: List<SignalType>
) {
    val background = when {
        signals.contains(SignalType.UNCERTAINTY) ->
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
        signals.contains(SignalType.ELEVATED_TONE) ->
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
        signals.contains(SignalType.HIGH_ENGAGEMENT) ->
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        else -> Color.Transparent
    }

    val annotated = buildAnnotatedString {
        append(text)
        if (signals.isNotEmpty()) {
            append("  ")
            pushStyle(
                SpanStyle(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            append(
                signals.joinToString(prefix = "• ", separator = " • ") {
                    when (it) {
                        SignalType.UNCERTAINTY -> "uncertainty"
                        SignalType.ELEVATED_TONE -> "elevated tone"
                        SignalType.HIGH_ENGAGEMENT -> "high engagement"
                    }
                }
            )
            pop()
        }
    }

    Text(
        text = annotated,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .background(background),
        style = MaterialTheme.typography.bodyMedium
    )
}
