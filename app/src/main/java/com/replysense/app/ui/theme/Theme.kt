package com.replysense.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun ReplySenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    val context = LocalContext.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Light status bar icons on dark backgrounds; dark icons on light backgrounds.
            val useDarkIcons = colors.background.luminance() > 0.5f
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = useDarkIcons
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = useDarkIcons
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
