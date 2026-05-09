package com.eventpro.admin.domain.model

import androidx.compose.runtime.Immutable

enum class ClientTier(val displayName: String) {
    STANDARD("Standard"),
    VIP("VIP"),
    ENTERPRISE("Enterprise")
}
enum class ClientStatus(val displayName: String) {
    ACTIVE("Active"),
    PAST("Past")
}

@Immutable
data class Client(
    val id: Long = 0,
    val companyName: String,
    val contactName: String,
    val phone: String,
    val email: String,
    val tier: ClientTier,
    val status: ClientStatus,
    val lastEventDateMillis: Long?,
    val createdAtMillis: Long
)
