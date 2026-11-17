package com.karimali.movix.features.movies.domain.usecase


import androidx.paging.PagingData
import com.karimali.movix.features.movies.domain.model.Movie
import com.karimali.movix.features.movies.domain.repository.MoviesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class SearchMoviesUseCaseTest {

    private lateinit var repository: MoviesRepository
    private lateinit var searchMoviesUseCase: SearchMoviesUseCase

    @Before
    fun setup() {
        repository = mockk()
        searchMoviesUseCase = SearchMoviesUseCase(repository)
    }

    @Test
    fun `invoke should trim leading and trailing spaces from query`() {
        // Arrange
        val query = " Batman "
        val expectedQuery = "Batman"

        every {
            repository.searchMovies(expectedQuery)
        } returns emptyFlow()

        // Act
        searchMoviesUseCase(query)

        // Assert
        verify(exactly = 1) {
            repository.searchMovies(expectedQuery)
        }
    }

    @Test
    fun `invoke should return flow from repository`() {
        // Arrange
        val expectedFlow: Flow<PagingData<Movie>> = emptyFlow()

        every {
            repository.searchMovies("Batman")
        } returns expectedFlow

        // Act
        val result = searchMoviesUseCase("Batman")

        // Assert
        assertSame(expectedFlow, result)
    }

}