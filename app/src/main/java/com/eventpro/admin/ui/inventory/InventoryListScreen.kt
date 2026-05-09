package com.eventpro.admin.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.eventpro.admin.domain.model.InventoryCategory
import com.eventpro.admin.domain.model.StockStatus
import com.eventpro.admin.ui.components.AppTopBar
import com.eventpro.admin.ui.components.EmptyState
import com.eventpro.admin.ui.components.FilterChipRow
import com.eventpro.admin.ui.components.InventoryCard
import com.eventpro.admin.ui.components.ShimmerBox
import com.eventpro.admin.ui.inventory.InventoryDetailBottomSheet
import com.eventpro.admin.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavController, vm: InventoryViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    if (state.selectedItem != null) {
        InventoryDetailBottomSheet(
            item = state.selectedItem!!,
            onDismiss = { vm.selectItem(null) },
            onIncrement = { vm.updateAvailableUnits(state.selectedItem!!, 1) },
            onDecrement = { vm.updateAvailableUnits(state.selectedItem!!, -1) },
            onSave = { vm.selectItem(null) }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Inventory & Resources",
                showOfflineBadge = false,
                actions = {
                    IconButton(onClick = vm::onSearchToggle) { Icon(Icons.Outlined.Search, "Search") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddEditInventory.createRoute()) }) {
                Icon(Icons.Default.Add, "Add Item")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (state.isSearchActive) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = vm::onSearch,
                    placeholder = { Text("Search inventory\u2026") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true
                )
            }
            FilterChipRow(
                options = listOf("All" to null) + InventoryCategory.entries.map { it.displayName to it },
                selected = state.selectedCategory,
                onSelect = vm::onCategoryFilter
            )
            FilterChipRow(
                options = listOf("All Stock" to null, "In Stock" to StockStatus.IN_STOCK, "Low Stock" to StockStatus.LOW_STOCK, "Out of Stock" to StockStatus.OUT_OF_STOCK),
                selected = state.selectedStockStatus,
                onSelect = vm::onStockFilter
            )
            Spacer(Modifier.height(4.dp))
            if (state.isLoading) {
                Box(Modifier.fillMaxSize().padding(16.dp)) {
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(180.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(6) {
                            ShimmerBox(Modifier.height(120.dp).fillMaxWidth())
                        }
                    }
                }
            } else if (state.items.isEmpty()) {
                EmptyState(Icons.Outlined.Inventory2, "No items found", "Add inventory items with the + button")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(180.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        InventoryCard(item = item, onViewDetails = { vm.selectItem(item) })
                    }
                }
            }
        }
    }
}
