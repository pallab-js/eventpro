package com.eventpro.admin.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.eventpro.admin.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY startDateMillis ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    fun getEventById(id: Long): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE status = :status ORDER BY startDateMillis ASC")
    fun getEventsByStatus(status: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE startDateMillis >= :nowMillis ORDER BY startDateMillis ASC LIMIT :limit")
    fun getUpcomingEvents(nowMillis: Long, limit: Int): Flow<List<EventEntity>>

    @Query("""
        SELECT * FROM events
        WHERE startDateMillis >= :nowMillis
          AND status IN ('CONFIRMED', 'IN_PROGRESS')
        ORDER BY startDateMillis ASC
        LIMIT :limit
    """)
    fun getCriticalMilestones(nowMillis: Long, limit: Int = 5): Flow<List<EventEntity>>

    @Query("SELECT COUNT(*) FROM events")
    fun getEventCount(): Flow<Int>

    @Upsert
    suspend fun upsertEvent(event: EventEntity): Long

    @Delete
    suspend fun deleteEvent(event: EventEntity)
}
