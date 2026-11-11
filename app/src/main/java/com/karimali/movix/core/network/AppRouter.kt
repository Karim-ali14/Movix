package com.karimali.movix.core.network

object AppRouter {
    const val BASE_URL = "https://api.themoviedb.org/3/"
    const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"

    const val DISCOVER_MOVIES = "discover/movie"
    const val SEARCH_MOVIES = "search/movie"
    const val MOVIE_DETAILS = "movie/{movie_id}"
}