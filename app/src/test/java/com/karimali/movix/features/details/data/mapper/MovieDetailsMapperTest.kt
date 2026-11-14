package com.karimali.movix.features.details.data.mapper

import com.google.gson.Gson
import com.karimali.movix.core.network.AppRouter
import com.karimali.movix.core.network.AppRouter.IMDB_TITLE_URL
import com.karimali.movix.core.testutil.BACKDROP_PATH
import com.karimali.movix.core.testutil.MOVIE_ID
import com.karimali.movix.core.testutil.POSTER_PATH
import com.karimali.movix.core.testutil.STAR_WARS_JSON
import com.karimali.movix.core.testutil.movieDetailsDto
import com.karimali.movix.core.testutil.movieDetailsEntity
import com.karimali.movix.features.details.data.datasource.remote.dto.CollectionDto
import com.karimali.movix.features.details.data.datasource.remote.dto.GenreDto
import com.karimali.movix.features.details.data.datasource.remote.dto.MovieDetailsDto
import com.karimali.movix.features.details.data.datasource.remote.dto.ProductionCompanyDto
import com.karimali.movix.features.details.data.datasource.remote.dto.SpokenLanguageDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieDetailsMapperTest {

    // ───────────────────────── DTO -> Entity ─────────────────────────

    @Test
    fun `toEntityOrNull maps every field of a complete dto`() {
        // Arrange
        val dto = movieDetailsDto()

        // Act
        val entity = dto.toEntityOrNull()

        // Assert
        assertEquals(movieDetailsEntity(), entity)
    }

    @Test
    fun `toEntityOrNull returns null when id is missing`() {
        // Arrange
        val dto = movieDetailsDto(id = null)

        // Act
        val entity = dto.toEntityOrNull()

        // Assert
        assertNull(entity)
    }

    @Test
    fun `toEntityOrNull returns null when title is missing`() {
        // Arrange
        val dto = movieDetailsDto(title = null)

        // Act
        val entity = dto.toEntityOrNull()

        // Assert
        assertNull(entity)
    }

    @Test
    fun `toEntityOrNull returns null when title is empty`() {
        // Arrange
        val dto = movieDetailsDto(title = "")

        // Act
        val entity = dto.toEntityOrNull()

        // Assert
        assertNull(entity)
    }

    @Test
    fun `toEntityOrNull returns null when title is only whitespace`() {
        // Arrange
        val dto = movieDetailsDto(title = "   ")

        // Act
        val entity = dto.toEntityOrNull()

        // Assert
        assertNull(entity)
    }

    @Test
    fun `toEntityOrNull turns blank optional strings into null`() {
        // Arrange
        val dto = movieDetailsDto(
            originalTitle = " ",
            tagline = "",
            overview = "  ",
            posterPath = "",
            backdropPath = " ",
            releaseDate = "",
            status = "",
            originalLanguage = " ",
            homepage = "",
            imdbId = " ",
            collection = CollectionDto(1, " "),
        )

        // Act
        val entity = dto.toEntityOrNull()!!

        // Assert
        assertNull(entity.originalTitle)
        assertNull(entity.tagline)
        assertNull(entity.overview)
        assertNull(entity.posterPath)
        assertNull(entity.backdropPath)
        assertNull(entity.releaseDate)
        assertNull(entity.status)
        assertNull(entity.originalLanguage)
        assertNull(entity.homepage)
        assertNull(entity.imdbId)
        assertNull(entity.collectionName)
    }

    @Test
    fun `toEntityOrNull keeps a positive runtime`() {
        // Arrange
        val dto = movieDetailsDto(runtime = 95)

        // Act
        val entity = dto.toEntityOrNull()!!

        // Assert
        assertEquals(95, entity.runtime)
    }

    @Test
    fun `toEntityOrNull treats zero runtime as unknown`() {
        // Arrange
        val dto = movieDetailsDto(runtime = 0)

        // Act
        val entity = dto.toEntityOrNull()!!

        // Assert
        assertNull(entity.runtime)
    }

    @Test
    fun `toEntityOrNull treats negative runtime as unknown`() {
        // Arrange
        val dto = movieDetailsDto(runtime = -5)

        // Act
        val entity = dto.toEntityOrNull()!!

        // Assert
        assertNull(entity.runtime)
    }

    @Test
    fun `toEntityOrNull treats zero budget and revenue as unknown`() {
        // Arrange
        val dto = movieDetailsDto(budget = 0L, revenue = 0L)

        // Act
        val entity = dto.toEntityOrNull()!!

        // Assert
        assertNull(entity.budget)
        assertNull(entity.revenue)
    }

    @Test
    fun `toEntityOrNull treats negative budget and revenue as unknown`() {
        // Arrange
        val dto = movieDetailsDto(budget = -1L, revenue = -1L)

        // Act
        val entity = dto.toEntityOrNull()!!

        // Assert
        assertNull(entity.budget)
        assertNull(entity.revenue)
    }

    @Test
    fun `toEntityOrNull keeps budget and revenue above Int range`() {
        // Arrange
        val dto = movieDetailsDto(budget = 3_000_000_000L, revenue = 5_000_000_000L)

        // Act
        val entity = dto.toEntityOrNull()!!

        // Assert
        assertEquals(3_000_000_000L, entity.budget)
        assertEquals(5_000_000_000L, entity.revenue)
    }

    @Test
    fun `toEntityOrNull uses empty lists when collections are null`() {
        // Arrange
        val dto = movieDetailsDto(genres = null, productionCompanies = null, spokenLanguages = null)

        // Act
        val entity = dto.toEntityOrNull()!!

        // Assert
        assertEquals(emptyList<String>(), entity.genres)
        assertEquals(emptyList<String>(), entity.productionCompanies)
        assertEquals(emptyList<String>(), entity.spokenLanguages)
    }

    @Test
    fun `toEntityOrNull drops genres with null or blank names and keeps order`() {
        // Arrange
        val dto = movieDetailsDto(
            genres = listOf(
                GenreDto(1, "Drama"),
                GenreDto(2, null),
                GenreDto(3, "  "),
                GenreDto(4, "Comedy"),
            ),
        )

        // Act
        val entity = dto.toEntityOrNull()!!

        // Assert
        assertEquals(listOf("Drama", "Comedy"), entity.genres)
    }

    @Test
    fun `toEntityOrNull drops production companies with null or blank names`() {
        // Arrange
        val dto = movieDetailsDto(
            productionCompanies = listOf(
                ProductionCompanyDto(1, "Pixar"),
                ProductionCompanyDto(2, null),
                ProductionCompanyDto(3, ""),
            ),
        )

        // Act
        val entity = dto.toEntityOrNull()!!

        // Assert
        assertEquals(listOf("Pixar"), entity.productionCompanies)
    }

    @Test
    fun `toEntityOrNull maps spoken languages by english name and ignores the code`() {
        // Arrange
        val dto = movieDetailsDto(
            spokenLanguages = listOf(
                SpokenLanguageDto("fr", "French"),
                SpokenLanguageDto("xx", null),
                SpokenLanguageDto("de", ""),
            ),
        )

        // Act
        val entity = dto.toEntityOrNull()!!

        // Assert
        assertEquals(listOf("French"), entity.spokenLanguages)
    }

    @Test
    fun `toEntityOrNull leaves collectionName null when the movie has no collection`() {
        // Arrange
        val dto = movieDetailsDto(collection = null)

        // Act
        val entity = dto.toEntityOrNull()!!

        // Assert
        assertNull(entity.collectionName)
    }

    // ───────────────────────── Entity -> Domain ─────────────────────────

    @Test
    fun `toDomain maps every field and builds absolute urls`() {
        // Arrange
        val entity = movieDetailsEntity()

        // Act
        val domain = entity.toDomain()

        // Assert
        assertEquals(MOVIE_ID, domain.id)
        assertEquals("Star Wars", domain.title)
        assertEquals("Star Wars", domain.originalTitle)
        assertEquals(entity.tagline, domain.tagline)
        assertEquals(entity.overview, domain.overview)
        assertEquals("${AppRouter.POSTER_BASE_URL}$POSTER_PATH", domain.posterUrl)
        assertEquals("${AppRouter.BACKDROP_BASE_URL}$BACKDROP_PATH", domain.backdropUrl)
        assertEquals("1977-05-25", domain.releaseDate)
        assertEquals(121, domain.runtimeMinutes)
        assertEquals(8.2, requireNotNull(domain.rating), 0.001)
        assertEquals(22_061, domain.voteCount)
        assertEquals("Released", domain.status)
        assertEquals("en", domain.originalLanguageCode)
        assertEquals(11_000_000L, domain.budget)
        assertEquals(775_398_007L, domain.revenue)
        assertEquals(entity.homepage, domain.homepage)
        assertEquals("${IMDB_TITLE_URL}tt0076759", domain.imdbUrl)
        assertEquals("Star Wars Collection", domain.collectionName)
        assertEquals(entity.genres, domain.genres)
        assertEquals(entity.productionCompanies, domain.productionCompanies)
        assertEquals(entity.spokenLanguages, domain.spokenLanguages)
    }

    @Test
    fun `toDomain falls back to title when originalTitle is null`() {
        // Arrange
        val entity = movieDetailsEntity(title = "Le Film", originalTitle = null)

        // Act
        val domain = entity.toDomain()

        // Assert
        assertEquals("Le Film", domain.originalTitle)
    }

    @Test
    fun `toDomain applies safe defaults for every nullable field`() {
        // Arrange
        val entity = movieDetailsEntity(
            originalTitle = null,
            tagline = null,
            overview = null,
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            runtime = null,
            voteAverage = null,
            voteCount = null,
            status = null,
            originalLanguage = null,
            budget = null,
            revenue = null,
            homepage = null,
            imdbId = null,
            collectionName = null,
            genres = emptyList(),
            productionCompanies = emptyList(),
            spokenLanguages = emptyList(),
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertEquals("", domain.tagline)
        assertEquals("", domain.overview)
        assertEquals("", domain.releaseDate)
        assertEquals("", domain.status)
        assertEquals("", domain.originalLanguageCode)
        assertNull(domain.rating)
        assertEquals(0, domain.voteCount)
        assertEquals(0L, domain.budget)
        assertEquals(0L, domain.revenue)
        assertNull(domain.runtimeMinutes)
        assertNull(domain.homepage)
        assertNull(domain.collectionName)
        assertEquals(emptyList<String>(), domain.genres)
    }

    @Test
    fun `toDomain leaves urls when paths and imdb id are missing`() {
        // Arrange
        val entity = movieDetailsEntity(posterPath = null, backdropPath = null, imdbId = null)

        // Act
        val domain = entity.toDomain()

        // Assert
        assertNull(domain.posterUrl)
        assertNull(domain.backdropUrl)
        assertNull(domain.imdbUrl)
    }

    // ───────────────────────── Pipeline ─────────────────────────

    @Test
    fun `real TMDb json flows from dto to entity to domain`() {
        // Arrange
        val dto = Gson().fromJson(STAR_WARS_JSON, MovieDetailsDto::class.java)

        // Act
        val domain = requireNotNull(dto.toEntityOrNull()).toDomain()

        // Assert
        assertEquals(11, domain.id)
        assertEquals("Star Wars", domain.title)
        assertEquals("1977", domain.releaseYear)
        assertEquals(121, domain.runtimeMinutes)
        assertEquals(listOf("Adventure", "Action", "Science Fiction"), domain.genres)
        assertEquals(listOf("Lucasfilm Ltd.", "20th Century Fox"), domain.productionCompanies)
        assertEquals(listOf("English"), domain.spokenLanguages)
        assertEquals("Star Wars Collection", domain.collectionName)
        assertEquals(11_000_000L, domain.budget)
        assertEquals(775_398_007L, domain.revenue)
        assertEquals("${IMDB_TITLE_URL}tt0076759", domain.imdbUrl)
        assertEquals("${AppRouter.POSTER_BASE_URL}$POSTER_PATH", domain.posterUrl)
    }
}