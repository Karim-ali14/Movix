package com.karimali.movix.features.details.domain.usecase

import com.karimali.movix.features.details.domain.repository.MovieDetailsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RefreshMovieDetailsUseCaseTest {

    private lateinit var repository: MovieDetailsRepository
    private lateinit var refreshMovieDetailsUseCase: RefreshMovieDetailsUseCase

    @Before
    fun setup() {
        repository = mockk()
        refreshMovieDetailsUseCase = RefreshMovieDetailsUseCase(repository)
    }

    @Test
    fun `invoke should return success when repository refreshes successfully`() = runTest {
        // Arrange
        val movieId = 1

        coEvery {
            repository.refreshMovieDetails(movieId)
        } returns Unit

        // Act
        val result = refreshMovieDetailsUseCase(movieId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())

        coVerify(exactly = 1) {
            repository.refreshMovieDetails(movieId)
        }
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Arrange
        val movieId = 1
        val exception = IllegalStateException("Something went wrong")

        coEvery {
            repository.refreshMovieDetails(movieId)
        } throws exception

        // Act
        val result = refreshMovieDetailsUseCase(movieId)

        // Assert
        assertTrue(result.isFailure)
        assertSame(exception, result.exceptionOrNull())

        coVerify(exactly = 1) {
            repository.refreshMovieDetails(movieId)
        }
    }

    @Test
    fun `invoke should rethrow cancellation exception`() = runTest {
        // Arrange
        val movieId = 1
        val exception = CancellationException("Coroutine cancelled")

        coEvery {
            repository.refreshMovieDetails(movieId)
        } throws exception

        // Act
        val thrown = try {
            refreshMovieDetailsUseCase(movieId)
            null
        } catch (e: CancellationException) {
            e
        }

        // Assert
        assertSame(exception, thrown)

        coVerify(exactly = 1) {
            repository.refreshMovieDetails(movieId)
        }
    }
}
