package com.karimali.movix.features.movies.navigation

import kotlinx.serialization.Serializable

sealed class Screens {
    @Serializable
    object MoviesRoute : Screens()

    @Serializable
    data class MovieDetailRoute(
        val movieId: Int
    ) : Screens()
}