package com.karimali.movix.features.details.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

data class GenreDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
)