package com.karimali.movix.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape scale. Components pick a size, not a number:
 *   MaterialTheme.shapes.medium   // movie card, poster
 *   MaterialTheme.shapes.small    // banners, chips
 *   MaterialTheme.shapes.large    // bottom sheets, dialogs
 *
 * Use CircleShape only for pills (search bar, rating badge).
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)