package com.eventpro.admin.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.eventpro.admin.data.local.entity.AgendaItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaDao {
    @Query("SELECT * FROM agenda_items WHERE scheduledDateMillis BETWEEN :startMillis AND :endMillis ORDER BY scheduledDateMillis ASC")
    fun getTodayAgenda(startMillis: Long, endMillis: Long): Flow<List<AgendaItemEntity>>

    @Query("SELECT * FROM agenda_items ORDER BY scheduledDateMillis ASC")
    fun getAllAgendaItems(): Flow<List<AgendaItemEntity>>

    @Upsert
    suspend fun upsertAgendaItem(item: AgendaItemEntity)

    @Delete
    suspend fun deleteAgendaItem(item: AgendaItemEntity)
}
