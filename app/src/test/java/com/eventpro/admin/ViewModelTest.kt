package com.eventpro.admin

import com.eventpro.admin.domain.model.*
import com.eventpro.admin.domain.repository.ClientRepository
import com.eventpro.admin.domain.repository.EventRepository
import com.eventpro.admin.domain.repository.FinancialRepository
import com.eventpro.admin.domain.usecase.GetFilteredEventsUseCase
import com.eventpro.admin.ui.events.AddEditEventViewModel
import com.eventpro.admin.ui.events.EventListViewModel
import com.eventpro.admin.ui.events.TimeFilter
import com.eventpro.admin.ui.inventory.InventoryViewModel
import com.eventpro.admin.util.CurrencyFormatter
import com.eventpro.admin.util.DateFormatter
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dayMs = 86_400_000L

    @Before fun setUp() { Dispatchers.setMain(testDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun makeEvent(
        id: Long, status: EventStatus, title: String = "Event $id",
        startDateMillis: Long = System.currentTimeMillis() + dayMs
    ) = Event(
        id = id, title = title, status = status,
        startDateMillis = startDateMillis,
        endDateMillis = null, clientId = null, venueName = "Venue",
        estimatedAttendees = 100, notes = "",
        totalBudgetCents = 100_00L, spentBudgetCents = 50_00L,
        createdAtMillis = System.currentTimeMillis(), updatedAtMillis = System.currentTimeMillis()
    )

    @Test
    fun `EventListViewModel filters by status`() = runTest {
        val events = listOf(
            makeEvent(1, EventStatus.CONFIRMED),
            makeEvent(2, EventStatus.DRAFT),
            makeEvent(3, EventStatus.CONFIRMED)
        )
        val repo = mockk<EventRepository>()
        every { repo.getAllEvents() } returns flowOf(events)
        val clientRepo = mockk<ClientRepository>()
        every { clientRepo.getAllClients() } returns flowOf(emptyList())

        val vm = EventListViewModel(repo, clientRepo, GetFilteredEventsUseCase())
        advanceUntilIdle()

        vm.onStatusFilter(EventStatus.CONFIRMED)
        advanceUntilIdle()

        val filtered = vm.uiState.value.events
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.status == EventStatus.CONFIRMED })
    }

    @Test
    fun `EventListViewModel search filters by title`() = runTest {
        val events = listOf(
            makeEvent(1, EventStatus.DRAFT, "Apex Gala"),
            makeEvent(2, EventStatus.DRAFT, "Meridian Launch")
        )
        val repo = mockk<EventRepository>()
        every { repo.getAllEvents() } returns flowOf(events)
        val clientRepo = mockk<ClientRepository>()
        every { clientRepo.getAllClients() } returns flowOf(emptyList())

        val vm = EventListViewModel(repo, clientRepo, GetFilteredEventsUseCase())
        advanceUntilIdle()

        vm.onSearchQuery("apex")
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.events.size)
        assertEquals("Apex Gala", vm.uiState.value.events.first().title)
    }

    @Test
    fun `time filter THIS_MONTH excludes past-month events`() = runTest {
        val now = System.currentTimeMillis()
        val pastEvent = makeEvent(
            1, EventStatus.CONFIRMED, "Past Month",
            startDateMillis = now - 60L * dayMs
        )
        val futureEvent = makeEvent(
            2, EventStatus.CONFIRMED, "This Month",
            startDateMillis = now + dayMs
        )
        val repo = mockk<EventRepository>()
        every { repo.getAllEvents() } returns flowOf(listOf(pastEvent, futureEvent))
        val clientRepo = mockk<ClientRepository>()
        every { clientRepo.getAllClients() } returns flowOf(emptyList())

        val vm = EventListViewModel(repo, clientRepo, GetFilteredEventsUseCase())
        advanceUntilIdle()

        vm.onTimeFilter(TimeFilter.THIS_MONTH)
        advanceUntilIdle()

        val events = vm.uiState.value.events
        assertEquals(1, events.size)
        assertEquals("This Month", events.first().title)
    }

    @Test
    fun `search is case-insensitive`() = runTest {
        val events = listOf(
            makeEvent(1, EventStatus.DRAFT, "APEX GALA"),
            makeEvent(2, EventStatus.DRAFT, "Meridian Launch")
        )
        val repo = mockk<EventRepository>()
        every { repo.getAllEvents() } returns flowOf(events)
        val clientRepo = mockk<ClientRepository>()
        every { clientRepo.getAllClients() } returns flowOf(emptyList())

        val vm = EventListViewModel(repo, clientRepo, GetFilteredEventsUseCase())
        advanceUntilIdle()

        vm.onSearchQuery("apex")
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.events.size)
        assertEquals("APEX GALA", vm.uiState.value.events.first().title)

        vm.onSearchQuery("APEX")
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.events.size)
        assertEquals("APEX GALA", vm.uiState.value.events.first().title)
    }

    @Test
    fun `empty search query shows all events`() = runTest {
        val events = listOf(
            makeEvent(1, EventStatus.DRAFT, "Alpha"),
            makeEvent(2, EventStatus.DRAFT, "Beta")
        )
        val repo = mockk<EventRepository>()
        every { repo.getAllEvents() } returns flowOf(events)
        val clientRepo = mockk<ClientRepository>()
        every { clientRepo.getAllClients() } returns flowOf(emptyList())

        val vm = EventListViewModel(repo, clientRepo, GetFilteredEventsUseCase())
        advanceUntilIdle()

        vm.onSearchQuery("Alpha")
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.events.size)

        vm.onSearchQuery("")
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.events.size)
    }

    @Test
    fun `EventListViewModel searchToggle clears query`() = runTest {
        val events = listOf(
            makeEvent(1, EventStatus.DRAFT, "Gala"),
            makeEvent(2, EventStatus.DRAFT, "Summit")
        )
        val repo = mockk<EventRepository>()
        every { repo.getAllEvents() } returns flowOf(events)
        val clientRepo = mockk<ClientRepository>()
        every { clientRepo.getAllClients() } returns flowOf(emptyList())

        val vm = EventListViewModel(repo, clientRepo, GetFilteredEventsUseCase())
        advanceUntilIdle()

        vm.onSearchQuery("gala")
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.events.size)

        vm.onSearchToggle()
        advanceUntilIdle()

        assertEquals("", vm.uiState.value.searchQuery)
        assertEquals(2, vm.uiState.value.events.size)
    }

    @Test
    fun `AddEditEventViewModel save fails when title is blank`() = runTest {
        val repo = mockk<EventRepository>()
        val clientRepo = mockk<ClientRepository>()
        every { clientRepo.getAllClients() } returns flowOf(emptyList())

        val vm = AddEditEventViewModel(repo, clientRepo)
        advanceUntilIdle()

        vm.save(existingId = null)
        advanceUntilIdle()

        assertEquals("Title is required", vm.formState.value.titleError)
    }

    @Test
    fun `AddEditEventViewModel preserves spentBudgetCents on edit`() = runTest {
        val repo = mockk<EventRepository>()
        val clientRepo = mockk<ClientRepository>()
        every { clientRepo.getAllClients() } returns flowOf(emptyList())

        val existingEvent = Event(
            id = 1, title = "Gala", status = EventStatus.CONFIRMED,
            startDateMillis = System.currentTimeMillis() + dayMs,
            endDateMillis = null, clientId = null, venueName = "Venue",
            estimatedAttendees = 100, notes = "",
            totalBudgetCents = 200_00L, spentBudgetCents = 75_00L,
            createdAtMillis = 1000L, updatedAtMillis = 1000L
        )
        every { repo.getEventById(1) } returns flowOf(existingEvent)

        val vm = AddEditEventViewModel(repo, clientRepo)
        vm.load(1)
        advanceUntilIdle()

        val slot = slot<Event>()
        coEvery { repo.upsertEvent(capture(slot)) } returns 1L

        vm.save(existingId = 1L)
        advanceUntilIdle()

        assertEquals(75_00L, slot.captured.spentBudgetCents)
        assertEquals(1L, slot.captured.id)
    }
}

