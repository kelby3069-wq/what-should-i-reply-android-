package com.replysense.app.viewmodel.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.replysense.app.model.AnalysisResult

@Composable
fun CoachingScreen(
    viewModel: CoachingViewModel,
    analysisResult: AnalysisResult
) {
    // Keep Phase E analysis flow intact
    LaunchedEffect(analysisResult) {
        viewModel.setAnalysisResult(analysisResult)
    }

    val coachingSummary by viewModel.coachingSummary.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ─────────────────────────────────────────────
        // Coaching summary (Phase F applied)
        // ─────────────────────────────────────────────
        if (coachingSummary.isNotBlank()) {
            Text(
                text = coachingSummary,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // ─────────────────────────────────────────────
        // Suggested reply (Phase A nullable respected)
        // ─────────────────────────────────────────────
        analysisResult.coaching?.suggestedReply?.let { reply ->
            Text(
                text = "Suggested reply",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = reply,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // ─────────────────────────────────────────────
        // Boundary guidance (Phase A nullable respected)
        // ─────────────────────────────────────────────
        analysisResult.coaching?.boundaryGuidance?.let { guidance ->
            Text(
                text = "Boundary guidance",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = guidance,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
