package com.karimali.movix.features.details.data.datasource.remote

import javax.inject.Inject

class MovieDetailsRemoteDataSource @Inject constructor(
    private val api: MovieDetailsApi,
) {
    suspend fun fetchMovieDetails(movieId: Int) = api.fetchMovieDetails(movieId)
}