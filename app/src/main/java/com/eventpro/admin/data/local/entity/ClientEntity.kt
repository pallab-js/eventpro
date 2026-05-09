package com.eventpro.admin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyName: String,
    val contactName: String,
    val phone: String,
    val email: String,
    val tier: String, // STANDARD | VIP | ENTERPRISE
    val status: String, // ACTIVE | PAST
    val lastEventDateMillis: Long?,
    val createdAtMillis: Long
)
