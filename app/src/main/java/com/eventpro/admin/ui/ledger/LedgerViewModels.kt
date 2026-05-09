package com.eventpro.admin.ui.ledger

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventpro.admin.data.local.dao.CategoryTotal
import com.eventpro.admin.domain.model.*
import com.eventpro.admin.domain.repository.EventRepository
import com.eventpro.admin.domain.repository.FinancialRepository
import com.eventpro.admin.util.CurrencyFormatter
import com.eventpro.admin.util.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class LedgerUiState(
    val isLoading: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val totalIncomeCents: Long = 0L,
    val totalExpenseCents: Long = 0L,
    val pendingInvoicesCents: Long = 0L,
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val showAll: Boolean = false,
    val exportStatus: String? = null
)

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val repo: FinancialRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LedgerUiState())
    val uiState: StateFlow<LedgerUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.getAllTransactions(),
                repo.getTotalByType("INCOME"),
                repo.getTotalByType("EXPENSE"),
                repo.getExpenseTotalsGroupedByCategory()
            ) { txs, income, expense, cats ->
                LedgerUiState(isLoading = false, transactions = txs, totalIncomeCents = income, totalExpenseCents = expense, pendingInvoicesCents = income - expense, categoryTotals = cats)
            }.collect { _state.value = it }
        }
    }

    fun toggleShowAll() = _state.update { it.copy(showAll = !it.showAll) }
    fun deleteTransaction(t: Transaction) = viewModelScope.launch { repo.deleteTransaction(t) }

    fun exportCsv(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val txs = _state.value.transactions
        val csv = buildString {
            appendLine("Date,Description,Type,Category,Amount,Reference,Counterparty")
            txs.forEach { t ->
                appendLine("${DateFormatter.format(t.dateMillis)},\"${t.description}\",${t.type},${t.category},${CurrencyFormatter.formatCents(t.amountCents)},${t.referenceNumber},\"${t.clientOrVendorName}\"")
            }
        }
        val fileName = "eventpro_ledger_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.csv"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let { context.contentResolver.openOutputStream(it)?.use { os -> os.write(csv.toByteArray()) } }
            }
            withContext(Dispatchers.Main) { _state.update { it.copy(exportStatus = "Exported to Downloads/$fileName") } }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { _state.update { it.copy(exportStatus = "Export failed: ${e.message}") } }
        }
    }

    fun clearExportStatus() = _state.update { it.copy(exportStatus = null) }
}

fun categoriesForType(type: TransactionType): List<Pair<String, ExpenseCategory>> {
    return when (type) {
        TransactionType.INCOME -> listOf(
            "Service Fee" to ExpenseCategory.MISC,
            "Product Sale" to ExpenseCategory.MISC,
            "Deposit" to ExpenseCategory.MISC,
            "Other Income" to ExpenseCategory.MISC
        )
        TransactionType.EXPENSE -> ExpenseCategory.values().map { c ->
            c.name.lowercase().replaceFirstChar { it.uppercase() } to c
        }
    }
}

// ─── Add Transaction ──────────────────────────────────────────────────────────

data class AddTransactionFormState(
    val type: TransactionType = TransactionType.INCOME,
    val description: String = "",
    val descriptionError: String? = null,
    val amountStr: String = "",
    val amountError: String? = null,
    val category: ExpenseCategory = ExpenseCategory.MISC,
    val referenceNumber: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val linkedEventId: Long? = null,
    val clientOrVendorName: String = "",
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repo: FinancialRepository,
    private val eventRepo: EventRepository
) : ViewModel() {
    private val _form = MutableStateFlow(AddTransactionFormState())
    val formState: StateFlow<AddTransactionFormState> = _form.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    init {
        viewModelScope.launch { eventRepo.getAllEvents().collect { _events.value = it } }
    }

    fun onTypeChange(t: TransactionType) = _form.update { it.copy(type = t) }
    fun onDescriptionChange(v: String) = _form.update { it.copy(description = v, descriptionError = null) }
    fun onAmountChange(v: String) = _form.update { it.copy(amountStr = v, amountError = null) }
    fun onCategoryChange(c: ExpenseCategory) = _form.update { it.copy(category = c) }
    fun onReferenceChange(v: String) = _form.update { it.copy(referenceNumber = v) }
    fun onDateChange(v: Long) = _form.update { it.copy(dateMillis = v) }
    fun onEventChange(id: Long?) = _form.update { it.copy(linkedEventId = id) }
    fun onCounterpartyChange(v: String) = _form.update { it.copy(clientOrVendorName = v) }

    fun save() {
        val f = _form.value
        var hasError = false
        if (f.description.isBlank()) { _form.update { it.copy(descriptionError = "Required") }; hasError = true }
        val amount = f.amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) { _form.update { it.copy(amountError = "Enter a valid amount") }; hasError = true }
        if (hasError) return
        viewModelScope.launch {
            _form.update { it.copy(isSaving = true) }
            repo.upsertTransaction(Transaction(
                description = f.description.trim(),
                amountCents = ((amount ?: 0.0) * 100).toLong(),
                type = f.type,
                category = f.category,
                dateMillis = f.dateMillis,
                referenceNumber = f.referenceNumber.trim(),
                eventId = f.linkedEventId,
                clientOrVendorName = f.clientOrVendorName.trim()
            ))
            _form.update { it.copy(isSaving = false, savedSuccessfully = true) }
        }
    }
}
