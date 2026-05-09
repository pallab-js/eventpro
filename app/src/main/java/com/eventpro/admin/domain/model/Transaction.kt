package com.eventpro.admin.domain.model

import androidx.compose.runtime.Immutable

enum class TransactionType { INCOME, EXPENSE }
enum class ExpenseCategory(val displayName: String) {
    VENUE("Venue"),
    CATERING("Catering"),
    LOGISTICS("Logistics"),
    MARKETING("Marketing"),
    STAFF("Staff"),
    MISC("Miscellaneous")
}

@Immutable
data class Transaction(
    val id: Long = 0,
    val description: String,
    val amountCents: Long,
    val type: TransactionType,
    val category: ExpenseCategory,
    val dateMillis: Long,
    val referenceNumber: String,
    val eventId: Long?,
    val clientOrVendorName: String
)
