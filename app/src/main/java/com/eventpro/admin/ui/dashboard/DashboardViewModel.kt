package com.eventpro.admin.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventpro.admin.data.local.dao.AgendaDao
import com.eventpro.admin.domain.model.AgendaItem
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.repository.EventRepository
import com.eventpro.admin.domain.repository.FinancialRepository
import com.eventpro.admin.repository.toDomain
import com.eventpro.admin.util.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
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
    private val eventRepository: EventRepository,
    private val financialRepository: FinancialRepository,
    private val agendaDao: AgendaDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    private fun loadDashboard() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val thirtyDaysAgo = now - 30L * 24 * 60 * 60 * 1000
            val sixtyDaysAgo = now - 60L * 24 * 60 * 60 * 1000

            val incomeFlow = financialRepository.getTotalByType("INCOME")
            val expenseFlow = financialRepository.getTotalByType("EXPENSE")
            val recentIncomeFlow = financialRepository.getTotalByTypeInRange("INCOME", thirtyDaysAgo, now)
            val prevIncomeFlow = financialRepository.getTotalByTypeInRange("INCOME", sixtyDaysAgo, thirtyDaysAgo)
            val milestonesFlow = eventRepository.getCriticalMilestones()
            val agendaFlow = agendaDao.getTodayAgenda(DateFormatter.todayStartMillis(), DateFormatter.todayEndMillis())
                .map { list -> list.map { it.toDomain() } }
            val recentTxFlow = financialRepository.getTransactionsByDateRange(now - 7 * 24 * 60 * 60 * 1000L, now)

            combine(incomeFlow, expenseFlow, recentIncomeFlow, prevIncomeFlow, milestonesFlow) { a, b, c, d, e ->
                arrayOf<Any>(a, b, c, d, e)
            }.combine(agendaFlow) { arr, agenda ->
                arrayOf<Any>(arr[0], arr[1], arr[2], arr[3], arr[4], agenda)
            }.combine(recentTxFlow) { arr, recentTx ->
                val totalIncome = arr[0] as Long
                val totalExpenses = arr[1] as Long
                val recentIncome = arr[2] as Long
                val prevIncome = arr[3] as Long
                val milestones = arr[4] as List<Event>
                val agenda = arr[5] as List<AgendaItem>
                val chartData = buildChartData(recentTx.map { it.dateMillis to it.amountCents.toFloat() })
                val changePct = if (prevIncome > 0) ((recentIncome - prevIncome).toFloat() / prevIncome * 100) else 0f
                DashboardUiState(
                    isLoading = false,
                    revenueYtdCents = totalIncome,
                    pendingCents = totalIncome,
                    overdueCents = totalExpenses,
                    revenueChangePercent = changePct,
                    revenueChartData = chartData,
                    criticalMilestones = milestones,
                    todayAgenda = agenda
                )
            }.collect { _uiState.value = it }
        }
    }

    private fun buildChartData(points: List<Pair<Long, Float>>): List<Pair<String, Float>> {
        val days = (0..6).map { i ->
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -(6 - i))
            val label = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault()).format(cal.time)
            val dayStart = cal.apply { set(java.util.Calendar.HOUR_OF_DAY, 0) }.timeInMillis
            val dayEnd = dayStart + 86_400_000L
            val total = points.filter { it.first in dayStart..dayEnd }.sumOf { it.second.toDouble() }.toFloat()
            label to total
        }
        return days
    }
}
