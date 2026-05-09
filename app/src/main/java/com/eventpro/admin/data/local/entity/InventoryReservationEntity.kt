package com.eventpro.admin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_reservations",
    foreignKeys = [
        ForeignKey(
            entity = InventoryItemEntity::class, parentColumns = ["id"],
            childColumns = ["itemId"], onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EventEntity::class, parentColumns = ["id"],
            childColumns = ["eventId"], onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("itemId"), Index("eventId")
    ]
)
data class InventoryReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val eventId: Long,
    val quantityReserved: Int,
    val reservedAtMillis: Long
)
