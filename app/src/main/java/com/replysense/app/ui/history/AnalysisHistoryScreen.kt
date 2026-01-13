package com.replysense.app.ui.history

import androidx.compose.runtime.Composable
import com.replysense.app.model.AnalysisResult

@Composable
fun AnalysisHistoryScreen(
    onOpen: (AnalysisResult) -> Unit = {},
    onBack: () -> Unit = {}
) {
    HistoryScreen(
        onOpen = onOpen,
        onBack = onBack
    )
}
