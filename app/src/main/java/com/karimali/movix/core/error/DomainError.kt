package com.karimali.movix.core.error

sealed interface DomainError {
    data object NoInternet : DomainError
    data object Timeout : DomainError
    data object Unauthorized : DomainError
    data object NotFound : DomainError
    data object RateLimited : DomainError
    data class Server(val code: Int) : DomainError
    data class Unknown(val message: String?) : DomainError
}