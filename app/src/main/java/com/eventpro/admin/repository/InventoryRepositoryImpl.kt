package com.eventpro.admin.repository

import com.eventpro.admin.data.local.dao.InventoryDao
import com.eventpro.admin.data.local.dao.InventoryReservationDao
import com.eventpro.admin.data.local.entity.InventoryReservationEntity
import com.eventpro.admin.domain.model.InventoryCategory
import com.eventpro.admin.domain.model.InventoryItem
import com.eventpro.admin.domain.model.InventoryReservation
import com.eventpro.admin.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val dao: InventoryDao,
    private val reservationDao: InventoryReservationDao
) : InventoryRepository {
    override fun getAllItems() = dao.getAllItems().map { it.map { i -> i.toDomain() } }
    override fun getItemsByCategory(category: String) = dao.getItemsByCategory(category).map { it.map { i -> i.toDomain() } }
    override suspend fun upsertItem(item: InventoryItem) = dao.upsertItem(item.toEntity())
    override suspend fun deleteItem(item: InventoryItem) = dao.deleteItem(item.toEntity())

    override fun getReservationsForEvent(eventId: Long): Flow<List<InventoryReservation>> {
        return combine(
            reservationDao.getReservationsForEvent(eventId),
            dao.getAllItems()
        ) { reservations, items ->
            val itemMap = items.associateBy { it.id }
            reservations.map { r ->
                val item = itemMap[r.itemId]
                InventoryReservation(
                    id = r.id,
                    itemId = r.itemId,
                    eventId = r.eventId,
                    quantityReserved = r.quantityReserved,
                    reservedAtMillis = r.reservedAtMillis,
                    itemName = item?.name ?: "Unknown",
                    itemCategory = item?.category?.let { safeValueOf(it, InventoryCategory.OTHER) } ?: InventoryCategory.OTHER
                )
            }
        }
    }

    override suspend fun reserveItem(eventId: Long, itemId: Long, quantity: Int) {
        val totalReserved = reservationDao.getTotalReservedForItem(itemId)
        val items = dao.getAllItems().first()
        val item = items.find { it.id == itemId } ?: return
        val newReserved = totalReserved + quantity
        if (newReserved <= item.availableUnits) {
            reservationDao.upsertReservation(
                InventoryReservationEntity(itemId = itemId, eventId = eventId, quantityReserved = quantity, reservedAtMillis = System.currentTimeMillis())
            )
        }
    }

    override suspend fun releaseReservation(eventId: Long, itemId: Long) {
        reservationDao.deleteReservationByEventAndItem(eventId, itemId)
    }

    override suspend fun deleteReservation(reservationId: Long) {
        reservationDao.deleteReservation(reservationId)
    }
}
