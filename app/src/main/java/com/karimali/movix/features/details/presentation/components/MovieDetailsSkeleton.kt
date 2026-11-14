package com.karimali.movix.features.details.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.karimali.movix.core.ui.animation.shimmerEffect
import com.karimali.movix.core.ui.theme.AppSpacing

/** Lets UI tests assert that the loading placeholder is on screen. */
internal const val SKELETON_TEST_TAG = "details_skeleton"
private val TitleLineHeight = 28.dp
private val SubtitleLineHeight = 18.dp
private val BodyLineHeight = 14.dp
private val ChipHeight = 32.dp
private val InfoCardHeight = 64.dp
private val ButtonHeight = 40.dp

/** Placeholder matching every section of [MovieDetailsContent] to prevent layout shifts. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MovieDetailsSkeleton(modifier: Modifier = Modifier) {
    val lineShape = MaterialTheme.shapes.small
    val sheetShape = detailsSheetShape()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(SKELETON_TEST_TAG),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
        ) {
            Box(Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(BackdropHeight)
                        .shimmerEffect(),
                )

                Column(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(SheetTopOffset))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = sheetShape,
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(AppSpacing.lg)
                                .navigationBarsPadding(),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                        ) {
                            // MovieHeader Skeleton
                            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                                Box(
                                    Modifier
                                        .width(PosterWidth)
                                        .aspectRatio(2f / 3f)
                                        .clip(MaterialTheme.shapes.medium)
                                        .shimmerEffect(),
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                ) {
                                    SkeletonLine(lineShape, widthFraction = 0.85f, height = TitleLineHeight)
                                    SkeletonLine(lineShape, widthFraction = 0.55f, height = SubtitleLineHeight)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                    ) {
                                        Box(
                                            Modifier
                                                .size(RatingRingSize)
                                                .clip(CircleShape)
                                                .shimmerEffect(),
                                        )
                                        SkeletonLine(lineShape, widthFraction = 0.4f, height = BodyLineHeight)
                                    }
                                }
                            }

                            // Tagline Skeleton
                            SkeletonLine(lineShape, widthFraction = 0.7f, height = SubtitleLineHeight)

                            // GenresRow Skeleton
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            ) {
                                listOf(72.dp, 88.dp, 64.dp).forEach { chipWidth ->
                                    Box(
                                        Modifier
                                            .width(chipWidth)
                                            .height(ChipHeight)
                                            .clip(MaterialTheme.shapes.small)
                                            .shimmerEffect(),
                                    )
                                }
                            }

                            // Synopsis Skeleton
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                SkeletonLine(lineShape, widthFraction = 0.3f, height = SubtitleLineHeight)
                                repeat(3) {
                                    SkeletonLine(lineShape, widthFraction = 1f, height = BodyLineHeight)
                                }
                                SkeletonLine(lineShape, widthFraction = 0.65f, height = BodyLineHeight)
                            }

                            // InfoGrid Skeleton
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                repeat(2) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                    ) {
                                        repeat(2) {
                                            Box(
                                                Modifier
                                                    .weight(1f)
                                                    .height(InfoCardHeight)
                                                    .clip(MaterialTheme.shapes.medium)
                                                    .shimmerEffect(),
                                            )
                                        }
                                    }
                                }
                            }

                            // ProductionSection Skeleton
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                SkeletonLine(lineShape, widthFraction = 0.45f, height = SubtitleLineHeight)
                                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                    repeat(2) {
                                        Box(
                                            Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .clip(MaterialTheme.shapes.medium)
                                                .shimmerEffect(),
                                        )
                                    }
                                }
                            }

                            // LinksRow Skeleton
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            ) {
                                repeat(2) {
                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .height(ButtonHeight)
                                            .clip(MaterialTheme.shapes.medium)
                                            .shimmerEffect(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkeletonLine(shape: Shape, widthFraction: Float, height: Dp) {
    Box(
        Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(shape)
            .shimmerEffect(),
    )
}