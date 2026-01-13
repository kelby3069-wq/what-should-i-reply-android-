package com.replysense.app.ui.components

import androidx.compose.animation.core.tween

object MicroMotion {

    // Canonical motion timings (locked)
    val Fast = tween<Float>(durationMillis = 120)
    val Standard = tween<Float>(durationMillis = 180)
    val Slow = tween<Float>(durationMillis = 240)
}
