package com.eventpro.admin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eventpro.admin.data.local.dao.AgendaDao
import com.eventpro.admin.data.local.dao.ClientDao
import com.eventpro.admin.data.local.dao.EventDao
import com.eventpro.admin.data.local.dao.InventoryDao
import com.eventpro.admin.data.local.dao.InventoryReservationDao
import com.eventpro.admin.data.local.dao.TimelineDao
import com.eventpro.admin.data.local.dao.TransactionDao
import com.eventpro.admin.data.local.dao.VendorDao
import com.eventpro.admin.data.local.entity.AgendaItemEntity
import com.eventpro.admin.data.local.entity.ClientEntity
import com.eventpro.admin.data.local.entity.EventEntity
import com.eventpro.admin.data.local.entity.EventVendorEntity
import com.eventpro.admin.data.local.entity.InventoryItemEntity
import com.eventpro.admin.data.local.entity.InventoryReservationEntity
import com.eventpro.admin.data.local.entity.TimelineItemEntity
import com.eventpro.admin.data.local.entity.TransactionEntity
import com.eventpro.admin.data.local.entity.VendorEntity

@Database(
    entities = [
        EventEntity::class,
        ClientEntity::class,
        VendorEntity::class,
        EventVendorEntity::class,
        InventoryItemEntity::class,
        InventoryReservationEntity::class,
        TransactionEntity::class,
        TimelineItemEntity::class,
        AgendaItemEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun clientDao(): ClientDao
    abstract fun vendorDao(): VendorDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun inventoryReservationDao(): InventoryReservationDao
    abstract fun transactionDao(): TransactionDao
    abstract fun timelineDao(): TimelineDao
    abstract fun agendaDao(): AgendaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: android.content.Context): AppDatabase {
            return androidx.room.Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "eventpro.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
        }

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN isPaid INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS inventory_reservations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        itemId INTEGER NOT NULL,
                        eventId INTEGER NOT NULL,
                        quantityReserved INTEGER NOT NULL,
                        reservedAtMillis INTEGER NOT NULL,
                        FOREIGN KEY (itemId) REFERENCES inventory_items(id) ON DELETE CASCADE,
                        FOREIGN KEY (eventId) REFERENCES events(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inventory_reservations_itemId ON inventory_reservations(itemId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_inventory_reservations_eventId ON inventory_reservations(eventId)")
            }
        }
    }
}
