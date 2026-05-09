package com.eventpro.admin.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class TimelineItem(
    val id: Long = 0,
    val eventId: Long,
    val title: String,
    val description: String,
    val scheduledDateMillis: Long,
    val completed: Boolean = false
)

@Immutable
data class AgendaItem(
    val id: Long = 0,
    val title: String,
    val description: String,
    val scheduledDateMillis: Long,
    val eventId: Long?
)
