package com.karimali.movix.features.movies.domain.usecase

import androidx.paging.PagingData
import com.karimali.movix.features.movies.domain.model.Movie
import com.karimali.movix.features.movies.domain.repository.MoviesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertSame

class FetchMoviesUseCaseTest {

    private lateinit var repository: MoviesRepository
    private lateinit var fetchMoviesUseCase: FetchMoviesUseCase

    @Before
    fun setup() {
        repository = mockk()
        fetchMoviesUseCase = FetchMoviesUseCase(repository)
    }

    @Test
    fun `invoke calls repository fetchMovies successfully`() {
        // Arrange
        every { repository.fetchMovies() } returns emptyFlow()

        // Act
        val result = fetchMoviesUseCase()

        // Assert
        verify(exactly = 1) { repository.fetchMovies() }
    }


    @Test
    fun `invoke should return flow from repository`() {
        // Arrange
        val expectedFlow: Flow<PagingData<Movie>> = emptyFlow()

        every { repository.fetchMovies() } returns expectedFlow

        // Act
        val result = fetchMoviesUseCase()

        // Assert
        assertSame(expectedFlow, result)
    }
}