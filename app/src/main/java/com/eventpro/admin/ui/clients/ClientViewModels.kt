package com.eventpro.admin.ui.clients

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventpro.admin.domain.model.Client
import com.eventpro.admin.domain.model.ClientStatus
import com.eventpro.admin.domain.model.ClientTier
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.repository.ClientRepository
import com.eventpro.admin.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── List ─────────────────────────────────────────────────────────────────────

@Immutable
data class ClientListUiState(
    val isLoading: Boolean = true,
    val clients: List<Client> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: ClientStatus? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class ClientListViewModel @Inject constructor(private val repo: ClientRepository) : ViewModel() {
    private val _state = MutableStateFlow(ClientListUiState())
    val uiState: StateFlow<ClientListUiState> = _state.asStateFlow()
    private val allClients = MutableStateFlow<List<Client>>(emptyList())

    init {
        viewModelScope.launch {
            repo.getAllClients().collect { clients ->
                allClients.value = clients
                applyFilters()
            }
        }
    }

    fun onSearch(q: String) { _state.update { it.copy(searchQuery = q) }; applyFilters() }
    fun onStatusFilter(s: ClientStatus?) { _state.update { it.copy(selectedStatus = s) }; applyFilters() }

    private var pendingDeletedClient: Client? = null
    private var clearPendingJob: Job? = null

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            repo.deleteClient(client)
            pendingDeletedClient = client
            _state.update { it.copy(snackbarMessage = "Deleted \"${client.companyName}\"") }
            clearPendingJob?.cancel()
            clearPendingJob = viewModelScope.launch {
                delay(5000)
                pendingDeletedClient = null
                _state.update { it.copy(snackbarMessage = null) }
            }
        }
    }

    fun undoDelete() {
        clearPendingJob?.cancel()
        viewModelScope.launch {
            pendingDeletedClient?.let { repo.upsertClient(it) }
            pendingDeletedClient = null
            _state.update { it.copy(snackbarMessage = null) }
        }
    }

    fun clearSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }

    private fun applyFilters() {
        val s = _state.value
        val filtered = allClients.value
            .filter { s.selectedStatus == null || it.status == s.selectedStatus }
            .filter { s.searchQuery.isBlank() || it.companyName.contains(s.searchQuery, true) || it.contactName.contains(s.searchQuery, true) }
        _state.update { it.copy(isLoading = false, clients = filtered) }
    }
}

// ─── Detail ───────────────────────────────────────────────────────────────────

@Immutable
data class ClientDetailUiState(
    val isLoading: Boolean = true,
    val client: Client? = null,
    val linkedEvents: List<Event> = emptyList()
)

@HiltViewModel
class ClientDetailViewModel @Inject constructor(
    private val clientRepo: ClientRepository,
    private val eventRepo: EventRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ClientDetailUiState())
    val uiState: StateFlow<ClientDetailUiState> = _state.asStateFlow()

    fun load(clientId: Long) {
        viewModelScope.launch {
            combine(
                clientRepo.getClientById(clientId),
                eventRepo.getAllEvents()
            ) { client, events ->
                _state.update { it.copy(isLoading = false, client = client, linkedEvents = events.filter { e -> e.clientId == clientId }) }
            }.collect {}
        }
    }
}

// ─── Add/Edit ─────────────────────────────────────────────────────────────────

@Immutable
data class AddEditClientFormState(
    val companyName: String = "",
    val companyNameError: String? = null,
    val contactName: String = "",
    val phone: String = "",
    val email: String = "",
    val tier: ClientTier = ClientTier.STANDARD,
    val status: ClientStatus = ClientStatus.ACTIVE,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false
)

@HiltViewModel
class AddEditClientViewModel @Inject constructor(private val repo: ClientRepository) : ViewModel() {
    private val _form = MutableStateFlow(AddEditClientFormState())
    val formState: StateFlow<AddEditClientFormState> = _form.asStateFlow()

    fun load(clientId: Long) = viewModelScope.launch {
        repo.getClientById(clientId).first()?.let { c ->
            _form.update { it.copy(companyName = c.companyName, contactName = c.contactName, phone = c.phone, email = c.email, tier = c.tier, status = c.status) }
        }
    }

    fun onCompanyChange(v: String) = _form.update { it.copy(companyName = v, companyNameError = null) }
    fun onContactChange(v: String) = _form.update { it.copy(contactName = v) }
    fun onPhoneChange(v: String) = _form.update { it.copy(phone = v) }
    fun onEmailChange(v: String) = _form.update { it.copy(email = v) }
    fun onTierChange(v: ClientTier) = _form.update { it.copy(tier = v) }
    fun onStatusChange(v: ClientStatus) = _form.update { it.copy(status = v) }

    fun save(existingId: Long? = null) {
        val f = _form.value
        if (f.companyName.isBlank()) { _form.update { it.copy(companyNameError = "Company name is required") }; return }
        viewModelScope.launch {
            _form.update { it.copy(isSaving = true) }
            repo.upsertClient(Client(id = existingId ?: 0L, companyName = f.companyName.trim(), contactName = f.contactName.trim(), phone = f.phone.trim(), email = f.email.trim(), tier = f.tier, status = f.status, lastEventDateMillis = null, createdAtMillis = System.currentTimeMillis()))
            _form.update { it.copy(isSaving = false, savedSuccessfully = true) }
        }
    }
}
