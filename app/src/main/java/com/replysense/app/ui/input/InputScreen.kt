package com.replysense.app.ui.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.replysense.app.viewmodel.AnalysisViewModel

@Composable
fun InputScreen(
    viewModel: AnalysisViewModel = viewModel()
) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Paste message") }
        )

        Button(
            onClick = { viewModel.analyzeText(text) },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Analyze")
        }
    }
}
