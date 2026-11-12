package com.karimali.movix.features.movies.data.paging

import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import com.karimali.movix.core.error.DomainError
import com.karimali.movix.core.error.DomainException
import com.karimali.movix.features.movies.data.datasource.local.MoviesLocalDataSource
import com.karimali.movix.features.movies.data.datasource.remote.MoviesRemoteDataSource
import com.karimali.movix.features.movies.data.datasource.remote.dto.MoviesResponseDto
import com.karimali.movix.testutil.movieDto
import com.karimali.movix.testutil.movieEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.UnknownHostException

class SearchMoviesPagingSourceTest {

    private lateinit var remote: MoviesRemoteDataSource
    private lateinit var local: MoviesLocalDataSource
    private lateinit var source: SearchMoviesPagingSource

    private val testQuery = "batman"

    @Before
    fun setUp() {
        remote = mockk()
        local = mockk()
        source = SearchMoviesPagingSource(remote, local, query = testQuery)
    }

    private fun refreshParams() =
        LoadParams.Refresh<Int>(key = null, loadSize = 20, placeholdersEnabled = false)

    private fun appendParams(page: Int) =
        LoadParams.Append(key = page, loadSize = 20, placeholdersEnabled = false)

    // ---------- Online: paging keys ----------

    @Test
    fun `first page has no prev key and points to next page correctly`() = runTest {
        // Arrange
        val response = MoviesResponseDto(
            page = 1,
            results = listOf(movieDto(1), movieDto(2)),
            totalPages = 3,
            totalResults = 6,
        )
        coEvery { remote.searchMovies(testQuery, 1) } returns response

        // Act
        val page = source.load(refreshParams()) as LoadResult.Page

        // Assert
        assertEquals(listOf(1, 2), page.data.map { it.id })
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `last page has no next key`() = runTest {
        // Arrange
        val response = MoviesResponseDto(
            page = 3,
            results = listOf(movieDto(9)),
            totalPages = 3,
            totalResults = 9,
        )
        coEvery { remote.searchMovies(testQuery, 3) } returns response

        // Act
        val page = source.load(appendParams(3)) as LoadResult.Page

        // Assert
        assertEquals(2, page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun `empty results stop pagination`() = runTest {
        // Arrange
        val response = MoviesResponseDto(
            page = 1,
            results = emptyList(),
            totalPages = 5,
            totalResults = 0,
        )
        coEvery { remote.searchMovies(testQuery, 1) } returns response

        // Act
        val page = source.load(refreshParams()) as LoadResult.Page

        // Assert
        assertTrue(page.data.isEmpty())
        assertNull(page.nextKey)
    }

    // ---------- Online: data validation ----------

    @Test
    fun `invalid items are filtered out seamlessly`() = runTest {
        // Arrange
        val response = MoviesResponseDto(
            page = 1,
            results = listOf(
                movieDto(1),
                movieDto(id = null),
                movieDto(3, title = ""),
            ),
            totalPages = 1,
            totalResults = 3,
        )
        coEvery { remote.searchMovies(testQuery, 1) } returns response

        // Act
        val page = source.load(refreshParams()) as LoadResult.Page

        // Assert
        assertEquals(listOf(1), page.data.map { it.id })
    }

    @Test
    fun `duplicate ids inside a page are removed`() = runTest {
        // Arrange
        val response = MoviesResponseDto(
            page = 1,
            results = listOf(movieDto(1), movieDto(1), movieDto(2)),
            totalPages = 1,
            totalResults = 3,
        )
        coEvery { remote.searchMovies(testQuery, 1) } returns response

        // Act
        val page = source.load(refreshParams()) as LoadResult.Page

        // Assert
        assertEquals(listOf(1, 2), page.data.map { it.id })
    }

    // ---------- Offline fallback ----------

    @Test
    fun `offline with cached matches returns them as final page`() = runTest {
        // Arrange
        coEvery { remote.searchMovies(any(), any()) } throws UnknownHostException()
        coEvery { local.searchCached(testQuery) } returns listOf(
            movieEntity(1, "Batman Begins"),
            movieEntity(2, "The Batman"),
        )

        // Act
        val page = source.load(refreshParams()) as LoadResult.Page

        // Assert
        assertEquals(listOf(1, 2), page.data.map { it.id })
        assertNull(page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun `offline with no cached matches returns no internet error`() = runTest {
        // Arrange
        coEvery { remote.searchMovies(any(), any()) } throws UnknownHostException()
        coEvery { local.searchCached(testQuery) } returns emptyList()

        // Act
        val error = source.load(refreshParams()) as LoadResult.Error

        // Assert
        assertEquals(DomainError.NoInternet, (error.throwable as DomainException).error)
    }

    @Test
    fun `offline on later page does not fall back to cache`() = runTest {
        // Arrange
        coEvery { remote.searchMovies(testQuery, 2) } throws UnknownHostException()

        // Act
        val result = source.load(appendParams(2))

        // Assert
        assertTrue(result is LoadResult.Error)
        coVerify(exactly = 0) { local.searchCached(any()) }
    }

    @Test
    fun `offline when cache lookup fails returns error instead of crashing`() = runTest {
        // Arrange
        coEvery { remote.searchMovies(any(), any()) } throws UnknownHostException()
        coEvery { local.searchCached(any()) } throws IllegalStateException("db is broken")

        // Act
        val result = source.load(refreshParams())

        // Assert
        assertTrue(result is LoadResult.Error)
    }

    // ---------- Server errors ----------

    @Test
    fun `server error is reported as error without using cache`() = runTest {
        // Arrange
        val body = "".toResponseBody("application/json".toMediaType())
        coEvery { remote.searchMovies(any(), any()) } throws
                HttpException(Response.error<Any>(500, body))

        // Act
        val error = source.load(refreshParams()) as LoadResult.Error

        // Assert
        assertEquals(DomainError.Server(500), (error.throwable as DomainException).error)
        coVerify(exactly = 0) { local.searchCached(any()) }
    }
}