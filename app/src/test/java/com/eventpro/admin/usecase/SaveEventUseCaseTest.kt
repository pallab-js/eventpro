package com.eventpro.admin.usecase

import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.domain.repository.EventRepository
import com.eventpro.admin.domain.usecase.SaveEventUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveEventUseCaseTest {

    private val repo = mockk<EventRepository>()
    private val useCase = SaveEventUseCase(repo)

    private fun makeEvent(id: Long = 0, title: String = "Event") = Event(
        id = id, title = title, status = EventStatus.DRAFT,
        startDateMillis = 1000L, endDateMillis = null, clientId = null,
        venueName = "Venue", estimatedAttendees = 0, notes = "",
        totalBudgetCents = 0, spentBudgetCents = 0,
        createdAtMillis = 100L, updatedAtMillis = 100L
    )

    @Test
    fun `blank title returns validation error`() = runTest {
        val event = makeEvent(title = "   ")
        val result = useCase(event, existingId = null)

        assertTrue(result is SaveEventUseCase.Result.ValidationError)
        val err = result as SaveEventUseCase.Result.ValidationError
        assertEquals("title", err.field)
        assertEquals("Title is required", err.message)
    }

    @Test
    fun `blank title with whitespace only returns validation error`() = runTest {
        val event = makeEvent(title = "\t\n  ")
        val result = useCase(event, existingId = null)

        assertTrue(result is SaveEventUseCase.Result.ValidationError)
    }

    @Test
    fun `save calls repo upsertEvent with correct params`() = runTest {
        val event = makeEvent(title = "Gala")
        coEvery { repo.upsertEvent(event) } returns 42L

        val result = useCase(event, existingId = null)

        assertTrue(result is SaveEventUseCase.Result.Success)
        assertEquals(42L, (result as SaveEventUseCase.Result.Success).eventId)
        coVerify { repo.upsertEvent(event) }
    }

    @Test
    fun `creation path works with null existingId`() = runTest {
        val newEvent = makeEvent(id = 0, title = "New Event")
        coEvery { repo.upsertEvent(any()) } returns 99L

        val result = useCase(newEvent, existingId = null)

        assertEquals(99L, (result as SaveEventUseCase.Result.Success).eventId)
        coVerify { repo.upsertEvent(newEvent) }
    }

    @Test
    fun `edit path works with existingId`() = runTest {
        val existingEvent = makeEvent(id = 7, title = "Edit Event")
        coEvery { repo.upsertEvent(any()) } returns 7L

        val result = useCase(existingEvent, existingId = 7L)

        assertEquals(7L, (result as SaveEventUseCase.Result.Success).eventId)
        coVerify { repo.upsertEvent(existingEvent) }
    }
}
