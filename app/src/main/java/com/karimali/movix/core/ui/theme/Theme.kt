package com.karimali.movix.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Single entry point for the design system.
 * Wrap EVERYTHING (the whole NavHost, and every @Preview) with MovieAppTheme.
 *
 * Color selection order:
 *  1. Android 12+ and dynamicColor = true -> colors derived from the user's wallpaper
 *  2. otherwise -> our brand palette (light or dark)
 *
 * Note: dynamic color replaces our brand red on Android 12+. If you want the
 * brand to stay consistent for the review, pass dynamicColor = false.
 */
@Composable
fun MovieAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}