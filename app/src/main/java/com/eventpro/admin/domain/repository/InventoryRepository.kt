package com.eventpro.admin.domain.repository

import com.eventpro.admin.domain.model.InventoryItem
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getAllItems(): Flow<List<InventoryItem>>
    fun getItemsByCategory(category: String): Flow<List<InventoryItem>>
    suspend fun upsertItem(item: InventoryItem): Long
    suspend fun deleteItem(item: InventoryItem)
}
