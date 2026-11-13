package com.karimali.movix.features.details.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_details")
data class MovieDetailsEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val originalTitle: String?,
    val tagline: String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val runtime: Int?,
    val voteAverage: Double?,
    val voteCount: Int?,
    val status: String?,
    val originalLanguage: String?,
    val budget: Long?,
    val revenue: Long?,
    val homepage: String?,
    val imdbId: String?,
    val collectionName: String?,
    val genres: List<String>,
    val productionCompanies: List<String>,
    val spokenLanguages: List<String>,
)