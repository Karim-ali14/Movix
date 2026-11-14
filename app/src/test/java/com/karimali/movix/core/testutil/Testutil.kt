package com.karimali.movix.core.testutil

import com.karimali.movix.features.details.data.datasource.local.entity.MovieDetailsEntity
import com.karimali.movix.features.details.data.datasource.remote.dto.CollectionDto
import com.karimali.movix.features.details.data.datasource.remote.dto.GenreDto
import com.karimali.movix.features.details.data.datasource.remote.dto.MovieDetailsDto
import com.karimali.movix.features.details.data.datasource.remote.dto.ProductionCompanyDto
import com.karimali.movix.features.details.data.datasource.remote.dto.SpokenLanguageDto
import com.karimali.movix.features.details.domain.model.MovieDetails
import com.karimali.movix.features.movies.data.datasource.local.entity.MovieEntity
import com.karimali.movix.features.movies.data.datasource.remote.dto.MovieDto
import com.karimali.movix.features.movies.domain.model.Movie
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

// ============================================================================
// CONSTANTS & JSON FIXTURES
// ============================================================================

const val MOVIE_ID = 11
const val POSTER_PATH = "/6FfCtAuVAW8XJjZ7eWeLibRLWTw.jpg"
const val BACKDROP_PATH = "/2w4xG178RpB4MDAIfTkqAuSJzec.jpg"
const val HOMEPAGE = "http://www.starwars.com/films/star-wars-episode-iv-a-new-hope"
const val STAR_WARS_TAGLINE = "A long time ago in a galaxy far, far away..."
const val STAR_WARS_OVERVIEW =
    "Princess Leia is captured and held hostage by the evil Imperial forces in their effort " +
            "to take over the galactic Empire."

/** Real TMDb response shape for Star Wars. Mirrors [movieDetailsDto] defaults exactly. */
val STAR_WARS_JSON = """
{
  "adult": false,
  "backdrop_path": "$BACKDROP_PATH",
  "belongs_to_collection": {
    "id": 10,
    "name": "Star Wars Collection",
    "poster_path": "/pWVLFh4OuejTpUaDQbB1C4zoS2p.jpg",
    "backdrop_path": "/iY2ujEY2m68OTTlPFTiHub9joHS.jpg"
  },
  "budget": 11000000,
  "genres": [
    { "id": 12, "name": "Adventure" },
    { "id": 28, "name": "Action" },
    { "id": 878, "name": "Science Fiction" }
  ],
  "homepage": "$HOMEPAGE",
  "id": 11,
  "imdb_id": "tt0076759",
  "origin_country": ["US"],
  "original_language": "en",
  "original_title": "Star Wars",
  "overview": "$STAR_WARS_OVERVIEW",
  "popularity": 20.6912,
  "poster_path": "$POSTER_PATH",
  "production_companies": [
    { "id": 1, "logo_path": "/tlVSws0RvvtPBwViUyOFAO0vcQS.png", "name": "Lucasfilm Ltd.", "origin_country": "US" },
    { "id": 25, "logo_path": "/qZCc1lty5FzX30aOCVRBLzaVmcp.png", "name": "20th Century Fox", "origin_country": "US" }
  ],
  "production_countries": [{ "iso_3166_1": "US", "name": "United States of America" }],
  "release_date": "1977-05-25",
  "revenue": 775398007,
  "runtime": 121,
  "spoken_languages": [{ "english_name": "English", "iso_639_1": "en", "name": "English" }],
  "status": "Released",
  "tagline": "$STAR_WARS_TAGLINE",
  "title": "Star Wars",
  "video": false,
  "vote_average": 8.2,
  "vote_count": 22061
}
""".trimIndent()

// ============================================================================
// MOVIES FEATURE FIXTURES
// ============================================================================

