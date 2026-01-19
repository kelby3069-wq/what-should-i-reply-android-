package com.replysense.app.ui.components

import com.replysense.app.ui.theme.RsSpacing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.replysense.app.viewmodel.ui.ExplainabilityUi

@Composable
fun ExplainabilitySection(explainability: ExplainabilityUi) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Hide explanation" else "Why this assessment?")
        }

        if (expanded) {
            Spacer(Modifier.height(RsSpacing.MinorGap))

            ExplainabilityBlock("Risk factors", explainability.riskDrivers)
            ExplainabilityBlock("Confidence factors", explainability.confidenceDrivers)
            ExplainabilityBlock("Uncertainty factors", explainability.uncertaintyDrivers)
        }
    }
}

@Composable
private fun ExplainabilityBlock(title: String, items: List<String>) {
    if (items.isEmpty()) return

    Column {
        Text(title, style = MaterialTheme.typography.labelMedium)
        items.forEach {
            Text("• ", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(RsSpacing.MinorGap))
    }
}


