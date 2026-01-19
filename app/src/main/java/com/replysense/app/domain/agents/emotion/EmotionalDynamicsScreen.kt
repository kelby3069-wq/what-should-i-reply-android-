package com.replysense.app.ui.emotion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.replysense.app.ui.theme.RsSpacing

@Composable
fun EmotionalDynamicsScreen(
    emotions: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(RsSpacing.ScreenPadding)
    ) {

        Text(
            text = "Emotional Dynamics",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(RsSpacing.NarrativeSection))

        emotions.forEach { emotion ->
            EmotionItem(emotion)
            Spacer(modifier = Modifier.height(RsSpacing.ParagraphGap))
        }
    }
}

@Composable
private fun EmotionItem(
    emotion: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = emotion,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start
        )
    }
}
