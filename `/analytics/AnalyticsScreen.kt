package com.replysense.app.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.replysense.app.ui.components.GlassCard

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel()
) {
    val state by viewModel.analyticsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Analytics",
            style = MaterialTheme.typography.titleLarge
        )

        // ─────────────────────────────
        // FUNNEL
        // ─────────────────────────────
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Funnel", style = MaterialTheme.typography.labelMedium)
                Text("Paywall shown: ${state.paywallShown}")
                Text("Upgrade started: ${state.upgradeStarted}")
                Text("Upgrade completed: ${state.upgradeCompleted}")
            }
        }

        // ─────────────────────────────
        // TRIAL
        // ─────────────────────────────
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Trial", style = MaterialTheme.typography.labelMedium)
                Text("Trials started: ${state.trialStarted}")
                Text("Trials converted: ${state.trialConverted}")
            }
        }

        // ─────────────────────────────
        // RETENTION
        // ─────────────────────────────
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Retention", style = MaterialTheme.typography.labelMedium)
                Text("Day 1 retained: ${state.retainedDay1}")
                Text("Day 7 retained: ${state.retainedDay7}")
            }
        }
    }
}
