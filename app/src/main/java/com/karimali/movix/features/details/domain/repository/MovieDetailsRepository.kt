package com.karimali.movix.features.details.domain.repository

import com.karimali.movix.features.details.domain.model.MovieDetails
import kotlinx.coroutines.flow.Flow

interface MovieDetailsRepository {
    /** Emits the cached details (or null if none) and re-emits whenever the cache changes. */
    fun observeMovieDetails(movieId: Int): Flow<MovieDetails?>

    /** Fetches the latest details and writes them to the cache. Throws a domain exception on failure. */
    suspend fun refreshMovieDetails(movieId: Int)
}