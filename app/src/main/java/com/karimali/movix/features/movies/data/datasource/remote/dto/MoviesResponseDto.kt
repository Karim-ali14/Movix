package com.karimali.movix.features.movies.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

data class MoviesResponseDto(
    @SerializedName("page") val page: Int?,
    @SerializedName("results") val results: List<MovieDto>?,
    @SerializedName("total_pages") val totalPages: Int?,
    @SerializedName("total_results") val totalResults: Int?,
)