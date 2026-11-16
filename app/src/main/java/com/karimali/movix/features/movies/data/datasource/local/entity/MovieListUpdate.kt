package com.karimali.movix.features.movies.data.datasource.local.entity

/**
 * The columns of [MovieEntity] that a fresh server copy of a movie may change.
 * Used as a partial update so the movie keeps its `page` and `position` in the list.
 */
data class MovieListUpdate(
    val id: Int,
    val title: String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double?,
    val voteCount: Int?,
)