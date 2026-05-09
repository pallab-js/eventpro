package com.eventpro.admin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // AUDIO | VISUAL | LIGHTING | STAGING | FURNITURE | OTHER
    val description: String,
    val totalUnits: Int,
    val availableUnits: Int,
    val materialIconName: String
)
