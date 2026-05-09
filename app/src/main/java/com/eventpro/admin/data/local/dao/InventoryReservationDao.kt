package com.eventpro.admin.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.eventpro.admin.data.local.entity.InventoryReservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryReservationDao {
    @Query("SELECT * FROM inventory_reservations WHERE eventId = :eventId")
    fun getReservationsForEvent(eventId: Long): Flow<List<InventoryReservationEntity>>

    @Query("SELECT * FROM inventory_reservations WHERE itemId = :itemId")
    fun getReservationsForItem(itemId: Long): Flow<List<InventoryReservationEntity>>

    @Query("SELECT COALESCE(SUM(quantityReserved), 0) FROM inventory_reservations WHERE itemId = :itemId")
    suspend fun getTotalReservedForItem(itemId: Long): Int

    @Upsert
    suspend fun upsertReservation(reservation: InventoryReservationEntity)

    @Query("DELETE FROM inventory_reservations WHERE id = :id")
    suspend fun deleteReservation(id: Long)

    @Query("DELETE FROM inventory_reservations WHERE eventId = :eventId AND itemId = :itemId")
    suspend fun deleteReservationByEventAndItem(eventId: Long, itemId: Long)
}
