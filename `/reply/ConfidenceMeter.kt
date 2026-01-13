package com.replysense.app.ui.reply

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.replysense.app.domain.emotion.EmotionResult

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ConfidenceMeter(result: EmotionResult) {
    val (label, description) = when {
        result.confidence >= 0.7f ->
            "High" to "Signals are clear and consistent."

        result.confidence >= 0.4f ->
            "Medium" to "Signals are fairly clear, with some ambiguity."

        else ->
            "Low" to "Signals are mixed. Interpret cautiously."
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically { it / 2 }
    ) {
        Column {
            Text(
                text = "Confidence: $label",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
