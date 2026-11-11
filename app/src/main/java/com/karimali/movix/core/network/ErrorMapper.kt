package com.karimali.movix.core.network

import com.google.gson.JsonSyntaxException
import com.karimali.movix.core.error.DomainError
import com.karimali.movix.core.error.DomainException
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

fun Throwable.toDomainException(): DomainException =
    this as? DomainException ?: DomainException(toDomainError())

private fun Throwable.toDomainError(): DomainError = when (this) {
    is CancellationException -> throw this
    is SocketTimeoutException,
    is InterruptedIOException -> DomainError.Timeout

    is JsonSyntaxException -> DomainError.Unknown("Data parsing error occurred.")
    is HttpException -> when (val c = code()) {
        401 -> DomainError.Unauthorized
        404 -> DomainError.NotFound
        429 -> DomainError.RateLimited
        in 500..599 -> DomainError.Server(c)
        else -> DomainError.Unknown(message())
    }

    is IOException -> DomainError.NoInternet
    else -> DomainError.Unknown(message)
}