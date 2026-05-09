package com.eventpro.admin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val status: String, // DRAFT | CONFIRMED | IN_PROGRESS | COMPLETED | CANCELLED
    val startDateMillis: Long,
    val endDateMillis: Long?,
    val clientId: Long?,
    val venueName: String,
    val estimatedAttendees: Int,
    val notes: String,
    val totalBudgetCents: Long,
    val spentBudgetCents: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
