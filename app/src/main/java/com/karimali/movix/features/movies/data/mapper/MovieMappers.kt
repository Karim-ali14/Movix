package com.karimali.movix.features.movies.data.mapper

import com.karimali.movix.core.network.AppRouter
import com.karimali.movix.features.movies.data.datasource.local.entity.MovieEntity
import com.karimali.movix.features.movies.data.datasource.remote.dto.MovieDto
import com.karimali.movix.features.movies.domain.model.Movie

fun MovieDto.toEntityOrNull(page: Int, position: Int): MovieEntity? {
    val movieId = id ?: return null
    val movieTitle = title?.takeIf { it.isNotBlank() } ?: return null
    return MovieEntity(
        id = movieId,
        title = movieTitle,
        overview = this.overview,
        posterPath = this.posterPath,
        backdropPath = this.backdropPath,
        releaseDate = this.releaseDate,
        voteAverage = this.voteAverage,
        voteCount = this.voteCount,
        genreIds = this.genreIds,
        page = page,
        position = position
    )
}

fun MovieEntity.toDomain(): Movie {
    return Movie(
        id = this.id,
        title = this.title ?: "Unknown Title",
        posterUrl = this.posterPath?.let { "${AppRouter.POSTER_BASE_URL}$it" },
        releaseDate = this.releaseDate ?: ""
    )
}

fun MovieDto.toDomainOrNull(): Movie? {
    val movieId = id ?: return null
    val movieTitle = title?.takeIf { it.isNotBlank() } ?: return null
    return Movie(
        id = movieId,
        title = movieTitle,
        posterUrl = posterPath?.let { "${AppRouter.POSTER_BASE_URL}$it" },
        releaseDate = this.releaseDate ?: ""
    )
}