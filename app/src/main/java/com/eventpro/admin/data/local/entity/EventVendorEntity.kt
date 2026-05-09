package com.eventpro.admin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "event_vendors",
    primaryKeys = ["eventId", "vendorId"],
    foreignKeys = [
        ForeignKey(EventEntity::class, ["id"], ["eventId"], ForeignKey.CASCADE),
        ForeignKey(VendorEntity::class, ["id"], ["vendorId"], ForeignKey.CASCADE)
    ]
)
data class EventVendorEntity(
    val eventId: Long,
    val vendorId: Long,
    val status: String, // CONFIRMED | PENDING_CONTRACT | AWAITING_DEPOSIT | CANCELLED
    val depositPaidCents: Long,
    val contractSigned: Boolean
)
