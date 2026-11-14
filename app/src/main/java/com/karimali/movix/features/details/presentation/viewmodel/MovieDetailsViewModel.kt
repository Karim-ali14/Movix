package com.karimali.movix.features.details.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.karimali.movix.core.mvi.MviViewModel
import com.karimali.movix.core.ui.asDomainError
import com.karimali.movix.features.details.domain.usecase.ObserveMovieDetailsUseCase
import com.karimali.movix.features.details.domain.usecase.RefreshMovieDetailsUseCase
import com.karimali.movix.features.details.presentation.contract.MovieDetailsEffect
import com.karimali.movix.features.details.presentation.contract.MovieDetailsIntent
import com.karimali.movix.features.details.presentation.contract.MovieDetailsUiState
import com.karimali.movix.features.movies.navigation.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeMovieDetails: ObserveMovieDetailsUseCase,
    private val refreshMovieDetails: RefreshMovieDetailsUseCase,
) : MviViewModel<MovieDetailsUiState, MovieDetailsIntent, MovieDetailsEffect>(
    MovieDetailsUiState()
) {

    private val movieId: Int =
        savedStateHandle.toRoute<Screens.MovieDetailRoute>().movieId

    private var refreshJob: Job? = null

    init {
        observeCache()
        refresh()
    }

    override fun onIntent(intent: MovieDetailsIntent) {
        when (intent) {
            MovieDetailsIntent.Retry -> refresh()
            MovieDetailsIntent.BackClicked -> sendEffect(MovieDetailsEffect.NavigateBack)
            MovieDetailsIntent.WebsiteClicked -> openUrl(state.value.movie?.homepage)
            MovieDetailsIntent.ImdbClicked -> openUrl(state.value.movie?.imdbUrl)
        }
    }

    /** Room is the single source of truth: any cache write re-emits here. */
    private fun observeCache() {
        observeMovieDetails(movieId)
            .filterNotNull()
            .onEach { movie -> setState { copy(movie = movie) } }
            .launchIn(viewModelScope)
    }

    private fun refresh() {
        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {
            setState { copy(isRefreshing = true, error = null) }

            refreshMovieDetails(movieId)
                .onSuccess { setState { copy(isRefreshing = false) } }
                .onFailure { handleRefreshFailure(it) }
        }
    }

    private suspend fun handleRefreshFailure(error: Throwable) {
        // The cache collector may not have emitted yet (fast offline failure),
        // so read the DB directly to avoid flashing a full-screen error over cached data.
        val cached = state.value.movie ?: observeMovieDetails(movieId).first()

        setState {
            copy(
                movie = cached ?: movie,
                isRefreshing = false,
                error = error.asDomainError(),
            )
        }
    }

    private fun openUrl(url: String?) {
        if (url.isNullOrBlank()) return
        sendEffect(MovieDetailsEffect.OpenUrl(url))
    }
}

