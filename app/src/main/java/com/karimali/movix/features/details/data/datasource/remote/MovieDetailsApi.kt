package com.karimali.movix.features.details.data.datasource.remote

import com.karimali.movix.core.network.AppRouter
import com.karimali.movix.features.details.data.datasource.remote.dto.MovieDetailsDto
import retrofit2.http.GET
import retrofit2.http.Path

interface MovieDetailsApi {
    @GET(AppRouter.MOVIE_DETAILS)
    suspend fun fetchMovieDetails(@Path("movie_id") movieId: Int): MovieDetailsDto
}