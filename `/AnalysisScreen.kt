package com.replysense.app._disabled_phase_c

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.replysense.app.domain.analysis.AnalysisResult
import com.replysense.app.ui.reply.HelpMeReplyButton

@Composable
fun AnalysisScreen(
    result: AnalysisResult,
    onHelpMeReply: () -> Unit,
    onUnlockRequested: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = AnalysisCopy.overview(result),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = AnalysisCopy.contribution(result),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = AnalysisCopy.recoverability(result),
            style = MaterialTheme.typography.bodyMedium
        )

        HelpMeReplyButton(
            enabled = result.allowSexualReplyHelp,
            onClick = onHelpMeReply
        )

        if (result.requiresDeepAnalysis) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onUnlockRequested) {
                Text("Unlock full analysis")
            }
        }
    }
}
