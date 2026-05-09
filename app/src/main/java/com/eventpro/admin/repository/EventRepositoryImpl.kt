package com.eventpro.admin.repository

import com.eventpro.admin.data.local.dao.EventDao
import com.eventpro.admin.data.local.dao.TimelineDao
import com.eventpro.admin.data.local.entity.TimelineItemEntity
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.domain.model.TimelineItem
import com.eventpro.admin.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val dao: EventDao,
    private val timelineDao: TimelineDao
) : EventRepository {
    override fun getAllEvents() = dao.getAllEvents().map { it.map { e -> e.toDomain() } }
    override fun getEventById(id: Long) = dao.getEventById(id).map { it?.toDomain() }
    override fun getEventsByStatus(status: EventStatus) = dao.getEventsByStatus(status.name).map { it.map { e -> e.toDomain() } }
    override fun getUpcomingEvents(limit: Int): Flow<List<Event>> = dao.getUpcomingEvents(System.currentTimeMillis(), limit).map { it.map { e -> e.toDomain() } }
    override fun getCriticalMilestones(): Flow<List<Event>> =
        dao.getCriticalMilestones(System.currentTimeMillis(), 5).map { it.map { e -> e.toDomain() } }
    override suspend fun upsertEvent(event: Event) = dao.upsertEvent(event.toEntity())
    override suspend fun deleteEvent(event: Event) = dao.deleteEvent(event.toEntity())
    override fun getTimelineItemsForEvent(eventId: Long): Flow<List<TimelineItem>> =
        timelineDao.getTimelineItemsForEvent(eventId).map { it.map { t -> t.toDomain() } }
    override suspend fun addTimelineItem(eventId: Long, title: String, description: String, dateMillis: Long) =
        timelineDao.upsertTimelineItem(TimelineItemEntity(eventId = eventId, title = title, description = description, scheduledDateMillis = dateMillis))
    override suspend fun toggleTimelineItem(id: Long, completed: Boolean) =
        timelineDao.markCompleted(id, completed)
}
