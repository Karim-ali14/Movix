package com.karimali.movix.core.error

class DomainException(
    val error: DomainError,
    cause: Throwable? = null
) : Exception(cause)