package com.eventpro.admin.domain.model

import androidx.compose.runtime.Immutable

enum class EventStatus(val displayName: String) {
    DRAFT("Draft"),
    CONFIRMED("Confirmed"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

@Immutable
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
