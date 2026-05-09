package com.eventpro.admin.domain.model

enum class VendorCategory { CATERING, FLORAL, AV, MUSIC, PHOTOGRAPHY, LOGISTICS, OTHER }
enum class VendorStatus { CONFIRMED, PENDING_CONTRACT, AWAITING_DEPOSIT, CANCELLED }

data class Vendor(
    val id: Long = 0,
    val name: String,
    val category: VendorCategory,
    val phone: String,
    val email: String
)

data class EventVendor(
    val eventId: Long,
    val vendor: Vendor,
    val status: VendorStatus,
    val depositPaidCents: Long,
    val contractSigned: Boolean
)
