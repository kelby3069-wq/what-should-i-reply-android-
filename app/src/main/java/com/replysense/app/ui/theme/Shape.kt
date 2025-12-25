package com.replysense.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape philosophy:
 * - Premium apps lean on consistent rounded geometry.
 * - 12dp small for inputs/chips, 16dp medium for cards/buttons,
 *   24dp large for sheets/dialogs/cropper surfaces.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
