package com.eventpro.admin.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class InventoryReservation(
    val id: Long = 0,
    val itemId: Long,
    val eventId: Long,
    val quantityReserved: Int,
    val reservedAtMillis: Long,
    val itemName: String = "",
    val itemCategory: InventoryCategory = InventoryCategory.OTHER
)
