package com.karimali.movix.testutil

import com.karimali.movix.features.movies.data.datasource.local.entity.MovieEntity
import com.karimali.movix.features.movies.data.datasource.remote.dto.MovieDto
import com.karimali.movix.features.movies.domain.model.Movie

fun movieDto(
    id: Int? = 1,
    title: String? = "Movie $id",
    adult : Boolean? = false,
    posterPath: String? = "/poster.jpg",
    releaseDate: String? = "2024-05-17",
    genreIds: List<Int>? = listOf(1, 2)
) = MovieDto(
    adult = adult,
    backdropPath = null,
    genreIds = genreIds,
    id = id,
    originalLanguage = "en",
    originalTitle = title,
    overview = "overview",
    popularity = 1.0,
    posterPath = posterPath,
    releaseDate = releaseDate,
    title = title,
    video = false,
    voteAverage = 7.5,
    voteCount = 10,
)


fun movieEntity(
    id: Int = 1,
    title: String = "Movie $id",
    page: Int = 1,
    position: Int = 0,
) = MovieEntity(
    id = id,
    title = title,
    overview = "Overview",
    posterPath = "/path.jpg",
    backdropPath = null,
    releaseDate = "2026-01-01",
    voteAverage = 8.0,
    voteCount = 100,
    genreIds = listOf(28),
    page = page,
    position = position
)

fun movie(
    id: Int = 1,
    title: String = "Movie $id",
    posterUrl: String? = "https://image.tmdb.org/t/p/w500/poster.jpg",
    releaseDate: String? = "2024-05-01",
) = Movie(id = id, title = title, posterUrl = posterUrl, releaseDate = releaseDate)