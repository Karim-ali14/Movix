package com.karimali.movix.features.movies.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movies",
    indices = [Index(value = ["page", "position"])],
)
data class MovieEntity(
    @PrimaryKey
    val id: Int,
    val title: String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double?,
    val voteCount: Int?,
    val genreIds: List<Int>?,
    val page: Int,
    val position: Int
)