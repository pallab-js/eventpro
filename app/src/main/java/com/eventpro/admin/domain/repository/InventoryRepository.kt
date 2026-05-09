package com.eventpro.admin.domain.repository

import com.eventpro.admin.domain.model.InventoryItem
import com.eventpro.admin.domain.model.InventoryReservation
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getAllItems(): Flow<List<InventoryItem>>
    fun getItemsByCategory(category: String): Flow<List<InventoryItem>>
    suspend fun upsertItem(item: InventoryItem): Long
    suspend fun deleteItem(item: InventoryItem)
    fun getReservationsForEvent(eventId: Long): Flow<List<InventoryReservation>>
    suspend fun reserveItem(eventId: Long, itemId: Long, quantity: Int)
    suspend fun releaseReservation(eventId: Long, itemId: Long)
    suspend fun deleteReservation(reservationId: Long)
}
