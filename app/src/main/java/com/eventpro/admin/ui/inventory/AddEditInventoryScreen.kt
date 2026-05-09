package com.eventpro.admin.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.eventpro.admin.domain.model.InventoryCategory
import com.eventpro.admin.ui.components.AppTopBar

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
                showOfflineBadge = false,
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
                    InventoryCategory.entries.forEach { c -> DropdownMenuItem(text = { Text(c.displayName) }, onClick = { vm.onCategoryChange(c); catExpanded = false }) }
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
