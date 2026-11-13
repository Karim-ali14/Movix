package com.karimali.movix.features.details.data.repository

import com.karimali.movix.core.network.toDomainException
import com.karimali.movix.features.details.data.datasource.local.MovieDetailsLocalDataSource
import com.karimali.movix.features.details.data.datasource.remote.MovieDetailsRemoteDataSource
import com.karimali.movix.features.details.data.mapper.toDomain
import com.karimali.movix.features.details.data.mapper.toEntityOrNull
import com.karimali.movix.features.details.domain.model.MovieDetails
import com.karimali.movix.features.details.domain.repository.MovieDetailsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class MovieDetailsRepositoryImpl @Inject constructor(
    private val remote: MovieDetailsRemoteDataSource,
    private val local: MovieDetailsLocalDataSource,
) : MovieDetailsRepository {

    override fun observeMovieDetails(movieId: Int): Flow<MovieDetails?> =
        local.observe(movieId)
            .map { it?.toDomain() }
            .distinctUntilChanged()

    override suspend fun refreshMovieDetails(movieId: Int) {
        try {
            val entity = remote.fetchMovieDetails(movieId).toEntityOrNull()
                ?: throw IllegalStateException("Invalid movie details payload for id $movieId")
            local.save(entity)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e.toDomainException()
        }
    }
}