package com.replysense.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.replysense.app.ui.theme.Shape
import com.replysense.app.ui.theme.Spacing

@Composable
fun GlassPillButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 0.9f else 0.4f

    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clip(Shape.pill)
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = alpha)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(
                horizontal = Spacing.card,
                vertical = Spacing.inline
            )
    )
}
