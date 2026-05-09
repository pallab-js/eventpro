package com.eventpro.admin.repository

import com.eventpro.admin.data.local.dao.InventoryDao
import com.eventpro.admin.domain.model.InventoryItem
import com.eventpro.admin.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepositoryImpl @Inject constructor(private val dao: InventoryDao) : InventoryRepository {
    override fun getAllItems() = dao.getAllItems().map { it.map { i -> i.toDomain() } }
    override fun getItemsByCategory(category: String) = dao.getItemsByCategory(category).map { it.map { i -> i.toDomain() } }
    override suspend fun upsertItem(item: InventoryItem) = dao.upsertItem(item.toEntity())
    override suspend fun deleteItem(item: InventoryItem) = dao.deleteItem(item.toEntity())
}
