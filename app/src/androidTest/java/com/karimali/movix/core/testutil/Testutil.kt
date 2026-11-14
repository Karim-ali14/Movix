package com.karimali.movix.core.testutil

import com.karimali.movix.core.error.DomainError
import com.karimali.movix.core.ui.asDomainError
import com.karimali.movix.features.details.domain.model.MovieDetails
import com.karimali.movix.features.movies.domain.model.Movie
import java.io.IOException

const val MOVIE_ID = 11
const val HOMEPAGE = "http://www.starwars.com/films/star-wars-episode-iv-a-new-hope"
const val IMDB_URL = "https://www.imdb.com/title/tt0076759"

/** Built through the real mapper, so the tests don't depend on DomainError's subclasses. */
fun someDomainError(): DomainError = IOException("offline").asDomainError()

fun movieDetails(
    id: Int = MOVIE_ID,
    title: String = "Star Wars",
    originalTitle: String = title,
    tagline: String = "A long time ago in a galaxy far, far away...",
    overview: String = "Princess Leia is captured and held hostage.",
    releaseDate: String? = "1977-05-25",
    runtimeMinutes: Int? = 121,
    rating: Double? = 8.2,
    voteCount: Int = 22_061,
    status: String = "Released",
    originalLanguageCode: String = "en",
    budget: Long? = 11_000_000L,
    revenue: Long? = 775_398_007L,
    homepage: String? = HOMEPAGE,
    imdbUrl: String? = IMDB_URL,
    collectionName: String? = "Star Wars Collection",
    genres: List<String> = listOf("Adventure", "Action", "Science Fiction"),
    productionCompanies: List<String> = listOf("Lucasfilm Ltd.", "20th Century Fox"),
    // Not just "English": that word is already the "Original language" tile.
    spokenLanguages: List<String> = listOf("English", "Spanish"),
) = MovieDetails(
    id = id,
    title = title,
    originalTitle = originalTitle,
    tagline = tagline,
    overview = overview,
    posterUrl = null, // null on purpose: no network image loading in UI tests
    backdropUrl = null,
    releaseDate = releaseDate,
    runtimeMinutes = runtimeMinutes,
    rating = rating,
    voteCount = voteCount,
    status = status,
    originalLanguageCode = originalLanguageCode,
    budget = budget,
    revenue = revenue,
    homepage = homepage,
    imdbUrl = imdbUrl,
    collectionName = collectionName,
    genres = genres,
    productionCompanies = productionCompanies,
    spokenLanguages = spokenLanguages,
)

fun movie(
    id: Int = 1,
    title: String = "Movie $id",
    posterUrl: String? = null,
    releaseDate: String? = "2024-05-01",
) = Movie(id = id, title = title, posterUrl = posterUrl, releaseDate = releaseDate)