package com.replysense.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PaywallScreen(
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Upgrade to ReplySense Pro",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "• Unlimited analyses\n• All reply tones\n• Voice-style matching",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Tap to upgrade",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(top = 12.dp)
        )
    }
}
