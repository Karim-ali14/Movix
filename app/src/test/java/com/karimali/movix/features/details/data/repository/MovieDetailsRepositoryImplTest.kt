package com.karimali.movix.features.details.data.repository

import app.cash.turbine.test
import com.karimali.movix.core.network.toDomainException
import com.karimali.movix.core.testutil.MOVIE_ID
import com.karimali.movix.core.testutil.httpException
import com.karimali.movix.core.testutil.movieDetailsDto
import com.karimali.movix.core.testutil.movieDetailsEntity
import com.karimali.movix.core.testutil.validDto
import com.karimali.movix.features.details.data.datasource.local.MovieDetailsLocalDataSource
import com.karimali.movix.features.details.data.datasource.local.entity.MovieDetailsEntity
import com.karimali.movix.features.details.data.datasource.remote.MovieDetailsRemoteDataSource
import com.karimali.movix.features.details.data.mapper.toDomain
import com.karimali.movix.features.movies.data.datasource.local.MoviesLocalDataSource
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

class MovieDetailsRepositoryImplTest {

    private val remote = mockk<MovieDetailsRemoteDataSource>()
    private val local = mockk<MovieDetailsLocalDataSource>()
    private lateinit var repository: MovieDetailsRepositoryImpl

    private val moviesLocal = mockk<MoviesLocalDataSource>(relaxed = true)

    @Before
    fun setUp() {
        repository = MovieDetailsRepositoryImpl(remote, local,moviesLocal)
    }

    private suspend fun refreshFailure(): Throwable =
        runCatching { repository.refreshMovieDetails(MOVIE_ID) }.exceptionOrNull()
            ?: error("Expected refreshMovieDetails to throw")

    // ───────────────────────── observeMovieDetails ─────────────────────────

