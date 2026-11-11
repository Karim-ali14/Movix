package com.karimali.movix.features.movies.data.datasource.local

import androidx.room.TypeConverter

class MoviesTypeConverters {

    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        if (value.isNullOrBlank()) return emptyList()

        return value.split(",")
            .mapNotNull { it.toIntOrNull() }
    }
}