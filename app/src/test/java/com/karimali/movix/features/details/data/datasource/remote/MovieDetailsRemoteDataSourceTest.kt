package com.karimali.movix.features.details.data.datasource.remote

import com.karimali.movix.core.testutil.MOVIE_ID
import com.karimali.movix.core.testutil.movieDetailsDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import java.io.IOException

class MovieDetailsRemoteDataSourceTest {

    private val api = mockk<MovieDetailsApi>()
    private lateinit var dataSource: MovieDetailsRemoteDataSource

    @Before
    fun setUp() {
        dataSource = MovieDetailsRemoteDataSource(api)
    }

    @Test
    fun `fetchMovieDetails returns the dto from the api`() = runTest {
        // Arrange
        val dto = movieDetailsDto()
        coEvery { api.fetchMovieDetails(MOVIE_ID) } returns dto

        // Act
        val result = dataSource.fetchMovieDetails(MOVIE_ID)

        // Assert
        assertSame(dto, result)
    }

    @Test
    fun `fetchMovieDetails calls the api once with the given id`() = runTest {
        // Arrange
        coEvery { api.fetchMovieDetails(any()) } returns movieDetailsDto(id = 99)

        // Act
        dataSource.fetchMovieDetails(99)

        // Assert
        coVerify(exactly = 1) { api.fetchMovieDetails(99) }
    }

    @Test
    fun `fetchMovieDetails propagates api failures unchanged`() = runTest {
        // Arrange
        val failure = IOException("offline")
        coEvery { api.fetchMovieDetails(any()) } throws failure

        // Act
        val thrown = runCatching { dataSource.fetchMovieDetails(MOVIE_ID) }.exceptionOrNull()

        // Assert
        assertSame(failure, thrown)
    }
}