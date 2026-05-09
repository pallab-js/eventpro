package com.eventpro.admin.data.local.dao

import androidx.room.*
import com.eventpro.admin.data.local.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE category = :category ORDER BY name ASC")
    fun getItemsByCategory(category: String): Flow<List<InventoryItemEntity>>

    @Upsert
    suspend fun upsertItem(item: InventoryItemEntity): Long

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)
}
