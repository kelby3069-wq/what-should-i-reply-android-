package com.replysense.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/**
 * Theme decisions (strong defaults):
 * - Dynamic color OFF by default (brand consistency > wallpaper vibes)
 * - Neutral-first surfaces (soft, "expensive" background)
 * - Blue-cyan accent (confident, startup-tier)
 * - Elevated surfaces use subtle contrast, not heavy shadows
 */

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7EEFF),
    onPrimaryContainer = Color(0xFF0A1B44),

    secondary = BrandMint,
    onSecondary = Color(0xFF062018),
    secondaryContainer = Color(0xFFCCF5EA),
    onSecondaryContainer = Color(0xFF062018),

    tertiary = BrandCyan,
    onTertiary = Color(0xFF00212A),
    tertiaryContainer = Color(0xFFCFF7FF),
    onTertiaryContainer = Color(0xFF00212A),

    background = Neutral5,
    onBackground = OnLight,

    surface = Neutral0,
    onSurface = OnLight,

    surfaceVariant = Neutral10,
    onSurfaceVariant = Color(0xFF333A46),

    surfaceTint = BrandBlue,

    outline = Neutral30,
    outlineVariant = Neutral20,

    error = Danger,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    scrim = Color(0x99000000)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7AA2FF),
    onPrimary = Color(0xFF0B163A),
    primaryContainer = Color(0xFF132456),
    onPrimaryContainer = Color(0xFFDEE7FF),

    secondary = Color(0xFF58E3C1),
    onSecondary = Color(0xFF082018),
    secondaryContainer = Color(0xFF0E2D25),
    onSecondaryContainer = Color(0xFFC9F5EA),

    tertiary = Color(0xFF6FE4FF),
    onTertiary = Color(0xFF00212A),
    tertiaryContainer = Color(0xFF08313B),
    onTertiaryContainer = Color(0xFFCCF7FF),

    background = DarkBase,
    onBackground = OnDark,

    surface = DarkSurface,
    onSurface = OnDark,

    surfaceVariant = DarkSurface2,
    onSurfaceVariant = OnDarkMuted,

    surfaceTint = Color(0xFF7AA2FF),

    outline = DarkStroke,
    outlineVariant = Color(0xFF20283A),

    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2B0000),
    errorContainer = Color(0xFF3D0B0B),
    onErrorContainer = Color(0xFFFFDAD6),

    scrim = Color(0xB3000000)
)

/**
 * Elevation strategy:
 * - Prefer "tonal elevation" (surface container contrast) over big drop shadows
 * - Keep shadows subtle; premium apps look calm, not skeuomorphic
 */
object AppElevations {
    val Level0 = 0.dp
    val Level1 = 1.dp
    val Level2 = 3.dp
    val Level3 = 6.dp
}

@Composable
fun ReplySenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // keep brand consistent by default
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme: ColorScheme = when {
        dynamicColor && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    // Status bar: align icons with background luminance (premium detail)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val isLightIcons = colorScheme.background.luminance() < 0.5f
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isLightIcons
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isLightIcons
        }
    }

    CompositionLocalProvider(
        LocalAppSpacing provides AppSpacing(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
