package com.karimali.movix.features.details.presentation.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.karimali.movix.R
import com.karimali.movix.core.ui.components.ErrorView
import com.karimali.movix.core.ui.components.OfflineBanner
import com.karimali.movix.core.ui.theme.AppSpacing
import com.karimali.movix.features.details.domain.model.MovieDetails
import com.karimali.movix.features.details.presentation.components.DetailsBackdrop
import com.karimali.movix.features.details.presentation.components.GenresRow
import com.karimali.movix.features.details.presentation.components.InfoGrid
import com.karimali.movix.features.details.presentation.components.LinksRow
import com.karimali.movix.features.details.presentation.components.MovieDetailsSkeleton
import com.karimali.movix.features.details.presentation.components.MovieHeader
import com.karimali.movix.features.details.presentation.components.ProductionSection
import com.karimali.movix.features.details.presentation.components.Synopsis
import com.karimali.movix.features.details.presentation.components.Tagline
import com.karimali.movix.features.details.presentation.contract.MovieDetailsIntent
import com.karimali.movix.features.details.presentation.contract.MovieDetailsUiState

/** Where the sheet starts, measured from the top; the rest of the backdrop shows above it. */
private val SheetTopOffset = 220.dp

/** Extra space under the last item so the offline banner never covers it. */
private val BannerClearance = 72.dp

private enum class ScreenContent { Content, Error, Loading }

/** Stateless: renders [state] and reports user actions through [onIntent]. */
@Composable
fun MovieDetailsScreen(
    state: MovieDetailsUiState,
    onIntent: (MovieDetailsIntent) -> Unit,
) {
    val movie = state.movie
    val error = state.error

    val content = when {
        movie != null -> ScreenContent.Content
        error != null -> ScreenContent.Error
        else -> ScreenContent.Loading
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
    ) { _ ->
        Box(Modifier.fillMaxSize()) {
            Crossfade(targetState = content, label = "details-content") { target ->
                when (target) {
                    ScreenContent.Content -> movie?.let {
                        MovieDetailsContent(movie = it, onIntent = onIntent)
                    }

                    ScreenContent.Error -> error?.let {
                        ErrorView(error = it, onRetry = { onIntent(MovieDetailsIntent.Retry) })
                    }

                    ScreenContent.Loading -> MovieDetailsSkeleton()
                }
            }

            if (state.isRefreshing && content == ScreenContent.Content) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .align(Alignment.TopCenter),
                )
            }

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

            FilledTonalIconButton(
                onClick = { onIntent(MovieDetailsIntent.BackClicked) },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(AppSpacing.sm)
                    .align(Alignment.TopStart),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.details_back),
                )
            }
        }
    }
}

@Composable
private fun MovieDetailsContent(
    movie: MovieDetails,
    onIntent: (MovieDetailsIntent) -> Unit,
) {
    val scrollState = rememberScrollState()
    // A bottom sheet uses the "large" shape; only its top corners are rounded.
    val sheetShape = MaterialTheme.shapes.large.copy(
        bottomStart = ZeroCornerSize,
        bottomEnd = ZeroCornerSize,
    )

    Box(Modifier.fillMaxSize()) {
        DetailsBackdrop(movie = movie, scrollOffset = { scrollState.value })

        Column(Modifier.verticalScroll(scrollState)) {
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