package com.eventpro.admin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "timeline_items",
    foreignKeys = [ForeignKey(EventEntity::class, ["id"], ["eventId"], ForeignKey.CASCADE)]
)
data class TimelineItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val title: String,
    val description: String,
    val scheduledDateMillis: Long,
    val completed: Boolean = false
)
