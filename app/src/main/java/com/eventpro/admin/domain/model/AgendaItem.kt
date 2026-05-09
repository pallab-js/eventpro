package com.eventpro.admin.domain.model

data class TimelineItem(
    val id: Long = 0,
    val eventId: Long,
    val title: String,
    val description: String,
    val scheduledDateMillis: Long,
    val completed: Boolean = false
)

data class AgendaItem(
    val id: Long = 0,
    val title: String,
    val description: String,
    val scheduledDateMillis: Long,
    val eventId: Long?
)
