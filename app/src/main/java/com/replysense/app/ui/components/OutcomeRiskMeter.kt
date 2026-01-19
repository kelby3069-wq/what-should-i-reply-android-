package com.replysense.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun OutcomeRiskMeter(
    riskPercent: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = riskPercent / 100f,
        animationSpec = tween(durationMillis = 600),
        label = "risk-meter"
    )

    LinearProgressIndicator(
        progress = animatedProgress,
        modifier = modifier
    )
}
