package com.karimali.movix.features.details.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.karimali.movix.core.ui.theme.AppSpacing

/** Lets UI tests assert that the loading placeholder is on screen. */
internal const val SKELETON_TEST_TAG = "details_skeleton"

// Heights of the placeholder text lines, roughly matching the real text they stand in for.
private val TitleLineHeight = 28.dp
private val SubtitleLineHeight = 18.dp
private val BodyLineHeight = 14.dp

/** Placeholder with the same layout as the content, shown while the first load is running. */
@Composable
internal fun MovieDetailsSkeleton(modifier: Modifier = Modifier) {
    // One brush (one animation) shared by every block.
    val shimmer = rememberShimmerBrush()
    val lineShape = MaterialTheme.shapes.small

    Column(modifier.fillMaxSize().testTag(SKELETON_TEST_TAG)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(BackdropHeight)
                .background(shimmer),
        )
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Box(
                    Modifier
                        .width(PosterWidth)
                        .aspectRatio(2f / 3f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(shimmer),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    SkeletonLine(shimmer, lineShape, widthFraction = 0.8f, height = TitleLineHeight)
                    SkeletonLine(shimmer, lineShape, widthFraction = 0.5f, height = SubtitleLineHeight)
                    Box(
                        Modifier
                            .size(RatingRingSize)
                            .clip(CircleShape)
                            .background(shimmer),
                    )
                }
            }
            repeat(4) {
                SkeletonLine(shimmer, lineShape, widthFraction = 1f, height = BodyLineHeight)
            }
        }
    }
}

@Composable
private fun SkeletonLine(brush: Brush, shape: Shape, widthFraction: Float, height: Dp) {
    Box(
        Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(shape)
            .background(brush),
    )
}