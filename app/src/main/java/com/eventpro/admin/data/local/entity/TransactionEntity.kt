package com.eventpro.admin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amountCents: Long,
    val type: String, // INCOME | EXPENSE
    val category: String, // VENUE | CATERING | LOGISTICS | MARKETING | STAFF | MISC
    val dateMillis: Long,
    val referenceNumber: String,
    val eventId: Long?,
    val clientOrVendorName: String
)
