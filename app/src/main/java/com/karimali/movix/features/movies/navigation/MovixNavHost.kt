package com.karimali.movix.features.movies.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.karimali.movix.features.movies.presentation.screen.MoviesRoute

@Composable
fun MovixNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screens.MoviesRoute
    ) {

        composable<Screens.MoviesRoute> {
            MoviesRoute(
                onMovieClick = { movieId ->
                    navController.navigate(Screens.MovieDetailRoute(movieId = movieId))
                }
            )
        }

        composable<Screens.MovieDetailRoute> { backStackEntry ->
            val detailRoute = backStackEntry.toRoute<Screens.MovieDetailRoute>()
            val movieId = detailRoute.movieId


        }
    }
}