package com.eventpro.admin.domain.model

enum class EventStatus { DRAFT, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED }

data class Event(
    val id: Long = 0,
    val title: String,
    val status: EventStatus,
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
