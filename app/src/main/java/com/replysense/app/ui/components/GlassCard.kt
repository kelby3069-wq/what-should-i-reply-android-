package com.replysense.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.replysense.app.ui.theme.Shape
import com.replysense.app.ui.theme.Spacing

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(Shape.card)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
            )
            .padding(Spacing.card)
    ) {
        content()
    }
}
