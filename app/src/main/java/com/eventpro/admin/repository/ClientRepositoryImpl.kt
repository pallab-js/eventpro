package com.eventpro.admin.repository

import com.eventpro.admin.data.local.dao.ClientDao
import com.eventpro.admin.domain.model.Client
import com.eventpro.admin.domain.model.ClientStatus
import com.eventpro.admin.domain.repository.ClientRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepositoryImpl @Inject constructor(private val dao: ClientDao) : ClientRepository {
    override fun getAllClients() = dao.getAllClients().map { it.map { c -> c.toDomain() } }
    override fun getClientsByStatus(status: ClientStatus) = dao.getClientsByStatus(status.name).map { it.map { c -> c.toDomain() } }
    override fun searchClients(query: String) = dao.searchClients(query).map { it.map { c -> c.toDomain() } }
    override fun getClientById(id: Long) = dao.getClientById(id).map { it?.toDomain() }
    override suspend fun upsertClient(client: Client) = dao.upsertClient(client.toEntity())
    override suspend fun deleteClient(client: Client) = dao.deleteClient(client.toEntity())
}
