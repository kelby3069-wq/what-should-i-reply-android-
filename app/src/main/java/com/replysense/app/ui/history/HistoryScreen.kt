package com.replysense.app.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.replysense.app.data.analysis.AnalysisHistoryStore
import com.replysense.app.model.AnalysisResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onOpen: (AnalysisResult) -> Unit,
    onBack: () -> Unit
) {
    val history by AnalysisHistoryStore.history.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            history.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { onOpen(item) }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.overallRead, maxLines = 2)
                    }
                }
            }
        }
    }
}
