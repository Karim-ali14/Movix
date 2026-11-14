package com.karimali.movix.features.movies.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.karimali.movix.core.ui.asDomainError
import com.karimali.movix.core.ui.components.FooterError
import com.karimali.movix.core.ui.components.FooterLoading
import com.karimali.movix.core.ui.theme.AppSpacing
import com.karimali.movix.features.movies.domain.model.Movie

@Composable
internal fun MoviesGrid(
    movies: LazyPagingItems<Movie>,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            count = movies.itemCount,
            key = movies.itemKey { it.id },
            contentType = movies.itemContentType { "movie" },
        ) { index ->
            movies[index]?.let { movie ->
                MovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
            }
        }

        when (val append = movies.loadState.append) {
            LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) { FooterLoading() }
            is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                FooterError(error = append.error.asDomainError(), onRetry = movies::retry)
            }
            else -> Unit
        }
    }
}