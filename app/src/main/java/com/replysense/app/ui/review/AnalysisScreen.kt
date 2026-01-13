package com.replysense.app.ui.review

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.replysense.app.viewmodel.AnalysisViewModel
import com.replysense.app.ui.components.ConfidenceIndicator
import com.replysense.app.ui.components.IntentMismatchWarning

@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        ConfidenceIndicator(
            confidencePercent = uiState.confidencePercent
        )

        if (uiState.showRiskWarning) {
            IntentMismatchWarning()

            Text(
                text = uiState.explanation,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
