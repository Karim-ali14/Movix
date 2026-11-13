package com.karimali.movix.features.movies.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karimali.movix.core.ui.theme.MovieAppTheme
import com.karimali.movix.features.movies.domain.model.Movie
import com.karimali.movix.testutil.movie
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MovieCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun showCard(movie: Movie, onClick: () -> Unit = {}) {
        composeRule.setContent { MovieAppTheme { MovieCard(movie = movie, onClick = onClick) } }
    }

    @Test
    fun card_showsTitleAndReleaseYear() {
        // Arrange
        showCard(movie(title = "Inception", releaseDate = "2010-07-16"))

        // Act & Assert
        composeRule.onNodeWithText("Inception").assertIsDisplayed()
        composeRule.onNodeWithText("2010").assertIsDisplayed()
    }

    @Test
    fun card_withInvalidReleaseDate_hidesTheYear() {
        // Arrange
        showCard(movie(title = "Inception", releaseDate = "abcd-01-01"))

        // Act & Assert
        composeRule.onNodeWithText("Inception").assertIsDisplayed()
        composeRule.onNodeWithText("abcd").assertDoesNotExist()
    }

    @Test
    fun card_withoutPoster_stillRendersTheTitle() {
        // Arrange
        showCard(movie(title = "Inception", posterUrl = null))

        // Act & Assert
        composeRule.onNodeWithText("Inception").assertIsDisplayed()
    }

    @Test
    fun clickingCard_invokesOnClick() {
        // Arrange
        var clicked = false
        showCard(movie(title = "Inception"), onClick = { clicked = true })

        // Act
        composeRule.onNodeWithText("Inception").performClick()

        // Assert
        assertTrue(clicked)
    }
}