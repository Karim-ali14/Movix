package com.karimali.movix.features.movies.domain.model

import junit.framework.Assert.assertEquals
import org.junit.Test

class MovieTest {

    @Test
    fun `releaseYear should return year when releaseDate valid`(){
        // Arrange
        val movie = Movie(
            id = 1,
            title = "Batman",
            releaseDate = "2024-05-10"
        )

        // Act
        val releaseYear = movie.releaseYear

        // Assert
        assertEquals("2024" , releaseYear)
    }

    @Test
    fun `releaseYear returns null when releaseDate is less then 4 digits`() {
        // Arrange
        val movie = Movie(
            id = 1,
            title = "Test Movie",
            releaseDate = "202"
        )
        // Act
        val releaseYear = movie.releaseYear

        // Assert
        assertEquals(null, releaseYear)
    }

    @Test
    fun `releaseYear returns null when releaseDate contains non-digits`() {
        // Arrange
        val movie = Movie(
            id = 1,
            title = "Test Movie",
            releaseDate = "20ab-11-12"
        )
        // Act
        val releaseYear = movie.releaseYear

        // Assert
        assertEquals(null, releaseYear)
    }

    @Test
    fun `releaseYear returns null when releaseDate is empty or blank`() {
        // Arrange
        val movie = Movie(
            id = 1,
            title = "Test Movie",
            releaseDate = ""
        )
        // Act
        val releaseYear = movie.releaseYear

        // Assert
        assertEquals(null, releaseYear)
    }
}