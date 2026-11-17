package com.karimali.movix.features.movies.data.datasource.local


import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karimali.movix.core.database.MovixDatabase
import com.karimali.movix.features.movies.data.datasource.local.entity.MovieEntity
import com.karimali.movix.features.movies.data.datasource.local.entity.RemoteKeysEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MoviesLocalDataSourceTest {

    private lateinit var db: MovixDatabase
    private lateinit var local: MoviesLocalDataSource

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MovixDatabase::class.java,
        ).allowMainThreadQueries().build()
        local = MoviesLocalDataSource(db, db.moviesDao(), db.remoteKeysDao())
    }

    @After
    fun tearDown() = db.close()

    private fun movie(id: Int, title: String, page: Int = 1, position: Int = id) = MovieEntity(
        id = id, title = title, overview = null, posterPath = null, backdropPath = null,
        releaseDate = null, voteAverage = null, voteCount = null, genreIds = emptyList(),
        page = page, position = position,
    )

    private fun key(id: Int) = RemoteKeysEntity(movieId = id, prevKey = null, nextKey = 2)

    @Test
    fun search_treatsPercentLiterally_notAsWildcard() = runTest {
        // Arrange
        val movies = listOf(movie(1, "100% Wolf"), movie(2, "Wolf Hall"))
        val keys = listOf(key(1), key(2))
        local.savePage(movies, keys, clearFirst = false)

        // Act
        val result = local.searchCached("100%")

        // Assert
        assertEquals(listOf(1), result.map { it.id })
    }

    @Test
    fun search_treatsUnderscoreLiterally_notAsSingleCharWildcard() = runTest {
        // Arrange
        val movies = listOf(movie(1, "A_B"), movie(2, "AXB"))
        val keys = listOf(key(1), key(2))
        local.savePage(movies, keys, clearFirst = false)

        // Act
        val result = local.searchCached("A_B")

        // Assert
        assertEquals(listOf(1), result.map { it.id })
    }

    @Test
    fun search_isCaseInsensitive_andKeepsFeedOrder() = runTest {
        // Arrange
        val movies = listOf(
            movie(1, "The Batman", position = 1),
            movie(2, "batman begins", position = 0),
        )
        val keys = listOf(key(1), key(2))
        local.savePage(movies, keys, clearFirst = false)

        // Act
        val result = local.searchCached("BATMAN")

        // Assert
        assertEquals(listOf(2, 1), result.map { it.id })
    }

    @Test
    fun savePage_withClearFirst_replacesOldMoviesAndKeys() = runTest {
        // Arrange
        local.savePage(listOf(movie(1, "Old")), listOf(key(1)), clearFirst = false)

        // Act
        local.savePage(listOf(movie(2, "New")), listOf(key(2)), clearFirst = true)

        // Assert
        assertTrue(local.searchCached("Old").isEmpty())
        assertEquals(listOf(2), local.searchCached("New").map { it.id })
        assertNull(local.remoteKeys(1))
        assertEquals(2, local.remoteKeys(2)?.movieId)
    }
}