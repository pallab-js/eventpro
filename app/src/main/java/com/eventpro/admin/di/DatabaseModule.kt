package com.eventpro.admin.di

import android.content.Context
import com.eventpro.admin.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier @Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase {
        return AppDatabase.getInstance(ctx)
    }

    @Provides fun provideEventDao(db: AppDatabase) = db.eventDao()
    @Provides fun provideClientDao(db: AppDatabase) = db.clientDao()
    @Provides fun provideVendorDao(db: AppDatabase) = db.vendorDao()
    @Provides fun provideInventoryDao(db: AppDatabase) = db.inventoryDao()
    @Provides fun provideInventoryReservationDao(db: AppDatabase) = db.inventoryReservationDao()
    @Provides fun provideTransactionDao(db: AppDatabase) = db.transactionDao()
    @Provides fun provideTimelineDao(db: AppDatabase) = db.timelineDao()
    @Provides fun provideAgendaDao(db: AppDatabase) = db.agendaDao()
}
