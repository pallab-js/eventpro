package com.eventpro.admin.data.local.dao

import androidx.room.*
import com.eventpro.admin.data.local.entity.EventVendorEntity
import com.eventpro.admin.data.local.entity.VendorEntity
import kotlinx.coroutines.flow.Flow

data class EventVendorWithVendor(
    val eventId: Long,
    val vendorId: Long,
    val status: String,
    val depositPaidCents: Long,
    val contractSigned: Boolean,
    val name: String,
    val category: String,
    val phone: String,
    val email: String
)

@Dao
interface VendorDao {
    @Query("SELECT * FROM vendors ORDER BY name ASC")
    fun getAllVendors(): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE id = :id")
    fun getVendorById(id: Long): Flow<VendorEntity?>

    @Upsert
    suspend fun upsertVendor(v: VendorEntity): Long

    @Delete
    suspend fun deleteVendor(v: VendorEntity)

    @Query("SELECT * FROM event_vendors WHERE eventId = :eventId")
    fun getEventVendors(eventId: Long): Flow<List<EventVendorEntity>>

    @Query("""
        SELECT ev.eventId, ev.vendorId, ev.status, ev.depositPaidCents, ev.contractSigned,
               v.name, v.category, v.phone, v.email
        FROM event_vendors ev
        INNER JOIN vendors v ON v.id = ev.vendorId
        WHERE ev.eventId = :eventId
        ORDER BY v.name ASC
    """)
    fun getEventVendorsWithDetails(eventId: Long): Flow<List<EventVendorWithVendor>>

    @Upsert
    suspend fun upsertEventVendor(ev: EventVendorEntity)

    @Delete
    suspend fun deleteEventVendor(ev: EventVendorEntity)
}
