package com.karimali.movix.features.movies.domain.usecase

import androidx.paging.PagingData
import com.karimali.movix.features.movies.domain.model.Movie
import com.karimali.movix.features.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchMoviesUseCase @Inject constructor(
    private val repository: MoviesRepository
) {
    operator fun invoke(query: String): Flow<PagingData<Movie>> =
        repository.searchMovies(query.trim())
}