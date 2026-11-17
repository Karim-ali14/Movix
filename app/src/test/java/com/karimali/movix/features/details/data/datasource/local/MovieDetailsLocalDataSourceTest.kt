package com.karimali.movix.features.details.data.datasource.local

import app.cash.turbine.test
import com.karimali.movix.core.testutil.MOVIE_ID
import com.karimali.movix.core.testutil.movieDetailsEntity
import com.karimali.movix.features.details.data.datasource.local.dao.MovieDetailsDao
import com.karimali.movix.features.details.data.datasource.local.entity.MovieDetailsEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class MovieDetailsLocalDataSourceTest {

    private val dao = mockk<MovieDetailsDao>()
    private lateinit var dataSource: MovieDetailsLocalDataSource

    @Before
    fun setUp() {
        dataSource = MovieDetailsLocalDataSource(dao)
    }

    @Test
    fun `observe emits the entity coming from the dao`() = runTest {
        // Arrange
        val entity = movieDetailsEntity()
        every { dao.observe(MOVIE_ID) } returns flowOf(entity)

        // Act & Assert
        dataSource.observe(MOVIE_ID).test {
            assertEquals(entity, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observe emits null when nothing is cached`() = runTest {
        // Arrange
        every { dao.observe(MOVIE_ID) } returns flowOf(null)

        // Act & Assert
        dataSource.observe(MOVIE_ID).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observe queries the dao with the requested id`() {
        // Arrange
        every { dao.observe(any()) } returns flowOf(null)

        // Act
        dataSource.observe(42)

        // Assert
        verify(exactly = 1) { dao.observe(42) }
    }

    @Test
    fun `observe forwards later cache updates`() = runTest {
        // Arrange
        val first = movieDetailsEntity(title = "Old")
        val second = movieDetailsEntity(title = "New")
        val cache = MutableStateFlow<MovieDetailsEntity?>(first)
        every { dao.observe(MOVIE_ID) } returns cache

        // Act & Assert
        dataSource.observe(MOVIE_ID).test {
            assertEquals(first, awaitItem())
            cache.value = second
            assertEquals(second, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save upserts the entity once`() = runTest {
        // Arrange
        val entity = movieDetailsEntity()
        coEvery { dao.upsert(entity) } just Runs

        // Act
        dataSource.save(entity)

        // Assert
        coVerify(exactly = 1) { dao.upsert(entity) }
    }

    @Test
    fun `save propagates dao failures unchanged`() = runTest {
        // Arrange
        val failure = IllegalStateException("disk full")
        coEvery { dao.upsert(any()) } throws failure

        // Act
        val thrown = runCatching { dataSource.save(movieDetailsEntity()) }.exceptionOrNull()

        // Assert
        assertSame(failure, thrown)
    }
}