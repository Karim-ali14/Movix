package com.karimali.movix.features.movies.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.karimali.movix.R
import com.karimali.movix.core.ui.asDomainError
import com.karimali.movix.core.ui.components.ErrorView
import com.karimali.movix.core.ui.components.OfflineBanner
import com.karimali.movix.features.movies.domain.model.Movie
import com.karimali.movix.features.movies.presentation.components.EmptyView
import com.karimali.movix.features.movies.presentation.components.MoviesGrid
import com.karimali.movix.features.movies.presentation.components.MoviesHeader
import com.karimali.movix.features.movies.presentation.components.MoviesListSkeleton
import com.karimali.movix.features.movies.presentation.contract.MoviesIntent
import com.karimali.movix.features.movies.presentation.contract.MoviesState

@Composable
fun MoviesScreen(
    state: MoviesState,
    movies: LazyPagingItems<Movie>,
    onIntent: (MoviesIntent) -> Unit,
) {
    val refresh = movies.loadState.refresh
    val isEmpty = movies.itemCount == 0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MoviesHeader(
                query = state.query,
                isLoading = refresh is LoadState.Loading && !isEmpty,
                onQueryChange = { onIntent(MoviesIntent.QueryChanged(it)) },
                onClear = { onIntent(MoviesIntent.ClearQuery) },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isEmpty && refresh is LoadState.Loading -> MoviesListSkeleton()

                isEmpty && refresh is LoadState.Error -> ErrorView(
                    error = refresh.error.asDomainError(),
                    onRetry = movies::retry,
                )

                isEmpty && refresh is LoadState.NotLoading -> EmptyView(state.query)

                else -> Column(Modifier.fillMaxSize()) {
                    if (refresh is LoadState.Error) {
                        OfflineBanner(
                            title = stringResource(R.string.showing_saved_movies),
                            error = refresh.error.asDomainError(),
                            onRetry = movies::retry,
                        )
                    }
                    MoviesGrid(
                        movies = movies,
                        onMovieClick = { onIntent(MoviesIntent.MovieClicked(it)) },
                    )
                }
            }
        }
    }
}