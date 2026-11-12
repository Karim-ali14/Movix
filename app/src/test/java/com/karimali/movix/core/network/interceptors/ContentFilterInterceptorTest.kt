package com.karimali.movix.core.network.interceptors

import com.karimali.movix.core.network.AppRouter
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContentFilterInterceptorTest {

    private val interceptor = ContentFilterInterceptor()

    private fun url(path: String, query: String = "") =
        "https://api.themoviedb.org/3/$path$query"

    private fun forwardedUrlFor(originalUrl: String): HttpUrl {
        val request = Request.Builder().url(originalUrl).build()
        val forwarded = slot<Request>()
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.proceed(capture(forwarded)) } returns Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        interceptor.intercept(chain)

        return forwarded.captured.url
    }

    @Test
    fun `discover request gets adult and certification filters`() {
        // Arrange
        val original = url(AppRouter.DISCOVER_MOVIES, "?page=1")

        // Act
        val forwarded = forwardedUrlFor(original)

        // Assert
        assertEquals("false", forwarded.queryParameter("include_adult"))
        assertEquals("US", forwarded.queryParameter("certification_country"))
        assertEquals("PG-13", forwarded.queryParameter("certification.lte"))
    }

    @Test
    fun `search request gets only the adult filter`() {
        // Arrange
        val original = url(AppRouter.SEARCH_MOVIES, "?query=batman&page=1")

        // Act
        val forwarded = forwardedUrlFor(original)

        // Assert
        assertEquals("false", forwarded.queryParameter("include_adult"))
        assertNull(forwarded.queryParameter("certification_country"))
        assertNull(forwarded.queryParameter("certification.lte"))
    }

    @Test
    fun `existing query parameters are preserved`() {
        // Arrange
        val original = url(AppRouter.SEARCH_MOVIES, "?query=batman&page=3")

        // Act
        val forwarded = forwardedUrlFor(original)

        // Assert
        assertEquals("batman", forwarded.queryParameter("query"))
        assertEquals("3", forwarded.queryParameter("page"))
    }

    @Test
    fun `other endpoints are forwarded untouched`() {
        // Arrange
        val original = url("movie/123")

        // Act
        val forwarded = forwardedUrlFor(original)

        // Assert
        assertEquals(original, forwarded.toString())
    }

    /** A pre-existing include_adult=true must be overridden, not duplicated. */
    @Test
    fun `pre-existing adult parameter is overridden not duplicated`() {
        // Arrange
        val original = url(AppRouter.DISCOVER_MOVIES, "?include_adult=true")

        // Act
        val forwarded = forwardedUrlFor(original)

        // Assert
        assertEquals(listOf("false"), forwarded.queryParameterValues("include_adult"))
    }
}