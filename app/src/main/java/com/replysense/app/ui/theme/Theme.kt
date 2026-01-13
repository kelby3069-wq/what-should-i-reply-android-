package com.replysense.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    background = NightBackground,
    surface = NightBackground,
    primary = PrimaryCTA,
    onPrimary = NightBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun ReplySenseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = ReplySenseTypography,
        content = content
    )
}
