package com.eventpro.admin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vendors")
data class VendorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // CATERING | FLORAL | AV | MUSIC | PHOTOGRAPHY | LOGISTICS | OTHER
    val phone: String,
    val email: String
)
