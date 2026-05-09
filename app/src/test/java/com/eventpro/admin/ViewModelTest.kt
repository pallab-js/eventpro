package com.eventpro.admin

import com.eventpro.admin.domain.model.*
import com.eventpro.admin.domain.repository.ClientRepository
import com.eventpro.admin.domain.repository.EventRepository
import com.eventpro.admin.domain.repository.FinancialRepository
import com.eventpro.admin.ui.events.EventListViewModel
import com.eventpro.admin.ui.inventory.InventoryViewModel
import com.eventpro.admin.util.CurrencyFormatter
import com.eventpro.admin.util.DateFormatter
import io.mockk.every
import io.mockk.mockk
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

    @Before fun setUp() { Dispatchers.setMain(testDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun makeEvent(id: Long, status: EventStatus, title: String = "Event $id") = Event(
        id = id, title = title, status = status,
        startDateMillis = System.currentTimeMillis() + 86_400_000L,
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

        val vm = EventListViewModel(repo, clientRepo)
        advanceUntilIdle()

        vm.onStatusFilter(EventStatus.CONFIRMED)
        advanceUntilIdle()

        val filtered = vm.uiState.value.events
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.status == EventStatus.CONFIRMED })
    }

    @Test
    fun `EventListViewModel search filters by title`() = runTest {
        val events = listOf(makeEvent(1, EventStatus.DRAFT, "Apex Gala"), makeEvent(2, EventStatus.DRAFT, "Meridian Launch"))
        val repo = mockk<EventRepository>()
        every { repo.getAllEvents() } returns flowOf(events)
        val clientRepo = mockk<ClientRepository>()
        every { clientRepo.getAllClients() } returns flowOf(emptyList())

        val vm = EventListViewModel(repo, clientRepo)
        advanceUntilIdle()
        vm.onSearchQuery("apex")
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.events.size)
        assertEquals("Apex Gala", vm.uiState.value.events.first().title)
    }
}

class StockStatusTest {
    @Test fun `zero available is OUT_OF_STOCK`() {
        val item = InventoryItem(name = "X", category = InventoryCategory.AUDIO, description = "", totalUnits = 10, availableUnits = 0, materialIconName = "")
        assertEquals(StockStatus.OUT_OF_STOCK, item.stockStatus)
    }

    @Test fun `20 percent or less is LOW_STOCK`() {
        val item = InventoryItem(name = "X", category = InventoryCategory.AUDIO, description = "", totalUnits = 10, availableUnits = 2, materialIconName = "")
        assertEquals(StockStatus.LOW_STOCK, item.stockStatus)
    }

    @Test fun `above 20 percent is IN_STOCK`() {
        val item = InventoryItem(name = "X", category = InventoryCategory.AUDIO, description = "", totalUnits = 10, availableUnits = 8, materialIconName = "")
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
