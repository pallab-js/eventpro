package com.eventpro.admin.ui.dashboard

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventpro.admin.data.preferences.AppPreferences
import com.eventpro.admin.data.preferences.ThemeMode
import com.eventpro.admin.domain.model.AgendaItem
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.repository.AgendaRepository
import com.eventpro.admin.domain.repository.EventRepository
import com.eventpro.admin.domain.usecase.BuildRevenueChartUseCase
import com.eventpro.admin.domain.usecase.GetDashboardMetricsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class DashboardUiState(
    val isLoading: Boolean = true,
    val isInitialLoad: Boolean = true,
    val snackbarMessage: String? = null,
    val revenueYtdCents: Long = 0L,
    val pendingCents: Long = 0L,
    val overdueCents: Long = 0L,
    val revenueChangePercent: Float = 0f,
    val revenueChartData: List<Pair<String, Float>> = emptyList(),
    val criticalMilestones: List<Event> = emptyList(),
    val todayAgenda: List<AgendaItem> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardMetricsUseCase: GetDashboardMetricsUseCase,
    private val buildRevenueChartUseCase: BuildRevenueChartUseCase,
    private val appPreferences: AppPreferences,
    private val agendaRepo: AgendaRepository,
    private val eventRepo: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    val themeMode = appPreferences.themeMode
    private val refreshTrigger = MutableStateFlow(0)

    init { observeDashboard() }

    fun toggleTheme() = viewModelScope.launch {
        val current = appPreferences.themeMode.first()
        val next = when (current) {
            ThemeMode.SYSTEM.value -> ThemeMode.LIGHT.value
            ThemeMode.LIGHT.value -> ThemeMode.DARK.value
            else -> ThemeMode.SYSTEM.value
        }
        appPreferences.setThemeMode(next)
    }

    private fun observeDashboard() {
        viewModelScope.launch {
            refreshTrigger
                .flatMapLatest { getDashboardMetricsUseCase() }
                .collect { raw ->
                    val chartData = buildRevenueChartUseCase(raw.recentTx.map { it.dateMillis to it.amountCents.toFloat() })
                    val changePct = if (raw.prevIncome > 0) ((raw.recentIncome - raw.prevIncome).toFloat() / raw.prevIncome * 100) else 0f
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isInitialLoad = false,
                        revenueYtdCents = raw.totalIncome,
                        pendingCents = (raw.totalIncome - raw.recentIncome).coerceAtLeast(0L),
                        overdueCents = raw.totalExpenses,
                        revenueChangePercent = changePct,
                        revenueChartData = chartData,
                        criticalMilestones = raw.milestones,
                        todayAgenda = raw.agenda
                    )
                }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        refreshTrigger.value++
    }

    private val _todayEvents = MutableStateFlow<List<Event>>(emptyList())
    val todayEvents: StateFlow<List<Event>> = _todayEvents.asStateFlow()

    init {
        viewModelScope.launch {
            eventRepo.getAllEvents().collect { _todayEvents.value = it }
        }
    }

    private var pendingDeletedAgenda: AgendaItem? = null
    private var clearPendingAgendaJob: Job? = null

    fun addAgendaItem(title: String, description: String, scheduledDateMillis: Long, eventId: Long?) {
        viewModelScope.launch {
            agendaRepo.upsertAgendaItem(AgendaItem(
                title = title.trim(),
                description = description.trim(),
                scheduledDateMillis = scheduledDateMillis,
                eventId = eventId
            ))
            refresh()
        }
    }

    fun deleteAgendaItem(item: AgendaItem) {
        viewModelScope.launch {
            agendaRepo.deleteAgendaItem(item)
            pendingDeletedAgenda = item
            _uiState.update { it.copy(snackbarMessage = "Deleted \"${item.title}\"") }
            clearPendingAgendaJob?.cancel()
            clearPendingAgendaJob = viewModelScope.launch {
                delay(5000)
                pendingDeletedAgenda = null
                _uiState.update { it.copy(snackbarMessage = null) }
            }
        }
    }

    fun undoDeleteAgenda() {
        clearPendingAgendaJob?.cancel()
        viewModelScope.launch {
            pendingDeletedAgenda?.let { agendaRepo.upsertAgendaItem(it) }
            pendingDeletedAgenda = null
            _uiState.update { it.copy(snackbarMessage = null) }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
