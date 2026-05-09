package com.eventpro.admin.di

import com.eventpro.admin.domain.repository.*
import com.eventpro.admin.repository.*
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
}