    @Test
    fun `observe emits the cached entity mapped to domain`() = runTest {
        // Arrange
        val entity = movieDetailsEntity()
        every { local.observe(MOVIE_ID) } returns flowOf(entity)

        // Act & Assert
        repository.observeMovieDetails(MOVIE_ID).test {
            assertEquals(entity.toDomain(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observe emits null when nothing is cached`() = runTest {
        // Arrange
        every { local.observe(MOVIE_ID) } returns flowOf(null)

        // Act & Assert
        repository.observeMovieDetails(MOVIE_ID).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observe re-emits whenever the cache changes`() = runTest {
        // Arrange
        val original = movieDetailsEntity(title = "Old")
        val updated = movieDetailsEntity(title = "New")
        val cache = MutableStateFlow<MovieDetailsEntity?>(null)
        every { local.observe(MOVIE_ID) } returns cache

        // Act & Assert
        repository.observeMovieDetails(MOVIE_ID).test {
            assertNull(awaitItem())
            cache.value = original
            assertEquals(original.toDomain(), awaitItem())
            cache.value = updated
            assertEquals(updated.toDomain(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe suppresses consecutive duplicates`() = runTest {
        // Arrange
        val entity = movieDetailsEntity()
        every { local.observe(MOVIE_ID) } returns flowOf(entity, entity.copy())

        // Act & Assert
        repository.observeMovieDetails(MOVIE_ID).test {
            assertEquals(entity.toDomain(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observe reads only from the local source`() = runTest {
        // Arrange
        every { local.observe(MOVIE_ID) } returns flowOf(movieDetailsEntity())

        // Act
        repository.observeMovieDetails(MOVIE_ID).test {
            awaitItem()
            awaitComplete()
        }

        // Assert
        verify(exactly = 1) { local.observe(MOVIE_ID) }
        coVerify(exactly = 0) { remote.fetchMovieDetails(any()) }
    }

    // ───────────────────────── refreshMovieDetails: success ─────────────────────────

    @Test
    fun `refresh fetches by id maps the dto and saves the entity`() = runTest {
        // Arrange
        val saved = slot<MovieDetailsEntity>()
        coEvery { remote.fetchMovieDetails(MOVIE_ID) } returns movieDetailsDto()
        coEvery { local.save(capture(saved)) } just Runs

        // Act
        repository.refreshMovieDetails(MOVIE_ID)

        // Assert
        assertEquals(movieDetailsEntity(), saved.captured)
        coVerify(exactly = 1) { remote.fetchMovieDetails(MOVIE_ID) }
        coVerify(exactly = 1) { local.save(any()) }
    }

    @Test
    fun `refresh pushes fresh data to an active observer`() = runTest {
        // Arrange
        val cache = MutableStateFlow<MovieDetailsEntity?>(null)
        every { local.observe(MOVIE_ID) } returns cache
        coEvery { local.save(any()) } coAnswers { cache.value = firstArg() }
        coEvery { remote.fetchMovieDetails(MOVIE_ID) } returns movieDetailsDto()

        // Act & Assert
        repository.observeMovieDetails(MOVIE_ID).test {
            assertNull(awaitItem())
            repository.refreshMovieDetails(MOVIE_ID)
            assertEquals(movieDetailsEntity().toDomain(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh replaces stale cached data with the latest from the server`() = runTest {
        // Arrange
        val stale = movieDetailsEntity(voteAverage = 5.0)
        val cache = MutableStateFlow<MovieDetailsEntity?>(stale)
        every { local.observe(MOVIE_ID) } returns cache
        coEvery { local.save(any()) } coAnswers { cache.value = firstArg() }
        coEvery { remote.fetchMovieDetails(MOVIE_ID) } returns movieDetailsDto(voteAverage = 9.1)

        // Act & Assert
        repository.observeMovieDetails(MOVIE_ID).test {
            assertEquals(stale.toDomain(), awaitItem())
            repository.refreshMovieDetails(MOVIE_ID)

            // Fix: Use requireNotNull to unwrap the nullable rating safely
            val domainModel = awaitItem()
            assertEquals(9.1, requireNotNull(domainModel?.rating), 0.001)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ───────────────────────── refreshMovieDetails: invalid payload ─────────────────────────

    @Test
    fun `refresh fails and saves nothing when the payload has no id`() = runTest {
        // Arrange
        coEvery { remote.fetchMovieDetails(MOVIE_ID) } returns movieDetailsDto(id = null)

        // Act
        val failure = refreshFailure()

        // Assert
        assertEquals(IllegalStateException().toDomainException()::class, failure::class)
        coVerify(exactly = 0) { local.save(any()) }
    }

    @Test
    fun `refresh fails and saves nothing when the payload has a blank title`() = runTest {
        // Arrange
        coEvery { remote.fetchMovieDetails(MOVIE_ID) } returns movieDetailsDto(title = "  ")

        // Act
        val failure = refreshFailure()

        // Assert
        assertEquals(IllegalStateException().toDomainException()::class, failure::class)
        coVerify(exactly = 0) { local.save(any()) }
    }

    // ───────────────────────── refreshMovieDetails: error mapping ─────────────────────────

    @Test
    fun `refresh maps IOException to a domain error`() = runTest {
        // Arrange
        val original = IOException("offline")
        coEvery { remote.fetchMovieDetails(MOVIE_ID) } throws original

        // Act
        val failure = refreshFailure()

        // Assert
        assertNotSame(original, failure)
        assertEquals(original.toDomainException()::class, failure::class)
    }

    @Test
    fun `refresh maps timeouts to a domain error`() = runTest {
        // Arrange
        val original = SocketTimeoutException("timeout")
        coEvery { remote.fetchMovieDetails(MOVIE_ID) } throws original

        // Act
        val failure = refreshFailure()

        // Assert
        assertNotSame(original, failure)
        assertEquals(original.toDomainException()::class, failure::class)
    }

    @Test
    fun `refresh maps HTTP 404 to a domain error`() = runTest {
        // Arrange
        val original = httpException(404)
        coEvery { remote.fetchMovieDetails(MOVIE_ID) } throws original

        // Act
        val failure = refreshFailure()

        // Assert
        assertNotSame(original, failure)
        assertEquals(original.toDomainException()::class, failure::class)
    }

    @Test
    fun `refresh maps HTTP 500 to a domain error`() = runTest {
        // Arrange
        val original = httpException(500)
        coEvery { remote.fetchMovieDetails(MOVIE_ID) } throws original

        // Act
        val failure = refreshFailure()

        // Assert
        assertNotSame(original, failure)
        assertEquals(original.toDomainException()::class, failure::class)
    }

    @Test
    fun `refresh maps unexpected exceptions to a domain error`() = runTest {
        // Arrange
        val original = RuntimeException("boom")
        coEvery { remote.fetchMovieDetails(MOVIE_ID) } throws original

        // Act
        val failure = refreshFailure()

        // Assert
        assertNotSame(original, failure)
        assertEquals(original.toDomainException()::class, failure::class)
    }

    @Test
    fun `refresh maps local save failures to a domain error`() = runTest {
        // Arrange
        val original = IllegalStateException("disk full")
        coEvery { remote.fetchMovieDetails(MOVIE_ID) } returns movieDetailsDto()
        coEvery { local.save(any()) } throws original

        // Act
        val failure = refreshFailure()

        // Assert
        assertNotSame(original, failure)
        assertEquals(original.toDomainException()::class, failure::class)
    }

    // ───────────────────────── refreshMovieDetails: cache & cancellation ─────────────────────────

    @Test
    fun `refresh failure never saves and leaves the cache untouched`() = runTest {
        // Arrange
        val cached = movieDetailsEntity()
        val cache = MutableStateFlow<MovieDetailsEntity?>(cached)
        every { local.observe(MOVIE_ID) } returns cache
        coEvery { remote.fetchMovieDetails(MOVIE_ID) } throws IOException("offline")

        // Act & Assert
        repository.observeMovieDetails(MOVIE_ID).test {
            assertEquals(cached.toDomain(), awaitItem())
            refreshFailure()
            expectNoEvents()
            coVerify(exactly = 0) { local.save(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh rethrows cancellation from the remote call without mapping it`() = runTest {
        // Arrange
        val cancellation = CancellationException("cancelled")
        coEvery { remote.fetchMovieDetails(MOVIE_ID) } throws cancellation

        // Act
        val failure = refreshFailure()

        // Assert
        assertSame(cancellation, failure)
        coVerify(exactly = 0) { local.save(any()) }
    }

    @Test
    fun `refresh rethrows cancellation from the local save without mapping it`() = runTest {
        // Arrange
        val cancellation = CancellationException("cancelled")
        coEvery { remote.fetchMovieDetails(MOVIE_ID) } returns movieDetailsDto()
        coEvery { local.save(any()) } throws cancellation

        // Act
        val failure = refreshFailure()

        // Assert
        assertSame(cancellation, failure)
    }

    @Test
    fun `refresh also updates the cached list row`() = runTest {
        // Arrange
        coEvery { remote.fetchMovieDetails(1) } returns validDto(id = 1, title = "New title")

        coEvery { local.save(any()) } just runs
        coEvery { moviesLocal.updateCachedMovie(any()) } just runs

        // Act
        repository.refreshMovieDetails(1)

        // Assert
        coVerify {
            moviesLocal.updateCachedMovie(match { it.id == 1 && it.title == "New title" })
        }
    }

    @Test
    fun `failed refresh does not touch the list cache`() = runTest {
        coEvery { remote.fetchMovieDetails(1) } throws IOException()

        runCatching { repository.refreshMovieDetails(1) }

        coVerify(exactly = 0) { moviesLocal.updateCachedMovie(any()) }
    }
}