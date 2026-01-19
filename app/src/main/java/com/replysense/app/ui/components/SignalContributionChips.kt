package com.replysense.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.replysense.app.ui.theme.RsSpacing

@Composable
fun SignalContributionChips(
    signals: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(RsSpacing.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(RsSpacing.MinorGap)
    ) {
        signals.forEach { signal ->
            Text(
                text = signal,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
