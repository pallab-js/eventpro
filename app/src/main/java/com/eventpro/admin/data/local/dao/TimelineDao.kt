package com.eventpro.admin.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.eventpro.admin.data.local.entity.TimelineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineDao {
    @Query("SELECT * FROM timeline_items WHERE eventId = :eventId ORDER BY scheduledDateMillis ASC")
    fun getTimelineItemsForEvent(eventId: Long): Flow<List<TimelineItemEntity>>

    @Upsert
    suspend fun upsertTimelineItem(item: TimelineItemEntity)

    @Delete
    suspend fun deleteTimelineItem(item: TimelineItemEntity)

    @Query("UPDATE timeline_items SET completed = :completed WHERE id = :id")
    suspend fun markCompleted(id: Long, completed: Boolean)

    @Query("SELECT * FROM timeline_items WHERE scheduledDateMillis BETWEEN :from AND :to AND completed = 0")
    suspend fun getUpcomingMilestones(from: Long, to: Long): List<TimelineItemEntity>
}
