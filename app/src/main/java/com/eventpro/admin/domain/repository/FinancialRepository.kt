package com.eventpro.admin.domain.repository

import com.eventpro.admin.data.local.dao.CategoryTotal
import com.eventpro.admin.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface FinancialRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByType(type: String): Flow<List<Transaction>>
    fun getTotalByType(type: String): Flow<Long>
    fun getTotalByTypeInRange(type: String, from: Long, to: Long): Flow<Long>
    fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>>
    fun getExpenseTotalsGroupedByCategory(): Flow<List<CategoryTotal>>
    fun getTransactionsByEvent(eventId: Long): Flow<List<Transaction>>
    suspend fun upsertTransaction(t: Transaction): Long
    suspend fun deleteTransaction(t: Transaction)
}
