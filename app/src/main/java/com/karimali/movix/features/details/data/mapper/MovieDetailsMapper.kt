package com.karimali.movix.features.details.data.mapper

import com.karimali.movix.core.network.AppRouter
import com.karimali.movix.core.network.AppRouter.IMDB_TITLE_URL
import com.karimali.movix.features.details.data.datasource.local.entity.MovieDetailsEntity
import com.karimali.movix.features.details.data.datasource.remote.dto.MovieDetailsDto
import com.karimali.movix.features.details.domain.model.MovieDetails


fun MovieDetailsDto.toEntityOrNull(): MovieDetailsEntity? {
    val movieId = id ?: return null
    val movieTitle = title?.takeIf { it.isNotBlank() } ?: return null
    return MovieDetailsEntity(
        id = movieId,
        title = movieTitle,
        originalTitle = originalTitle?.takeIf { it.isNotBlank() },
        tagline = tagline?.takeIf { it.isNotBlank() },
        overview = overview?.takeIf { it.isNotBlank() },
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate?.takeIf { it.isNotBlank() },
        runtime = runtime?.takeIf { it > 0 },
        voteAverage = voteAverage,
        voteCount = voteCount,
        status = status,
        originalLanguage = originalLanguage,
        budget = budget?.takeIf { it > 0 },
        revenue = revenue?.takeIf { it > 0 },
        homepage = homepage?.takeIf { it.isNotBlank() },
        imdbId = imdbId?.takeIf { it.isNotBlank() },
        collectionName = collection?.name?.takeIf { it.isNotBlank() },
        genres = genres.orEmpty().mapNotNull { it.name?.takeIf(String::isNotBlank) },
        productionCompanies = productionCompanies.orEmpty()
            .mapNotNull { it.name?.takeIf(String::isNotBlank) },
        spokenLanguages = spokenLanguages.orEmpty()
            .mapNotNull { it.englishName?.takeIf(String::isNotBlank) },
    )
}

fun MovieDetailsEntity.toDomain(): MovieDetails = MovieDetails(
    id = id,
    title = title,
    originalTitle = originalTitle ?: title,
    tagline = tagline.orEmpty(),
    overview = overview.orEmpty(),
    posterUrl = posterPath?.let { "${AppRouter.POSTER_BASE_URL}$it" },
    backdropUrl = backdropPath?.let { "${AppRouter.BACKDROP_BASE_URL}$it" },
    releaseDate = releaseDate.orEmpty(),
    runtimeMinutes = runtime,
    rating = voteAverage ?: 0.0,
    voteCount = voteCount ?: 0,
    status = status.orEmpty(),
    originalLanguageCode = originalLanguage.orEmpty(),
    budget = budget ?: 0L,
    revenue = revenue ?: 0L,
    homepage = homepage,
    imdbUrl = imdbId?.let { "$IMDB_TITLE_URL$it" },
    collectionName = collectionName,
    genres = genres,
    productionCompanies = productionCompanies,
    spokenLanguages = spokenLanguages,
)