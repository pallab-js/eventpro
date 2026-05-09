package com.eventpro.admin.repository

import com.eventpro.admin.data.local.dao.AgendaDao
import com.eventpro.admin.domain.model.AgendaItem
import com.eventpro.admin.domain.repository.AgendaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgendaRepositoryImpl @Inject constructor(private val agendaDao: AgendaDao) : AgendaRepository {
    override fun getTodayAgenda(startMillis: Long, endMillis: Long): Flow<List<AgendaItem>> =
        agendaDao.getTodayAgenda(startMillis, endMillis).map { list -> list.map { it.toDomain() } }

    override fun getAllAgendaItems(): Flow<List<AgendaItem>> =
        agendaDao.getAllAgendaItems().map { list -> list.map { it.toDomain() } }

    override suspend fun upsertAgendaItem(item: AgendaItem) =
        agendaDao.upsertAgendaItem(item.toEntity())

    override suspend fun deleteAgendaItem(item: AgendaItem) =
        agendaDao.deleteAgendaItem(item.toEntity())
}