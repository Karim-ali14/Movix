package com.karimali.movix.features.details.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

data class SpokenLanguageDto(
    @SerializedName("iso_639_1") val code: String?,
    @SerializedName("english_name") val englishName: String?,
)