package com.karimali.movix.features.details.domain.usecase

import com.karimali.movix.features.details.domain.model.MovieDetails
import com.karimali.movix.features.details.domain.repository.MovieDetailsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class ObserveMovieDetailsUseCaseTest {

    private lateinit var repository: MovieDetailsRepository
    private lateinit var observeMovieDetailsUseCase: ObserveMovieDetailsUseCase

    @Before
    fun setup() {
        repository = mockk()
        observeMovieDetailsUseCase = ObserveMovieDetailsUseCase(repository)
    }

    @Test
    fun `invoke should call repository observeMovieDetails with correct movie id`() {
        // Arrange
        val movieId = 1
        every {
            repository.observeMovieDetails(movieId)
        } returns flowOf(null)

        // Act
        observeMovieDetailsUseCase(movieId)

        // Assert
        verify(exactly = 1) {
            repository.observeMovieDetails(movieId)
        }
    }

    @Test
    fun `invoke should return flow from repository`() {
        // Arrange
        val movieId = 1
        val expectedFlow: Flow<MovieDetails?> = flowOf(null)

        every {
            repository.observeMovieDetails(movieId)
        } returns expectedFlow

        // Act
        val result = observeMovieDetailsUseCase(movieId)

        // Assert
        assertSame(expectedFlow, result)
    }
}
