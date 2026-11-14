package com.karimali.movix.features.movies.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.karimali.movix.R
import com.karimali.movix.core.ui.theme.AppSpacing

/** Screen title, the search field and a thin progress line that reserves its own space. */
@Composable
internal fun MoviesHeader(
    query: String,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = AppSpacing.md)
                .padding(top = AppSpacing.xs),
        ) {
            Text(
                text = stringResource(R.string.movies_title),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(AppSpacing.xs))

            SearchField(
                query = query,
                onQueryChange = onQueryChange,
                onClear = onClear,
            )

            Spacer(Modifier.height(AppSpacing.xs))

            Box(Modifier.fillMaxWidth().height(AppSpacing.xxs)) {
                if (isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }
    }
}