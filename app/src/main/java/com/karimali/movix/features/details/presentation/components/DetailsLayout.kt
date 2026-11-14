package com.karimali.movix.features.details.presentation.components

import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// Layout values of this screen, defined once so header, content and skeleton always match.
internal val BackdropHeight = 300.dp
internal val PosterWidth = 112.dp
internal val RatingRingSize = 56.dp
internal val ContentMaxWidth = 640.dp

/** Where the sheet starts, measured from the top; the rest of the backdrop shows above it. */
internal val SheetTopOffset = 220.dp

/** Sheet shape shared by the content and the skeleton. */
@Composable
@ReadOnlyComposable
internal fun detailsSheetShape(): Shape = MaterialTheme.shapes.large.copy(
    bottomStart = ZeroCornerSize,
    bottomEnd = ZeroCornerSize,
)