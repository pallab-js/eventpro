package com.eventpro.admin.ui.events

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventpro.admin.domain.model.Client
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.domain.model.EventVendor
import com.eventpro.admin.domain.model.InventoryItem
import com.eventpro.admin.domain.model.InventoryReservation
import com.eventpro.admin.domain.model.TimelineItem
import com.eventpro.admin.domain.model.Transaction
import com.eventpro.admin.domain.model.Vendor
import com.eventpro.admin.domain.model.VendorStatus
import com.eventpro.admin.domain.repository.ClientRepository
import com.eventpro.admin.domain.repository.EventRepository
import com.eventpro.admin.domain.repository.FinancialRepository
import com.eventpro.admin.domain.repository.InventoryRepository
import com.eventpro.admin.domain.repository.VendorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class EventDetailUiState(
    val isLoading: Boolean = true,
    val event: Event? = null,
    val timelineItems: List<TimelineItem> = emptyList(),
    val vendors: List<EventVendor> = emptyList(),
    val vendorList: List<Vendor> = emptyList(),
    val client: Client? = null,
    val quickNote: String = "",
    val selectedTab: Int = 0,
    val error: String? = null,
    val eventTransactions: List<Transaction> = emptyList(),
    val reservations: List<InventoryReservation> = emptyList(),
    val inventoryItems: List<InventoryItem> = emptyList()
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepo: EventRepository,
    private val clientRepo: ClientRepository,
    private val vendorRepo: VendorRepository,
    private val financialRepo: FinancialRepository,
    private val inventoryRepo: InventoryRepository
) : ViewModel() {
    private val _state = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _state.asStateFlow()
    private val _navigationEvent = Channel<Long>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun load(eventId: Long) {
        viewModelScope.launch {
            combine(
                eventRepo.getEventById(eventId),
                eventRepo.getTimelineItemsForEvent(eventId),
                vendorRepo.getEventVendors(eventId),
                vendorRepo.getAllVendors(),
                financialRepo.getTransactionsByEvent(eventId)
            ) { event, timeline, vendors, allVendors, txs ->
                EventDetailPrelim(event, timeline, vendors, allVendors, txs)
            }.combine(
                    combine(
                        inventoryRepo.getReservationsForEvent(eventId),
                        inventoryRepo.getAllItems()
                    ) { reservations, items -> reservations to items }
                ) { prelim, (reservations, items) ->
                    val client = prelim.event?.clientId?.let { clientRepo.getClientById(it).first() }
                    _state.update { it.copy(
                        isLoading = false,
                        event = prelim.event,
                        timelineItems = prelim.timeline,
                        vendors = prelim.vendors,
                        vendorList = prelim.allVendors,
                        client = client,
                        quickNote = prelim.event?.notes ?: "",
                        eventTransactions = prelim.txs,
                        reservations = reservations,
                        inventoryItems = items
                    )}
                }.onEach { }.launchIn(viewModelScope)
        }
    }

    private data class EventDetailPrelim(
        val event: Event?,
        val timeline: List<TimelineItem>,
        val vendors: List<EventVendor>,
        val allVendors: List<Vendor>,
        val txs: List<Transaction>
    )

    fun onTabSelect(tab: Int) = _state.update { it.copy(selectedTab = tab) }
    fun onNoteChange(note: String) = _state.update { it.copy(quickNote = note) }
    fun saveNote() = viewModelScope.launch {
        _state.value.event?.let { eventRepo.upsertEvent(it.copy(notes = _state.value.quickNote)) }
    }
    fun toggleTimelineItem(item: TimelineItem) = viewModelScope.launch {
        eventRepo.toggleTimelineItem(item.id, !item.completed)
    }
    fun deleteEvent(event: Event) = viewModelScope.launch { eventRepo.deleteEvent(event) }

    fun markComplete(event: Event) = viewModelScope.launch {
        eventRepo.upsertEvent(event.copy(status = EventStatus.COMPLETED))
    }

    fun addTimelineItem(title: String, description: String, dateMillis: Long) = viewModelScope.launch {
        val event = _state.value.event ?: return@launch
        eventRepo.addTimelineItem(event.id, title, description, dateMillis)
    }

    fun assignVendor(vendorId: Long, status: VendorStatus) = viewModelScope.launch {
        val event = _state.value.event ?: return@launch
        vendorRepo.upsertEventVendor(
            EventVendor(eventId = event.id, vendor = Vendor(vendorId, "", com.eventpro.admin.domain.model.VendorCategory.OTHER, "", ""), status = status, depositPaidCents = 0, contractSigned = false)
        )
    }

    fun duplicateEvent() {
        viewModelScope.launch {
            val event = _state.value.event ?: return@launch
            val now = System.currentTimeMillis()
            val newId = eventRepo.upsertEvent(Event(
                id = 0,
                title = "Copy of ${event.title}",
                status = EventStatus.DRAFT,
                startDateMillis = event.startDateMillis,
                endDateMillis = event.endDateMillis,
                clientId = event.clientId,
                venueName = event.venueName,
                estimatedAttendees = event.estimatedAttendees,
                notes = event.notes,
                totalBudgetCents = event.totalBudgetCents,
                spentBudgetCents = 0L,
                createdAtMillis = now,
                updatedAtMillis = now
            ))
            eventRepo.getTimelineItemsForEvent(event.id).first().forEach { item ->
                eventRepo.addTimelineItem(newId, item.title, item.description, item.scheduledDateMillis)
            }
            _navigationEvent.trySend(newId)
        }
    }

    fun reserveItem(itemId: Long, quantity: Int) = viewModelScope.launch {
        val event = _state.value.event ?: return@launch
        inventoryRepo.reserveItem(event.id, itemId, quantity)
    }

    fun releaseReservation(itemId: Long) = viewModelScope.launch {
        val event = _state.value.event ?: return@launch
        inventoryRepo.releaseReservation(event.id, itemId)
    }

    fun deleteReservation(reservationId: Long) = viewModelScope.launch {
        inventoryRepo.deleteReservation(reservationId)
    }
}
