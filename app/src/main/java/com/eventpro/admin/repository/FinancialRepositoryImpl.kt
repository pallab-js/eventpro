package com.eventpro.admin.repository

import com.eventpro.admin.data.local.dao.TransactionDao
import com.eventpro.admin.domain.model.Transaction
import com.eventpro.admin.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialRepositoryImpl @Inject constructor(private val dao: TransactionDao) : FinancialRepository {
    override fun getAllTransactions() = dao.getAllTransactions().map { it.map { t -> t.toDomain() } }
    override fun getTransactionsByType(type: String) = dao.getTransactionsByType(type).map { it.map { t -> t.toDomain() } }
    override fun getTotalByType(type: String) = dao.getTotalByType(type)
    override fun getTotalByTypeInRange(type: String, from: Long, to: Long) = dao.getTotalByTypeInRange(type, from, to)
    override fun getTransactionsByDateRange(from: Long, to: Long) = dao.getTransactionsByDateRange(from, to).map { it.map { t -> t.toDomain() } }
    override fun getExpenseTotalsGroupedByCategory() = dao.getExpenseTotalsGroupedByCategory()
    override fun getTransactionsByEvent(eventId: Long) = dao.getTransactionsByEvent(eventId).map { it.map { t -> t.toDomain() } }
    override suspend fun upsertTransaction(t: Transaction) = dao.upsertTransaction(t.toEntity())
    override suspend fun deleteTransaction(t: Transaction) = dao.deleteTransaction(t.toEntity())
}
