package com.karimali.movix.features.movies.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.karimali.movix.features.movies.presentation.contract.MoviesEffect
import com.karimali.movix.features.movies.presentation.viewmodel.MoviesViewModel

@Composable
fun MoviesRoute(
    onMovieClick: (Int) -> Unit,
    viewModel: MoviesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val currentOnMovieClick by rememberUpdatedState(onMovieClick)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MoviesEffect.NavigateToDetails -> currentOnMovieClick(effect.movieId)
            }
        }
    }

    MoviesScreen(state = state, movies = movies, onIntent = viewModel::onIntent)
}