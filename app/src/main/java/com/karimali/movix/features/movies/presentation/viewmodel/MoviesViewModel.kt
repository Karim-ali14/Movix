package com.karimali.movix.features.movies.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.karimali.movix.core.mvi.MviViewModel
import com.karimali.movix.features.movies.domain.model.Movie
import com.karimali.movix.features.movies.domain.usecase.FetchMoviesUseCase
import com.karimali.movix.features.movies.domain.usecase.SearchMoviesUseCase
import com.karimali.movix.features.movies.presentation.contract.MoviesEffect
import com.karimali.movix.features.movies.presentation.contract.MoviesIntent
import com.karimali.movix.features.movies.presentation.contract.MoviesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val fetchMovies: FetchMoviesUseCase,
    private val searchMovies: SearchMoviesUseCase,
) : MviViewModel<MoviesState, MoviesIntent, MoviesEffect>(MoviesState()) {

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val movies: Flow<PagingData<Movie>> = state
        .map { it.query.trim() }
        .distinctUntilChanged()
        // Typing waits 400ms; clearing the box shows the cached list immediately.
        .debounce { query -> if (query.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }
        .flatMapLatest { query ->                       // cancels the previous search
            if (query.isEmpty()) fetchMovies() else searchMovies(query)
        }
        .cachedIn(viewModelScope)

    override fun onIntent(intent: MoviesIntent) {
        when (intent) {
            is MoviesIntent.QueryChanged -> setState { copy(query = intent.query) }
            MoviesIntent.ClearQuery -> setState { copy(query = "") }
            is MoviesIntent.MovieClicked ->
                sendEffect(MoviesEffect.NavigateToDetails(intent.movieId))
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}