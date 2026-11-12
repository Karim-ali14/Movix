package com.karimali.movix.features.movies.data.mapper

import com.karimali.movix.core.network.AppRouter
import com.karimali.movix.testutil.movieDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test


class MovieMappersTest {

    // ---------- toEntityOrNull ----------

    @Test
    fun `toEntityOrNull rejects dto with missing id`() {
        // Arrange
        val dto = movieDto(id = null)

        // Act
        val entity = dto.toEntityOrNull(page = 1, position = 0)

        // Assert
        assertNull(entity)
    }

    @Test
    fun `toEntityOrNull rejects dto with null title`() {
        // Arrange
        val dto = movieDto(title = null)

        // Act
        val entity = dto.toEntityOrNull(page = 1, position = 0)

        // Assert
        assertNull(entity)
    }

    @Test
    fun `toEntityOrNull rejects dto with blank title`() {
        // Arrange
        val dto = movieDto(title = "   ")

        // Act
        val entity = dto.toEntityOrNull(page = 1, position = 0)

        // Assert
        assertNull(entity)
    }

    @Test
    fun `toEntityOrNull maps valid dto keeping page position and core data`() {
        // Arrange
        val dto = movieDto(id = 7, title = "Up", genreIds = listOf(1, 2))

        // Act
        val entity = dto.toEntityOrNull(page = 3, position = 4)

        // Assert
        assertEquals(7, entity?.id)
        assertEquals("Up", entity?.title)
        assertEquals(3, entity?.page)
        assertEquals(4, entity?.position)
        assertEquals(listOf(1, 2), entity?.genreIds)
    }

    @Test
    fun `toEntityOrNull maps null genre ids to empty list`() {
        // Arrange
        val dto = movieDto(genreIds = null)

        // Act
        val entity = dto.toEntityOrNull(page = 1, position = 0)

        // Assert
        assertEquals(emptyList<Int>(), entity?.genreIds)
    }

    // ---------- toDomainOrNull ----------

    @Test
    fun `toDomainOrNull rejects dto with missing id`() {
        // Arrange
        val dto = movieDto(id = null)

        // Act
        val movie = dto.toDomainOrNull()

        // Assert
        assertNull(movie)
    }

    @Test
    fun `toDomainOrNull rejects dto with empty title`() {
        // Arrange
        val dto = movieDto(title = "")

        // Act
        val movie = dto.toDomainOrNull()

        // Assert
        assertNull(movie)
    }

    @Test
    fun `toDomainOrNull builds full poster url`() {
        // Arrange
        val dto = movieDto(posterPath = "/abc.jpg")

        // Act
        val movie = dto.toDomainOrNull()

        // Assert
        assertEquals("${AppRouter.POSTER_BASE_URL}/abc.jpg", movie?.posterUrl)
    }

    @Test
    fun `toDomainOrNull without poster path has null url`() {
        // Arrange
        val dto = movieDto(posterPath = null)

        // Act
        val movie = dto.toDomainOrNull()

        // Assert
        assertNull(movie?.posterUrl)
    }
}
