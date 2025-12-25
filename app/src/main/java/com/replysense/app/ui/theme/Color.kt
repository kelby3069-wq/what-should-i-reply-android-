package com.replysense.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * ReplySense premium palette:
 * - Neutral-first surfaces (calm, "expensive")
 * - Confident blue accent (startup-tier, not default purple)
 * - Great in both light and dark
 */

// Accent
private val BrandBlue = Color(0xFF2F6BFF)
private val BrandBlueDark = Color(0xFF7AA2FF)

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
private val DarkTextMuted = Color(0xFFB9C2D6)

// Semantic
private val Danger = Color(0xFFFF4D4D)

val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7EEFF),
    onPrimaryContainer = Color(0xFF0A1B44),

    background = LightBg,
    onBackground = LightText,

    surface = LightSurface,
    onSurface = LightText,

    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF333A46),

    outline = LightOutline,
    outlineVariant = Color(0xFFE6E9EF),

    error = Danger,
    onError = Color.White
)

val DarkColors = darkColorScheme(
    primary = BrandBlueDark,
    onPrimary = Color(0xFF0B163A),
    primaryContainer = Color(0xFF132456),
    onPrimaryContainer = Color(0xFFDEE7FF),

    background = DarkBg,
    onBackground = DarkText,

    surface = DarkSurface,
    onSurface = DarkText,

    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextMuted,

    outline = DarkOutline,
    outlineVariant = Color(0xFF20283A),

    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2B0000)
)
