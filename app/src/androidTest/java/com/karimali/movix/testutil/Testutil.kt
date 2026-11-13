package com.karimali.movix.testutil

import com.karimali.movix.features.movies.domain.model.Movie

fun movie(
    id: Int = 1,
    title: String = "Movie $id",
    posterUrl: String? = null,
    releaseDate: String? = "2024-05-01",
) = Movie(id = id, title = title, posterUrl = posterUrl, releaseDate = releaseDate)