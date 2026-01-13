package com.replysense.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun NightSkyBackground(
    modifier: Modifier = Modifier
) {
    modifier.background(
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0E1015),
                Color(0xFF141823)
            )
        )
    )
}
