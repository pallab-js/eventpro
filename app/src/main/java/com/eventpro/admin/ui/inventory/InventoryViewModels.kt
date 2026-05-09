package com.eventpro.admin.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventpro.admin.domain.model.*
import com.eventpro.admin.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val isLoading: Boolean = true,
    val items: List<InventoryItem> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedCategory: InventoryCategory? = null,
    val selectedStockStatus: StockStatus? = null,
    val selectedItem: InventoryItem? = null
)

@HiltViewModel
class InventoryViewModel @Inject constructor(private val repo: InventoryRepository) : ViewModel() {
    private val _state = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _state.asStateFlow()
    private val allItems = MutableStateFlow<List<InventoryItem>>(emptyList())

    init {
        viewModelScope.launch {
            repo.getAllItems().collect { items ->
                allItems.value = items
                applyFilters()
            }
        }
    }

    fun onSearch(q: String) { _state.update { it.copy(searchQuery = q) }; applyFilters() }
    fun onSearchToggle() { _state.update { it.copy(isSearchActive = !it.isSearchActive, searchQuery = "") }; applyFilters() }
    fun onCategoryFilter(c: InventoryCategory?) { _state.update { it.copy(selectedCategory = c) }; applyFilters() }
    fun onStockFilter(s: StockStatus?) { _state.update { it.copy(selectedStockStatus = s) }; applyFilters() }
    fun selectItem(item: InventoryItem?) = _state.update { it.copy(selectedItem = item) }
    fun deleteItem(item: InventoryItem) = viewModelScope.launch { repo.deleteItem(item) }

    fun updateAvailableUnits(item: InventoryItem, delta: Int) = viewModelScope.launch {
        val newUnits = (item.availableUnits + delta).coerceIn(0, item.totalUnits)
        repo.upsertItem(item.copy(availableUnits = newUnits))
        _state.update { it.copy(selectedItem = item.copy(availableUnits = newUnits)) }
    }

    private fun applyFilters() {
        val s = _state.value
        val filtered = allItems.value
            .filter { s.selectedCategory == null || it.category == s.selectedCategory }
            .filter { s.selectedStockStatus == null || it.stockStatus == s.selectedStockStatus }
            .filter { s.searchQuery.isBlank() || it.name.contains(s.searchQuery, true) }
        _state.update { it.copy(isLoading = false, items = filtered) }
    }
}

data class AddEditInventoryFormState(
    val name: String = "",
    val nameError: String? = null,
    val category: InventoryCategory = InventoryCategory.OTHER,
    val description: String = "",
    val totalUnits: String = "",
    val availableUnits: String = "",
    val materialIconName: String = "inventory_2",
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false
)

@HiltViewModel
class AddEditInventoryViewModel @Inject constructor(private val repo: InventoryRepository) : ViewModel() {
    private val _form = MutableStateFlow(AddEditInventoryFormState())
    val formState: StateFlow<AddEditInventoryFormState> = _form.asStateFlow()

    fun load(itemId: Long) = viewModelScope.launch {
        repo.getAllItems().firstOrNull()?.find { it.id == itemId }?.let { item ->
            _form.update { it.copy(name = item.name, category = item.category, description = item.description, totalUnits = item.totalUnits.toString(), availableUnits = item.availableUnits.toString(), materialIconName = item.materialIconName) }
        }
    }

    fun onNameChange(v: String) = _form.update { it.copy(name = v, nameError = null) }
    fun onCategoryChange(v: InventoryCategory) = _form.update { it.copy(category = v) }
    fun onDescriptionChange(v: String) = _form.update { it.copy(description = v) }
    fun onTotalUnitsChange(v: String) = _form.update { it.copy(totalUnits = v) }
    fun onAvailableUnitsChange(v: String) = _form.update { it.copy(availableUnits = v) }

    fun save(existingId: Long? = null) {
        val f = _form.value
        if (f.name.isBlank()) { _form.update { it.copy(nameError = "Name is required") }; return }
        viewModelScope.launch {
            _form.update { it.copy(isSaving = true) }
            repo.upsertItem(InventoryItem(id = existingId ?: 0L, name = f.name.trim(), category = f.category, description = f.description.trim(), totalUnits = f.totalUnits.toIntOrNull() ?: 0, availableUnits = f.availableUnits.toIntOrNull() ?: 0, materialIconName = f.materialIconName))
            _form.update { it.copy(isSaving = false, savedSuccessfully = true) }
        }
    }
}
