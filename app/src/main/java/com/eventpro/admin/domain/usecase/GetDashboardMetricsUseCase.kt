package com.eventpro.admin.domain.usecase

import com.eventpro.admin.domain.model.AgendaItem
import com.eventpro.admin.domain.model.Event
import com.eventpro.admin.domain.model.Transaction
import com.eventpro.admin.domain.repository.AgendaRepository
import com.eventpro.admin.domain.repository.EventRepository
import com.eventpro.admin.domain.repository.FinancialRepository
import com.eventpro.admin.util.DateFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class DashboardRawData(
    val totalIncome: Long,
    val totalExpenses: Long,
    val recentIncome: Long,
    val prevIncome: Long,
    val milestones: List<Event>,
    val agenda: List<AgendaItem>,
    val recentTx: List<Transaction>
)

class GetDashboardMetricsUseCase @Inject constructor(
    private val financialRepository: FinancialRepository,
    private val eventRepository: EventRepository,
    private val agendaRepository: AgendaRepository
) {
    operator fun invoke(): Flow<DashboardRawData> {
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - 30L * 24 * 60 * 60 * 1000
        val sixtyDaysAgo = now - 60L * 24 * 60 * 60 * 1000

        return combine(
            financialRepository.getTotalByType("INCOME"),
            financialRepository.getTotalByType("EXPENSE"),
            financialRepository.getTotalByTypeInRange("INCOME", thirtyDaysAgo, now),
            financialRepository.getTotalByTypeInRange("INCOME", sixtyDaysAgo, thirtyDaysAgo),
            eventRepository.getCriticalMilestones(),
            agendaRepository.getTodayAgenda(DateFormatter.todayStartMillis(), DateFormatter.todayEndMillis()),
            financialRepository.getTransactionsByDateRange(now - 7 * 24 * 60 * 60 * 1000L, now)
        ) { array ->
            @Suppress("UNCHECKED_CAST")
            DashboardRawData(
                totalIncome = array[0] as Long,
                totalExpenses = array[1] as Long,
                recentIncome = array[2] as Long,
                prevIncome = array[3] as Long,
                milestones = array[4] as List<Event>,
                agenda = array[5] as List<AgendaItem>,
                recentTx = array[6] as List<Transaction>
            )
        }
    }
}