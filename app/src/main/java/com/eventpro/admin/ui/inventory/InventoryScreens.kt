package com.eventpro.admin.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
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
import com.eventpro.admin.ui.components.*
import com.eventpro.admin.ui.navigation.Screen

// ─── Inventory Screen ─────────────────────────────────────────────────────────

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
                showSyncChip = false,
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
                    placeholder = { Text("Search inventory…") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true
                )
            }
            FilterChipRow(
                options = listOf("All" to null) + InventoryCategory.values().map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } to it },
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryDetailBottomSheet(
    item: com.eventpro.admin.domain.model.InventoryItem,
    onDismiss: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onSave: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleLarge)
            Text(item.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Category: ${item.category.name}", style = MaterialTheme.typography.bodyMedium)
            Divider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Available Units", style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledIconButton(onClick = onDecrement, enabled = item.availableUnits > 0) {
                        Icon(Icons.Outlined.Remove, "Decrease")
                    }
                    Text("${item.availableUnits}", style = MaterialTheme.typography.headlineMedium)
                    FilledIconButton(onClick = onIncrement, enabled = item.availableUnits < item.totalUnits) {
                        Icon(Icons.Outlined.Add, "Increase")
                    }
                }
            }
            Text("Total: ${item.totalUnits}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            StockStatusChip(item.stockStatus)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Save Changes") }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Add/Edit Inventory ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditInventoryScreen(navController: NavController, itemId: Long?, vm: AddEditInventoryViewModel = hiltViewModel()) {
    LaunchedEffect(itemId) { itemId?.let { vm.load(it) } }
    val form by vm.formState.collectAsStateWithLifecycle()

    LaunchedEffect(form.savedSuccessfully) {
        if (form.savedSuccessfully) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (itemId == null) "Add Item" else "Edit Item",
                showSyncChip = false,
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Outlined.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = form.name, onValueChange = vm::onNameChange, label = { Text("Item Name *") }, isError = form.nameError != null, supportingText = form.nameError?.let { { Text(it) } }, modifier = Modifier.fillMaxWidth())

            var catExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                OutlinedTextField(value = form.category.name, onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                    InventoryCategory.values().forEach { c -> DropdownMenuItem(text = { Text(c.name) }, onClick = { vm.onCategoryChange(c); catExpanded = false }) }
                }
            }

            OutlinedTextField(value = form.description, onValueChange = vm::onDescriptionChange, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            OutlinedTextField(value = form.totalUnits, onValueChange = vm::onTotalUnitsChange, label = { Text("Total Units") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = form.availableUnits, onValueChange = vm::onAvailableUnitsChange, label = { Text("Available Units") }, modifier = Modifier.fillMaxWidth())

            Button(onClick = { vm.save(itemId) }, modifier = Modifier.fillMaxWidth(), enabled = !form.isSaving) {
                if (form.isSaving) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Save Item")
            }
        }
    }
}
