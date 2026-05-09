package com.eventpro.admin.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.eventpro.admin.data.local.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY companyName ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE status = :status ORDER BY companyName ASC")
    fun getClientsByStatus(status: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE companyName LIKE '%' || :query || '%' OR contactName LIKE '%' || :query || '%'")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id")
    fun getClientById(id: Long): Flow<ClientEntity?>

    @Upsert
    suspend fun upsertClient(c: ClientEntity): Long

    @Delete
    suspend fun deleteClient(c: ClientEntity)
}
