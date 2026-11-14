package com.karimali.movix.features.details.domain.model

data class MovieDetails(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val tagline: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String?,
    val runtimeMinutes: Int?,
    val rating: Double?,
    val voteCount: Int,
    val status: String,
    val originalLanguageCode: String,
    val budget: Long?,
    val revenue: Long?,
    val homepage: String?,
    val imdbUrl: String?,
    val collectionName: String?,
    val genres: List<String>,
    val productionCompanies: List<String>,
    val spokenLanguages: List<String>,
) {
    val releaseYear: String?
        get() = releaseDate?.take(4)?.takeIf { y -> y.length == 4 && y.all(Char::isDigit) }
}