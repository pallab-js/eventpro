package com.eventpro.admin.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    fun formatCents(cents: Long): String =
        NumberFormat.getCurrencyInstance(Locale.US).format(cents / 100.0)

    fun formatCentsCompact(cents: Long): String {
        val dollars = cents / 100.0
        val fmt = NumberFormat.getCurrencyInstance(Locale.US)
        return when {
            dollars >= 1_000_000 -> "$%.2fM".format(dollars / 1_000_000)
            dollars >= 1_000 -> "$%.1fK".format(dollars / 1_000)
            else -> fmt.format(dollars)
        }
    }
}
