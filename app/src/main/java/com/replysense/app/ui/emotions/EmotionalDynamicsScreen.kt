package com.replysense.app.ui.emotions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import com.replysense.app.model.EmotionalRead
import com.replysense.app.viewmodel.ui.EmotionalDynamicsViewModel

/**
 * Phase E — E3 Emotional Dynamics Screen
 *
 * Displays emotional state for:
 * - User
 * - Other party
 *
 * Neutral, non-clinical, non-blaming.
 */
@Composable
fun EmotionalDynamicsScreen(
    viewModel: EmotionalDynamicsViewModel
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Text(
                text = "Emotional dynamics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            EmotionCard(
                title = "You",
                emotion = viewModel.userEmotion
            )

            EmotionCard(
                title = "Other person",
                emotion = viewModel.otherEmotion
            )
        }
    }
}

@Composable
private fun EmotionCard(
    title: String,
    emotion: EmotionalRead
) {
    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Primary emotion: ${emotion.primary}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (emotion.secondary.isNotEmpty()) {
                Text(
                    text = "Also present: ${emotion.secondary.joinToString()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Intensity: ${emotion.intensity}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
