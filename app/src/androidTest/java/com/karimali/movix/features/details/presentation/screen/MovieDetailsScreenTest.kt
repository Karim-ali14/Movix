package com.karimali.movix.features.details.presentation.screen

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.karimali.movix.R
import com.karimali.movix.core.testutil.movieDetails
import com.karimali.movix.core.testutil.someDomainError
import com.karimali.movix.features.details.presentation.components.NOT_AVAILABLE
import com.karimali.movix.features.details.presentation.components.SKELETON_TEST_TAG
import com.karimali.movix.features.details.presentation.contract.MovieDetailsIntent
import com.karimali.movix.features.details.presentation.contract.MovieDetailsUiState
import com.karimali.movix.core.util.formatter.formatMoney
import com.karimali.movix.core.util.formatter.formatRuntime
import com.karimali.movix.core.util.formatter.formatWithCommas
import com.karimali.movix.core.util.formatter.languageName
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

class MovieDetailsScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val intents = CopyOnWriteArrayList<MovieDetailsIntent>()

    private val refreshBar = hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)

    private val longOverview = "A very long synopsis sentence that keeps going. ".repeat(40)

    private fun str(@StringRes id: Int, vararg args: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)

    private fun show(state: MovieDetailsUiState) {
        rule.setContent {
            MaterialTheme { MovieDetailsScreen(state = state, onIntent = { intents += it }) }
        }
    }

    /** Lets a test change the state after the first composition. */
    private fun showMutable(initial: MovieDetailsUiState): MutableState<MovieDetailsUiState> {
        val state = mutableStateOf(initial)
        rule.setContent {
            MaterialTheme { MovieDetailsScreen(state = state.value, onIntent = { intents += it }) }
        }
        return state
    }

    // ───────────────────────── Loading ─────────────────────────

    @Test
    fun loading_showsTheSkeletonAndNothingElse() {
        // Arrange
        val state = MovieDetailsUiState(isRefreshing = true)

        // Act
        show(state)

        // Assert
        rule.onNodeWithTag(SKELETON_TEST_TAG).assertExists()
        rule.onNodeWithText(str(R.string.details_synopsis)).assertDoesNotExist()
        rule.onNodeWithText(str(R.string.retry)).assertDoesNotExist()
        rule.onNodeWithText(str(R.string.details_showing_saved)).assertDoesNotExist()
    }

    @Test
    fun loading_doesNotShowTheRefreshBar() {
        // Arrange
        val state = MovieDetailsUiState(isRefreshing = true)

        // Act
        show(state)

        // Assert: the skeleton is the loading indicator when nothing is cached
        rule.onNode(refreshBar).assertDoesNotExist()
    }

    @Test
    fun content_doesNotShowTheSkeleton() {
        // Arrange
        val state = MovieDetailsUiState(movie = movieDetails())

        // Act
        show(state)

        // Assert
        rule.onNodeWithTag(SKELETON_TEST_TAG).assertDoesNotExist()
    }

    // ───────────────────────── Content: header ─────────────────────────

    @Test
    fun content_showsTitleTaglineSynopsisAndVotes() {
        // Arrange
        val movie = movieDetails()

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText("Star Wars").assertIsDisplayed()
        rule.onNodeWithText(movie.tagline).assertExists()
        rule.onNodeWithText(str(R.string.details_synopsis)).assertExists()
        rule.onNodeWithText(movie.overview).assertExists()
        rule.onNodeWithText(str(R.string.details_votes_format, 22_061.formatWithCommas()))
            .assertExists()
        rule.onNodeWithText(str(R.string.details_part_of_format, "Star Wars Collection"))
            .assertExists()
    }

    @Test
    fun header_showsYearAndRuntimeTogether() {
        // Arrange
        val movie = movieDetails(releaseDate = "1977-05-25", runtimeMinutes = 121)

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText("1977  •  ${formatRuntime(121)}").assertExists()
    }

    @Test
    fun header_withoutRuntime_showsOnlyTheYear() {
        // Arrange
        val movie = movieDetails(releaseDate = "1977-05-25", runtimeMinutes = null)

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText("1977").assertExists()
        rule.onNodeWithText(formatRuntime(121), substring = true).assertDoesNotExist()
    }

    @Test
    fun header_withoutReleaseDate_showsOnlyTheRuntime() {
        // Arrange
        val movie = movieDetails(releaseDate = null, runtimeMinutes = 121)

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText(formatRuntime(121)).assertExists()
        rule.onNodeWithText("1977", substring = true).assertDoesNotExist()
    }

    @Test
    fun header_ratingIsShownWithOneDecimal() {
        // Arrange
        val movie = movieDetails(rating = 7.04, voteCount = 10)

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText("7.0").assertExists()
    }

    @Test
    fun header_withoutVotes_showsADashInsteadOfTheRating() {
        // Arrange
        val movie = movieDetails(voteCount = 0, rating = 0.0)

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert: only the rating ring shows a dash (budget and revenue are known)
        rule.onNodeWithText("0.0").assertDoesNotExist()
        rule.onAllNodesWithText(NOT_AVAILABLE).assertCountEquals(1)
    }

    @Test
    fun header_showsTheOriginalTitleWhenItDiffersFromTheTitle() {
        // Arrange
        val movie = movieDetails(title = "The Hunt", originalTitle = "Jagten")

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText("Jagten").assertExists()
    }

    @Test
    fun header_hidesTheOriginalTitleWhenItMatchesIgnoringCase() {
        // Arrange
        val movie = movieDetails(title = "Star Wars", originalTitle = "STAR WARS")

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText("STAR WARS").assertDoesNotExist()
    }

    @Test
    fun header_hidesTheCollectionWhenMissing() {
        // Arrange
        val movie = movieDetails(collectionName = null)

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText(str(R.string.details_part_of_format, "Star Wars Collection"))
            .assertDoesNotExist()
    }

    @Test
    fun tagline_isHiddenWhenBlank() {
        // Arrange
        val movie = movieDetails(tagline = "")

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText("A long time ago in a galaxy far, far away...").assertDoesNotExist()
    }

    // ───────────────────────── Content: genres, info, production ─────────────────────────

    @Test
    fun genres_everyTagIsShown() {
        // Arrange
        val movie = movieDetails(genres = listOf("Adventure", "Action", "Science Fiction"))

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        movie.genres.forEach { rule.onNodeWithText(it).assertExists() }
    }

    @Test
    fun infoGrid_showsFormattedValues() {
        // Arrange
        val movie = movieDetails()

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText("Released").assertExists()
        rule.onNodeWithText(languageName("en")).assertExists()
        rule.onNodeWithText(requireNotNull(formatMoney(11_000_000L))).assertExists()
        rule.onNodeWithText(requireNotNull(formatMoney(775_398_007L))).assertExists()
    }

    @Test
    fun infoGrid_unknownBudgetAndRevenue_showDashes() {
        // Arrange
        val movie = movieDetails(budget = null, revenue = null)

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onAllNodesWithText(NOT_AVAILABLE).assertCountEquals(2)
    }

    @Test
    fun infoGrid_blankStatusAndLanguage_showDashes() {
        // Arrange
        val movie = movieDetails(status = "", originalLanguageCode = "")

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onAllNodesWithText(NOT_AVAILABLE).assertCountEquals(2)
    }

    @Test
    fun production_showsCompaniesAndSpokenLanguages() {
        // Arrange
        val movie = movieDetails()

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText(str(R.string.details_production)).assertExists()
        rule.onNodeWithText("Lucasfilm Ltd., 20th Century Fox").assertExists()
        rule.onNodeWithText(str(R.string.details_spoken_languages)).assertExists()
        rule.onNodeWithText("English, Spanish").assertExists()
    }

    @Test
    fun production_isHiddenWhenThereIsNothingToShow() {
        // Arrange
        val movie = movieDetails(productionCompanies = emptyList(), spokenLanguages = emptyList())

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText(str(R.string.details_production)).assertDoesNotExist()
        rule.onNodeWithText(str(R.string.details_spoken_languages)).assertDoesNotExist()
    }

    @Test
    fun production_onlyTheNonEmptyPartIsShown() {
        // Arrange
        val movie = movieDetails(spokenLanguages = emptyList())

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText(str(R.string.details_production)).assertExists()
        rule.onNodeWithText(str(R.string.details_spoken_languages)).assertDoesNotExist()
    }

    // ───────────────────────── Synopsis ─────────────────────────

    @Test
    fun synopsis_blankOverview_showsThePlaceholder() {
        // Arrange
        val movie = movieDetails(overview = "")

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText(str(R.string.details_no_synopsis)).assertExists()
    }

    @Test
    fun synopsis_shortText_hasNoReadMoreButton() {
        // Arrange
        val movie = movieDetails(overview = "Short and sweet.")

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText(str(R.string.details_read_more)).assertDoesNotExist()
    }

    @Test
    fun synopsis_longText_canBeExpandedAndCollapsed() {
        // Arrange
        show(MovieDetailsUiState(movie = movieDetails(overview = longOverview)))

        // Act: expand
        rule.onNodeWithText(str(R.string.details_read_more)).performScrollTo().performClick()

        // Assert
        rule.onNodeWithText(str(R.string.details_show_less)).assertExists()
        rule.onNodeWithText(str(R.string.details_read_more)).assertDoesNotExist()

        // Act: collapse
        rule.onNodeWithText(str(R.string.details_show_less)).performScrollTo().performClick()

        // Assert
        rule.onNodeWithText(str(R.string.details_read_more)).assertExists()
    }

    @Test
    fun synopsis_expandedState_survivesRecreation() {
        // Arrange
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            MaterialTheme {
                MovieDetailsScreen(
                    state = MovieDetailsUiState(movie = movieDetails(overview = longOverview)),
                    onIntent = {},
                )
            }
        }
        rule.onNodeWithText(str(R.string.details_read_more)).performScrollTo().performClick()
        rule.onNodeWithText(str(R.string.details_show_less)).assertExists()

        // Act
        restoration.emulateSavedInstanceStateRestore()

        // Assert
        rule.onNodeWithText(str(R.string.details_show_less)).assertExists()
    }

    // ───────────────────────── Links ─────────────────────────

    @Test
    fun links_websiteButton_sendsWebsiteClicked() {
        // Arrange
        show(MovieDetailsUiState(movie = movieDetails()))

        // Act
        rule.onNodeWithText(str(R.string.details_website)).performScrollTo().performClick()

        // Assert
        assertEquals(
            listOf<MovieDetailsIntent>(MovieDetailsIntent.WebsiteClicked),
            intents.toList()
        )
    }

    @Test
    fun links_imdbButton_sendsImdbClicked() {
        // Arrange
        show(MovieDetailsUiState(movie = movieDetails()))

        // Act
        rule.onNodeWithText(str(R.string.details_imdb)).performScrollTo().performClick()

        // Assert
        assertEquals(listOf<MovieDetailsIntent>(MovieDetailsIntent.ImdbClicked), intents.toList())
    }

    @Test
    fun links_areHiddenWhenTheMovieHasNone() {
        // Arrange
        val movie = movieDetails(homepage = null, imdbUrl = null)

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText(str(R.string.details_website)).assertDoesNotExist()
        rule.onNodeWithText(str(R.string.details_imdb)).assertDoesNotExist()
    }

    @Test
    fun links_onlyTheAvailableOneIsShown() {
        // Arrange
        val movie = movieDetails(homepage = null)

        // Act
        show(MovieDetailsUiState(movie = movie))

        // Assert
        rule.onNodeWithText(str(R.string.details_website)).assertDoesNotExist()
        rule.onNodeWithText(str(R.string.details_imdb)).assertExists()
    }

    // ───────────────────────── Back ─────────────────────────

    @Test
    fun backButton_sendsBackClicked_inLoadingErrorAndContentStates() {
        // Arrange
        val state = showMutable(MovieDetailsUiState(isRefreshing = true))
        val back = str(R.string.details_back)

        // Act
        rule.onNodeWithContentDescription(back).performClick()
        rule.runOnIdle { state.value = MovieDetailsUiState(error = someDomainError()) }
        rule.onNodeWithContentDescription(back).performClick()
        rule.runOnIdle { state.value = MovieDetailsUiState(movie = movieDetails()) }
        rule.onNodeWithContentDescription(back).performClick()

        // Assert
        assertEquals(List(3) { MovieDetailsIntent.BackClicked }, intents.toList())
    }

    // ───────────────────────── Full-screen error ─────────────────────────

    @Test
    fun error_withoutMovie_showsErrorViewAndNoContent() {
        // Arrange
        val state = MovieDetailsUiState(error = someDomainError())

        // Act
        show(state)

        // Assert
        rule.onNodeWithText(str(R.string.retry)).assertIsDisplayed()
        rule.onNodeWithTag(SKELETON_TEST_TAG).assertDoesNotExist()
        rule.onNodeWithText(str(R.string.details_synopsis)).assertDoesNotExist()
        rule.onNodeWithText(str(R.string.details_showing_saved)).assertDoesNotExist()
    }

    @Test
    fun error_withoutMovie_retrySendsRetryIntent() {
        // Arrange
        show(MovieDetailsUiState(error = someDomainError()))

        // Act
        rule.onNodeWithText(str(R.string.retry)).performClick()

        // Assert
        assertEquals(listOf<MovieDetailsIntent>(MovieDetailsIntent.Retry), intents.toList())
    }

    // ───────────────────────── Offline banner ─────────────────────────

    @Test
    fun offlineBanner_withMovieAndError_keepsContentAndShowsBanner() {
        // Arrange
        val state = MovieDetailsUiState(movie = movieDetails(), error = someDomainError())

        // Act
        show(state)

        // Assert
        rule.onNodeWithText("Star Wars").assertIsDisplayed()
        rule.onNodeWithText(str(R.string.details_showing_saved)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.retry)).assertIsDisplayed()
    }

    @Test
    fun offlineBanner_retrySendsRetryIntent() {
        // Arrange
        show(MovieDetailsUiState(movie = movieDetails(), error = someDomainError()))

        // Act
        rule.onNodeWithText(str(R.string.retry)).performClick()

        // Assert
        assertEquals(listOf<MovieDetailsIntent>(MovieDetailsIntent.Retry), intents.toList())
    }

    @Test
    fun offlineBanner_isAbsentWhenThereIsNoError() {
        // Arrange
        val state = MovieDetailsUiState(movie = movieDetails())

        // Act
        show(state)

        // Assert
        rule.onNodeWithText(str(R.string.details_showing_saved)).assertDoesNotExist()
        rule.onNodeWithText(str(R.string.retry)).assertDoesNotExist()
    }

    // ───────────────────────── Refresh indicator ─────────────────────────

    @Test
    fun refreshBar_isShownWhileRefreshingCachedContent() {
        // Arrange
        val state = MovieDetailsUiState(movie = movieDetails(), isRefreshing = true)

        // Act
        show(state)

        // Assert
        rule.onNode(refreshBar).assertExists()
    }

    @Test
    fun refreshBar_isHiddenWhenNotRefreshing() {
        // Arrange
        val state = MovieDetailsUiState(movie = movieDetails(), isRefreshing = false)

        // Act
        show(state)

        // Assert
        rule.onNode(refreshBar).assertDoesNotExist()
    }

    // ───────────────────────── State transitions ─────────────────────────

    @Test
    fun transition_loadingToContent() {
        // Arrange
        val state = showMutable(MovieDetailsUiState(isRefreshing = true))
        rule.onNodeWithTag(SKELETON_TEST_TAG).assertExists()

        // Act
        rule.runOnIdle { state.value = MovieDetailsUiState(movie = movieDetails()) }

        // Assert
        rule.onNodeWithText("Star Wars").assertIsDisplayed()
        rule.onNodeWithTag(SKELETON_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun transition_loadingToError_thenRetryShowsLoadingAgain() {
        // Arrange
        val state = showMutable(MovieDetailsUiState(isRefreshing = true))

        // Act: failure
        rule.runOnIdle { state.value = MovieDetailsUiState(error = someDomainError()) }

        // Assert
        rule.onNodeWithText(str(R.string.retry)).assertIsDisplayed()

        // Act: retry in flight (the ViewModel clears the error)
        rule.runOnIdle { state.value = MovieDetailsUiState(isRefreshing = true) }

        // Assert
        rule.onNodeWithText(str(R.string.retry)).assertDoesNotExist()
        rule.onNodeWithTag(SKELETON_TEST_TAG).assertExists()
    }

    @Test
    fun transition_cachedContentGetsABannerThenRecovers() {
        // Arrange
        val movie = movieDetails()
        val state = showMutable(MovieDetailsUiState(movie = movie))

        // Act: refresh failed
        rule.runOnIdle {
            state.value = MovieDetailsUiState(movie = movie, error = someDomainError())
        }

        // Assert
        rule.onNodeWithText(str(R.string.details_showing_saved)).assertIsDisplayed()

        // Act: retry succeeded
        rule.runOnIdle { state.value = MovieDetailsUiState(movie = movie) }

        // Assert
        rule.onNodeWithText(str(R.string.details_showing_saved)).assertDoesNotExist()
        rule.onNodeWithText("Star Wars").assertIsDisplayed()
    }

    @Test
    fun transition_contentUpdatesInPlaceWhenTheMovieChanges() {
        // Arrange
        val state = showMutable(MovieDetailsUiState(movie = movieDetails(title = "Old Title")))
        rule.onNodeWithText("Old Title").assertIsDisplayed()

        // Act
        rule.runOnIdle {
            state.value = MovieDetailsUiState(movie = movieDetails(title = "New Title"))
        }

        // Assert
        rule.onNodeWithText("New Title").assertIsDisplayed()
        rule.onNodeWithText("Old Title").assertDoesNotExist()
    }
}