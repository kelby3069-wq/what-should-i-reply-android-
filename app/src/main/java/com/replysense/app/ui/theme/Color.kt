package com.replysense.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ReplySense palette goals:
 * - Premium, modern, neutral-first surfaces (not purple default)
 * - Confident accent (blue-cyan) + tasteful secondary (mint)
 * - High-contrast text without harsh pure-black/white
 *
 * This palette is tuned to feel "YC / Stripe-tier" while still being Material 3 compliant.
 */

// ---------- Brand / Accent ----------
val BrandBlue = Color(0xFF2F6BFF)      // confident, clean blue
val BrandCyan = Color(0xFF2ED3FF)      // energetic accent for subtle gradients/tones
val BrandMint = Color(0xFF31D0AA)      // secondary accent

// ---------- Neutrals (light) ----------
val Neutral0 = Color(0xFFFFFFFF)
val Neutral5 = Color(0xFFF7F8FA)
val Neutral10 = Color(0xFFF1F3F6)
val Neutral20 = Color(0xFFE6E9EF)
val Neutral30 = Color(0xFFD3D9E4)
val Neutral40 = Color(0xFFB8C0CF)
val Neutral50 = Color(0xFF8D98AD)
val Neutral60 = Color(0xFF6F7A90)
val Neutral70 = Color(0xFF4F5768)
val Neutral80 = Color(0xFF2F3440)
val Neutral90 = Color(0xFF171A20)
val Neutral95 = Color(0xFF0F1115)

// ---------- Neutrals (dark) ----------
val DarkBase = Color(0xFF0B0D12)
val DarkSurface = Color(0xFF10131A)
val DarkSurface2 = Color(0xFF141926)
val DarkSurface3 = Color(0xFF1A2030)
val DarkStroke = Color(0xFF2A3348)

// ---------- Semantic ----------
val Success = Color(0xFF23C483)
val Warning = Color(0xFFFFB020)
val Danger = Color(0xFFFF4D4D)
val Info = Color(0xFF4DA3FF)

// ---------- Tints / On-colors ----------
val OnLight = Color(0xFF0E1014)      // softer than pure black
val OnDark = Color(0xFFECEFF6)       // softer than pure white
val OnDarkMuted = Color(0xFFB9C2D6)
