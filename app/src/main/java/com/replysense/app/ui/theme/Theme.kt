package com.replysense.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE9FF),
    onPrimaryContainer = Ink,

    secondary = Accent2,
    onSecondary = Ink,
    secondaryContainer = Color(0xFFCCFFF5),
    onSecondaryContainer = Ink,

    background = L_Bg,
    onBackground = L_On,

    surface = L_Surface,
    onSurface = L_On,
    surfaceVariant = L_Surface2,
    onSurfaceVariant = L_OnMuted,

    outline = L_Border,
    outlineVariant = Color(0xFFD7DCE8),

    error = Danger,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF75A7FF),
    onPrimary = Ink,
    primaryContainer = Color(0xFF1C2F55),
    onPrimaryContainer = D_On,

    secondary = Color(0xFF57E0CD),
    onSecondary = Ink,
    secondaryContainer = Color(0xFF0F3B36),
    onSecondaryContainer = D_On,

    background = D_Bg,
    onBackground = D_On,

    surface = D_Surface,
    onSurface = D_On,
    surfaceVariant = D_Surface2,
    onSurfaceVariant = D_OnMuted,

    outline = D_Border,
    outlineVariant = Color(0xFF2A3454),

    error = Color(0xFFFF6B6D),
    onError = Ink
)

@Immutable
data class ReplySenseDesignTokens(
    val elevation1: Float = 1f,
    val elevation2: Float = 3f,
    val elevation3: Float = 6f,
    val hairline: Float = 1f
)

val LocalTokens = staticCompositionLocalOf { ReplySenseDesignTokens() }

@Composable
fun ReplySenseTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors: ColorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = ReplySenseTypography,
        shapes = ReplySenseShapes,
        content = content
    )
}
