package com.karimali.movix.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Material 3 color roles.
 *
 * Screens should never use raw colors directly.
 * Always access colors through MaterialTheme.colorScheme.
 *
 * This keeps the app consistent and supports light and dark themes.
 *
 * Cinematic Blue palette. Source color: 0xFF315DA8, generated with Material Theme Builder.
 * Light and dark use the same tonal palettes (primary blue, blue-grey secondary, gold tertiary),
 * so the brand identity stays the same in both modes.
 */


// ---------- Light palette ----------

private val lightPrimary = Color(0xFF315DA8)
private val lightOnPrimary = Color(0xFFFFFFFF)
private val lightPrimaryContainer = Color(0xFFDCE4FF)
private val lightOnPrimaryContainer = Color(0xFF101B36)

private val lightSecondary = Color(0xFF526477)
private val lightOnSecondary = Color(0xFFFFFFFF)
private val lightSecondaryContainer = Color(0xFFDCE7F0)
private val lightOnSecondaryContainer = Color(0xFF101D29)

private val lightTertiary = Color(0xFF806020)
private val lightOnTertiary = Color(0xFFFFFFFF)
private val lightTertiaryContainer = Color(0xFFF5E6C8)
private val lightOnTertiaryContainer = Color(0xFF291B00)

private val lightError = Color(0xFFBA1A1A)
private val lightOnError = Color(0xFFFFFFFF)
private val lightErrorContainer = Color(0xFFFFDAD6)
private val lightOnErrorContainer = Color(0xFF410002)

private val lightBackground = Color(0xFFF7F8FC)
private val lightOnBackground = Color(0xFF191C24)

private val lightSurface = Color(0xFFF7F8FC)
private val lightOnSurface = Color(0xFF191C24)
private val lightSurfaceVariant = Color(0xFFE3E7F0)
private val lightOnSurfaceVariant = Color(0xFF444956)

private val lightOutline = Color(0xFF747985)
private val lightOutlineVariant = Color(0xFFC4C9D5)

private val lightInverseSurface = Color(0xFF2E303A)
private val lightInverseOnSurface = Color(0xFFF0F0F7)
private val lightInversePrimary = Color(0xFFB2C5FF)

private val lightSurfaceContainerLowest = Color(0xFFFFFFFF)
private val lightSurfaceContainerLow = Color(0xFFF1F3F9)
private val lightSurfaceContainer = Color(0xFFEBEEF5)
private val lightSurfaceContainerHigh = Color(0xFFE5E8F0)
private val lightSurfaceContainerHighest = Color(0xFFDFE3EC)



// ---------- Dark palette ----------
// Same tonal palettes as the light palette, read at the dark tones
// (accent 80/20/30/90, neutral 6 and up).

private val darkPrimary = Color(0xFFADC6FF)
private val darkOnPrimary = Color(0xFF002E69)
private val darkPrimaryContainer = Color(0xFF0E448E)
private val darkOnPrimaryContainer = Color(0xFFD8E2FF)

private val darkSecondary = Color(0xFFB6C8DE)
private val darkOnSecondary = Color(0xFF203243)
private val darkSecondaryContainer = Color(0xFF37485A)
private val darkOnSecondaryContainer = Color(0xFFD1E4FB)

private val darkTertiary = Color(0xFFEAC077)
private val darkOnTertiary = Color(0xFF412D00)
private val darkTertiaryContainer = Color(0xFF5E4201)
private val darkOnTertiaryContainer = Color(0xFFFFDEA8)

private val darkError = Color(0xFFFFB4AB)
private val darkOnError = Color(0xFF690005)
private val darkErrorContainer = Color(0xFF93000A)
private val darkOnErrorContainer = Color(0xFFFFDAD6)

private val darkBackground = Color(0xFF121318)
private val darkOnBackground = Color(0xFFE2E2E9)

private val darkSurface = Color(0xFF121318)
private val darkOnSurface = Color(0xFFE2E2E9)
private val darkSurfaceVariant = Color(0xFF434751)
private val darkOnSurfaceVariant = Color(0xFFC3C6D3)

