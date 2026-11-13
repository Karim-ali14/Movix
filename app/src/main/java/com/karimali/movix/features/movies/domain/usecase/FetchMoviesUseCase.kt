package com.karimali.movix.features.movies.domain.usecase

import androidx.paging.PagingData
import com.karimali.movix.features.movies.domain.model.Movie
import com.karimali.movix.features.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchMoviesUseCase @Inject constructor(
    private val repository: MoviesRepository
) {
    operator fun invoke(): Flow<PagingData<Movie>> = repository.fetchMovies()
}