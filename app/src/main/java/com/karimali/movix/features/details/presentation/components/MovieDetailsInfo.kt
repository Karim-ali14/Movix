package com.karimali.movix.features.details.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.karimali.movix.R
import com.karimali.movix.core.ui.theme.AppSpacing
import com.karimali.movix.features.details.domain.model.MovieDetails
import com.karimali.movix.features.details.presentation.contract.MovieDetailsIntent
import com.karimali.movix.core.util.formatter.formatMoney
import com.karimali.movix.core.util.formatter.languageName

/** Shown wherever a value is unknown. */
internal const val NOT_AVAILABLE = "–"

/** Status / language / budget / revenue as a 2x2 grid of tiles. */
@Composable
internal fun InfoGrid(movie: MovieDetails, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            InfoTile(
                label = stringResource(R.string.details_status),
                value = movie.status.ifBlank { NOT_AVAILABLE },
                modifier = Modifier.weight(1f),
            )
            InfoTile(
                label = stringResource(R.string.details_language),
                value = languageName(movie.originalLanguageCode).ifBlank { NOT_AVAILABLE },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            InfoTile(
                label = stringResource(R.string.details_budget),
                value = movie.budget?.let { formatMoney(it) } ?: NOT_AVAILABLE,
                modifier = Modifier.weight(1f),
            )
            InfoTile(
                label = stringResource(R.string.details_revenue),
                value = movie.revenue?.let { formatMoney(it) } ?: NOT_AVAILABLE,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun InfoTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun ProductionSection(movie: MovieDetails, modifier: Modifier = Modifier) {
    if (movie.productionCompanies.isEmpty() && movie.spokenLanguages.isEmpty()) return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        if (movie.productionCompanies.isNotEmpty()) {
            LabeledText(
                title = stringResource(R.string.details_production),
                value = movie.productionCompanies.joinToString(", "),
            )
        }
        if (movie.spokenLanguages.isNotEmpty()) {
            LabeledText(
                title = stringResource(R.string.details_spoken_languages),
                value = movie.spokenLanguages.joinToString(", "),
            )
        }
    }
}

@Composable
private fun LabeledText(title: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
        SectionTitle(title)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Website / IMDb buttons; each one appears only when the movie has that link. */
/** Website / IMDb buttons; each one appears only when the movie has a non-blank link. */
@Composable
internal fun LinksRow(
    movie: MovieDetails,
    onIntent: (MovieDetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasWebsite = !movie.homepage.isNullOrBlank()
    val hasImdb = !movie.imdbUrl.isNullOrBlank()

    if (!hasWebsite && !hasImdb) return

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        if (hasWebsite) {
            OutlinedButton(
                onClick = { onIntent(MovieDetailsIntent.WebsiteClicked) },
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.details_website)) }
        }
        if (hasImdb) {
            OutlinedButton(
                onClick = { onIntent(MovieDetailsIntent.ImdbClicked) },
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.details_imdb)) }
        }
    }
}