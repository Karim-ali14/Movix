package com.karimali.movix.features.movies.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.karimali.movix.R
import com.karimali.movix.core.ui.theme.MovieAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchFieldTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun showField(
        query: String = "",
        onQueryChange: (String) -> Unit = {},
        onClear: () -> Unit = {},
    ) {
        composeRule.setContent {
            MovieAppTheme {
                SearchField(query = query, onQueryChange = onQueryChange, onClear = onClear)
            }
        }
    }

    @Test
    fun emptyQuery_showsPlaceholder() {
        // Arrange
        showField(query = "")

        // Act & Assert
        composeRule.onNodeWithText(context.getString(R.string.search_hint)).assertIsDisplayed()
    }

    @Test
    fun emptyQuery_hidesClearButton() {
        // Arrange
        showField(query = "")

        // Act & Assert
        composeRule.onNodeWithContentDescription(context.getString(R.string.clear)).assertDoesNotExist()
    }

    @Test
    fun nonEmptyQuery_showsClearButton() {
        // Arrange
        showField(query = "bat")

        // Act & Assert
        composeRule.onNodeWithContentDescription(context.getString(R.string.clear)).assertIsDisplayed()
    }

    @Test
    fun typing_invokesOnQueryChange() {
        // Arrange
        var received: String? = null
        showField(onQueryChange = { received = it })

        // Act
        composeRule.onNode(hasSetTextAction()).performTextInput("bat")

        // Assert
        assertEquals("bat", received)
    }

    @Test
    fun clickingClear_invokesOnClear() {
        // Arrange
        var cleared = false
        showField(query = "bat", onClear = { cleared = true })

        // Act
        composeRule.onNodeWithContentDescription(context.getString(R.string.clear)).performClick()

        // Assert
        assertTrue(cleared)
    }
}