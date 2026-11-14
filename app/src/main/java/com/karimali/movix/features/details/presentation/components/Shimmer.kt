package com.karimali.movix.features.details.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

private const val SHIMMER_TRAVEL_PX = 1000f
private const val SHIMMER_WIDTH_PX = 500f

/**
 * A moving highlight for loading placeholders. Create it once per skeleton and pass it to
 * `Modifier.background(brush)` on every block, so the whole skeleton runs a single animation.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1200, easing = LinearEasing)),
        label = "shimmer-shift",
    )
    val base = MaterialTheme.colorScheme.surfaceContainerHighest
    val highlight = MaterialTheme.colorScheme.surfaceContainerLow

    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(shift * SHIMMER_TRAVEL_PX, 0f),
        end = Offset(shift * SHIMMER_TRAVEL_PX + SHIMMER_WIDTH_PX, 0f),
    )
}