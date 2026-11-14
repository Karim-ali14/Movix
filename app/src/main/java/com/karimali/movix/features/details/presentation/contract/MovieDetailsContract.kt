package com.karimali.movix.features.details.presentation.contract

import com.karimali.movix.core.error.DomainError
import com.karimali.movix.core.mvi.UiEffect
import com.karimali.movix.core.mvi.UiIntent
import com.karimali.movix.core.mvi.UiState
import com.karimali.movix.features.details.domain.model.MovieDetails

data class MovieDetailsUiState(
    val movie: MovieDetails? = null,
    /** True while a background refresh runs (cached content may already be visible). */
    val isRefreshing: Boolean = false,
    /**
     * Last refresh failure. The screen decides how to show it:
     * full-screen error when [movie] is null, offline banner otherwise.
     */
    val error: DomainError? = null,
) : UiState

sealed interface MovieDetailsIntent : UiIntent {
    data object Retry : MovieDetailsIntent
    data object BackClicked : MovieDetailsIntent
    data object WebsiteClicked : MovieDetailsIntent
    data object ImdbClicked : MovieDetailsIntent
}

sealed interface MovieDetailsEffect : UiEffect {
    data class OpenUrl(val url: String) : MovieDetailsEffect
    data object NavigateBack : MovieDetailsEffect
}