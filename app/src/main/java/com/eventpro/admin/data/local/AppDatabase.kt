package com.eventpro.admin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eventpro.admin.data.local.dao.*
import com.eventpro.admin.data.local.entity.*

@Database(
    entities = [
        EventEntity::class,
        ClientEntity::class,
        VendorEntity::class,
        EventVendorEntity::class,
        InventoryItemEntity::class,
        TransactionEntity::class,
        TimelineItemEntity::class,
        AgendaItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun clientDao(): ClientDao
    abstract fun vendorDao(): VendorDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun timelineDao(): TimelineDao
    abstract fun agendaDao(): AgendaDao
}
