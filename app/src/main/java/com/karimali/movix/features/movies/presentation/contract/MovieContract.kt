package com.karimali.movix.features.movies.presentation.contract

import com.karimali.movix.core.mvi.UiEffect
import com.karimali.movix.core.mvi.UiIntent
import com.karimali.movix.core.mvi.UiState

data class MoviesState(
    val query: String = "",
) : UiState

sealed interface MoviesIntent : UiIntent {
    data class QueryChanged(val query: String) : MoviesIntent   // [search]
    data object ClearQuery : MoviesIntent                       // [search]
    data class MovieClicked(val movieId: Int) : MoviesIntent
}

sealed interface MoviesEffect : UiEffect {
    data class NavigateToDetails(val movieId: Int) : MoviesEffect
}