package com.karimali.movix.features.movies.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.karimali.movix.core.navigation.Screens
import com.karimali.movix.features.details.presentation.screen.MovieDetailsRoute
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
            MovieDetailsRoute(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}