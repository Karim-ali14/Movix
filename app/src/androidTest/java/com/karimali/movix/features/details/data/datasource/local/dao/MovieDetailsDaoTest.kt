package com.karimali.movix.features.details.data.datasource.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.karimali.movix.core.database.MovixDatabase
import com.karimali.movix.features.details.data.datasource.local.entity.MovieDetailsEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MovieDetailsDaoTest {

    private lateinit var db: MovixDatabase
    private lateinit var dao: MovieDetailsDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MovixDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.movieDetailsDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(
        id: Int = 11,
        title: String = "Star Wars",
        voteAverage: Double? = 8.2,
        budget: Long? = 11_000_000L,
        tagline: String? = "A long time ago...",
        genres: List<String> = listOf("Adventure", "Action"),
        productionCompanies: List<String> = listOf("Lucasfilm Ltd."),
        spokenLanguages: List<String> = listOf("English"),
    ) = MovieDetailsEntity(
        id = id,
        title = title,
        originalTitle = "Star Wars",
        tagline = tagline,
        overview = "Overview",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "1977-05-25",
        runtime = 121,
        voteAverage = voteAverage,
        voteCount = 22_061,
        status = "Released",
        originalLanguage = "en",
        budget = budget,
        revenue = 775_398_007L,
        homepage = "http://www.starwars.com",
        imdbId = "tt0076759",
        collectionName = "Star Wars Collection",
        genres = genres,
        productionCompanies = productionCompanies,
        spokenLanguages = spokenLanguages,
    )

    @Test
    fun observe_emitsNull_whenNoRowExists() = runTest {
        // Arrange
        // (empty database)

        // Act & Assert
        dao.observe(11).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun upsert_thenObserve_returnsTheStoredEntity() = runTest {
        // Arrange
        val entity = entity()

        // Act
        dao.upsert(entity)

        // Assert
        assertEquals(entity, dao.observe(11).first())
    }

    @Test
    fun upsert_withSameId_replacesTheExistingRow() = runTest {
        // Arrange
        dao.upsert(entity(title = "Old", voteAverage = 5.0))
        val updated = entity(title = "New", voteAverage = 9.1)

        // Act
        dao.upsert(updated)

        // Assert
        assertEquals(updated, dao.observe(11).first())
    }

    @Test
    fun observe_reEmits_whenTheRowIsInsertedAndThenUpdated() = runTest {
        // Arrange
        val first = entity(title = "First")
        val second = entity(title = "Second")

        // Act & Assert
        dao.observe(11).test {
            assertNull(awaitItem())
            dao.upsert(first)
            assertEquals(first, awaitItem())
            dao.upsert(second)
            assertEquals(second, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observe_returnsOnlyTheRowOfTheRequestedId() = runTest {
        // Arrange
        dao.upsert(entity(id = 11, title = "Eleven"))
        dao.upsert(entity(id = 12, title = "Twelve"))

        // Act
        val result = dao.observe(12).first()

        // Assert
        assertEquals(12, result?.id)
        assertEquals("Twelve", result?.title)
    }

    @Test
    fun observe_emitsNull_forAnIdThatWasNeverStored() = runTest {
        // Arrange
        dao.upsert(entity(id = 11))

        // Act
        val result = dao.observe(999).first()

        // Assert
        assertNull(result)
    }

    @Test
    fun nullableColumns_roundTripAsNull() = runTest {
        // Arrange
        val entity = entity(voteAverage = null, budget = null, tagline = null)

        // Act
        dao.upsert(entity)

        // Assert
        val stored = dao.observe(11).first()!!
        assertNull(stored.voteAverage)
        assertNull(stored.budget)
        assertNull(stored.tagline)
    }

    @Test
    fun emptyLists_roundTripAsEmpty() = runTest {
        // Arrange
        val entity = entity(
            genres = emptyList(),
            productionCompanies = emptyList(),
            spokenLanguages = emptyList(),
        )

        // Act
        dao.upsert(entity)

        // Assert
        val stored = dao.observe(11).first()!!
        assertEquals(emptyList<String>(), stored.genres)
        assertEquals(emptyList<String>(), stored.productionCompanies)
        assertEquals(emptyList<String>(), stored.spokenLanguages)
    }

    @Test
    fun lists_withSpecialCharacters_roundTripThroughTheTypeConverters() = runTest {
        // Arrange
        val companies = listOf("Studio | A", "Name, Inc.", "He said \"hi\"", "Café Müller", "أفلام")
        val entity = entity(productionCompanies = companies)

        // Act
        dao.upsert(entity)

        // Assert
        assertEquals(companies, dao.observe(11).first()!!.productionCompanies)
    }

    @Test
    fun budgetAboveIntRange_roundTripsAsLong() = runTest {
        // Arrange
        val entity = entity(budget = 3_000_000_000L)

        // Act
        dao.upsert(entity)

        // Assert
        assertEquals(3_000_000_000L, dao.observe(11).first()!!.budget)
    }
}