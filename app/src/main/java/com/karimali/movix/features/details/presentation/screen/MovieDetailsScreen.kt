package com.karimali.movix.features.details.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.karimali.movix.R
import com.karimali.movix.core.ui.components.ErrorView
import com.karimali.movix.core.ui.components.OfflineBanner
import com.karimali.movix.core.ui.theme.AppSpacing
import com.karimali.movix.features.details.domain.model.MovieDetails
import com.karimali.movix.features.details.presentation.components.ContentMaxWidth
import com.karimali.movix.features.details.presentation.components.DetailsBackdrop
import com.karimali.movix.features.details.presentation.components.GenresRow
import com.karimali.movix.features.details.presentation.components.InfoGrid
import com.karimali.movix.features.details.presentation.components.LinksRow
import com.karimali.movix.features.details.presentation.components.MovieDetailsSkeleton
import com.karimali.movix.features.details.presentation.components.MovieHeader
import com.karimali.movix.features.details.presentation.components.ProductionSection
import com.karimali.movix.features.details.presentation.components.SheetTopOffset
import com.karimali.movix.features.details.presentation.components.Synopsis
import com.karimali.movix.features.details.presentation.components.Tagline
import com.karimali.movix.features.details.presentation.components.detailsSheetShape
import com.karimali.movix.features.details.presentation.contract.MovieDetailsIntent
import com.karimali.movix.features.details.presentation.contract.MovieDetailsUiState

/** Extra space under the last item so the offline banner never covers it. */
private val BannerClearance = 72.dp

private enum class ScreenContent { Content, Error, Loading }

/** Stateless: renders [state] and reports user actions through [onIntent]. */
@Composable
fun MovieDetailsScreen(
    state: MovieDetailsUiState,
    onIntent: (MovieDetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val movie = state.movie
    val error = state.error
    val scrollState = rememberScrollState(initial = 0)

    val content = when {
        movie != null -> ScreenContent.Content
        error != null -> ScreenContent.Error
        else -> ScreenContent.Loading
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
    ) { _ ->
        Box(Modifier.fillMaxSize()) {
            Crossfade(targetState = content, label = "details-content") { target ->
                when (target) {
                    ScreenContent.Content -> movie?.let {
                        MovieDetailsContent(
                            movie = it,
                            scrollState = scrollState,
                            onIntent = onIntent,
                        )
                    }

                    ScreenContent.Error -> error?.let {
                        ErrorView(error = it, onRetry = { onIntent(MovieDetailsIntent.Retry) })
                    }

                    ScreenContent.Loading -> MovieDetailsSkeleton()
                }
            }

            // Always on screen, so the user can leave from Loading, Error and Content.
            DetailsTopBar(
                title = movie?.title,
                scrollState = scrollState,
                isRefreshing = state.isRefreshing,
                onBackClick = { onIntent(MovieDetailsIntent.BackClicked) },
                modifier = Modifier.align(Alignment.TopCenter),
            )

            // Offline-first: cached details stay visible, the failure is only a banner.
            if (movie != null && error != null) {
                OfflineBanner(
                    title = stringResource(R.string.details_showing_saved),
                    error = error,
                    onRetry = { onIntent(MovieDetailsIntent.Retry) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                )
            }
        }
    }
}

/**
 * Back button, title and refresh line. The scroll-driven alpha is computed here,
 * so scrolling only recomposes this bar and not the whole screen.
 * [title] is null while there is no movie yet.
 */
@Composable
private fun DetailsTopBar(
    title: String?,
    scrollState: ScrollState,
    isRefreshing: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    val alpha by remember(scrollState, density, title) {
        derivedStateOf {
            if (title == null) {
                0f
            } else {
                val thresholdPx = with(density) { (SheetTopOffset - 56.dp).toPx() }
                if (thresholdPx > 0f) (scrollState.value / thresholdPx).coerceIn(0f, 1f) else 0f
            }
        }
    }
    val showTitle by remember { derivedStateOf { alpha > 0.65f } }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = alpha),
        shadowElevation = 2.dp * alpha,
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                // The scrim is the button's own container, so nothing covers it.
                // Over the image the icon is white; as the bar turns solid it fades to onSurface.
                IconButton(
                    onClick = onBackClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f * (1f - alpha)),
                        contentColor = lerp(Color.White, MaterialTheme.colorScheme.onSurface, alpha),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.details_back),
                    )
                }

                AnimatedVisibility(
                    visible = showTitle && title != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = title.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun MovieDetailsContent(
    movie: MovieDetails,
    scrollState: ScrollState,
    onIntent: (MovieDetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
        ) {
            Box(Modifier.fillMaxWidth()) {
                DetailsBackdrop(movie = movie, scrollOffset = { scrollState.value })

                Column(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(SheetTopOffset))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = detailsSheetShape(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(AppSpacing.lg)
                                .navigationBarsPadding(),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                        ) {
                            MovieHeader(movie)
                            if (movie.tagline.isNotBlank()) Tagline(movie.tagline)
                            GenresRow(movie.genres)
                            Synopsis(movie.overview)
                            InfoGrid(movie)
                            ProductionSection(movie)
                            LinksRow(movie, onIntent)
                            Spacer(Modifier.height(BannerClearance))
                        }
                    }
                }
            }
        }
    }
}