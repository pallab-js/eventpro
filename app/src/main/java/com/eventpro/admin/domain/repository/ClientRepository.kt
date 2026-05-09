package com.eventpro.admin.domain.repository

import com.eventpro.admin.domain.model.Client
import com.eventpro.admin.domain.model.ClientStatus
import kotlinx.coroutines.flow.Flow

interface ClientRepository {
    fun getAllClients(): Flow<List<Client>>
    fun getClientsByStatus(status: ClientStatus): Flow<List<Client>>
    fun searchClients(query: String): Flow<List<Client>>
    fun getClientById(id: Long): Flow<Client?>
    suspend fun upsertClient(client: Client): Long
    suspend fun deleteClient(client: Client)
}
