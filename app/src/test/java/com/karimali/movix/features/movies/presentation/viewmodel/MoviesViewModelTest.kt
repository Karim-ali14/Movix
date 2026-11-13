package com.karimali.movix.features.movies.presentation.viewmodel


import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.karimali.movix.features.movies.domain.usecase.FetchMoviesUseCase
import com.karimali.movix.features.movies.domain.usecase.SearchMoviesUseCase
import com.karimali.movix.features.movies.presentation.contract.MoviesEffect
import com.karimali.movix.features.movies.presentation.contract.MoviesIntent
import com.karimali.movix.features.movies.presentation.contract.MoviesState
import com.karimali.movix.testutil.MainDispatcherRule
import com.karimali.movix.testutil.movie
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var fetchMovies: FetchMoviesUseCase
    private lateinit var searchMovies: SearchMoviesUseCase

    @Before
    fun setUp() {
        fetchMovies = mockk()
        searchMovies = mockk()
        every { fetchMovies() } returns flowOf(PagingData.empty())
        every { searchMovies(any()) } returns flowOf(PagingData.empty())
    }

    private fun createViewModel() = MoviesViewModel(fetchMovies, searchMovies)

    /** `movies` is cold: nothing is requested until somebody collects it, like the UI does. */
    private fun TestScope.collectMovies(viewModel: MoviesViewModel) {
        backgroundScope.launch { viewModel.movies.collect {} }
        runCurrent()
    }

    // ---------- State ----------

    @Test
    fun `initial state has an empty query`() {
        // Arrange & Act
        val viewModel = createViewModel()

        // Assert
        assertEquals(MoviesState(), viewModel.state.value)
    }

    @Test
    fun `QueryChanged updates the query in state`() {
        // Arrange
        val viewModel = createViewModel()

        // Act
        viewModel.onIntent(MoviesIntent.QueryChanged("bat"))

        // Assert
        assertEquals("bat", viewModel.state.value.query)
    }

    @Test
    fun `ClearQuery resets the query`() {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onIntent(MoviesIntent.QueryChanged("bat"))

        // Act
        viewModel.onIntent(MoviesIntent.ClearQuery)

        // Assert
        assertEquals("", viewModel.state.value.query)
    }

    // ---------- Effects ----------

    @Test
    fun `MovieClicked emits NavigateToDetails with the movie id`() = runTest {
        // Arrange
        val viewModel = createViewModel()

        viewModel.effects.test {
            // Act
            viewModel.onIntent(MoviesIntent.MovieClicked(movieId = 5))

            // Assert
            assertEquals(MoviesEffect.NavigateToDetails(5), awaitItem())
        }
    }

    // ---------- Main list ----------

    @Test
    fun `blank query loads the main list and never searches`() = runTest {
        // Arrange
        val viewModel = createViewModel()

        // Act
        collectMovies(viewModel)

        // Assert
        verify(exactly = 1) { fetchMovies() }
        verify(exactly = 0) { searchMovies(any()) }
    }


    @Test
    fun `movies flow emits paging data`() =
        runTest(mainRule.dispatcher) {

            // Arrange
            every {
                fetchMovies()
            } returns flowOf(PagingData.empty())

            val viewModel = createViewModel()

            // Act & Assert
            viewModel.movies.test {
                val pagingData = awaitItem()

                assertNotNull(pagingData)

                cancelAndIgnoreRemainingEvents()
            }
        }


    // ---------- Search ----------

    @Test
    fun `search runs only after the debounce delay`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        collectMovies(viewModel)

        // Act
        viewModel.onIntent(MoviesIntent.QueryChanged("bat"))
        advanceTimeBy(300)
        runCurrent()

        // Assert: still waiting
        verify(exactly = 0) { searchMovies(any()) }

        // Act
        advanceTimeBy(150)
        runCurrent()

        // Assert: debounce elapsed
        verify(exactly = 1) { searchMovies("bat") }
    }

    @Test
    fun `search query is trimmed before it reaches the use case`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        collectMovies(viewModel)

        // Act
        viewModel.onIntent(MoviesIntent.QueryChanged("  bat  "))
        advanceTimeBy(500)
        runCurrent()

        // Assert
        verify(exactly = 1) { searchMovies("bat") }
    }

    @Test
    fun `fast typing searches only for the final query`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        collectMovies(viewModel)

        // Act
        viewModel.onIntent(MoviesIntent.QueryChanged("b"))
        advanceTimeBy(100)
        viewModel.onIntent(MoviesIntent.QueryChanged("ba"))
        advanceTimeBy(100)
        viewModel.onIntent(MoviesIntent.QueryChanged("bat"))
        advanceTimeBy(500)
        runCurrent()

        // Assert
        verify(exactly = 1) { searchMovies("bat") }
        verify(exactly = 0) { searchMovies("b") }
        verify(exactly = 0) { searchMovies("ba") }
    }

    @Test
    fun `whitespace only query does not trigger a search`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        collectMovies(viewModel)

        // Act
        viewModel.onIntent(MoviesIntent.QueryChanged("   "))
        advanceTimeBy(500)
        runCurrent()

        // Assert
        verify(exactly = 0) { searchMovies(any()) }
        verify(exactly = 1) { fetchMovies() }
    }

    @Test
    fun `clearing the query returns to the main list without waiting for the debounce`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        collectMovies(viewModel)
        viewModel.onIntent(MoviesIntent.QueryChanged("bat"))
        advanceTimeBy(500)
        runCurrent()

        // Act (no time advance)
        viewModel.onIntent(MoviesIntent.ClearQuery)
        runCurrent()

        // Assert
        verify(exactly = 2) { fetchMovies() }
    }
}