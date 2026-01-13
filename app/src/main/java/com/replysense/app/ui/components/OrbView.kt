package com.replysense.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun OrbView(
    state: OrbState,
    modifier: Modifier = Modifier
) {
    // Subtle aura intensity by state (LOCKED semantics)
    val auraIntensity by animateFloatAsState(
        targetValue = when (state) {
            OrbState.IDLE -> 0.55f
            OrbState.READY -> 0.6f
            OrbState.SCANNING -> 0.75f
            OrbState.THINKING -> 0.8f
            OrbState.RESULT_READY -> 0.9f
            OrbState.LOW_CONFIDENCE -> 0.45f
            OrbState.ERROR -> 0.5f
        },
        animationSpec = tween(durationMillis = 600),
        label = "auraIntensity"
    )

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f

        // Outer aura (implicit state)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF8FA3FF).copy(alpha = 0.35f * auraIntensity),
                    Color.Transparent
                )
            ),
            radius = radius * 1.08f
        )

        // Inner core (identity, calm)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE6ECFF),
                    Color(0xFF6F7FBF)
                )
            ),
            radius = radius
        )
    }
}