private val darkOutline = Color(0xFF8D909C)
private val darkOutlineVariant = Color(0xFF434751)

private val darkInverseSurface = Color(0xFFE2E2E9)
private val darkInverseOnSurface = Color(0xFF2F3036)
private val darkInversePrimary = Color(0xFF315DA8)

private val darkSurfaceContainerLowest = Color(0xFF0C0E13)
private val darkSurfaceContainerLow = Color(0xFF1A1B20)
private val darkSurfaceContainer = Color(0xFF1E1F25)
private val darkSurfaceContainerHigh = Color(0xFF282A2F)
private val darkSurfaceContainerHighest = Color(0xFF33353A)


// ---------- Light Color Scheme ----------

val LightColorScheme = lightColorScheme(
    primary = lightPrimary,
    onPrimary = lightOnPrimary,
    primaryContainer = lightPrimaryContainer,
    onPrimaryContainer = lightOnPrimaryContainer,

    secondary = lightSecondary,
    onSecondary = lightOnSecondary,
    secondaryContainer = lightSecondaryContainer,
    onSecondaryContainer = lightOnSecondaryContainer,

    tertiary = lightTertiary,
    onTertiary = lightOnTertiary,
    tertiaryContainer = lightTertiaryContainer,
    onTertiaryContainer = lightOnTertiaryContainer,

    error = lightError,
    onError = lightOnError,
    errorContainer = lightErrorContainer,
    onErrorContainer = lightOnErrorContainer,

    background = lightBackground,
    onBackground = lightOnBackground,

    surface = lightSurface,
    onSurface = lightOnSurface,
    surfaceVariant = lightSurfaceVariant,
    onSurfaceVariant = lightOnSurfaceVariant,

    outline = lightOutline,
    outlineVariant = lightOutlineVariant,

    inverseSurface = lightInverseSurface,
    inverseOnSurface = lightInverseOnSurface,
    inversePrimary = lightInversePrimary,

    surfaceContainerLowest = lightSurfaceContainerLowest,
    surfaceContainerLow = lightSurfaceContainerLow,
    surfaceContainer = lightSurfaceContainer,
    surfaceContainerHigh = lightSurfaceContainerHigh,
    surfaceContainerHighest = lightSurfaceContainerHighest
)


// ---------- Dark Color Scheme ----------

val DarkColorScheme = darkColorScheme(
    primary = darkPrimary,
    onPrimary = darkOnPrimary,
    primaryContainer = darkPrimaryContainer,
    onPrimaryContainer = darkOnPrimaryContainer,

    secondary = darkSecondary,
    onSecondary = darkOnSecondary,
    secondaryContainer = darkSecondaryContainer,
    onSecondaryContainer = darkOnSecondaryContainer,

    tertiary = darkTertiary,
    onTertiary = darkOnTertiary,
    tertiaryContainer = darkTertiaryContainer,
    onTertiaryContainer = darkOnTertiaryContainer,

    error = darkError,
    onError = darkOnError,
    errorContainer = darkErrorContainer,
    onErrorContainer = darkOnErrorContainer,

    background = darkBackground,
    onBackground = darkOnBackground,

    surface = darkSurface,
    onSurface = darkOnSurface,
    surfaceVariant = darkSurfaceVariant,
    onSurfaceVariant = darkOnSurfaceVariant,

    outline = darkOutline,
    outlineVariant = darkOutlineVariant,

    inverseSurface = darkInverseSurface,
    inverseOnSurface = darkInverseOnSurface,
    inversePrimary = darkInversePrimary,

    surfaceContainerLowest = darkSurfaceContainerLowest,
    surfaceContainerLow = darkSurfaceContainerLow,
    surfaceContainer = darkSurfaceContainer,
    surfaceContainerHigh = darkSurfaceContainerHigh,
    surfaceContainerHighest = darkSurfaceContainerHighest
)
