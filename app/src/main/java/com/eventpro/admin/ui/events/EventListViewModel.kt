package com.eventpro.admin.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.domain.repository.ClientRepository
import com.eventpro.admin.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class TimeFilter { ALL, THIS_MONTH, THIS_QUARTER }

data class EventListUiState(
    val isLoading: Boolean = true,
    val events: List<Event> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: EventStatus? = null,
    val selectedTimeFilter: TimeFilter = TimeFilter.ALL,
    val isSearchActive: Boolean = false,
    val clientNameMap: Map<Long, String> = emptyMap()
)

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val repo: EventRepository,
    private val clientRepo: ClientRepository
) : ViewModel() {
    private val _state = MutableStateFlow(EventListUiState())
    val uiState: StateFlow<EventListUiState> = _state.asStateFlow()

    private val allEvents = MutableStateFlow<List<Event>>(emptyList())

    init {
        viewModelScope.launch {
            combine(
                repo.getAllEvents(),
                clientRepo.getAllClients()
            ) { events, clients ->
                events to clients.associate { it.id to it.companyName }
            }.collect { (events, nameMap) ->
                allEvents.value = events
                _state.update { it.copy(clientNameMap = nameMap) }
                applyFilters()
            }
        }
    }

    fun onSearchQuery(q: String) { _state.update { it.copy(searchQuery = q) }; applyFilters() }
    fun onStatusFilter(s: EventStatus?) { _state.update { it.copy(selectedStatus = s) }; applyFilters() }
    fun onTimeFilter(t: TimeFilter) { _state.update { it.copy(selectedTimeFilter = t) }; applyFilters() }
    fun onSearchToggle() { _state.update { it.copy(isSearchActive = !it.isSearchActive, searchQuery = "") }; applyFilters() }

    fun deleteEvent(event: Event) = viewModelScope.launch { repo.deleteEvent(event) }

    private fun applyFilters() {
        val s = _state.value
        val now = System.currentTimeMillis()
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val quarterStart = Calendar.getInstance().apply {
            val month = get(Calendar.MONTH)
            set(Calendar.MONTH, (month / 3) * 3)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val filtered = allEvents.value
            .filter { s.selectedStatus == null || it.status == s.selectedStatus }
            .filter {
                when (s.selectedTimeFilter) {
                    TimeFilter.ALL -> true
                    TimeFilter.THIS_MONTH -> it.startDateMillis >= monthStart
                    TimeFilter.THIS_QUARTER -> it.startDateMillis >= quarterStart
                }
            }
            .filter { ev ->
                s.searchQuery.isBlank() ||
                ev.title.contains(s.searchQuery, true) ||
                ev.venueName.contains(s.searchQuery, true) ||
                s.clientNameMap[ev.clientId]?.contains(s.searchQuery, true) == true
            }
        _state.update { it.copy(isLoading = false, events = filtered) }
    }
}
