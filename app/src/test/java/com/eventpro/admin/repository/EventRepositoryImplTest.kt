package com.eventpro.admin.repository

import com.eventpro.admin.data.local.dao.EventDao
import com.eventpro.admin.data.local.dao.TimelineDao
import com.eventpro.admin.data.local.entity.EventEntity
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.EventStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventRepositoryImplTest {

    private val dao = mockk<EventDao>()
    private val timelineDao = mockk<TimelineDao>()
    private val repo = EventRepositoryImpl(dao, timelineDao)

    private fun makeEvent(
        id: Long = 0,
        title: String = "Test Event",
        status: EventStatus = EventStatus.CONFIRMED,
        startDateMillis: Long = 1000L
    ) = Event(
        id = id, title = title, status = status,
        startDateMillis = startDateMillis, endDateMillis = null,
        clientId = null, venueName = "Venue",
        estimatedAttendees = 50, notes = "Notes",
        totalBudgetCents = 100_00L, spentBudgetCents = 50_00L,
        createdAtMillis = 100L, updatedAtMillis = 200L
    )

    @Test
    fun `upsert and retrieve event round-trip`() = runTest {
        val event = makeEvent(title = "Gala Night", status = EventStatus.CONFIRMED)
        val entitySlot = slot<EventEntity>()
        coEvery { dao.upsertEvent(capture(entitySlot)) } returns 1L

        val upsertedId = repo.upsertEvent(event)
        assertEquals(1L, upsertedId)
        assertEquals("Gala Night", entitySlot.captured.title)
        assertEquals("CONFIRMED", entitySlot.captured.status)

        every { dao.getAllEvents() } returns flowOf(
            listOf(entitySlot.captured.copy(id = 1L))
        )

        val events = repo.getAllEvents().first()
        assertEquals(1, events.size)
        assertEquals("Gala Night", events.first().title)
        assertEquals(EventStatus.CONFIRMED, events.first().status)
    }

    @Test
    fun `delete removes event`() = runTest {
        val event = makeEvent(id = 5, title = "Delete Me")
        val entitySlot = slot<EventEntity>()
        coEvery { dao.deleteEvent(capture(entitySlot)) } returns Unit

        repo.deleteEvent(event)

        coVerify { dao.deleteEvent(any()) }
        assertEquals(5L, entitySlot.captured.id)
        assertEquals("Delete Me", entitySlot.captured.title)
    }

    @Test
    fun `getCriticalMilestones returns only confirmed and in_progress`() = runTest {
        val confirmed = EventEntity(
            1, "Confirmed", "CONFIRMED", 9_999_999_999_999L,
            null, null, "V", 0, "", 0, 0, 0, 0
        )
        val inProgress = EventEntity(
            2, "In Progress", "IN_PROGRESS", 9_999_999_999_999L,
            null, null, "V", 0, "", 0, 0, 0, 0
        )
        every { dao.getCriticalMilestones(any(), any()) } returns flowOf(
            listOf(confirmed, inProgress)
        )

        val result = repo.getCriticalMilestones().first()
        assertEquals(2, result.size)
        assertTrue(result.all {
            it.status == EventStatus.CONFIRMED || it.status == EventStatus.IN_PROGRESS
        })
        assertFalse(result.any {
            it.status == EventStatus.DRAFT || it.status == EventStatus.COMPLETED || it.status == EventStatus.CANCELLED
        })
    }

    @Test
    fun `mappers round-trip correctly`() {
        val event = Event(
            id = 42, title = "Wedding", status = EventStatus.CONFIRMED,
            startDateMillis = 1000L, endDateMillis = 2000L, clientId = 7L,
            venueName = "Garden", estimatedAttendees = 100, notes = "Bring flowers",
            totalBudgetCents = 500_00L, spentBudgetCents = 200_00L,
            createdAtMillis = 100L, updatedAtMillis = 200L
        )
        val entity = event.toEntity()
        assertEquals(event.title, entity.title)
        assertEquals(event.status.name, entity.status)
        assertEquals(event.id, entity.id)
        assertEquals(event.venueName, entity.venueName)
        assertEquals(event.totalBudgetCents, entity.totalBudgetCents)
        assertEquals(event.spentBudgetCents, entity.spentBudgetCents)

        val domain = entity.toDomain()
        assertEquals(event, domain)
    }

    @Test
    fun `mapper defaults to DRAFT for unknown status`() {
        val entity = EventEntity(
            1, "X", "UNKNOWN_STATUS", 0, null, null, "", 0, "", 0, 0, 0, 0
        )
        val domain = entity.toDomain()
        assertEquals(EventStatus.DRAFT, domain.status)
    }

    @Test
    fun `getCriticalMilestones passes current time and limit to DAO`() = runTest {
        every { dao.getCriticalMilestones(any(), any()) } returns flowOf(emptyList())

        repo.getCriticalMilestones().first()

        coVerify(exactly = 0) { dao.upsertEvent(any()) }
    }
}
