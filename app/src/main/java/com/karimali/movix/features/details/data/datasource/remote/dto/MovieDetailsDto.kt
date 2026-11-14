package com.karimali.movix.features.details.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

data class MovieDetailsDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("original_title") val originalTitle: String?,
    @SerializedName("tagline") val tagline: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("runtime") val runtime: Int?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("vote_count") val voteCount: Int?,
    @SerializedName("status") val status: String?,
    @SerializedName("original_language") val originalLanguage: String?,
    @SerializedName("budget") val budget: Long?,
    @SerializedName("revenue") val revenue: Long?,
    @SerializedName("homepage") val homepage: String?,
    @SerializedName("imdb_id") val imdbId: String?,
    @SerializedName("genres") val genres: List<GenreDto>?,
    @SerializedName("production_companies") val productionCompanies: List<ProductionCompanyDto>?,
    @SerializedName("spoken_languages") val spokenLanguages: List<SpokenLanguageDto>?,
    @SerializedName("belongs_to_collection") val collection: CollectionDto?,
)