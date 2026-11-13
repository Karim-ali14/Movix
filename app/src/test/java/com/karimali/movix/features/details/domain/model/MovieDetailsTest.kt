package com.karimali.movix.features.details.domain.model

import com.karimali.movix.testutil.movieDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieDetailsTest {

    @Test
    fun `releaseYear should return year when releaseDate is valid`() {
        // Arrange
        val movie = movieDetails(
            releaseDate = "2024-05-10"
        )

        // Act
        val releaseYear = movie.releaseYear

        // Assert
        assertEquals("2024", releaseYear)
    }

    @Test
    fun `releaseYear should return null when releaseDate is null`() {
        // Arrange
        val movie = movieDetails(
            releaseDate = null
        )

        // Act
        val releaseYear = movie.releaseYear

        // Assert
        assertEquals(null, releaseYear)
    }

    @Test
    fun `releaseYear should return null when releaseDate has less than four digits`() {
        // Arrange
        val movie = movieDetails(
            releaseDate = "202"
        )

        // Act
        val releaseYear = movie.releaseYear

        // Assert
        assertNull(releaseYear)
    }

    @Test
    fun `releaseYear should return null when releaseDate contains non-digit characters`() {
        // Arrange
        val movie = movieDetails(
            releaseDate = "20ab-11-12"
        )

        // Act
        val releaseYear = movie.releaseYear

        // Assert
        assertEquals(null, releaseYear)
    }

    @Test
    fun `releaseYear should return null when releaseDate is empty`() {
        // Arrange
        val movie = movieDetails(
            releaseDate = ""
        )

        // Act
        val releaseYear = movie.releaseYear

        // Assert
        assertEquals(null, releaseYear)
    }

}

