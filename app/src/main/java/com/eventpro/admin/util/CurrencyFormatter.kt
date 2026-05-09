package com.eventpro.admin.util

import java.text.NumberFormat
import java.util.*

object CurrencyFormatter {
    private val fmt = NumberFormat.getCurrencyInstance(Locale.US)

    fun formatCents(cents: Long): String = fmt.format(cents / 100.0)

    fun formatCentsCompact(cents: Long): String {
        val dollars = cents / 100.0
        return when {
            dollars >= 1_000_000 -> "$%.2fM".format(dollars / 1_000_000)
            dollars >= 1_000 -> "$%.1fK".format(dollars / 1_000)
            else -> fmt.format(dollars)
        }
    }
}
