package com.replysense.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * ReplySense premium palette.
 * Neutral-first, confident blue accent.
 */

// Brand
private val BrandBlue = Color(0xFF2F6BFF)

// Light neutrals
private val LightBg = Color(0xFFF7F8FA)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFF1F3F6)
private val LightOutline = Color(0xFFD3D9E4)
private val LightText = Color(0xFF0E1014)

// Dark neutrals
private val DarkBg = Color(0xFF0B0D12)
private val DarkSurface = Color(0xFF10131A)
private val DarkSurfaceVariant = Color(0xFF141926)
private val DarkOutline = Color(0xFF2A3348)
private val DarkText = Color(0xFFECEFF6)

val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,

    background = LightBg,
    onBackground = LightText,

    surface = LightSurface,
    onSurface = LightText,

    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF333A46),

    outline = LightOutline
)

val DarkColors = darkColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,

    background = DarkBg,
    onBackground = DarkText,

    surface = DarkSurface,
    onSurface = DarkText,

    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB9C2D6),

    outline = DarkOutline
)
