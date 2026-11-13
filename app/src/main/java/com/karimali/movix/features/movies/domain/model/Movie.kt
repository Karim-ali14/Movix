package com.karimali.movix.features.movies.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String? = "" ,
    val releaseDate: String? = "",
) {
    val releaseYear: String?
        get() = releaseDate?.take(4)?.takeIf { y ->
            y.length == 4 && y.all(Char::isDigit)
        }
}