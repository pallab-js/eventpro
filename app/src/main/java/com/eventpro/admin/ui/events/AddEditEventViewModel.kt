package com.eventpro.admin.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventpro.admin.domain.model.Client
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.EventStatus
import com.eventpro.admin.domain.repository.ClientRepository
import com.eventpro.admin.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditEventFormState(
    val title: String = "",
    val titleError: String? = null,
    val status: EventStatus = EventStatus.DRAFT,
    val startDateMillis: Long = System.currentTimeMillis(),
    val endDateMillis: Long? = null,
    val venueName: String = "",
    val estimatedAttendees: String = "",
    val totalBudget: String = "",
    val notes: String = "",
    val clientId: Long? = null,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false
)

@HiltViewModel
class AddEditEventViewModel @Inject constructor(
    private val repo: EventRepository,
    private val clientRepo: ClientRepository
) : ViewModel() {
    private val _form = MutableStateFlow(AddEditEventFormState())
    val formState: StateFlow<AddEditEventFormState> = _form.asStateFlow()

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    init {
        viewModelScope.launch {
            clientRepo.getAllClients().collect { _clients.value = it }
        }
    }

    fun load(eventId: Long) = viewModelScope.launch {
        repo.getEventById(eventId).firstOrNull()?.let { e ->
            _form.update { it.copy(
                title = e.title, status = e.status, startDateMillis = e.startDateMillis,
                endDateMillis = e.endDateMillis, clientId = e.clientId,
                venueName = e.venueName, estimatedAttendees = e.estimatedAttendees.toString(),
                totalBudget = (e.totalBudgetCents / 100.0).toString(), notes = e.notes
            )}
        }
    }

    fun onTitleChange(v: String) = _form.update { it.copy(title = v, titleError = null) }
    fun onStatusChange(v: EventStatus) = _form.update { it.copy(status = v) }
    fun onDateChange(v: Long) = _form.update { it.copy(startDateMillis = v) }
    fun onEndDateChange(v: Long?) = _form.update { it.copy(endDateMillis = v) }
    fun onClientIdChange(v: Long?) = _form.update { it.copy(clientId = v) }
    fun onVenueChange(v: String) = _form.update { it.copy(venueName = v) }
    fun onAttendeesChange(v: String) = _form.update { it.copy(estimatedAttendees = v) }
    fun onBudgetChange(v: String) = _form.update { it.copy(totalBudget = v) }
    fun onNotesChange(v: String) = _form.update { it.copy(notes = v) }

    fun save(existingId: Long? = null) {
        val f = _form.value
        if (f.title.isBlank()) { _form.update { it.copy(titleError = "Title is required") }; return }
        viewModelScope.launch {
            _form.update { it.copy(isSaving = true) }
            val now = System.currentTimeMillis()
            repo.upsertEvent(Event(
                id = existingId ?: 0L,
                title = f.title.trim(),
                status = f.status,
                startDateMillis = f.startDateMillis,
                endDateMillis = f.endDateMillis,
                clientId = f.clientId,
                venueName = f.venueName.trim(),
                estimatedAttendees = f.estimatedAttendees.toIntOrNull() ?: 0,
                notes = f.notes.trim(),
                totalBudgetCents = ((f.totalBudget.toDoubleOrNull() ?: 0.0) * 100).toLong(),
                spentBudgetCents = 0L,
                createdAtMillis = now,
                updatedAtMillis = now
            ))
            _form.update { it.copy(isSaving = false, savedSuccessfully = true) }
        }
    }
}
