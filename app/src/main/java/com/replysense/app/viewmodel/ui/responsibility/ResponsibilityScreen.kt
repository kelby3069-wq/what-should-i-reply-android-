package com.replysense.app.ui.responsibility

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
import com.replysense.app.viewmodel.ui.ResponsibilityViewModel

/**
 * Phase E — E5 Responsibility & Boundaries Screen
 *
 * Authoritative, calm, and adult.
 * No hedging. No emotional dilution.
 */
@Composable
fun ResponsibilityScreen(
    viewModel: ResponsibilityViewModel
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
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {

            Text(
                text = "Responsibility & boundaries",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            ResponsibilitySection(
                title = "What you did well",
                items = viewModel.userDidWell
            )

            ResponsibilitySection(
                title = "What you did not cause",
                items = viewModel.userDidNotCause
            )

            ResponsibilitySection(
                title = "Other dynamics that contributed",
                items = viewModel.otherPartyActions
            )
        }
    }
}

@Composable
private fun ResponsibilitySection(
    title: String,
    items: List<String>
) {
    if (items.isEmpty()) return

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

            items.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
