package com.karimali.movix.core.network

import com.google.gson.JsonSyntaxException
import com.karimali.movix.core.error.DomainError
import com.karimali.movix.core.error.DomainException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

class ToDomainExceptionTest {

    private fun createHttpException(code: Int): HttpException {
        val emptyBody = "".toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(code, emptyBody))
    }

    private fun Throwable.toDomainError(): DomainError = this.toDomainException().error

    @Test
    fun `socket timeout maps to Timeout error`() {
        val error = SocketTimeoutException().toDomainError()
        assertEquals(DomainError.Timeout, error)
    }

    /** OkHttp's callTimeout throws InterruptedIOException, not SocketTimeoutException. */
    @Test
    fun `call timeout throws InterruptedIOException maps to Timeout error`() {
        val error = InterruptedIOException("timeout").toDomainError()
        assertEquals(DomainError.Timeout, error)
    }

    @Test
    fun `unknown host maps to NoInternet error`() {
        val error = UnknownHostException().toDomainError()
        assertEquals(DomainError.NoInternet, error)
    }

    @Test
    fun `generic IO exception maps to NoInternet error`() {
        val error = IOException("connection reset").toDomainError()
        assertEquals(DomainError.NoInternet, error)
    }

    @Test
    fun `http 401 maps to Unauthorized error`() {
        val error = createHttpException(401).toDomainError()
        assertEquals(DomainError.Unauthorized, error)
    }

    @Test
    fun `http 404 maps to NotFound error`() {
        val error = createHttpException(404).toDomainError()
        assertEquals(DomainError.NotFound, error)
    }

    @Test
    fun `http 429 maps to RateLimited error`() {
        val error = createHttpException(429).toDomainError()
        assertEquals(DomainError.RateLimited, error)
    }

    @Test
    fun `http 5xx maps to Server error with correct code`() {
        val error = createHttpException(503).toDomainError()
        assertEquals(DomainError.Server(503), error)
    }

    @Test
    fun `malformed body json syntax exception maps to Unknown error`() {
        val error = JsonSyntaxException("Expected BEGIN_OBJECT").toDomainError()
        assertTrue(error is DomainError.Unknown)
    }

    @Test
    fun `domain exception is returned as is without wrapping`() {
        val original = DomainException(DomainError.NotFound)
        assertSame(original, original.toDomainException())
    }

    /** Cancellation is control flow: converting it would break structured concurrency. */
    @Test
    fun `cancellation exception is rethrown and not converted`() {
        assertThrows(CancellationException::class.java) {
            CancellationException("cancelled").toDomainException()
        }
    }
}