package com.eventpro.admin.domain.repository

import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.domain.model.TimelineItem
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getAllEvents(): Flow<List<Event>>
    fun getEventById(id: Long): Flow<Event?>
    fun getEventsByStatus(status: EventStatus): Flow<List<Event>>
    fun getUpcomingEvents(limit: Int = 5): Flow<List<Event>>
    fun getCriticalMilestones(): Flow<List<Event>>
    suspend fun upsertEvent(event: Event): Long
    suspend fun deleteEvent(event: Event)
    fun getTimelineItemsForEvent(eventId: Long): Flow<List<TimelineItem>>
    suspend fun addTimelineItem(eventId: Long, title: String, description: String, dateMillis: Long)
    suspend fun toggleTimelineItem(id: Long, completed: Boolean)
}
