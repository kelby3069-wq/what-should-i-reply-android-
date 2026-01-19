package com.replysense.app.ui.analysis

import com.replysense.app.ui.theme.RsSpacing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import com.replysense.app.viewmodel.ui.AnalysisOverviewViewModel

/**
 * Phase E â€” E1 Analysis Overview Screen
 * UI-only, stable across Compose versions.
 */
@Composable
fun AnalysisOverviewScreen(
    viewModel: AnalysisOverviewViewModel
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = RsSpacing.ParagraphGap, vertical = RsSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(RsSpacing.ParagraphGap)
        ) {

            Text(
                text = "Conversation Analysis",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            ConfidenceIndicator(label = viewModel.confidenceLabel)

            Text(
                text = viewModel.overallRead,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ConfidenceIndicator(label: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(RsSpacing.MinorGap)
    ) {
        Text(
            text = "Signal strength",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}