fun movieDto(
    id: Int? = 1,
    title: String? = "Movie $id",
    adult: Boolean? = false,
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

// ============================================================================
// MOVIE DETAILS FEATURE FIXTURES
// ============================================================================

fun movieDetails(
    id: Int = 1,
    title: String = "Batman",
    originalTitle: String = "Batman",
    imdbUrl: String? = null,
    homepage: String? = null,
    releaseDate: String? = "2024-05-10",
    ): MovieDetails {
    return MovieDetails(
        id = id,
        title = title,
        originalTitle = originalTitle,
        tagline = "",
        overview = "",
        posterUrl = null,
        backdropUrl = null,
        releaseDate = releaseDate,
        runtimeMinutes = null,
        rating = null,
        voteCount = 0,
        status = "",
        originalLanguageCode = "",
        budget = null,
        revenue = null,
        homepage = null,
        imdbUrl = null,
        collectionName = null,
        genres = emptyList(),
        productionCompanies = emptyList(),
        spokenLanguages = emptyList(),
    )
}

fun movieDetailsDto(
    id: Int? = MOVIE_ID,
    title: String? = "Star Wars",
    originalTitle: String? = "Star Wars",
    tagline: String? = STAR_WARS_TAGLINE,
    overview: String? = STAR_WARS_OVERVIEW,
    posterPath: String? = POSTER_PATH,
    backdropPath: String? = BACKDROP_PATH,
    releaseDate: String? = "1977-05-25",
    runtime: Int? = 121,
    voteAverage: Double? = 8.2,
    voteCount: Int? = 22_061,
    status: String? = "Released",
    originalLanguage: String? = "en",
    budget: Long? = 11_000_000L,
    revenue: Long? = 775_398_007L,
    homepage: String? = HOMEPAGE,
    imdbId: String? = "tt0076759",
    genres: List<GenreDto>? = listOf(
        GenreDto(12, "Adventure"),
        GenreDto(28, "Action"),
        GenreDto(878, "Science Fiction"),
    ),
    productionCompanies: List<ProductionCompanyDto>? = listOf(
        ProductionCompanyDto(1, "Lucasfilm Ltd."),
        ProductionCompanyDto(25, "20th Century Fox"),
    ),
    spokenLanguages: List<SpokenLanguageDto>? = listOf(SpokenLanguageDto("en", "English")),
    collection: CollectionDto? = CollectionDto(10, "Star Wars Collection"),
) = MovieDetailsDto(
    id = id,
    title = title,
    originalTitle = originalTitle,
    tagline = tagline,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    runtime = runtime,
    voteAverage = voteAverage,
    voteCount = voteCount,
    status = status,
    originalLanguage = originalLanguage,
    budget = budget,
    revenue = revenue,
    homepage = homepage,
    imdbId = imdbId,
    genres = genres,
    productionCompanies = productionCompanies,
    spokenLanguages = spokenLanguages,
    collection = collection,
)

fun movieDetailsEntity(
    id: Int = MOVIE_ID,
    title: String = "Star Wars",
    originalTitle: String? = "Star Wars",
    tagline: String? = STAR_WARS_TAGLINE,
    overview: String? = STAR_WARS_OVERVIEW,
    posterPath: String? = POSTER_PATH,
    backdropPath: String? = BACKDROP_PATH,
    releaseDate: String? = "1977-05-25",
    runtime: Int? = 121,
    voteAverage: Double? = 8.2,
    voteCount: Int? = 22_061,
    status: String? = "Released",
    originalLanguage: String? = "en",
    budget: Long? = 11_000_000L,
    revenue: Long? = 775_398_007L,
    homepage: String? = HOMEPAGE,
    imdbId: String? = "tt0076759",
    collectionName: String? = "Star Wars Collection",
    genres: List<String> = listOf("Adventure", "Action", "Science Fiction"),
    productionCompanies: List<String> = listOf("Lucasfilm Ltd.", "20th Century Fox"),
    spokenLanguages: List<String> = listOf("English"),
) = MovieDetailsEntity(
    id = id,
    title = title,
    originalTitle = originalTitle,
    tagline = tagline,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    runtime = runtime,
    voteAverage = voteAverage,
    voteCount = voteCount,
    status = status,
    originalLanguage = originalLanguage,
    budget = budget,
    revenue = revenue,
    homepage = homepage,
    imdbId = imdbId,
    collectionName = collectionName,
    genres = genres,
    productionCompanies = productionCompanies,
    spokenLanguages = spokenLanguages,
)

// ============================================================================
// NETWORK / EXCEPTION HELPERS
// ============================================================================

fun httpException(code: Int): HttpException = HttpException(
    Response.error<Any>(code, "{}".toResponseBody("application/json".toMediaType())),
)