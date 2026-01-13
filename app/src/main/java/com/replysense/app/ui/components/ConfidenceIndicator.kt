package com.replysense.app.ui.components

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding

@Composable
fun ConfidenceIndicator(
    confidencePercent: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Confidence",
            style = MaterialTheme.typography.labelMedium
        )

        LinearProgressIndicator(
            progress = confidencePercent / 100f,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = "$confidencePercent%",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
