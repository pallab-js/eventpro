package com.eventpro.admin.di

import com.eventpro.admin.domain.repository.AgendaRepository
import com.eventpro.admin.domain.repository.ClientRepository
import com.eventpro.admin.domain.repository.EventRepository
import com.eventpro.admin.domain.repository.FinancialRepository
import com.eventpro.admin.domain.repository.InventoryRepository
import com.eventpro.admin.domain.repository.VendorRepository
import com.eventpro.admin.repository.AgendaRepositoryImpl
import com.eventpro.admin.repository.ClientRepositoryImpl
import com.eventpro.admin.repository.EventRepositoryImpl
import com.eventpro.admin.repository.FinancialRepositoryImpl
import com.eventpro.admin.repository.InventoryRepositoryImpl
import com.eventpro.admin.repository.VendorRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindEventRepo(impl: EventRepositoryImpl): EventRepository
    @Binds @Singleton abstract fun bindClientRepo(impl: ClientRepositoryImpl): ClientRepository
    @Binds @Singleton abstract fun bindVendorRepo(impl: VendorRepositoryImpl): VendorRepository
    @Binds @Singleton abstract fun bindInventoryRepo(impl: InventoryRepositoryImpl): InventoryRepository
    @Binds @Singleton abstract fun bindFinancialRepo(impl: FinancialRepositoryImpl): FinancialRepository
    @Binds @Singleton abstract fun bindAgendaRepo(impl: AgendaRepositoryImpl): AgendaRepository
}