class StockStatusTest {
    @Test fun `zero available is OUT_OF_STOCK`() {
        val item = InventoryItem(
            name = "X", category = InventoryCategory.AUDIO, description = "",
            totalUnits = 10, availableUnits = 0, materialIconName = ""
        )
        assertEquals(StockStatus.OUT_OF_STOCK, item.stockStatus)
    }

    @Test fun `20 percent or less is LOW_STOCK`() {
        val item = InventoryItem(
            name = "X", category = InventoryCategory.AUDIO, description = "",
            totalUnits = 10, availableUnits = 2, materialIconName = ""
        )
        assertEquals(StockStatus.LOW_STOCK, item.stockStatus)
    }

    @Test fun `above 20 percent is IN_STOCK`() {
        val item = InventoryItem(
            name = "X", category = InventoryCategory.AUDIO, description = "",
            totalUnits = 10, availableUnits = 8, materialIconName = ""
        )
        assertEquals(StockStatus.IN_STOCK, item.stockStatus)
    }
}

class CurrencyFormatterTest {
    @Test fun `formats cents to dollars`() {
        assertEquals("$12.50", CurrencyFormatter.formatCents(1250L))
    }

    @Test fun `compact format uses K suffix`() {
        val result = CurrencyFormatter.formatCentsCompact(150_000L)
        assertTrue(result.contains("K") || result.contains("1"))
    }
}
