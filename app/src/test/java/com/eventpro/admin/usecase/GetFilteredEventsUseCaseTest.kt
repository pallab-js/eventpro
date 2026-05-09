package com.eventpro.admin.usecase

import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.domain.usecase.GetFilteredEventsUseCase
import com.eventpro.admin.ui.events.TimeFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetFilteredEventsUseCaseTest {

    private val useCase = GetFilteredEventsUseCase()
    private val now = System.currentTimeMillis()
    private val day = 86_400_000L

    private fun makeEvent(id: Long, title: String = "Event $id", status: EventStatus = EventStatus.DRAFT, clientId: Long? = null, venueName: String = "Venue", startDateMillis: Long = now): Event = Event(
        id = id, title = title, status = status,
        startDateMillis = startDateMillis, endDateMillis = null, clientId = clientId,
        venueName = venueName, estimatedAttendees = 0, notes = "",
        totalBudgetCents = 0, spentBudgetCents = 0,
        createdAtMillis = 100L, updatedAtMillis = 100L
    )

    @Test
    fun `all filter returns all events`() {
        val events = listOf(makeEvent(1), makeEvent(2))
        val result = useCase(events, null, TimeFilter.ALL, "", emptyMap())
        assertEquals(2, result.size)
    }

    @Test
    fun `status filter returns only matching events`() {
        val events = listOf(
            makeEvent(1, status = EventStatus.DRAFT),
            makeEvent(2, status = EventStatus.CONFIRMED),
            makeEvent(3, status = EventStatus.IN_PROGRESS)
        )
        val result = useCase(events, EventStatus.CONFIRMED, TimeFilter.ALL, "", emptyMap())
        assertEquals(1, result.size)
        assertEquals(EventStatus.CONFIRMED, result[0].status)
    }

    @Test
    fun `search by title is case-insensitive`() {
        val events = listOf(
            makeEvent(1, title = "Wedding Gala"),
            makeEvent(2, title = "Corporate Event"),
            makeEvent(3, title = "Birthday Party")
        )
        val result = useCase(events, null, TimeFilter.ALL, "wedding", emptyMap())
        assertEquals(1, result.size)
        assertEquals("Wedding Gala", result[0].title)
    }

    @Test
    fun `search by venue name works`() {
        val events = listOf(
            makeEvent(1, venueName = "Grand Ballroom"),
            makeEvent(2, venueName = "Garden Terrace")
        )
        val result = useCase(events, null, TimeFilter.ALL, "garden", emptyMap())
        assertEquals(1, result.size)
    }

    @Test
    fun `search by client name works`() {
        val events = listOf(
            makeEvent(1, clientId = 1L),
            makeEvent(2, clientId = 2L)
        )
        val clientMap = mapOf(1L to "Acme Corp", 2L to "Beta Inc")
        val result = useCase(events, null, TimeFilter.ALL, "acme", clientMap)
        assertEquals(1, result.size)
    }

    @Test
    fun `empty search query returns all events`() {
        val events = listOf(makeEvent(1), makeEvent(2))
        val result = useCase(events, null, TimeFilter.ALL, "", emptyMap())
        assertEquals(2, result.size)
    }

    @Test
    fun `status filter with no matches returns empty`() {
        val events = listOf(
            makeEvent(1, status = EventStatus.DRAFT),
            makeEvent(2, status = EventStatus.CANCELLED)
        )
        val result = useCase(events, EventStatus.COMPLETED, TimeFilter.ALL, "", emptyMap())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `combined status and search filter`() {
        val events = listOf(
            makeEvent(1, title = "Wedding", status = EventStatus.DRAFT),
            makeEvent(2, title = "Wedding Reception", status = EventStatus.CONFIRMED),
            makeEvent(3, title = "Corporate", status = EventStatus.DRAFT)
        )
        val result = useCase(events, EventStatus.DRAFT, TimeFilter.ALL, "wedding", emptyMap())
        assertEquals(1, result.size)
    }
}
