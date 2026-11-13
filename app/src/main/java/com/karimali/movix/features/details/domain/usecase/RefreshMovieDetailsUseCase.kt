package com.karimali.movix.features.details.domain.usecase

import com.karimali.movix.features.details.domain.repository.MovieDetailsRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class RefreshMovieDetailsUseCase @Inject constructor(
    private val repository: MovieDetailsRepository,
) {
    suspend operator fun invoke(movieId: Int): Result<Unit> = try {
        repository.refreshMovieDetails(movieId)
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}