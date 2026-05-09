package com.eventpro.admin.ui.events

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.domain.repository.ClientRepository
import com.eventpro.admin.domain.repository.EventRepository
import com.eventpro.admin.domain.usecase.GetFilteredEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimeFilter { ALL, THIS_MONTH, THIS_QUARTER }

@Immutable
data class EventListUiState(
    val isLoading: Boolean = true,
    val events: List<Event> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: EventStatus? = null,
    val selectedTimeFilter: TimeFilter = TimeFilter.ALL,
    val isSearchActive: Boolean = false,
    val clientNameMap: Map<Long, String> = emptyMap(),
    val snackbarMessage: String? = null
)

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val repo: EventRepository,
    private val clientRepo: ClientRepository,
    private val getFilteredEventsUseCase: GetFilteredEventsUseCase
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

    private var pendingDeletedEvent: Event? = null
    private var clearPendingJob: Job? = null

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repo.deleteEvent(event)
            pendingDeletedEvent = event
            _state.update { it.copy(snackbarMessage = "Deleted \"${event.title}\"") }
            clearPendingJob?.cancel()
            clearPendingJob = viewModelScope.launch {
                delay(5000)
                pendingDeletedEvent = null
                _state.update { it.copy(snackbarMessage = null) }
            }
        }
    }

    fun undoDelete() {
        clearPendingJob?.cancel()
        viewModelScope.launch {
            pendingDeletedEvent?.let { repo.upsertEvent(it) }
            pendingDeletedEvent = null
            _state.update { it.copy(snackbarMessage = null) }
        }
    }

    fun clearSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }

    @VisibleForTesting
    internal fun applyFilters() {
        val s = _state.value
        val filtered = getFilteredEventsUseCase(
            events = allEvents.value,
            selectedStatus = s.selectedStatus,
            selectedTimeFilter = s.selectedTimeFilter,
            searchQuery = s.searchQuery,
            clientNameMap = s.clientNameMap
        )
        _state.update { it.copy(isLoading = false, events = filtered) }
    }
}
