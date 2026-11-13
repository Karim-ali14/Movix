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
 * Cinematic Red palette generated using Material Theme Builder.
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

private val darkPrimary = Color(0xFFFFB4A9)
private val darkOnPrimary = Color(0xFF690003)
private val darkPrimaryContainer = Color(0xFF93000A)
private val darkOnPrimaryContainer = Color(0xFFFFDAD5)

private val darkSecondary = Color(0xFFE7BDB6)
private val darkOnSecondary = Color(0xFF442925)
private val darkSecondaryContainer = Color(0xFF5D3F3B)
private val darkOnSecondaryContainer = Color(0xFFFFDAD5)

private val darkTertiary = Color(0xFFDEC48C)
private val darkOnTertiary = Color(0xFF3E2E04)
private val darkTertiaryContainer = Color(0xFF574519)
private val darkOnTertiaryContainer = Color(0xFFFBDFA6)

private val darkError = Color(0xFFFFB4AB)
private val darkOnError = Color(0xFF690005)
private val darkErrorContainer = Color(0xFF93000A)
private val darkOnErrorContainer = Color(0xFFFFDAD6)

private val darkBackground = Color(0xFF1A1110)
private val darkOnBackground = Color(0xFFF1DFDC)

private val darkSurface = Color(0xFF1A1110)
private val darkOnSurface = Color(0xFFF1DFDC)
private val darkSurfaceVariant = Color(0xFF534341)
private val darkOnSurfaceVariant = Color(0xFFD8C2BE)

private val darkOutline = Color(0xFFA08C89)
private val darkOutlineVariant = Color(0xFF534341)

private val darkInverseSurface = Color(0xFFF1DFDC)
private val darkInverseOnSurface = Color(0xFF392E2C)
private val darkInversePrimary = Color(0xFFBB1614)

private val darkSurfaceContainerLowest = Color(0xFF140C0B)
private val darkSurfaceContainerLow = Color(0xFF231918)
private val darkSurfaceContainer = Color(0xFF271D1C)
private val darkSurfaceContainerHigh = Color(0xFF322826)
private val darkSurfaceContainerHighest = Color(0xFF3D3231)


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
