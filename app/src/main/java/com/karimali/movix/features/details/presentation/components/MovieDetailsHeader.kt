package com.karimali.movix.features.details.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.karimali.movix.R
import com.karimali.movix.core.ui.components.PosterImage
import com.karimali.movix.core.ui.theme.AppSpacing
import com.karimali.movix.core.util.formatter.formatRuntime
import com.karimali.movix.core.util.formatter.formatWithCommas
import com.karimali.movix.features.details.domain.model.MovieDetails
import java.util.Locale

private val RatingRingStroke = 5.dp
private const val PARALLAX_FACTOR = 0.5f

/** Full-width backdrop that scrolls at half speed. [scrollOffset] is read in the layer, so scrolling never recomposes it. */
@Composable
internal fun DetailsBackdrop(
    movie: MovieDetails,
    scrollOffset: () -> Int,
    modifier: Modifier = Modifier,
) {
    val background = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BackdropHeight)
            .graphicsLayer { translationY = scrollOffset() * PARALLAX_FACTOR },
    ) {
        PosterImage(
            url = movie.backdropUrl ?: movie.posterUrl,
            contentDescription = movie.title,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )
        // Fades the image into the sheet below, using the same color so no edge shows.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.5f to background.copy(alpha = 0f),
                        1f to background,
                    ),
                ),
        )
    }
}

/** Poster on the left; title, original title, year / runtime, rating and collection on the right. */
@Composable
internal fun MovieHeader(movie: MovieDetails, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Card(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.width(PosterWidth),
        ) {
            PosterImage(
                url = movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            if (!movie.originalTitle.equals(movie.title, ignoreCase = true)) {
                Text(
                    text = movie.originalTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Year and runtime are both optional, so only the ones we have are shown.
            val meta = listOfNotNull(
                movie.releaseYear?.takeIf { it.isNotBlank() },
                movie.runtimeMinutes?.let(::formatRuntime),
            ).joinToString("  •  ")

            if (meta.isNotEmpty()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                RatingRing(rating = movie.rating ?: 0.0, hasVotes = movie.voteCount > 0)
                Text(
                    text = stringResource(R.string.details_votes_format, movie.voteCount.formatWithCommas()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            movie.collectionName?.let {
                Text(
                    text = stringResource(R.string.details_part_of_format, it),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/** Circular 0-10 rating that animates from empty when it first appears. */
@Composable
internal fun RatingRing(rating: Double, hasVotes: Boolean, modifier: Modifier = Modifier) {
    var target by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(rating, hasVotes) {
        target = if (hasVotes) (rating / 10.0).toFloat().coerceIn(0f, 1f) else 0f
    }
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "rating",
    )

    val ringColor = when {
        !hasVotes -> MaterialTheme.colorScheme.surfaceContainerHighest
        rating >= 7.5 -> MaterialTheme.colorScheme.primary
        rating >= 5.0 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    val ratingText = String.format(Locale.US, "%.1f", rating)
    val semanticsDescription = if (hasVotes) {
        stringResource(R.string.details_rating_description, ratingText)
    } else {
        stringResource(R.string.details_no_rating_description)
    }

    Box(
        modifier
            .size(RatingRingSize)
            .semantics { contentDescription = semanticsDescription },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeWidth = RatingRingStroke,
        )
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = ringColor,
            strokeWidth = RatingRingStroke,
        )
        Text(
            text = if (hasVotes) ratingText else NOT_AVAILABLE,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}