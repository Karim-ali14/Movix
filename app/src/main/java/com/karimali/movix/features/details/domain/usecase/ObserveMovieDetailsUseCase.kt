package com.karimali.movix.features.details.domain.usecase

import com.karimali.movix.features.details.domain.model.MovieDetails
import com.karimali.movix.features.details.domain.repository.MovieDetailsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMovieDetailsUseCase @Inject constructor(
    private val repository: MovieDetailsRepository,
) {
    operator fun invoke(movieId: Int): Flow<MovieDetails?> =
        repository.observeMovieDetails(movieId)
}