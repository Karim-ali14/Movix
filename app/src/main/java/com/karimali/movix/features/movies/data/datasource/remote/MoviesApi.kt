package com.karimali.movix.features.movies.data.datasource.remote

import com.karimali.movix.core.network.AppRouter
import com.karimali.movix.features.movies.data.datasource.remote.dto.MoviesResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MoviesApi {
    @GET(AppRouter.DISCOVER_MOVIES)
    suspend fun fetchMovies(
        @Query("page") page: Int
    ): MoviesResponseDto

    @GET(AppRouter.SEARCH_MOVIES)
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int
    ): MoviesResponseDto
}