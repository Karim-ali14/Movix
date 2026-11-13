package com.karimali.movix.features.movies.domain.repository

import androidx.paging.PagingData
import com.karimali.movix.features.movies.domain.model.Movie
import kotlinx.coroutines.flow.Flow


interface MoviesRepository {
    fun fetchMovies(): Flow<PagingData<Movie>>
    fun searchMovies(
        query: String,
    ): Flow<PagingData<Movie>>
}