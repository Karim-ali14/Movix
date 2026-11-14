package com.karimali.movix.features.details.data.mapper

import com.karimali.movix.core.network.AppRouter
import com.karimali.movix.core.network.AppRouter.IMDB_TITLE_URL
import com.karimali.movix.features.details.data.datasource.local.entity.MovieDetailsEntity
import com.karimali.movix.features.details.data.datasource.remote.dto.MovieDetailsDto
import com.karimali.movix.features.details.domain.model.MovieDetails

private fun String?.nullIfBlank(): String? = this?.takeIf { it.isNotBlank() }

fun MovieDetailsDto.toEntityOrNull(): MovieDetailsEntity? {
    val movieId = id ?: return null
    val movieTitle = title.nullIfBlank() ?: return null
    return MovieDetailsEntity(
        id = movieId,
        title = movieTitle,
        originalTitle = originalTitle.nullIfBlank(),
        tagline = tagline.nullIfBlank(),
        overview = overview.nullIfBlank(),
        posterPath = posterPath.nullIfBlank(),
        backdropPath = backdropPath.nullIfBlank(),
        releaseDate = releaseDate.nullIfBlank(),
        runtime = runtime?.takeIf { it > 0 },
        voteAverage = voteAverage,
        voteCount = voteCount,
        status = status.nullIfBlank(),
        originalLanguage = originalLanguage.nullIfBlank(),
        budget = budget?.takeIf { it > 0 },
        revenue = revenue?.takeIf { it > 0 },
        homepage = homepage.nullIfBlank(),
        imdbId = imdbId.nullIfBlank(),
        collectionName = collection?.name.nullIfBlank(),
        genres = genres.orEmpty().mapNotNull { it.name.nullIfBlank() },
        productionCompanies = productionCompanies.orEmpty().mapNotNull { it.name.nullIfBlank() },
        spokenLanguages = spokenLanguages.orEmpty().mapNotNull { it.englishName.nullIfBlank() },
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
    rating = voteAverage,
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