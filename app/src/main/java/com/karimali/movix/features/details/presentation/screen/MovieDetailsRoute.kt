package com.karimali.movix.features.details.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.karimali.movix.features.details.presentation.contract.MovieDetailsEffect
import com.karimali.movix.features.details.presentation.viewmodel.MovieDetailsViewModel

/**
 * Stateful entry point: owns the ViewModel and turns one-shot effects into navigation / system calls.
 * All the drawing lives in the stateless [MovieDetailsScreen].
 */
@Composable
fun MovieDetailsRoute(
    onBack: () -> Unit,
    viewModel: MovieDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val currentUriHandler by rememberUpdatedState(LocalUriHandler.current)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                // Ignore failures such as "no app can open this link".
                is MovieDetailsEffect.OpenUrl -> runCatching { currentUriHandler.openUri(effect.url) }
                MovieDetailsEffect.NavigateBack -> currentOnBack()
            }
        }
    }

    MovieDetailsScreen(
        state = state,
        onIntent = viewModel::onIntent,
    )
}