package com.karimali.movix.features.details.presentation.formatter

import java.util.Locale

internal fun formatRuntime(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

/** Returns null when the amount is unknown (0 or negative). */
internal fun formatMoney(amount: Long): String? = when {
    amount <= 0 -> null
    amount >= 1_000_000_000 -> String.format(Locale.US, "\$%.1fB", amount / 1_000_000_000.0)
    amount >= 1_000_000 -> String.format(Locale.US, "\$%.1fM", amount / 1_000_000.0)
    amount >= 1_000 -> String.format(Locale.US, "\$%.0fK", amount / 1_000.0)
    else -> "\$$amount"
}

internal fun languageName(code: String): String {
    if (code.isBlank()) return ""
    val name = Locale.forLanguageTag(code).getDisplayLanguage(Locale.ENGLISH)
    return name.ifBlank { code.uppercase(Locale.ROOT) }
}