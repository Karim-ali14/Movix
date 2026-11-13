package com.karimali.movix.features.movies.presentation.components

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.karimali.movix.R
import com.karimali.movix.core.error.DomainError
import com.karimali.movix.core.ui.theme.MovieAppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoadStateViewsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val retryLabel get() = context.getString(R.string.retry)

    @Test
    fun loadingView_showsProgressIndicator() {
        // Arrange
        composeRule.setContent { MovieAppTheme { LoadingView() } }

        // Act & Assert
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun errorView_showsMessageAndRetryInvokesCallback() {
        // Arrange
        var retried = false
        composeRule.setContent {
            MovieAppTheme { ErrorView(error = DomainError.NoInternet, onRetry = { retried = true }) }
        }

        // Act
        composeRule.onNodeWithText(retryLabel).performClick()

        // Assert
        composeRule.onNodeWithText(context.getString(R.string.error_no_internet)).assertIsDisplayed()
        assertTrue(retried)
    }

    @Test
    fun emptyView_withBlankQuery_showsEmptyListMessage() {
        // Arrange
        composeRule.setContent { MovieAppTheme { EmptyView(query = "") } }

        // Act & Assert
        composeRule.onNodeWithText(context.getString(R.string.empty_movies)).assertIsDisplayed()
    }

    @Test
    fun emptyView_withQuery_showsNoResultsMessageForThatQuery() {
        // Arrange
        composeRule.setContent { MovieAppTheme { EmptyView(query = "xyz") } }

        // Act & Assert
        composeRule.onNodeWithText(context.getString(R.string.empty_search, "xyz")).assertIsDisplayed()
    }

    @Test
    fun footerLoading_showsProgressIndicator() {
        // Arrange
        composeRule.setContent { MovieAppTheme { FooterLoading() } }

        // Act & Assert
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun footerError_showsMessageAndRetryInvokesCallback() {
        // Arrange
        var retried = false
        composeRule.setContent {
            MovieAppTheme { FooterError(error = DomainError.Timeout, onRetry = { retried = true }) }
        }

        // Act
        composeRule.onNodeWithText(retryLabel).performClick()

        // Assert
        composeRule.onNodeWithText(context.getString(R.string.error_timeout)).assertIsDisplayed()
        assertTrue(retried)
    }

    @Test
    fun offlineBanner_showsSavedMoviesNoticeMessageAndRetry() {
        // Arrange
        var retried = false
        composeRule.setContent {
            MovieAppTheme { OfflineBanner(error = DomainError.NoInternet, onRetry = { retried = true }) }
        }

        // Act
        composeRule.onNodeWithText(retryLabel).performClick()

        // Assert
        composeRule.onNodeWithText(context.getString(R.string.showing_saved_movies)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.error_no_internet)).assertIsDisplayed()
        assertTrue(retried)
    }
}