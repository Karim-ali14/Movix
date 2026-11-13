package com.karimali.movix.features.movies.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.karimali.movix.core.ui.asDomainError
import com.karimali.movix.features.movies.domain.model.Movie
import com.karimali.movix.features.movies.presentation.components.EmptyView
import com.karimali.movix.features.movies.presentation.components.ErrorView
import com.karimali.movix.features.movies.presentation.components.LoadingView
import com.karimali.movix.features.movies.presentation.components.MoviesGrid
import com.karimali.movix.features.movies.presentation.components.OfflineBanner
import com.karimali.movix.features.movies.presentation.components.SearchField
import com.karimali.movix.features.movies.presentation.contract.MoviesIntent
import com.karimali.movix.features.movies.presentation.contract.MoviesState

@Composable
fun MoviesScreen(
    state: MoviesState,
    movies: LazyPagingItems<Movie>,
    onIntent: (MoviesIntent) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SearchField(
                query = state.query,
                onQueryChange = {
                    onIntent(MoviesIntent.QueryChanged(it))
                },
                onClear = {
                    onIntent(MoviesIntent.ClearQuery)
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
            )
        },
    ) { innerPadding ->
        val refresh = movies.loadState.refresh
        val isEmpty = movies.itemCount == 0

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isEmpty && refresh is LoadState.Loading -> {
                    LoadingView()
                }

                isEmpty && refresh is LoadState.Error -> {
                    ErrorView(
                        error = refresh.error.asDomainError(),
                        onRetry = movies::retry,
                    )
                }

                isEmpty && refresh is LoadState.NotLoading -> {
                    EmptyView(state.query)
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Offline-first: keep cached movies visible when refresh fails.
                        if (refresh is LoadState.Error) {
                            OfflineBanner(
                                error = refresh.error.asDomainError(),
                                onRetry = movies::retry
                            )
                        }

                        MoviesGrid(
                            movies = movies,
                            onMovieClick = {
                                onIntent(MoviesIntent.MovieClicked(it))
                            },
                        )
                    }
                }
            }
        }
    }
}
