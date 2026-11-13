package com.karimali.movix.features.movies.data.datasource.remote

import javax.inject.Inject

class MoviesRemoteDataSource @Inject constructor(
    private val api: MoviesApi
) {
    suspend fun fetchMovies(page: Int) = api.fetchMovies(page)
    suspend fun searchMovies(query: String, page: Int) = api.searchMovies(query, page)
}