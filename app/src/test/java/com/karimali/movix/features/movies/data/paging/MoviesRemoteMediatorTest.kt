package com.karimali.movix.features.movies.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator.MediatorResult
import com.karimali.movix.core.error.DomainError
import com.karimali.movix.core.error.DomainException
import com.karimali.movix.features.movies.data.datasource.local.MoviesLocalDataSource
import com.karimali.movix.features.movies.data.datasource.local.entity.MovieEntity
import com.karimali.movix.features.movies.data.datasource.local.entity.RemoteKeysEntity
import com.karimali.movix.features.movies.data.datasource.remote.MoviesRemoteDataSource
import com.karimali.movix.features.movies.data.datasource.remote.dto.MoviesResponseDto
import com.karimali.movix.testutil.movieDto
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException

@OptIn(ExperimentalPagingApi::class)
class MoviesRemoteMediatorTest {

    private lateinit var remote: MoviesRemoteDataSource
    private lateinit var local: MoviesLocalDataSource
    private lateinit var mediator: MoviesRemoteMediator

    private val savedMovies = slot<List<MovieEntity>>()
    private val savedKeys = slot<List<RemoteKeysEntity>>()

    @Before
    fun setUp() {
        remote = mockk()
        local = mockk()
        mediator = MoviesRemoteMediator(remote, local)

        // Capture database saves automatically for clean assertions
        coEvery { local.savePage(capture(savedMovies), capture(savedKeys), any()) } just Runs
    }

    private fun createPagingState(vararg items: MovieEntity) = PagingState<Int, MovieEntity>(
        pages = if (items.isEmpty()) emptyList() else listOf(
            PagingSource.LoadResult.Page(data = items.toList(), prevKey = null, nextKey = null)
        ),
        anchorPosition = null,
        config = PagingConfig(pageSize = 20),
        leadingPlaceholderCount = 0,
    )


    @Test
    fun `refresh fetches first page successfully and continues when more pages exist`() = runTest {
        val response = MoviesResponseDto(
            page = 1,
            results = listOf(movieDto(1), movieDto(2)),
            totalPages = 3,
            totalResults = 6
        )
        coEvery { remote.fetchMovies(1) } returns response

        val result = mediator.load(LoadType.REFRESH, createPagingState())

        assertTrue(result is MediatorResult.Success)
        assertEquals(false, (result as MediatorResult.Success).endOfPaginationReached)
        coVerify { local.savePage(any(), any(), clearFirst = true) }
        assertEquals(listOf(1, 2), savedMovies.captured.map { it.id })
    }

    @Test
    fun `refresh on last page ends pagination correctly`() = runTest {
        val response = MoviesResponseDto(
            page = 1,
            results = listOf(movieDto(1)),
            totalPages = 1,
            totalResults = 1
        )
        coEvery { remote.fetchMovies(1) } returns response

        val result = mediator.load(LoadType.REFRESH, createPagingState())

        assertTrue(result is MediatorResult.Success)
        assertEquals(true, (result as MediatorResult.Success).endOfPaginationReached)
        assertNull(savedKeys.captured.single().nextKey)
    }

    @Test
    fun `append continues from next key without clearing cache`() = runTest {
        val lastEntity = MovieEntity(
            id = 5, title = "Last", overview = null, posterPath = null,
            backdropPath = null, releaseDate = null, voteAverage = null,
            voteCount = null, genreIds = null, page = 1, position = 0
        )
        coEvery { local.remoteKeys(5) } returns RemoteKeysEntity(movieId = 5, prevKey = 1, nextKey = 2)

        val response = MoviesResponseDto(
            page = 2,
            results = listOf(movieDto(21)),
            totalPages = 5,
            totalResults = 50
        )
        coEvery { remote.fetchMovies(2) } returns response

        val result = mediator.load(LoadType.APPEND, createPagingState(lastEntity))

        assertTrue(result is MediatorResult.Success)
        assertEquals(false, (result as MediatorResult.Success).endOfPaginationReached)
        coVerify { local.savePage(any(), any(), clearFirst = false) }
    }

    @Test
    fun `append when keys are missing returns domain error instead of guessing`() = runTest {
        val lastEntity = MovieEntity(
            id = 5, title = "Last", overview = null, posterPath = null,
            backdropPath = null, releaseDate = null, voteAverage = null,
            voteCount = null, genreIds = null, page = 1, position = 0
        )
        coEvery { local.remoteKeys(5) } returns null

        val result = mediator.load(LoadType.APPEND, createPagingState(lastEntity))

        assertTrue(result is MediatorResult.Error)
        assertTrue((result as MediatorResult.Error).throwable is DomainException)
        coVerify(exactly = 0) { remote.fetchMovies(any()) }
    }

    @Test
    fun `prepend ends immediately without network requests`() = runTest {
        val result = mediator.load(LoadType.PREPEND, createPagingState())

        assertTrue(result is MediatorResult.Success)
        assertEquals(true, (result as MediatorResult.Success).endOfPaginationReached)
        coVerify(exactly = 0) { remote.fetchMovies(any()) }
    }

    @Test
    fun `network failure returns domain error and leaves cache untouched`() = runTest {
        coEvery { remote.fetchMovies(1) } throws UnknownHostException()

        val result = mediator.load(LoadType.REFRESH, createPagingState())

        assertTrue(result is MediatorResult.Error)
        val error = (result as MediatorResult.Error).throwable
        assertEquals(DomainError.NoInternet, (error as DomainException).error)

        // Ensure database write was never called so cache remains safe
        coVerify(exactly = 0) { local.savePage(any(), any(), any()) }
    }
}