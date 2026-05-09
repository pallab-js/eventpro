package com.eventpro.admin.domain.model

enum class ClientTier { STANDARD, VIP, ENTERPRISE }
enum class ClientStatus { ACTIVE, PAST }

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
