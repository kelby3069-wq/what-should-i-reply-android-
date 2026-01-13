package com.replysense.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun onBg(alpha: Float): Color {
    return MaterialTheme.colorScheme.onBackground.copy(alpha = alpha)
}
