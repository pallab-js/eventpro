package com.eventpro.admin.domain.model

import androidx.compose.runtime.Immutable

enum class VendorCategory(val displayName: String) {
    CATERING("Catering"),
    FLORAL("Floral"),
    AV("AV"),
    MUSIC("Music"),
    PHOTOGRAPHY("Photography"),
    LOGISTICS("Logistics"),
    OTHER("Other")
}
enum class VendorStatus(val displayName: String) {
    CONFIRMED("Confirmed"),
    PENDING_CONTRACT("Pending Contract"),
    AWAITING_DEPOSIT("Awaiting Deposit"),
    CANCELLED("Cancelled")
}

@Immutable
data class Vendor(
    val id: Long = 0,
    val name: String,
    val category: VendorCategory,
    val phone: String,
    val email: String
)

@Immutable
data class EventVendor(
    val eventId: Long,
    val vendor: Vendor,
    val status: VendorStatus,
    val depositPaidCents: Long,
    val contractSigned: Boolean
)
