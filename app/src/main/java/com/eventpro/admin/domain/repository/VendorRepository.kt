package com.eventpro.admin.domain.repository

import com.eventpro.admin.domain.model.EventVendor
import com.eventpro.admin.domain.model.Vendor
import kotlinx.coroutines.flow.Flow

interface VendorRepository {
    fun getAllVendors(): Flow<List<Vendor>>
    fun getEventVendors(eventId: Long): Flow<List<EventVendor>>
    suspend fun upsertVendor(vendor: Vendor): Long
    suspend fun upsertEventVendor(eventVendor: EventVendor)
    suspend fun deleteEventVendor(eventVendor: EventVendor)
}
