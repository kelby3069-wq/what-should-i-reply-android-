package com.replysense.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * ReplySense baseline: minimal UI only.
 * This replaces the old tab-based UI (setTab/tab/ComposeScreen/HistoryScreen).
 */
@Composable
fun AppRoot() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ReplySense baseline is alive.",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "OCR screen is hosted in MainActivity for now.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
