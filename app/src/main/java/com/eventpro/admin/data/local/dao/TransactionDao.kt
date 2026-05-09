package com.eventpro.admin.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.eventpro.admin.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class CategoryTotal(val category: String, val total: Long)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY dateMillis DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE type = :type")
    fun getTotalByType(type: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE type = :type AND dateMillis BETWEEN :from AND :to")
    fun getTotalByTypeInRange(type: String, from: Long, to: Long): Flow<Long>

    @Query("SELECT * FROM transactions WHERE dateMillis BETWEEN :from AND :to ORDER BY dateMillis DESC")
    fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<TransactionEntity>>

    @Query("SELECT category, SUM(amountCents) as total FROM transactions WHERE type = 'EXPENSE' GROUP BY category")
    fun getExpenseTotalsGroupedByCategory(): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM transactions WHERE eventId = :eventId ORDER BY dateMillis DESC")
    fun getTransactionsByEvent(eventId: Long): Flow<List<TransactionEntity>>

    @Upsert
    suspend fun upsertTransaction(t: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(t: TransactionEntity)
}
