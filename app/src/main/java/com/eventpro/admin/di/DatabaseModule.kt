package com.eventpro.admin.di

import android.content.Context
import androidx.room.Room
import com.eventpro.admin.data.local.AppDatabase
import com.eventpro.admin.data.local.DatabaseSeedCallback
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase {
        lateinit var db: AppDatabase
        val callback = DatabaseSeedCallback { db }
        db = Room.databaseBuilder(ctx, AppDatabase::class.java, "eventpro.db")
            .addCallback(callback)
            .fallbackToDestructiveMigration()
            .build()
        return db
    }

    @Provides fun provideEventDao(db: AppDatabase) = db.eventDao()
    @Provides fun provideClientDao(db: AppDatabase) = db.clientDao()
    @Provides fun provideVendorDao(db: AppDatabase) = db.vendorDao()
    @Provides fun provideInventoryDao(db: AppDatabase) = db.inventoryDao()
    @Provides fun provideTransactionDao(db: AppDatabase) = db.transactionDao()
    @Provides fun provideTimelineDao(db: AppDatabase) = db.timelineDao()
    @Provides fun provideAgendaDao(db: AppDatabase) = db.agendaDao()
}
