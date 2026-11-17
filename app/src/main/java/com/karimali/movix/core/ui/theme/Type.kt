package com.karimali.movix.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type scale (Material 3 has 15 roles: display / headline / title / body / label x S/M/L).
 *
 * Rule #2: screens never write fontSize = 18.sp. They pick a ROLE:
 *   MaterialTheme.typography.titleMedium
 *
 * To use a custom font later (e.g. Poppins from Google Fonts), change
 * AppFontFamily only; the whole app updates.
 */
private val AppFontFamily = FontFamily.Default

private val Base = Typography()

val AppTypography = Typography(
    // Big screen titles (e.g. "Discover")
    displaySmall = Base.displaySmall.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    ),
    // Movie title on the details screen
    headlineMedium = Base.headlineMedium.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold
    ),
    // Section headers ("Synopsis")
    titleLarge = Base.titleLarge.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold
    ),
    // Movie title inside the grid card
    titleSmall = Base.titleSmall.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold
    ),
    // Synopsis paragraph: slightly taller line height for readability
    bodyLarge = Base.bodyLarge.copy(
        fontFamily = AppFontFamily,
        lineHeight = 26.sp
    ),
    // Release year, secondary info
    bodySmall = Base.bodySmall.copy(fontFamily = AppFontFamily),
    // Rating badge, chips, buttons
    labelMedium = Base.labelMedium.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium
    ),
    labelLarge = Base.labelLarge.copy(fontFamily = AppFontFamily),
)