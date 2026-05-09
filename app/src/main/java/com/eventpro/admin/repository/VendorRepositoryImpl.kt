package com.eventpro.admin.repository

import com.eventpro.admin.data.local.dao.VendorDao
import com.eventpro.admin.domain.model.EventVendor
import com.eventpro.admin.domain.model.Vendor
import com.eventpro.admin.domain.model.VendorCategory
import com.eventpro.admin.domain.model.VendorStatus
import com.eventpro.admin.domain.repository.VendorRepository
import com.eventpro.admin.data.local.entity.EventVendorEntity
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorRepositoryImpl @Inject constructor(private val dao: VendorDao) : VendorRepository {
    override fun getAllVendors() = dao.getAllVendors().map { it.map { v -> v.toDomain() } }
    override fun getEventVendors(eventId: Long) = dao.getEventVendorsWithDetails(eventId).map { list ->
        list.map { ev ->
            EventVendor(
                eventId = ev.eventId,
                vendor = Vendor(ev.vendorId, ev.name, VendorCategory.valueOf(ev.category), ev.phone, ev.email),
                status = VendorStatus.valueOf(ev.status),
                depositPaidCents = ev.depositPaidCents,
                contractSigned = ev.contractSigned
            )
        }
    }
    override suspend fun upsertVendor(vendor: Vendor) = dao.upsertVendor(vendor.toEntity())
    override suspend fun upsertEventVendor(eventVendor: EventVendor) =
        dao.upsertEventVendor(EventVendorEntity(eventVendor.eventId, eventVendor.vendor.id, eventVendor.status.name, eventVendor.depositPaidCents, eventVendor.contractSigned))
    override suspend fun deleteEventVendor(eventVendor: EventVendor) =
        dao.deleteEventVendor(EventVendorEntity(eventVendor.eventId, eventVendor.vendor.id, eventVendor.status.name, eventVendor.depositPaidCents, eventVendor.contractSigned))
}
