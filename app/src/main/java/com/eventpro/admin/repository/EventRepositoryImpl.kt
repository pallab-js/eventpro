package com.eventpro.admin.repository

import com.eventpro.admin.data.local.dao.EventDao
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(private val dao: EventDao) : EventRepository {
    override fun getAllEvents() = dao.getAllEvents().map { it.map { e -> e.toDomain() } }
    override fun getEventById(id: Long) = dao.getEventById(id).map { it?.toDomain() }
    override fun getEventsByStatus(status: EventStatus) = dao.getEventsByStatus(status.name).map { it.map { e -> e.toDomain() } }
    override fun getUpcomingEvents(limit: Int): Flow<List<Event>> = dao.getUpcomingEvents(System.currentTimeMillis(), limit).map { it.map { e -> e.toDomain() } }
    override fun getCriticalMilestones(): Flow<List<Event>> = dao.getUpcomingEvents(System.currentTimeMillis(), 5).map { list ->
        list.filter { it.status in listOf("CONFIRMED", "IN_PROGRESS") }.map { it.toDomain() }
    }
    override suspend fun upsertEvent(event: Event) = dao.upsertEvent(event.toEntity())
    override suspend fun deleteEvent(event: Event) = dao.deleteEvent(event.toEntity())
}
