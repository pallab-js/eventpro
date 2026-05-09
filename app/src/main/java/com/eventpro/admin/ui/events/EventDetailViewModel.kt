package com.eventpro.admin.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventpro.admin.data.local.dao.TimelineDao
import com.eventpro.admin.data.local.dao.VendorDao
import com.eventpro.admin.data.local.entity.EventVendorEntity
import com.eventpro.admin.data.local.entity.TimelineItemEntity
import com.eventpro.admin.domain.model.*
import com.eventpro.admin.domain.repository.ClientRepository
import com.eventpro.admin.domain.repository.EventRepository
import com.eventpro.admin.domain.repository.VendorRepository
import com.eventpro.admin.repository.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventDetailUiState(
    val isLoading: Boolean = true,
    val event: Event? = null,
    val timelineItems: List<TimelineItem> = emptyList(),
    val vendors: List<EventVendor> = emptyList(),
    val vendorList: List<Vendor> = emptyList(),
    val client: Client? = null,
    val quickNote: String = "",
    val selectedTab: Int = 0,
    val error: String? = null
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepo: EventRepository,
    private val clientRepo: ClientRepository,
    private val timelineDao: TimelineDao,
    private val vendorDao: VendorDao,
    private val vendorRepo: VendorRepository
) : ViewModel() {
    private val _state = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _state.asStateFlow()

    fun load(eventId: Long) {
        viewModelScope.launch {
            combine(
                eventRepo.getEventById(eventId),
                timelineDao.getTimelineItemsForEvent(eventId).map { it.map { t -> t.toDomain() } },
                vendorDao.getEventVendors(eventId),
                vendorRepo.getAllVendors()
            ) { event, timeline, evVendors, allVendors ->
                listOf(event, timeline, evVendors, allVendors)
            }.collect { list ->
                val event = list[0] as? Event
                @Suppress("UNCHECKED_CAST")
                val timeline = list[1] as List<TimelineItem>
                @Suppress("UNCHECKED_CAST")
                val evVendors = list[2] as List<EventVendorEntity>
                @Suppress("UNCHECKED_CAST")
                val allVendors = list[3] as List<Vendor>
                val client = event?.clientId?.let { clientRepo.getClientById(it).firstOrNull() }
                val vendors = evVendors.map { ev ->
                    val vendor = vendorDao.getVendorById(ev.vendorId).firstOrNull()
                    EventVendor(
                        eventId = ev.eventId,
                        vendor = vendor?.toDomain() ?: Vendor(ev.vendorId, "Unknown", VendorCategory.OTHER, "", ""),
                        status = VendorStatus.valueOf(ev.status),
                        depositPaidCents = ev.depositPaidCents,
                        contractSigned = ev.contractSigned
                    )
                }
                _state.update { it.copy(
                    isLoading = false,
                    event = event,
                    timelineItems = timeline,
                    vendors = vendors,
                    vendorList = allVendors,
                    client = client,
                    quickNote = event?.notes ?: ""
                )}
            }
        }
    }

    fun onTabSelect(tab: Int) = _state.update { it.copy(selectedTab = tab) }
    fun onNoteChange(note: String) = _state.update { it.copy(quickNote = note) }
    fun saveNote() = viewModelScope.launch {
        _state.value.event?.let { eventRepo.upsertEvent(it.copy(notes = _state.value.quickNote)) }
    }
    fun toggleTimelineItem(item: TimelineItem) = viewModelScope.launch {
        timelineDao.markCompleted(item.id, !item.completed)
    }
    fun deleteEvent(event: Event) = viewModelScope.launch { eventRepo.deleteEvent(event) }

    fun markComplete(event: Event) = viewModelScope.launch {
        eventRepo.upsertEvent(event.copy(status = EventStatus.COMPLETED))
    }

    fun addTimelineItem(title: String, description: String, dateMillis: Long) = viewModelScope.launch {
        val event = _state.value.event ?: return@launch
        timelineDao.upsertTimelineItem(
            TimelineItemEntity(eventId = event.id, title = title, description = description, scheduledDateMillis = dateMillis)
        )
    }

    fun assignVendor(vendorId: Long, status: VendorStatus) = viewModelScope.launch {
        val event = _state.value.event ?: return@launch
        vendorDao.upsertEventVendor(
            EventVendorEntity(eventId = event.id, vendorId = vendorId, status = status.name, depositPaidCents = 0, contractSigned = false)
        )
    }
}
