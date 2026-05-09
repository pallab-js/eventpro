package com.eventpro.admin.domain.model

enum class TransactionType { INCOME, EXPENSE }
enum class ExpenseCategory { VENUE, CATERING, LOGISTICS, MARKETING, STAFF, MISC }

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
