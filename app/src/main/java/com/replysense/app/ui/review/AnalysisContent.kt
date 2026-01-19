package com.replysense.app.ui.review

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.replysense.app.viewmodel.ui.AnalysisUiState

@Composable
fun AnalysisContent(
    uiState: AnalysisUiState
) {
    Column(modifier = Modifier.padding(16.dp)) {
        uiState.conversationLines.forEach { line ->
            Text(
                text = line.text,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
