package com.eventpro.admin.domain.repository

import com.eventpro.admin.domain.model.AgendaItem
import kotlinx.coroutines.flow.Flow

interface AgendaRepository {
    fun getTodayAgenda(startMillis: Long, endMillis: Long): Flow<List<AgendaItem>>
    fun getAllAgendaItems(): Flow<List<AgendaItem>>
    suspend fun upsertAgendaItem(item: AgendaItem)
    suspend fun deleteAgendaItem(item: AgendaItem)
}