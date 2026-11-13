package com.karimali.movix.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.karimali.movix.R
import com.karimali.movix.core.error.DomainError
import com.karimali.movix.core.error.DomainException

fun Throwable.asDomainError(): DomainError =
    (this as? DomainException)?.error ?: DomainError.Unknown(message)

@Composable
fun DomainError.asMessage(): String = when (this) {
    DomainError.NoInternet -> stringResource(R.string.error_no_internet)
    DomainError.Timeout -> stringResource(R.string.error_timeout)
    DomainError.Unauthorized -> stringResource(R.string.error_unauthorized)
    DomainError.NotFound -> stringResource(R.string.error_not_found)
    DomainError.RateLimited -> stringResource(R.string.error_rate_limited)
    is DomainError.Server -> stringResource(R.string.error_server, code)
    is DomainError.Unknown -> stringResource(R.string.error_unknown)
}