package com.replysense.app.ui.explainability

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import com.replysense.app.viewmodel.ui.ExplainabilitySection
import com.replysense.app.viewmodel.ui.ExplainabilityViewModel

/**
 * Phase E — E2 Explainability Screen
 * Stable inset handling.
 */
@Composable
fun ExplainabilityScreen(
    viewModel: ExplainabilityViewModel
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
                text = "Why this conclusion",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            viewModel.explainabilitySections.forEach { section ->
                ExplainabilityCard(section = section)
            }
        }
    }
}

@Composable
private fun ExplainabilityCard(
    section: ExplainabilitySection
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = section.summary,
                style = MaterialTheme.typography.bodyMedium
            )

            section.details?.let {
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (expanded) "Hide details" else "What contributed to this",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
