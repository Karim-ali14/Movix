package com.karimali.movix.features.movies.presentation.screen

import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.karimali.movix.R
import com.karimali.movix.core.error.DomainError
import com.karimali.movix.core.error.DomainException
import com.karimali.movix.core.ui.theme.MovieAppTheme
import com.karimali.movix.features.movies.domain.model.Movie
import com.karimali.movix.features.movies.presentation.contract.MoviesIntent
import com.karimali.movix.features.movies.presentation.contract.MoviesState
import com.karimali.movix.testutil.movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoviesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val receivedIntents = mutableListOf<MoviesIntent>()

    private val idle = LoadState.NotLoading(endOfPaginationReached = false)
    private val loadingIndicator = hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)

    private fun showScreen(
        movies: List<Movie> = emptyList(),
        query: String = "",
        refresh: LoadState = idle,
        append: LoadState = idle,
    ) {
        composeRule.setContent {
            MovieAppTheme {
                val pagingFlow = remember { pagingFlowOf(movies, refresh, append) }
                MoviesScreen(
                    state = MoviesState(query = query),
                    movies = pagingFlow.collectAsLazyPagingItems(),
                    onIntent = { receivedIntents += it },
                )
            }
        }
    }

    // ---------- List ----------

    @Test
    fun withMovies_showsTitlesAndReleaseYears() {
        // Arrange
        showScreen(
            movies = listOf(
                movie(id = 1, title = "Inception", releaseDate = "2010-07-16"),
                movie(id = 2, title = "Interstellar", releaseDate = "2014-11-05"),
            ),
        )

        // Act & Assert
        composeRule.onNodeWithText("Inception").assertIsDisplayed()
        composeRule.onNodeWithText("2010").assertIsDisplayed()
        composeRule.onNodeWithText("Interstellar").assertIsDisplayed()
        composeRule.onNodeWithText("2014").assertIsDisplayed()
    }

    @Test
    fun clickingAMovie_sendsMovieClickedIntentWithItsId() {
        // Arrange
        showScreen(movies = listOf(movie(id = 42, title = "Inception")))

        // Act
        composeRule.onNodeWithText("Inception").performClick()

        // Assert
        assertEquals(listOf<MoviesIntent>(MoviesIntent.MovieClicked(42)), receivedIntents)
    }

    // ---------- Refresh states (empty list) ----------

    @Test
    fun emptyListWhileRefreshing_showsLoadingIndicator() {
        // Arrange
        showScreen(refresh = LoadState.Loading)

        // Act & Assert
        composeRule.onNode(loadingIndicator).assertIsDisplayed()
    }

    @Test
    fun emptyListWithRefreshError_showsErrorMessageAndRetry() {
        // Arrange
        showScreen(refresh = LoadState.Error(DomainException(DomainError.NoInternet)))

        // Act & Assert
        composeRule.onNodeWithText(context.getString(R.string.error_no_internet)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.retry)).assertIsDisplayed()
    }

    @Test
    fun emptyListWithoutQuery_showsEmptyMessage() {
        // Arrange
        showScreen(refresh = idle, query = "")

        // Act & Assert
        composeRule.onNodeWithText(context.getString(R.string.empty_movies)).assertIsDisplayed()
    }

    @Test
    fun emptyListWithQuery_showsNoResultsMessage() {
        // Arrange
        showScreen(refresh = idle, query = "xyz")

        // Act & Assert
        composeRule.onNodeWithText(context.getString(R.string.empty_search, "xyz")).assertIsDisplayed()
    }

    // ---------- Offline-first ----------

    @Test
    fun refreshErrorWithCachedMovies_keepsMoviesVisibleAndShowsOfflineBanner() {
        // Arrange
        showScreen(
            movies = listOf(movie(id = 1, title = "Inception")),
            refresh = LoadState.Error(DomainException(DomainError.NoInternet)),
        )

        // Act & Assert
        composeRule.onNodeWithText("Inception").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.showing_saved_movies)).assertIsDisplayed()
    }

    // ---------- Pagination footer ----------

    @Test
    fun appendLoading_showsFooterProgress() {
        // Arrange
        showScreen(movies = listOf(movie(id = 1, title = "Inception")), append = LoadState.Loading)

        // Act & Assert
        composeRule.onNode(loadingIndicator).assertIsDisplayed()
    }

    @Test
    fun appendError_showsFooterMessageAndRetry() {
        // Arrange
        showScreen(
            movies = listOf(movie(id = 1, title = "Inception")),
            append = LoadState.Error(DomainException(DomainError.Timeout)),
        )

        // Act & Assert
        composeRule.onNodeWithText("Inception").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.error_timeout)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.retry)).assertIsDisplayed()
    }

    // ---------- Search ----------

    @Test
    fun typingInSearchField_sendsQueryChangedIntent() {
        // Arrange
        showScreen()

        // Act
        composeRule.onNode(hasSetTextAction()).performTextInput("bat")

        // Assert
        assertEquals(MoviesIntent.QueryChanged("bat"), receivedIntents.last())
    }

    @Test
    fun clickingClear_sendsClearQueryIntent() {
        // Arrange
        showScreen(query = "bat")

        // Act
        composeRule.onNodeWithContentDescription(context.getString(R.string.clear)).performClick()

        // Assert
        assertTrue(receivedIntents.contains(MoviesIntent.ClearQuery))
    }
}

/** Builds a PagingData with fixed items and load states, to drive each screen state. */
private fun pagingFlowOf(
    movies: List<Movie>,
    refresh: LoadState,
    append: LoadState,
): Flow<PagingData<Movie>> = flowOf(
    PagingData.from(
        data = movies,
        sourceLoadStates = LoadStates(
            refresh = refresh,
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = append,
        ),
    ),
)